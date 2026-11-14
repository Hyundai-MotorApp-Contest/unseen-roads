package io.github.hyundaimotorappcontest.unseen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DadeuriFlowTest {
    private fun FlowState.on(vararg events: Event) = events.fold(this) { s, e -> DadeuriFlow.reduce(s, e) }

    /** 현재 대사를 끝까지 말하게 한다. */
    private fun FlowState.finishLine(): FlowState {
        var s = this
        var guard = 0
        while (s.speaking && guard++ < 2_000) s = DadeuriFlow.reduce(s, Event.Tick(100))
        return s
    }

    private fun FlowState.wait(ms: Long): FlowState {
        var s = this
        repeat((ms / 100).toInt()) { s = DadeuriFlow.reduce(s, Event.Tick(100)) }
        return s
    }

    private val idle = FlowState().on(Event.Connect).finishLine()

    @Test
    fun nothingHappensBeforeConnecting() {
        val s = FlowState().on(Event.Listen, Event.Tell, Event.ApproachTurn, Event.Tick(60_000))
        assertEquals(Scene.UNPAIRED, s.scene)
    }

    /** 접수양식 시나리오 1 전체: 먼저 묻기 → 사연 → 회전 안내 후 이어 듣기 → 좋아 → 이야기 남기기 → 응 → 게시. */
    @Test
    fun scenario1EndToEnd() {
        var s = FlowState().on(Event.Scenario1).wait(DadeuriFlow.SUGGEST_AFTER_MS)
        assertEquals(Scene.SUGGEST, s.scene)
        s = s.finishLine().on(Event.Yes)
        assertEquals(Scene.STORY, s.scene)

        s = s.wait(2_000)
        val before = s.elapsed
        s = s.on(Event.ApproachTurn)
        assertEquals(Scene.INTERRUPTED, s.scene)
        while (s.scene == Scene.INTERRUPTED) s = DadeuriFlow.reduce(s, Event.Tick(100))
        assertEquals("안내가 끝나면 자동으로 이어서 재생한다", Scene.STORY, s.scene)
        assertEquals(before, s.elapsed)

        s = s.finishLine()
        assertEquals(Scene.AFTER_STORY, s.scene)
        s = s.on(Event.Like)
        assertEquals(Scene.ASK_TELL, s.scene)
        assertTrue(s.liked)

        s = s.finishLine().on(Event.Yes).finishLine()
        assertEquals(Scene.LISTENING, s.scene)
        assertTrue(DadeuriFlow.transcript(s).contains("과장님"))
        s = s.on(Event.DoneSpeaking).wait(DadeuriFlow.THINKING_MS)
        assertEquals(Scene.CONFIRM, s.scene)
        assertFalse("다른 사람 이름은 지운다", DadeuriScript.DRAFT.contains("과장"))
        assertEquals(Scene.PUBLISHED, s.on(Event.Reply(Answer.APPROVE)).scene)
    }

    @Test
    fun suggestionNeedsSoloCongestedQuietDriving() {
        val base = idle.on(Event.Settings(DadeuriFlow.SCENARIO_1))
        assertEquals(Scene.SUGGEST, base.wait(DadeuriFlow.SUGGEST_AFTER_MS).scene)
        listOf(
            DadeuriFlow.SCENARIO_1.copy(passenger = true),
            DadeuriFlow.SCENARIO_1.copy(congested = false),
            DadeuriFlow.SCENARIO_1.copy(music = true),
            DadeuriFlow.SCENARIO_1.copy(driving = false),
        ).forEach {
            assertEquals(it.toString(), Scene.IDLE, idle.on(Event.Settings(it)).wait(30_000).scene)
        }
    }

    @Test
    fun suggestionOnlyOncePerDrive() {
        val declined = FlowState().on(Event.Scenario1).wait(DadeuriFlow.SUGGEST_AFTER_MS).finishLine().on(Event.No)
        assertEquals(Scene.IDLE, declined.scene)
        assertEquals(Ack.LATER, declined.ack)
        assertEquals(Scene.IDLE, declined.wait(60_000).scene)
        // 주차 후 다시 출발하면 새 주행이다.
        val parked = declined.on(Event.Settings(DadeuriFlow.SCENARIO_1.copy(driving = false)))
        val again = parked.on(Event.Settings(DadeuriFlow.SCENARIO_1)).finishLine().wait(DadeuriFlow.SUGGEST_AFTER_MS)
        assertEquals(Scene.SUGGEST, again.scene)
    }

    @Test
    fun pausedStoryDoesNotAdvance() {
        val paused = idle.on(Event.Listen, Event.TogglePause).wait(60_000)
        assertEquals(Scene.STORY, paused.scene)
        assertEquals(0L, paused.elapsed)
    }

    @Test
    fun emptyRoadIsANormalState() {
        assertEquals(Scene.EMPTY_ROAD, idle.on(Event.Settings(DemoSettings(emptyRoad = true)), Event.Listen).scene)
    }

    @Test
    fun noLikeWithinWindowStillAsksToTell() {
        val s = idle.on(Event.Listen).finishLine().wait(DadeuriFlow.LIKE_WINDOW_MS)
        assertEquals(Scene.ASK_TELL, s.scene)
        assertFalse(s.liked)
    }

    @Test
    fun clarifyThenConfirm() {
        var s = idle.on(Event.Settings(DemoSettings(judge = Judge.CLARIFY)), Event.Tell).finishLine()
        s = s.on(Event.DoneSpeaking).wait(DadeuriFlow.THINKING_MS)
        assertEquals(Scene.CLARIFY, s.scene)
        s = s.finishLine()
        assertEquals(Scene.LISTENING, s.scene)
        assertEquals(Round.CLARIFY_ANSWER, s.compose?.round)
        s = s.on(Event.DoneSpeaking).wait(DadeuriFlow.THINKING_MS)
        assertEquals(Scene.CONFIRM, s.scene)
    }

    @Test
    fun silenceOrUnsureNeverPublishes() {
        val confirm = idle.on(Event.Tell).finishLine().on(Event.DoneSpeaking).wait(DadeuriFlow.THINKING_MS)
        assertEquals(Scene.CONFIRM, confirm.scene)
        val silent = confirm.finishLine().wait(DadeuriFlow.ANSWER_WAIT_MS)
        assertEquals(Scene.SAVED_PRIVATE, silent.scene)
        assertEquals(PrivateReason.SILENCE, silent.privateReason)
        val unsure = confirm.on(Event.Reply(Answer.UNSURE))
        assertEquals(Scene.SAVED_PRIVATE, unsure.scene)
        assertEquals(PrivateReason.UNSURE, unsure.privateReason)
    }

    @Test
    fun turnGuidanceOnlyWhileDriving() {
        assertEquals(Scene.STORY, idle.on(Event.Listen, Event.ApproachTurn).scene)
    }

    @Test
    fun stopAlwaysReturnsToIdleWithoutPublishing() {
        val stopped = idle.on(Event.Tell).finishLine().on(Event.Stop)
        assertEquals(Scene.IDLE, stopped.scene)
        assertEquals(null, stopped.compose)
    }
}
