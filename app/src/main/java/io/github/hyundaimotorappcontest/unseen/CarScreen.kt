package io.github.hyundaimotorappcontest.unseen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Pleos Connect 차량 화면 (1차 기획 목업).
 *
 * 차량에서는 대화와 듣기만 한다. 화면은 다드리, 상태 한 줄, 큰 대답 버튼뿐이다.
 * 주행 중에는 자막·이야기 정보를 숨기고 다드리의 움직임을 멈춘다(PLEOS-SAFETY-008).
 * 오른쪽 "시연 제어"는 목업 시연용이며 실제 차량 화면에는 없다.
 */
@Composable
fun CarScreen(state: FlowState, dispatch: (Event) -> Unit) {
    OurroadTheme {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Ourroad.Background, Ourroad.BackgroundTint))),
        ) {
            if (maxWidth >= 1100.dp) {
                Row(Modifier.fillMaxSize().padding(horizontal = 56.dp, vertical = 40.dp)) {
                    DriverPane(state, dispatch, Modifier.weight(1f).fillMaxHeight())
                    Spacer(Modifier.width(48.dp))
                    DemoRail(state, dispatch, Modifier.width(460.dp).fillMaxHeight())
                }
            } else {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
                    DriverPane(state, dispatch, Modifier.fillMaxWidth().height(860.dp))
                    Spacer(Modifier.height(32.dp))
                    DemoRail(state, dispatch, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

// region Driver pane

@Composable
private fun DriverPane(state: FlowState, dispatch: (Event) -> Unit, modifier: Modifier) {
    val driving = state.settings.driving
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("ourroad", style = Ourroad.Headline, color = Ourroad.Label)
            Spacer(Modifier.weight(1f))
            StatePill(driving)
        }

        Spacer(Modifier.weight(1f))
        Dadeuri(state, Modifier.size(if (driving) 340.dp else 380.dp))
        Spacer(Modifier.height(40.dp))

        Text("다드리", style = Ourroad.Eyebrow, color = Ourroad.Accent)
        Spacer(Modifier.height(10.dp))
        Text(
            title(state),
            style = Ourroad.Title,
            color = Ourroad.Label,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics {
                heading()
                liveRegion = LiveRegionMode.Polite
            },
        )
        Spacer(Modifier.height(20.dp))
        Box(Modifier.widthIn(max = 1040.dp).heightIn(min = 140.dp), contentAlignment = Alignment.TopCenter) {
            if (driving) DrivingDetail(state) else ParkedDetail(state)
        }

        Spacer(Modifier.weight(1f))
        Actions(state, dispatch)
        Spacer(Modifier.height(18.dp))
        Text(hint(state), style = Ourroad.Footnote, color = Ourroad.Tertiary, textAlign = TextAlign.Center)
    }
}

/** 주차 중에만 보이는 자막과 이야기 정보. 차에서도 길게 읽게 하지 않도록 4줄로 제한한다. */
@Composable
private fun ParkedDetail(state: FlowState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when {
            state.scene == Scene.UNPAIRED ->
                BodyText("휴대폰 ourroad 앱에서 동의하고 이 차를 연결해 주세요.\n동의 전에는 경로를 기록하지 않아요.", Ourroad.Secondary)
            state.scene == Scene.LISTENING && !state.speaking -> {
                val heard = DadeuriFlow.transcript(state).take(DadeuriFlow.heardChars(state))
                BodyText(if (heard.isEmpty()) "듣고 있어요…" else "“$heard”", if (heard.isEmpty()) Ourroad.Tertiary else Ourroad.Accent)
            }
            else -> {
                if (state.scene == Scene.STORY || state.resume?.scene == Scene.STORY) {
                    StoryMeta(state, showPlace = true)
                    Spacer(Modifier.height(24.dp))
                }
                DadeuriFlow.line(state)?.let { BodyText(it, Ourroad.Secondary) }
            }
        }
    }
}

/** 주행 중에는 글을 읽게 하지 않는다. 진행 막대와 짧은 안내만 둔다. */
@Composable
private fun DrivingDetail(state: FlowState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (state.scene == Scene.STORY || state.resume?.scene == Scene.STORY) {
            StoryMeta(state, showPlace = false)
            Spacer(Modifier.height(20.dp))
        }
        Text(
            if (state.scene == Scene.UNPAIRED) "주차한 뒤 휴대폰에서 연결해 주세요" else "주행 중에는 소리로만 안내해요",
            style = Ourroad.Callout,
            color = Ourroad.Tertiary,
        )
    }
}

@Composable
private fun BodyText(text: String, color: Color) {
    Text(
        text,
        style = Ourroad.Body,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun StoryMeta(state: FlowState, showPlace: Boolean) {
    val index = (state.resume ?: state).story
    val story = DadeuriScript.stories[index]
    Column(Modifier.width(640.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (showPlace) {
            Text(
                "${story.place}  ·  ${story.heard}  ·  ${index + 1}/${DadeuriScript.stories.size}",
                style = Ourroad.Callout,
                color = Ourroad.Tertiary,
            )
            Spacer(Modifier.height(14.dp))
        }
        LinearProgressIndicator(
            progress = { DadeuriFlow.storyProgress(state) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = Ourroad.Label,
            trackColor = Ourroad.Fill,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
    }
}

@Composable
private fun StatePill(driving: Boolean) {
    Row(
        Modifier
            .background(Ourroad.Surface.copy(alpha = 0.72f), CircleShape)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(10.dp).background(if (driving) Ourroad.Accent else Ourroad.Green, CircleShape))
        Spacer(Modifier.width(10.dp))
        Text(if (driving) "주행 중 · 음성으로만" else "주차 중", style = Ourroad.Control, color = Ourroad.Label)
    }
}

@Composable
private fun Actions(state: FlowState, dispatch: (Event) -> Unit) {
    val more = state.story + 1 < DadeuriScript.stories.size
    Row(
        Modifier.heightIn(min = 88.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (state.scene) {
            Scene.UNPAIRED -> Pill("연결 확인", primary = true) { dispatch(Event.Connect) }
            Scene.IDLE -> {
                Pill("사연 듣기", primary = true) { dispatch(Event.Listen) }
                Pill("사연 남기기") { dispatch(Event.Tell) }
            }
            Scene.SUGGEST -> {
                Pill("“응”", primary = true) { dispatch(Event.Yes) }
                Pill("“괜찮아”") { dispatch(Event.No) }
            }
            Scene.STORY -> {
                Pill(if (state.paused) "이어 듣기" else "일시정지", primary = true) { dispatch(Event.TogglePause) }
                Pill("건너뛰기") { dispatch(Event.NextStory) }
                Pill("그만") { dispatch(Event.Stop) }
            }
            Scene.AFTER_STORY -> {
                Pill("“좋아”", primary = true) { dispatch(Event.Like) }
                Pill(if (more) "다음 사연" else "넘어가기") { dispatch(Event.NextStory) }
            }
            Scene.ASK_TELL -> {
                Pill("“응, 있어”", primary = true) { dispatch(Event.Yes) }
                Pill("“아니, 괜찮아”") { dispatch(Event.No) }
            }
            Scene.EMPTY_ROAD -> {
                Pill("“남길래”", primary = true) { dispatch(Event.Yes) }
                Pill("“괜찮아”") { dispatch(Event.No) }
            }
            Scene.LISTENING -> {
                Pill("다 말했어요", primary = true, enabled = !state.speaking) { dispatch(Event.DoneSpeaking) }
                Pill("그만") { dispatch(Event.Stop) }
            }
            Scene.THINKING, Scene.CLARIFY -> Pill("그만") { dispatch(Event.Stop) }
            Scene.CONFIRM -> {
                Pill("“응”", primary = true) { dispatch(Event.Reply(Answer.APPROVE)) }
                Pill("“음… 글쎄”") { dispatch(Event.Reply(Answer.UNSURE)) }
                Pill("“다시 말할게”") { dispatch(Event.Reply(Answer.RETRY)) }
            }
            Scene.PUBLISHED, Scene.SAVED_PRIVATE -> Pill("확인", primary = true) { dispatch(Event.Yes) }
            Scene.INTERRUPTED -> Pill("그만") { dispatch(Event.Stop) }
        }
    }
}

/** Apple 스타일 알약 버튼. 1차는 진한 채움, 2차는 회색 채움. 그림자·외곽선 없음. */
@Composable
private fun Pill(text: String, primary: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier
            .heightIn(min = 88.dp)
            .widthIn(min = 220.dp)
            .alpha(if (enabled) 1f else 0.35f)
            .clip(CircleShape)
            .background(if (primary) Ourroad.Label else Ourroad.Surface.copy(alpha = 0.8f))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 44.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = Ourroad.Button, color = if (primary) Color.White else Ourroad.Label, maxLines = 1)
    }
}

private fun hint(state: FlowState): String = when {
    state.scene == Scene.UNPAIRED -> " "
    state.scene == Scene.AFTER_STORY -> "“좋아”라고 말하면 공감이 남아요"
    state.scene == Scene.CONFIRM && !state.speaking -> {
        val left = ((DadeuriFlow.ANSWER_WAIT_MS - state.elapsed + 999) / 1000).coerceAtLeast(0)
        "“응”이라고 답해야만 올라가요 · ${left}초 안에 대답이 없으면 올리지 않아요"
    }
    state.scene == Scene.INTERRUPTED -> "안내가 끝나면 이어서 들려드려요"
    else -> "따옴표 버튼은 말로 대답하는 것을 대신해요"
}

private fun title(s: FlowState): String = when (s.scene) {
    Scene.UNPAIRED -> "휴대폰 연결이 필요해요"
    Scene.IDLE -> if (s.speaking) "말하는 중" else "대기 중"
    Scene.SUGGEST -> if (s.speaking) "이 길의 이야기, 들어볼까요?" else "대답을 듣고 있어요"
    Scene.STORY -> if (s.paused) "일시정지" else "사연을 들려주는 중"
    Scene.AFTER_STORY -> "사연이 끝났어요"
    Scene.ASK_TELL -> if (s.speaking) "떠오른 이야기 있으세요?" else "대답을 듣고 있어요"
    Scene.EMPTY_ROAD -> if (s.speaking) "이 길엔 아직 사연이 없어요" else "대답을 듣고 있어요"
    Scene.LISTENING -> if (s.speaking) "말하는 중" else "듣고 있어요"
    Scene.THINKING -> "사연을 다듬고 있어요"
    Scene.CLARIFY -> "조금만 더 들려주세요"
    Scene.CONFIRM -> if (s.speaking) "이렇게 올릴까요?" else "대답을 기다려요"
    Scene.PUBLISHED -> "길 위에 남겼어요"
    Scene.SAVED_PRIVATE -> "올리지 않고 보관했어요"
    Scene.INTERRUPTED -> "내비게이션 안내 중"
}

private fun voiceState(s: FlowState): DadeuriView.State = when {
    s.scene == Scene.UNPAIRED || s.scene == Scene.INTERRUPTED -> DadeuriView.State.IDLE
    s.scene == Scene.THINKING -> DadeuriView.State.THINKING
    s.scene == Scene.STORY -> if (s.paused) DadeuriView.State.IDLE else DadeuriView.State.SPEAKING
    s.speaking -> DadeuriView.State.SPEAKING
    s.scene in setOf(Scene.LISTENING, Scene.SUGGEST, Scene.AFTER_STORY, Scene.ASK_TELL, Scene.EMPTY_ROAD, Scene.CONFIRM) ->
        DadeuriView.State.LISTENING
    else -> DadeuriView.State.IDLE
}

@Composable
private fun Dadeuri(state: FlowState, modifier: Modifier) {
    if (LocalInspectionMode.current) {
        // Preview cannot run the AGSL shader; show a static stand-in.
        Box(modifier.background(Brush.radialGradient(listOf(Color(0xFFF6FFFF), Color(0xFF799FCA), Color(0xFF304365))), CircleShape))
        return
    }
    AndroidView(
        factory = { DadeuriView(it).apply { importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO } },
        update = {
            it.state = voiceState(state)
            it.still = state.settings.driving
        },
        modifier = modifier,
    )
}

// endregion

// region Demo rail (inset grouped list, like iOS Settings)

/** 목업 시연용 제어판. 실제 차량 신호·서버 결과 대신 발표자가 상황을 바꾼다. */
@Composable
private fun DemoRail(state: FlowState, dispatch: (Event) -> Unit, modifier: Modifier) {
    val settings = state.settings
    fun set(next: DemoSettings) = dispatch(Event.Settings(next))
    Column(modifier.verticalScroll(rememberScrollState())) {
        Text("시연 제어", style = Ourroad.Title.copy(fontSize = Ourroad.Headline.fontSize * 1.4f), color = Ourroad.Label)
        Spacer(Modifier.height(6.dp))
        Text("발표용 제어판이에요. 실제 차량 화면에는 없어요. 사연과 발화는 모두 예시예요.", style = Ourroad.Footnote, color = Ourroad.Secondary)

        Group("시나리오", footer = "혼자 퇴근하는 직장인, 20분째 정체, 차 안은 조용해요. 잠시 뒤 다드리가 먼저 말을 걸어요.") {
            ActionRow("시나리오 1 · 혼자 퇴근, 정체 구간") { dispatch(Event.Scenario1) }
        }

        Group(
            "차량 상황 · Vehicle·NaviHelper SDK 대신",
            footer = "혼자·정체·무음이 겹치면 주행당 한 번 먼저 물어요. " +
                if (state.suggestedThisDrive) "이번 주행에서는 이미 물었어요." else "이번 주행에서는 아직 묻지 않았어요.",
        ) {
            Row(Modifier.padding(14.dp)) {
                Segmented(listOf("주차", "주행"), if (settings.driving) 1 else 0) { set(settings.copy(driving = it == 1)) }
            }
            RowDivider()
            SwitchRow("동승자 있음 (좌석 탑승)", settings.passenger) { set(settings.copy(passenger = it)) }
            RowDivider()
            SwitchRow("정체 중 (브레이크·속도)", settings.congested) { set(settings.copy(congested = it)) }
            RowDivider()
            SwitchRow("음악 재생 중", settings.music) { set(settings.copy(music = it)) }
            RowDivider()
            SwitchRow("이 길에 사연 없음", settings.emptyRoad) { set(settings.copy(emptyRoad = it)) }
        }

        Group("서버 판단 결과", footer = "사연을 다듬을 때 서버가 돌려줄 결과예요. 시나리오 1은 이름 지움이에요.") {
            Row(Modifier.padding(14.dp)) {
                Segmented(listOf("이름 지움", "그대로", "되묻기"), settings.judge.ordinal) {
                    set(settings.copy(judge = Judge.entries[it]))
                }
            }
        }

        Group("내비게이션과 초기화", footer = "회전 지점이 가까우면 낭독을 멈추고, 안내가 끝나면 이어서 들려줘요.") {
            ActionRow("우회전 지점 접근", enabled = settings.driving && state.scene != Scene.UNPAIRED && state.scene != Scene.INTERRUPTED) {
                dispatch(Event.ApproachTurn)
            }
            RowDivider()
            ActionRow("처음부터 (연결 전)", destructive = true) { dispatch(Event.Reset) }
        }

        Group("다드리 자막", footer = "실제 차에서는 소리로만 들려요. 목업에서만 보여 주는 자막이에요.") {
            val text = when {
                state.scene == Scene.LISTENING && !state.speaking ->
                    "운전자  “" + DadeuriFlow.transcript(state).take(DadeuriFlow.heardChars(state)) + "”"
                state.scene == Scene.INTERRUPTED -> "내비  " + DadeuriScript.NAV_GUIDANCE
                else -> DadeuriFlow.line(state)?.let { "다드리  $it" } ?: "—"
            }
            Text(text, style = Ourroad.Callout, color = Ourroad.Label, modifier = Modifier.padding(20.dp))
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = Ourroad.Callout, color = Ourroad.Label, modifier = Modifier.weight(1f))
        Switch(
            checked,
            onChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Ourroad.Green,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = Ourroad.Fill,
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun Group(title: String, footer: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Spacer(Modifier.height(28.dp))
    Text(title, style = Ourroad.Footnote, color = Ourroad.Secondary, modifier = Modifier.padding(start = 20.dp, bottom = 8.dp))
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Ourroad.Surface), content = content)
    footer?.let {
        Text(it, style = Ourroad.Footnote, color = Ourroad.Tertiary, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp))
    }
}

@Composable
private fun RowDivider() = HorizontalDivider(Modifier.padding(start = 20.dp), thickness = 1.dp, color = Ourroad.Separator)

@Composable
private fun ActionRow(text: String, enabled: Boolean = true, destructive: Boolean = false, onClick: () -> Unit) {
    val color = when {
        !enabled -> Ourroad.Tertiary
        destructive -> Color(0xFFFF3B30)
        else -> Ourroad.Accent
    }
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text, style = Ourroad.Callout, color = color)
    }
}

/** iOS 세그먼트 컨트롤: 회색 트랙 위에 흰 선택 칸. */
@Composable
private fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Ourroad.Fill)
            .padding(3.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .then(if (on) Modifier.shadow(2.dp, RoundedCornerShape(10.dp)) else Modifier)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (on) Ourroad.Surface else Color.Transparent)
                    .clickable(role = Role.Tab) { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = segmentStyle(on), color = Ourroad.Label, maxLines = 1)
            }
        }
    }
}

private fun segmentStyle(on: Boolean): TextStyle =
    if (on) Ourroad.Control.copy(fontWeight = Ourroad.Headline.fontWeight) else Ourroad.Control

// endregion

@Preview(widthDp = 1920, heightDp = 1080, name = "대기 · 주차")
@Composable
private fun PreviewIdle() = CarScreen(FlowState(scene = Scene.IDLE)) {}

@Preview(widthDp = 1920, heightDp = 1080, name = "이야기 듣기 · 주차")
@Composable
private fun PreviewStory() = CarScreen(FlowState(scene = Scene.STORY, speaking = true, elapsed = 3_000)) {}

@Preview(widthDp = 1920, heightDp = 1080, name = "먼저 말 걸기 · 주행")
@Composable
private fun PreviewSuggestDriving() =
    CarScreen(FlowState(scene = Scene.SUGGEST, speaking = true, settings = DadeuriFlow.SCENARIO_1)) {}
