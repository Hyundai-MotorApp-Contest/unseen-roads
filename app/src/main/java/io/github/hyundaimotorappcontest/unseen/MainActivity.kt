package io.github.hyundaimotorappcontest.unseen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis

/**
 * ourroad 차량 앱 (Pleos Connect) 1차 기획 목업.
 *
 * 음성 인식·낭독·서버 판단은 SDK가 열리기 전이라 [DadeuriFlow]가 시간 흐름으로 흉내 낸다.
 * 시간은 화면 프레임에 맞춰 흐르므로 앱이 보이지 않으면 대화도 멈춘다.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var state by rememberSaveable(stateSaver = FlowStateSaver) { mutableStateOf(FlowState()) }
            val dispatch: (Event) -> Unit = { state = DadeuriFlow.reduce(state, it) }

            LaunchedEffect(Unit) {
                var last = withFrameMillis { it }
                var pending = 0L
                while (true) {
                    val now = withFrameMillis { it }
                    // Cap the gap so returning to the app does not skip a whole line of speech.
                    pending += (now - last).coerceAtMost(MAX_FRAME_GAP_MS)
                    last = now
                    if (pending >= TICK_MS) {
                        dispatch(Event.Tick(pending))
                        pending = 0
                    }
                }
            }

            // Back stops the current conversation. At the top level it is consumed and never exits the car app.
            BackHandler { dispatch(Event.Stop) }

            CarScreen(state, dispatch)
        }
    }

    private companion object {
        const val TICK_MS = 100L
        const val MAX_FRAME_GAP_MS = 250L
    }
}

/**
 * Restores the connection and demo settings after the Activity is recreated (PLEOS-UX-007, LIFECYCLE-002).
 * A conversation in progress is not resumed automatically: the driver starts it again.
 */
private val FlowStateSaver = Saver<FlowState, List<Any>>(
    save = { s ->
        listOf(s.scene != Scene.UNPAIRED, s.settings.driving, s.settings.emptyRoad, s.settings.judge.ordinal)
    },
    restore = { v ->
        FlowState(
            scene = if (v[0] as Boolean) Scene.IDLE else Scene.UNPAIRED,
            settings = DemoSettings(driving = v[1] as Boolean, emptyRoad = v[2] as Boolean, judge = Judge.entries[v[3] as Int]),
        )
    },
)
