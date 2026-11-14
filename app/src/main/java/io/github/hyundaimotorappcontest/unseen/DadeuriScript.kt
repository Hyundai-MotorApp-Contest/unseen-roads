package io.github.hyundaimotorappcontest.unseen

/**
 * 1차 기획 목업의 대사와 예시 데이터 (접수양식 시나리오 1: 혼자 퇴근하는 직장인의 정체 구간).
 *
 * 모든 사연·발화·정리본은 시연용 예시다. 실제 사용자 경험이나 실제 경로 매칭 결과가 아니다.
 * 구간 이름은 가상의 일반 명칭이며 타인의 출발지·목적지·정확한 통과 시각은 담지 않는다.
 */
object DadeuriScript {
    data class Story(val place: String, val heard: String, val text: String)

    val stories = listOf(
        Story(
            place = "강변북로 정체 구간",
            heard = "같은 방향 · 어제",
            text = "오늘도 여기서 한참 서 있었어요. 앞차 뒷유리에 붙은 '아기가 타고 있어요' 스티커를 보다가, " +
                "우리 아이 처음 태우고 이 길을 지나던 날이 생각났어요. 그땐 이 정체도 하나도 안 길었는데요.",
        ),
        Story(
            place = "강변북로 정체 구간",
            heard = "같은 방향 · 지난주",
            text = "비 오는 아침, 끼어들게 해 줬더니 앞차가 비상등으로 고맙다고 했어요. 별것 아닌데 하루 종일 기분이 좋았어요.",
        ),
    )

    // 시나리오 1에서 민수 씨가 하는 말. REDACT는 다른 사람 이름이 들어 있는 경우다.
    private const val TELL_WITH_NAME =
        "오늘 회의에서 박지훈 과장님이 내 아이디어 좋다고 해 줘서 진짜 기분 좋았어. 길은 막히는데 이상하게 하나도 짜증이 안 나네."
    private const val TELL_CLEAN =
        "오늘 회의에서 내 아이디어 좋다는 말 들어서 진짜 기분 좋았어. 길은 막히는데 이상하게 하나도 짜증이 안 나네."
    private const val TELL_VAGUE = "오늘 회의에서 그거 있잖아, 그거 때문에 기분이 좋았어."
    private const val CLARIFY_ANSWER = "내가 낸 아이디어가 좋다는 말을 들었어. 막히는 길인데도 기분이 좋네."

    /** 서버가 다듬은 사연. 원래 말에 없는 내용은 더하지 않는다. */
    const val DRAFT = "오늘 회의에서 제 아이디어가 좋다는 말을 들었어요. 길은 막히는데 이상하게 하나도 짜증이 나지 않는 퇴근길이에요."

    const val NAV_GUIDANCE = "300미터 앞에서 우회전입니다."

    fun transcript(compose: Compose, judge: Judge): String = when {
        compose.round == Round.CLARIFY_ANSWER -> CLARIFY_ANSWER
        judge == Judge.REDACT -> TELL_WITH_NAME
        judge == Judge.CLARIFY -> TELL_VAGUE
        else -> TELL_CLEAN
    }

    fun line(s: FlowState): String? = when (s.scene) {
        Scene.UNPAIRED -> null
        Scene.IDLE -> when (s.ack) {
            Ack.NONE -> null
            Ack.WELCOME -> "연결됐어요. 저는 다드리예요. 같은 길의 사연을 들려드리고, 말씀하시면 받아 적을게요."
            Ack.STOPPED -> "알겠어요. 여기서 멈출게요."
            Ack.LATER -> "알겠어요. 이번 주행에서는 다시 묻지 않을게요."
            Ack.STORIES_DONE -> "이 길의 사연은 여기까지예요."
            Ack.BYE -> "알겠어요. 편하게 가세요."
        }
        Scene.SUGGEST -> "길이 좀 막히네요. 이 길을 지난 사람들의 이야기 하나 들어볼까요?"
        Scene.STORY -> stories[s.story].let { "${it.heard.substringAfter("· ")} 이 길을 지난 분이 남긴 사연이에요. ${it.text}" }
        Scene.AFTER_STORY -> null
        Scene.ASK_TELL ->
            if (s.liked) "공감을 남겼어요. 이 길에서 떠오른 이야기 있으세요?" else "이 길에서 떠오른 이야기 있으세요?"
        Scene.EMPTY_ROAD -> "이 길엔 아직 사연이 없어요. 첫 이야기를 남겨볼까요?"
        Scene.LISTENING -> if (s.speaking) "네, 편하게 말씀해 주세요." else null
        Scene.THINKING -> null
        Scene.CLARIFY -> "회의에서 어떤 일이 있었는지 조금만 더 들려주시겠어요?"
        Scene.CONFIRM ->
            if (s.settings.judge == Judge.REDACT) "다른 사람을 알아볼 수 있는 이름은 빼고 다듬었어요. “$DRAFT” 이렇게 올릴까요?"
            else "“$DRAFT” 이렇게 올릴까요?"
        Scene.PUBLISHED -> "올렸어요. 이 길을 지나는 분들이 듣게 돼요."
        Scene.SAVED_PRIVATE -> when (s.privateReason) {
            PrivateReason.UNSURE -> "확실하지 않아서 올리지 않았어요. 휴대폰 보관함에 나만 보이게 저장했어요."
            PrivateReason.SILENCE -> "대답이 없어서 올리지 않았어요. 휴대폰 보관함에 나만 보이게 저장했어요."
        }
        Scene.INTERRUPTED -> if (s.speaking) NAV_GUIDANCE else null
    }
}
