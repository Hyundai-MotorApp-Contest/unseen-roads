# 작업: Prism Orb 홈과 개인 기록

- 상태: IN_REVIEW
- 담당자: Codex / 팀원 리뷰 미정
- 시작일 / 갱신일: 2026-11-06 KST

## 목표와 범위

- 지도 홈을 [Prism Orb](https://tanmaym.com/prism-orb)를 참고한 에이전트 홈으로 전환한다.
- 오브는 Kotlin View와 Android RuntimeShader로 렌더링한다. 하위 API와 셰이더를 사용할 수 없는 환경에서는 정적 유리 구슬을 표시한다.
- 개인 기록 화면에서 ‘차가 기억한 순간’과 ‘내가 남긴 이야기’를 구분한다. 실제 데이터 연결 전에는 빈 상태를 보여준다.
- 기존 라디오·기록 데모에도 오브를 사용한다. 실제 내비 오버레이, STT·TTS, 차량 데이터 수집 및 저장은 별도 작업이다. U-03·05·21·22 실제 동작 검증은 NOT_RUN이다.

## 실행·검증 기록

| 확인 항목 | 상태 | 명령·환경·결과 |
| --- | --- | --- |
| APK 빌드·Lint | PASS | macOS arm64, Android Studio JBR. 기본 AAPT2 데몬 시작 실패 후 SDK Build Tools 36.0.0의 AAPT2를 지정해 `./gradlew --offline --no-daemon -Pandroid.aapt2FromMavenOverride=<SDK AAPT2 경로> :app:assembleDebug :app:lintDebug` 성공. Lint 오류 0건·경고 28건 |
| Pleos 화면 | PASS | Pleos Connect v2.1.0(API 34)에 APK 설치·실행. 홈 유리 오브, 기록 데모 오브, 개인 기록 두 영역 및 빈 상태를 화면 캡처로 확인 |
| 개인 기록 화면에서 홈 복귀 자동 확인 | NOT_RUN | 화면 진입과 두 영역은 확인했으나 후속 UI 조회에서 복귀 버튼을 찾지 못해 자동 확인 미완료 |
| 문서 하네스 | PASS | `python3 scripts/check_harness.py` 및 `git diff --check` |

## 버튼·글자 가독성 보완

홈의 ‘이야기 듣기’를 기본 행동으로 강조하고, ‘이야기 남기기’와 같은 크기로 배치했다. 개인 기록은 별도 줄에 둔다. 홈 버튼은 최소 220×72dp에 글자 24sp를 적용한다. 사용 가능한 창 너비가 560dp 이상이면 듣기와 남기기를 가로로, 좁으면 세로로 배치한다. 이 수치는 이 화면의 구현값이다.

[Pleos 타이포그래피](https://document.pleos.ai/en/docs/connect/guide/docs-design/foundations/typography)는 중립적이고 가독성 있는 자체 글꼴을 허용하며 Asta Sans를 시스템 UI 글꼴로 소개한다. [Pleos 디자인 가이드](https://document.pleos.ai/docs/connect/guide/docs-design/Introduction/intro)는 대비 4.5:1 이상과 차량 화면에서 읽을 수 있는 물리적 글자 높이를 안내한다. 현재 화면은 Android 시스템 산세리프를 사용한다.

| 확인 항목 | 상태 | 명령·환경·결과 |
| --- | --- | --- |
| APK 빌드·Lint | PASS | 위와 동일한 JBR 및 SDK AAPT2 지정으로 assembleDebug·lintDebug 성공 |
| 가로 홈 | PASS | Pleos Connect v2.1.0, `adb shell uiautomator dump` 및 화면 캡처. 세 버튼 각각 220×72px, 첫 줄 버튼 사이 24px 간격, 글자 잘림 없음 |
| 좁은 창·실차 가독성 | NOT_RUN | 세로 배치는 리소스에 정의. 좁은 창의 실제 렌더링과 실차 거리·물리 글자 높이는 검증하지 않음 |

## PR 통합 범위

개인 기록 진입·빈 상태·홈 복귀를 연결한다. PLEOS-SAFETY-004·018, PLEOS-UX-007, PLEOS-CCP-003을 추적한다. U-21·22의 실제 데이터·주행 제한 검증은 후속 범위다.
