package io.github.hyundaimotorappcontest.unseen

/*
 * 다드리 차량 대화 흐름 (1차 기획 목업, 접수양식 시나리오 1 기준).
 *
 * Android 타입이 없는 순수 Kotlin 상태기계다. 차량에서는 대화와 듣기만 하고, 보기·수정·관리는 휴대폰 앱에서 한다.
 * 음성 인식·낭독·차량 신호·서버 판단은 SDK가 열리기 전이라 [DadeuriScript]의 예시 대사와 시연 제어 값으로 흉내 낸다.
 * 나중에 Pleos STT·TTS·Vehicle·NaviHelper와 서버 어댑터를 붙여도 이 상태기계는 그대로 쓴다.
 */

enum class Scene {
    /** 휴대폰 앱에서 동의·연결 전. */
    UNPAIRED,

    /** 대기. [Ack]가 있으면 짧게 말한 뒤 대기한다. */
    IDLE,

    /** 혼자·정체·무음이 겹칠 때 다드리가 먼저 묻는다. 주행당 한 번. */
    SUGGEST,

    /** 같은 길의 사연을 라디오처럼 낭독한다. */
    STORY,

    /** 사연이 끝난 직후. "좋아"라고 말하면 공감을 남긴다. */
    AFTER_STORY,

    /** "이 길에서 떠오른 이야기 있으세요?" */
    ASK_TELL,

    /** 이 길에 사연이 없을 때. 발견 없음은 정상 상태다. */
    EMPTY_ROAD,

    /** 운전자의 말을 듣는다 (STT). */
    LISTENING,

    /** 서버가 욕설·비방을 거르고 개인정보를 지우고 문장을 다듬는 중. */
    THINKING,

    /** 판단 모델이 불명확하다고 보고 되묻는다. */
    CLARIFY,

    /** 다듬은 사연을 읽어 주고 "이렇게 올릴까요?" 명시적 승인을 기다린다. */
    CONFIRM,

    /** "응"으로 게시했다. */
    PUBLISHED,

    /** 승인이 없어서 올리지 않고 휴대폰 보관함에 비공개로 저장했다. */
    SAVED_PRIVATE,

    /** 회전 안내 등 내비게이션 음성이 끼어들어 잠시 멈췄다. 안내가 끝나면 이어서 한다. */
    INTERRUPTED,
}

enum class Ack { NONE, WELCOME, STOPPED, LATER, STORIES_DONE, BYE }

/** 서버 판단 모델 결과 시나리오 (시연 제어에서 고른다). */
enum class Judge { REDACT, CLEAN, CLARIFY }

/** 확인 질문에 대한 운전자의 대답. */
enum class Answer { APPROVE, UNSURE, RETRY, SILENCE }

enum class Round { TELL, CLARIFY_ANSWER }

enum class PrivateReason { UNSURE, SILENCE }

data class Compose(val round: Round = Round.TELL)

/**
 * 실제 차량 신호(Vehicle·NaviHelper SDK) 대신 시연 제어에서 바꾸는 차량 상황.
 * 운전자의 기분은 추측하지 않고 차량 상황만 본다.
 */
data class DemoSettings(
    val driving: Boolean = false,
    /** 좌석 탑승 정보로 본 동승자 여부. */
    val passenger: Boolean = false,
    /** 브레이크·속도 변화로 본 정체 여부. */
    val congested: Boolean = false,
    /** 음악·라디오 재생 중이면 조용한 상태가 아니다. */
    val music: Boolean = false,
    val emptyRoad: Boolean = false,
    val judge: Judge = Judge.REDACT,
)

data class FlowState(
    val scene: Scene = Scene.UNPAIRED,
    /** true면 다드리(또는 끼어든 내비)가 이 장면의 대사를 말하는 중이다. */
    val speaking: Boolean = false,
    /** 현재 단계에서 흐른 시간(ms). 일시정지 중에는 멈춘다. */
    val elapsed: Long = 0,
    val story: Int = 0,
    val paused: Boolean = false,
    /** 방금 들은 사연에 "좋아"로 공감했다. */
    val liked: Boolean = false,
    val compose: Compose? = null,
    val ack: Ack = Ack.NONE,
    val privateReason: PrivateReason = PrivateReason.UNSURE,
    val resume: FlowState? = null,
    /** 이번 주행에서 이미 먼저 물었다. 주차하면 다음 주행을 위해 초기화한다. */
    val suggestedThisDrive: Boolean = false,
    val settings: DemoSettings = DemoSettings(),
)

sealed interface Event {
    data class Tick(val ms: Long) : Event
    data object Connect : Event
    data object Listen : Event
    data object Tell : Event
    data object Yes : Event
    data object No : Event
    data object Like : Event
    data object TogglePause : Event
    data object NextStory : Event
    data object DoneSpeaking : Event
    data object Stop : Event
    data class Reply(val answer: Answer) : Event
    data object ApproachTurn : Event
    data class Settings(val settings: DemoSettings) : Event
    data object Scenario1 : Event
    data object Reset : Event
}

object DadeuriFlow {
    const val SUGGEST_AFTER_MS = 2_000L
    const val THINKING_MS = 2_000L
    const val END_OF_SPEECH_MS = 1_200L
    const val LIKE_WINDOW_MS = 6_000L
    const val ANSWER_WAIT_MS = 10_000L
    const val PROMPT_WAIT_MS = 12_000L
    const val RESULT_HOLD_MS = 4_000L

    /** 시나리오 1: 혼자 퇴근하는 직장인, 20분째 정체, 차 안은 조용하다. */
    val SCENARIO_1 = DemoSettings(driving = true, passenger = false, congested = true, music = false, judge = Judge.REDACT)

    fun reduce(s: FlowState, e: Event): FlowState = when (e) {
        is Event.Tick -> tick(s, e.ms)
        Event.Connect -> if (s.scene == Scene.UNPAIRED) idle(s, Ack.WELCOME) else s
        Event.Listen -> if (s.scene == Scene.IDLE) startListening(s) else s
        Event.Tell -> if (s.scene == Scene.IDLE) listen(s) else s
        Event.Yes -> when (s.scene) {
            Scene.SUGGEST -> story(s, 0)
            Scene.ASK_TELL, Scene.EMPTY_ROAD -> listen(s)
            Scene.PUBLISHED, Scene.SAVED_PRIVATE -> idle(s, Ack.NONE)
            else -> s
        }
        Event.No -> declined(s)
        Event.Like -> if (s.scene == Scene.AFTER_STORY) phase(s, Scene.ASK_TELL).copy(liked = true) else s
        Event.TogglePause -> if (s.scene == Scene.STORY) s.copy(paused = !s.paused) else s
        Event.NextStory -> when (s.scene) {
            Scene.STORY -> afterStory(s)
            Scene.AFTER_STORY -> if (hasNext(s)) story(s, s.story + 1) else phase(s, Scene.ASK_TELL)
            else -> s
        }
        Event.DoneSpeaking ->
            if (s.scene == Scene.LISTENING && !s.speaking) phase(s, Scene.THINKING, speaking = false) else s
        Event.Stop -> if (s.scene == Scene.UNPAIRED || s.scene == Scene.IDLE) s else idle(s, Ack.STOPPED)
        is Event.Reply -> if (s.scene == Scene.CONFIRM) answer(s, e.answer) else s
        Event.ApproachTurn ->
            if (s.scene == Scene.UNPAIRED || s.scene == Scene.INTERRUPTED || !s.settings.driving) s
            else phase(s, Scene.INTERRUPTED).copy(resume = s)
        is Event.Settings -> withSettings(s, e.settings)
        Event.Scenario1 -> FlowState(scene = Scene.IDLE, settings = SCENARIO_1)
        Event.Reset -> FlowState(settings = s.settings)
    }

    /** 다드리가 지금 하는 말. 말하지 않는 장면은 null. */
    fun line(s: FlowState): String? = DadeuriScript.line(s)

    fun speakMs(text: String?): Long = if (text == null) 0 else maxOf(1_600L, text.length * 75L)

    fun transcript(s: FlowState): String = DadeuriScript.transcript(s.compose ?: Compose(), s.settings.judge)

    /** LISTENING에서 지금까지 들린 글자 수. 들리는 속도를 흉내 낸다. */
    fun heardChars(s: FlowState): Int {
        if (s.scene != Scene.LISTENING || s.speaking) return 0
        return (s.elapsed / CHAR_MS).toInt().coerceAtMost(transcript(s).length)
    }

    /** 0..1 사연 낭독 진행률. */
    fun storyProgress(s: FlowState): Float {
        val story = if (s.scene == Scene.INTERRUPTED) s.resume ?: return 0f else s
        if (story.scene != Scene.STORY) return 0f
        return (story.elapsed.toFloat() / speakMs(line(story))).coerceIn(0f, 1f)
    }

    /** 자동 제안 조건: 주행 중, 혼자, 정체, 조용함, 이번 주행에 아직 묻지 않음. */
    fun canSuggest(s: FlowState): Boolean = s.settings.let {
        it.driving && !it.passenger && it.congested && !it.music && !it.emptyRoad
    } && !s.suggestedThisDrive

    private const val CHAR_MS = 70L

    private fun tick(s: FlowState, ms: Long): FlowState {
        if (s.scene == Scene.UNPAIRED || (s.scene == Scene.STORY && s.paused)) return s
        val t = s.copy(elapsed = s.elapsed + ms)
        if (t.speaking) {
            if (t.elapsed < speakMs(line(t))) return t
            return lineDone(t)
        }
        return when (t.scene) {
            Scene.IDLE ->
                if (canSuggest(t) && t.elapsed >= SUGGEST_AFTER_MS) {
                    phase(t, Scene.SUGGEST).copy(suggestedThisDrive = true)
                } else {
                    t
                }
            Scene.LISTENING ->
                if (t.elapsed >= transcript(t).length * CHAR_MS + END_OF_SPEECH_MS) {
                    phase(t, Scene.THINKING, speaking = false)
                } else {
                    t
                }
            Scene.THINKING -> if (t.elapsed >= THINKING_MS) judged(t) else t
            Scene.AFTER_STORY -> if (t.elapsed >= LIKE_WINDOW_MS) phase(t, Scene.ASK_TELL) else t
            Scene.CONFIRM -> if (t.elapsed >= ANSWER_WAIT_MS) answer(t, Answer.SILENCE) else t
            Scene.SUGGEST, Scene.ASK_TELL, Scene.EMPTY_ROAD ->
                if (t.elapsed >= PROMPT_WAIT_MS) declined(t) else t
            Scene.PUBLISHED, Scene.SAVED_PRIVATE ->
                if (t.elapsed >= RESULT_HOLD_MS) idle(t, Ack.NONE) else t
            else -> t
        }
    }

    /** 대사를 다 말한 직후. 대부분은 대답을 기다리고(듣기), 일부는 다음 장면으로 넘어간다. */
    private fun lineDone(s: FlowState): FlowState = when (s.scene) {
        Scene.STORY -> afterStory(s)
        Scene.CLARIFY -> phase(s, Scene.LISTENING, speaking = false).copy(compose = Compose(Round.CLARIFY_ANSWER))
        // 내비 안내가 끝나면 멈춘 자리에서 이어서 한다.
        Scene.INTERRUPTED -> resumed(s)
        else -> s.copy(speaking = false, elapsed = 0)
    }

    private fun startListening(s: FlowState): FlowState =
        if (s.settings.emptyRoad) phase(s, Scene.EMPTY_ROAD) else story(s, 0)

    private fun story(s: FlowState, index: Int): FlowState =
        phase(s, Scene.STORY).copy(story = index, paused = false, liked = false)

    private fun afterStory(s: FlowState): FlowState = phase(s, Scene.AFTER_STORY, speaking = false).copy(paused = false)

    private fun hasNext(s: FlowState) = s.story + 1 < DadeuriScript.stories.size

    private fun listen(s: FlowState): FlowState = phase(s, Scene.LISTENING).copy(compose = Compose())

    private fun judged(s: FlowState): FlowState {
        val clarify = s.settings.judge == Judge.CLARIFY && s.compose?.round != Round.CLARIFY_ANSWER
        return phase(s, if (clarify) Scene.CLARIFY else Scene.CONFIRM)
    }

    private fun answer(s: FlowState, answer: Answer): FlowState = when (answer) {
        Answer.APPROVE -> phase(s, Scene.PUBLISHED)
        Answer.UNSURE -> phase(s, Scene.SAVED_PRIVATE).copy(privateReason = PrivateReason.UNSURE)
        Answer.SILENCE -> phase(s, Scene.SAVED_PRIVATE).copy(privateReason = PrivateReason.SILENCE)
        Answer.RETRY -> listen(s)
    }

    private fun declined(s: FlowState): FlowState = when (s.scene) {
        // 거절하면 이번 주행에서는 다시 묻지 않는다 (suggestedThisDrive 유지).
        Scene.SUGGEST -> idle(s, Ack.LATER)
        Scene.ASK_TELL -> idle(s, Ack.BYE)
        Scene.EMPTY_ROAD -> idle(s, Ack.NONE)
        else -> s
    }

    private fun resumed(s: FlowState): FlowState {
        val back = s.resume ?: return idle(s, Ack.NONE)
        val settings = s.settings
        return when (back.scene) {
            // 사연은 멈춘 자리에서 이어 듣는다.
            Scene.STORY -> back.copy(settings = settings)
            Scene.IDLE, Scene.LISTENING, Scene.THINKING -> back.copy(elapsed = 0, settings = settings)
            // 질문·결과 안내는 처음부터 다시 말한다.
            else -> back.copy(speaking = line(back.copy(speaking = true)) != null, elapsed = 0, settings = settings)
        }
    }

    private fun withSettings(s: FlowState, next: DemoSettings): FlowState {
        // 주차했다가 다시 출발하면 새 주행이다.
        val newDrive = !s.settings.driving && next.driving
        val updated = s.copy(
            settings = next,
            suggestedThisDrive = if (newDrive) false else s.suggestedThisDrive,
            elapsed = if (s.scene == Scene.IDLE && !s.speaking) 0 else s.elapsed,
        )
        return updated.resume?.let { updated.copy(resume = it.copy(settings = next)) } ?: updated
    }

    private fun idle(s: FlowState, ack: Ack): FlowState =
        phase(s, Scene.IDLE, speaking = ack != Ack.NONE).copy(ack = ack, compose = null, paused = false, liked = false)

    private fun phase(s: FlowState, scene: Scene, speaking: Boolean = true): FlowState =
        s.copy(scene = scene, speaking = speaking, elapsed = 0, resume = null)
}
