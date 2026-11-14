# ourroad · 다드리 음성 기획과 향후 SDK 사용 방안

갱신일: 2026-11-15 KST. 기준은 최신 논의 내용인 [README](../README.md)의 주요 기능·시스템 구조·음성 사연 흐름이다. 앱 표시 이름은 **ourroad**, 앱 안의 음성 에이전트는 **다드리**다. 저장소 `unseen-roads`와 applicationId는 유지한다.

## 1차 기획 제출 범위

- 1차 기획 제출 단계에서는 Pleos SDK 사용이 열리지 않는다. 이 문서는 **기획과 향후 사용 방안**이며 구현·검증 결과가 아니다.
- 실제 SDK 호출·Emulator·실차 검증은 1차 범위에서 NOT_APPLICABLE이다. 수행하지 않은 검증을 PASS로 쓰지 않는다.
- SDK 버전·권한·지원 상태는 사용이 열리는 시점에 [SDK API 참고](pleos/sdk-api-reference.md)와 공식 원문으로 다시 확인한다.

## 한 줄 경험

운전자가 라디오에 사연을 보내듯 말하면, 다드리가 받아 적고 다듬어서 "이렇게 올릴까요?"라고 되묻는다. 운전자가 "응"이라고 답해야만 그 길 위에 이야기가 남는다. 지금 지나는 구간에 남겨진 이야기는 음성으로 듣는다.

## 음성 사연 흐름

상태 다이어그램은 [README의 음성 사연 흐름](../README.md#음성-사연-흐름)을 따른다.

| 상태 | 하는 일 | 사용할 구성 |
| --- | --- | --- |
| IDLE | 대기. 운전자가 버튼으로 시작 | 화면 · 음성 대화 관리 |
| LISTENING | 사연·답변 음성 인식 | Pleos SpeechToText (ON_DEVICE) |
| DRAFTING | STT 텍스트를 사연 초안으로 정리 | 자체 서버 정리 AI |
| JUDGING | 원래 말과 같은지, 개인정보가 없는지, 부적절하지 않은지 판단 | 자체 서버 판단 모델(Judge), JSON 응답 |
| ASK_CLARIFY | 불명확하면 되묻고 다시 듣기 | Pleos TextToSpeech → SpeechToText |
| ASK_CONFIRM / CONFIRM_LISTEN | "이렇게 올릴까요?" 낭독 후 답변 듣기 | Pleos TextToSpeech → SpeechToText |
| PUBLISH | 명시적 승인일 때만 게시 | 자체 서버 API |
| SAVED_PRIVATE | 무응답·애매함·실패는 비공개 보관 | 자체 서버 API |

원칙:

- 무응답·애매한 대답·인식 실패는 승인이 아니다. AI가 혼자 게시하지 않는다.
- 시작과 중단은 언제든 버튼으로도 할 수 있다. 내비게이션 안내나 통화가 오면 낭독을 멈췄다가 이어서 묻는다.
- 음성 대화 관리는 SDK 없이 테스트할 수 있도록 순수 Kotlin 상태기계로 분리한다. SDK 호출은 [어댑터 경계](pleos/architecture.md) 뒤에 둔다.
- 이야기 듣기는 실제로 지나온 공통 도로 구간의 이야기만 대상으로 한다. 후보가 없으면 정상 빈 상태로 안내한다.

## 향후 Pleos SDK 사용 방안

| SDK | 문서상 버전 | 사용 계획 | 사용 열릴 때 확인할 것 |
| --- | --- | --- | --- |
| SpeechToText | 3.3.1 | 사연·확인 답변 인식. ON_DEVICE 모드로 차량 마이크 음성을 외부로 보내지 않음 | 사용 승인, 필요한 권한, 주행 중 마이크 입력 허용 범위, 한국어 인식 품질 |
| TextToSpeech | 3.3.1 | 되묻기·확인·이야기 낭독 | 사용 승인, 오디오 컨텍스트·포커스 동작, 선행 묵음 적용 가능 여부 |
| Vehicle | 미정 | 주행·주차 상태와 차량 정보 | 기어·주행 상태 API와 권한, capability |
| FusedLocation | 1.1.3 (미정) | 현재 구간 판별 | 위치 권한, 정확도, 동의 전 수집 차단 |
| NaviHelper | 사용 여부 미정 | 경로·안내 상태 연동 후보 | 필요성 판단 |

- 공식 근거: [STT 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro), [TTS 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro), [SDK 신청 상태](https://document.pleos.ai/en/docs/pleos-only/create-my-project). 2026-11-09에 확인했을 때 공개 사용 신청 안내는 "준비 중"이었다.
- ON_DEVICE 모드는 별도 앱 등록(Client ID/Secret)이 필요 없다고 문서에 적혀 있다. 실제 승인 상태는 프로젝트 콘솔 기준으로 다시 확인한다.
- 미승인 Pleos 권한은 Manifest에 선언하지 않는다. 사용이 열리면 승인 범위에 맞춰 추가한다.

## 자체 서버와 데이터

- 서버에는 STT가 만든 **텍스트만** HTTPS로 보낸다. 마이크 원음은 보내지 않는다.
- 정리 AI는 말을 사연 초안으로 다듬고, 판단 모델은 되묻기·승인·부적절 여부를 구조화된 값(JSON)으로 돌려준다.
- Pleos LLM SDK가 제공되지 않으므로 LLM은 자체 서버에서 외부 API로 처리한다. API 키와 서버 소스는 이 저장소에 넣지 않는다.
- 사연에 타인의 원시 경로·정확한 통과 시각·출발지·목적지·차량 식별자를 싣지 않는다. 보관 기간·공간 해상도는 미정 결정이다.

## 주행 상태별 동작 계획

| 상태 | 화면 | 음성 |
| --- | --- | --- |
| 주차 + UX 제한 해제 | 이야기 목록·개인 기록·상세 화면 | 전체 흐름 |
| 주행 중 (CarUxRestrictions 제한) | 최소 정보만. 팝업·피드·장문·키보드 없음 | 듣기·말하기·되묻기만 계획. 허용 범위는 아래 확인 결과를 따름 |
| 상태 미상·신호 유실·연결 실패 | 제한 화면 | 음성 세션 중단, 자동 재개 없음 |

주행 중 음성의 허용 근거는 아직 공식 문서에서 확인하지 못했다([주행 안전](pleos/driving-safety.md), [로드맵 D6](pleos/implementation-roadmap.md)). 확인 전 구현에서는 제한 상태에서 녹음·낭독을 시작하지 않는다. 카테고리 예시(Music/Audio 등)를 허용 근거로 쓰지 않는다.

오디오는 재생 전 포커스를 요청하고, 포커스 상실·통화·내비 안내 시 멈춘 뒤 사용자 재요청이나 복귀 규칙에 따라 이어간다. Resume 시 자동 재생하지 않는다.

## Gleo 연동 (향후)

Gleo Interface는 등록한 앱 function을 Gleo가 선택해 Android Intent로 호출하고 앱이 Callback으로 응답하는 계약이다. 우리 앱에 내장하는 범용 LLM이나 독립 웨이크워드 API가 아니다. `invokeWord: "다드리"`는 앱 호출 단어 스키마일 뿐이다. 출처: [Gleo 개념](https://document.pleos.ai/docs/pleos-only/app-integration-guide/guide/core-concepts), [App Schema](https://document.pleos.ai/docs/pleos-only/app-integration-guide/guide/app-capability), [전달·승인](https://document.pleos.ai/docs/pleos-only/app-integration-guide/guide/deliver-schema), [Callback](https://document.pleos.ai/docs/pleos-only/app-integration-guide/guide/action_callback).

후보 function은 "사연 남기기 시작"과 "음성 중단" 정도로 둔다. 외부 Intent만으로 게시·삭제를 승인하지 않는다. 송신자 인증, 제한 상태에서의 silent 응답, 스키마 승인을 확인하기 전에는 APK에 스키마나 진입점을 넣지 않는다.

## 현재 저장소 코드와의 관계

2026-11-15 기준 차량 앱은 1차 기획 목업이다. 차에서는 대화와 듣기만 하고 보기·관리는 휴대폰 앱이 맡는다는 역할 분리에 따라, 차량 화면은 다드리·상태 한 줄·대답 버튼만 둔다. 접수양식 시나리오 1(혼자 퇴근하는 직장인의 정체 구간)을 그대로 시연한다: 혼자·정체·무음일 때 주행당 한 번 먼저 묻기 → 사연 낭독 → 회전 안내 후 이어 듣기 → "좋아" 공감 → 이야기 남기기 → 이름 지움 → "응"으로만 게시.

- `DadeuriFlow`: Android 타입이 없는 대화 상태기계. SDK가 열리면 STT·TTS·Vehicle·NaviHelper·서버 어댑터가 이 상태기계에 이벤트를 보내도록 바꾼다.
- 음성·차량 신호·서버 판단은 예시 대사와 오른쪽 "시연 제어"로 흉내 낸다. 시연 제어와 예시 사연은 App Market 제출 빌드에 넣지 않는다.
- 2026-11-09의 Android 표준 엔진 프로토타입(AndroidVoice·ParkedAccess·VoiceSession)은 이 목업으로 대체하며 제거했다.

## 확인 대상

- 주행 중 마이크 입력과 공개 전 음성 확인의 허용 범위 (운영사무국·현대자동차)
- 탑승 시 자동 시작 안내의 허용 여부
- Pleos SpeechToText·TextToSpeech 사용 승인 시점과 권한
- 앱 카테고리와 DO 선언 범위 ([로드맵](pleos/implementation-roadmap.md) D1·D2)
