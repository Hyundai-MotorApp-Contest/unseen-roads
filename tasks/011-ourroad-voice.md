# 작업: ourroad · 다드리 음성 기획과 차량 1차 목업

- 상태: IN_REVIEW
- 담당자: Codex·Claude Code / 팀원 리뷰 미정
- 사용 AI 도구: Codex, Claude Code
- 관련 이슈·PR: dev 대상 PR (이 브랜치)
- 시작일 / 갱신일: 2026-11-09 / 2026-11-15 KST

## 목표와 완료 조건

- 기준: [README](../README.md)의 주요 기능·음성 사연 흐름과 1차 접수양식의 시나리오 1(혼자 퇴근하는 직장인의 정체 구간).
- 앱 이름 `ourroad`, 에이전트 이름 `다드리`. 차에서는 대화와 듣기만, 보기·관리는 휴대폰 앱(D-016).
- 1차 기획 제출에서는 Pleos SDK 사용이 열리지 않는다. 차량 앱은 예시 대사와 시연 제어로 움직이는 **목업**이며, 실제 SDK 호출·실차 검증은 범위 밖이다.
- 완료 조건:
  - [음성 기획](../docs/ourroad-voice.md)과 기준 문서가 README 방향과 충돌하지 않는다.
  - 차량 목업이 시나리오 1 전체를 시연한다: 혼자·정체·무음이면 주행당 한 번 먼저 묻기 → 사연 낭독 → 회전 안내로 멈춘 뒤 이어 듣기 → "좋아" 공감 → "떠오른 이야기 있으세요?" → 말하기 → 이름 지움 → "응"으로만 게시.
  - 무응답·애매함은 게시하지 않는다. 동승자가 있거나 음악이 나오면 먼저 묻지 않는다. 거절하면 그 주행에서는 다시 묻지 않는다.
  - 빌드·단위 테스트·Lint·`check-compliance`·문서 하네스 PASS.

## 범위와 요구사항

- 코드: `MainActivity`, `CarScreen`, `OurroadTheme`, `DadeuriView`, `DadeuriFlow`, `DadeuriScript`, 단위 테스트, Manifest, Gradle(Compose). 이전 XML 화면과 2026-11-09 Android 음성 프로토타입은 제거.
- 문서: 기준 문서, [음성 기획](../docs/ourroad-voice.md), Pleos 준수 문서.
- 관련 요구사항: PLEOS-SAFETY-001·008·009·013·014·018·019·023, PLEOS-UX-001·007·011, PLEOS-LIFECYCLE-002·003·006, PLEOS-CCP-003, PLEOS-SECURITY-010·017, PLEOS-DESIGN-003·006, PLEOS-PERF-008, PLEOS-AUDIO(소리 출력 없음).

## 근거와 결정

- [결정 D-011~D-018](../docs/decisions.md#2026-11-15--ourroad와-음성-기획-기준): README 기준, 주행 중 음성은 계획(허용 범위 미확정), 1차 제출은 기획·목업, 역할 분리, 시나리오 1, Compose·Apple 스타일.
- 주행 중 음성 허용 근거는 [주행 안전](../docs/pleos/driving-safety.md)의 "미확인" 항목과 [로드맵](../docs/pleos/implementation-roadmap.md) D6에서 추적한다. 운영사무국 회신 대기.
- Pleos SpeechToText·TextToSpeech 3.3.1은 문서에 정의되어 있으나 공개 사용 신청 안내는 준비 중이었다(2026-11-09 확인).

## 1차 목업 검증 (2026-11-15, macOS arm64, Android Studio JBR)

| 확인 항목 | 상태 | 명령·결과 |
| --- | --- | --- |
| 빌드 | PASS | `./gradlew :app:assembleDebug` |
| 단위 테스트 | PASS | `./gradlew :app:testDebugUnitTest`: `DadeuriFlowTest` 11개 실패 0(시나리오 1 전체, 자동 제안 조건·주행당 1회, 무응답·애매함 비게시, 회전 안내 후 이어 듣기, 되묻기) |
| Lint | PASS | `./gradlew :app:lintDebug`: 오류 0, 경고 6(아이콘·targetSdk·의존성 버전 등) |
| Pleos 준수 검사 | PASS | `scripts/pleos/check-compliance.sh`: PASS 25, FAIL 1(M13 아이콘 MINOR), NOT_RUN 3, BLOCKED 1(M14 카테고리), NOT_APPLICABLE 14 |
| 문서 하네스 | PASS | `python3 scripts/check_harness.py` |
| Emulator | NOT_RUN | Pleos 2.1.0 AVD에 설치해 첫 화면(Pretendard·다드리·시연 제어) 표시만 확인. 시나리오 1 조작은 에뮬레이터 연결이 끊겨 미수행 |
| Pleos SDK·실차 | NOT_APPLICABLE | 1차 기획 제출에서 SDK 미개방 |
| 다른 팀원 리뷰 | NOT_RUN | AI 검토는 팀원 승인으로 취급하지 않음 |

### 미해결·알려진 한계

- CarUxRestrictions 미구독, DO 미선언: 실제 주행 규제가 켜지면 플랫폼이 앱을 막는다. 목업의 주행은 시연 토글이다(SAFETY-001 NOT_IMPLEMENTED).
- 목업 대사·문구가 Kotlin에 직접 들어 있다(UX-011 NON_COMPLIANT, MINOR). 예시 사연과 시연 제어는 App Market 제출 빌드 전에 제거해야 한다(PERF-008).
- 접수양식에 있으나 목업에 없는 것: AI DJ의 곧 지날 길 우선·인기 사연, 욕설·비방 거르기 시연, 주행 길이·통화 조건, 차가 기억한 순간, 휴대폰 앱, 신고 시 숨김.
- 양식과 다르게 동작하는 것: 무응답·애매함은 README 기준으로 휴대폰 보관함에 비공개 저장까지 한다. 되묻기 단계는 README 기준이다. "좋아"는 사연 직후 6초 동안 받는다.

## 향후 작업 (SDK 사용이 열린 뒤)

1. STT·TTS·Vehicle·NaviHelper·FusedLocation 어댑터가 `DadeuriFlow`에 이벤트를 보내도록 연결하고 승인된 권한만 선언한다.
2. CarUxRestrictions 게이트와 DO 선언 범위를 정하고, 주행 중 `DadeuriView` 정지를 실제 규제 신호에 연결한다.
3. 자체 서버의 정리 AI·판단 모델 API 계약(텍스트만 전송, JSON 응답)을 정한다.
4. 운영사무국 회신에 따라 주행 중 음성 허용 범위를 확정한다.
5. 관련 시나리오와 `check-compliance`를 수행하고 compliance-matrix를 갱신한다.

## 이전 프로토타입 기록 (2026-11-09, 2026-11-15 제거)

아래는 README 기준 전환 이전에 Android 표준 엔진으로 만든 프로토타입(AndroidVoice·ParkedAccess·VoiceSession, 계측 probe)의 실행 기록이다. 코드는 1차 목업으로 대체하며 제거했고, 기록은 조사 사실로만 남긴다.

| 확인 항목 | 상태 | 명령/절차·환경·실제 결과·증거 |
| --- | --- | --- |
| 공식 문서 | PASS | 2026-11-09 Pleos Gleo·Speech 소개/신청 안내, Android Car·Speech API 원문 재열람. 상세 링크는 설계 문서 |
| 빌드·Lint·단위 정책 | PASS | macOS arm64·Android Studio JBR 25.0.3·API 37 SDK, `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`. 테스트 8개 실패 0, Lint 오류 0·경고 47. 기존 unused/demo 리소스·아이콘 경고 포함 |
| 문서 하네스 | PASS | `python3 scripts/check_harness.py` |
| 준수 검사 | PASS | `scripts/pleos/check-compliance.sh`(동일 JDK/SDK env). 최종 요약 PASS 25·FAIL 1(MINOR 아이콘)·NOT_RUN 3·BLOCKED 1(카테고리)·NOT_APPLICABLE 14. 종료 코드 0, BLOCKER/MAJOR 없음. EMU·실차·외부 승인 전체 통과라는 뜻 아님 |
| 미지원 STT 화면 | PASS | 최종 계측 `unsupported_stt_no_permission_request=PASS`: 동의 버튼에서도 미지원 안내 후 마이크 권한 요청 없이 종료 |
| 실제 Car API / U-04 | PASS | Pleos Connect 2.1.0/API 34 arm64, emulator-5554/user 10. Car 서비스 연결·GEAR_SELECTION 초기 콜백 P=true·UX unrestricted=true, 오류 없음. 실제 API 수신이며 Mock 아님 |
| 이름·빈 상태·안내·취소 UI / U-10 일부 | PASS | 테스트 APK `VoiceEnvironmentProbe`. Emulator를 깨운 뒤 window_focus=true, home_visible=true. ourroad·다드리 리소스, 빈 초안 → 동의 안내 → 삭제/취소 → 홈 확인. 공개 경로 발견 검증은 아님 |
| Android 기기 내 STT | BLOCKED | `SpeechRecognizer.isOnDeviceRecognitionAvailable=false`. 실제 한국어 인식 NOT_RUN. 서버 인식기로 우회하지 않음 |
| Android TTS | BLOCKED | 초기화 콜백 수신, status=-1(ERROR), local_korean_tts_voices=0. 실제 합성·가청 출력 NOT_RUN. Pleos Speech SDK 실패로 일반화하지 않음 |
| U-03·05·06·14 정책 | PASS | 순수 VoiceSession 테스트: foreground·P·UX 18조합, 한쪽 신호 상실, 제한/백그라운드 초안 폐기, 지연·중복 인식 결과 무효화, 중단·삭제 후 자동 재개 없음. 실제 차량 신호 전환은 아래와 별개 |
| 실제 주행 규제·기어 전환 / U-03·05~07·14 | BLOCKED | 문서의 `adb shell cmd hmg_car_service --service drivingstate ...` 진입점 조회에서 Can't find service. 대체 `cmd car_service help`도 non-user build 필요 SecurityException. 차량 기어/규제는 이번 세션에서 변경하지 않음. Virtual Console·별도 테스트 이미지 또는 실차 필요 |
| 포커스·Suspend·권한 거부·마이크 보호·재생성 | NOT_RUN | 음성 엔진 지원 환경 및 OEM Suspend 이벤트 필요. 가청 출력·duck·통화/내비/Gleo/비상 중재를 PASS로 기록하지 않음 |
| 실차·Gleo 승인 | NOT_RUN | 개발자가 대신 승인·실차 검증했다고 기록하지 않음 |

### 이전 프로토타입 마무리

- 다른 팀원 리뷰: 미정. AI 검토는 팀원 승인으로 취급하지 않는다.

#### 실행 중 발견한 환경 차이와 수정

- 최초 필수 android.car 공유 라이브러리 선언은 `INSTALL_FAILED_MISSING_SHARED_LIBRARY`로 설치 실패했다. optional 선언으로 수정한 뒤 앱·계측 APK 설치 성공, 공개 Car API 호출도 성공했다. 내부 vendor API/라이브러리로 대체하지 않았다.
- 최초 UI probe는 Emulator가 Asleep 상태여서 window_focus=false·제한 UI였다. Android Instrumentation 메인 스레드 assertion 예외가 프로세스를 종료해 결과 수집도 못한 부분을 테스트 전용 예외 전달 방식으로 수정했다. 깨운 후 foreground UI는 PASS였다. 실제 플랫폼 실패를 숨기거나 차량 게이트를 Fake로 바꾸지 않았다.
- 중간 전체 준수 검사에서 H03 요약 개수 불일치가 FAIL이었다. 매트릭스 본문과 요약을 맞춘 뒤 재실행했다.
- 계측 APK는 개발 검증 전용이며 제출 APK에 들어가지 않는다. 테스트에서 마이크 권한 승인·호스트 마이크 녹음·실사용자 초안 생성은 하지 않았다.

#### 이전 기준의 남은 범위

- Gleo: 공식 스키마·Intent·Callback과 승인 조건을 설계했으며, APK에 스키마·외부 호출 수신기를 넣지 않았다. 송신자 인증·silent 제한 보장·승인 확인 후 별도 연동한다.
- 실제 음성 사용: 현재 이미지의 Android 기기 내 엔진 조건이 충족되지 않아 BLOCKED다. 프로젝트에서 승인·제공된 Pleos Speech SDK 또는 지원이 확인된 표준 엔진 환경에서 인식·출력을 검증해야 한다. 임의 서버 음성 전송·엔진 설치·권한 신청은 하지 않았다.
- 준수 검사의 기존 M13 아이콘 MINOR와 M14 카테고리 BLOCKED, CCP 선언·제출용 예시 리소스 제거는 남아 있다. 이번 변경을 App Market 제출 완료로 보지 않는다.
- 새 커밋·push·PR 생성은 하지 않았다. 작업 브랜치 `feat/ourroad-voice`에서 변경 검토 가능하다.
