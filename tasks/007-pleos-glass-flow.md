# 작업: Pleos 글래스 UI와 전체 목업 흐름

- 상태: IN_REVIEW
- 담당자: Codex / 팀원 리뷰 미정
- 시작일 / 갱신일: 2026-11-05 KST

> 이 문서는 이전 디자인 단계의 작업·검증 이력이다. 현재 앱의 진입 화면은 Prism Orb 홈이다.

## 범위와 완료 조건

- 시작 → 예시 AI 라디오 → 공감 또는 홈 → 이야기 남기기 → 예시 문장·구간 확인 → 다음 주행 예시 공감까지 차량 앱 안에서 체험한다.
- 라디오에는 일시정지와 이어 듣기를, 이야기 작성 중에는 취소와 홈 복귀를 제공한다.
- 전달받은 현대 디자인 파일의 네이비·틸·시안과 제목 크기를 참고했다. 글래스 표면과 오브는 Android XML 리소스로 구성했다. [Prism Orb](https://tanmaym.com/prism-orb)의 사용자 제공 팔레트와 상태 예시를 참고했다.
- [Apple Liquid Glass](https://developer.apple.com/videos/play/wwdc2025/219/)의 터치 반응을 참고해 패널 하이라이트를 터치 위치로 이동시키고, 버튼 누름·복귀 모션을 적용했다. 패널은 Android Canvas로 그린다.
- 낭독·듣기·공개·전달은 모두 화면 상태 전환이다. 목업에는 SDK, 마이크·위치 권한, 백엔드, 실제 데이터 저장이 필요하지 않다.
- 앱에서 확인할 정보의 구성과 데이터 연동은 후속 작업에서 정한다.

## 검증

| 항목 | 결과 | 근거 |
| --- | --- | --- |
| 빌드·Lint | PASS | macOS arm64 / JBR 25.0.3, `./gradlew --offline :app:assembleDebug :app:lintDebug`, 오류 0건·경고 7건 |
| Pleos 설치·실행 | PASS | `adb emu avd name`으로 Pleos Connect v2.1.0 확인. 디버그 APK 설치 및 Activity 실행 |
| 터치 하이라이트 | PASS | Pleos 라디오 화면에서 `adb input swipe`를 누르고 있는 동안 `adb exec-out screencap -p`로 패널의 터치 위치에 빛이 나타남을 확인 |
| 전체 화면 흐름 | BLOCKED | 시작·낭독·일시정지·이어 듣기는 확인. 에뮬레이터 ADB 연결 해제 및 UI 계층 조회 실패로 전체 반복 검증 중단. 이후 보이는 창으로 재기동해 최종 APK 실행 |
| 문서 하네스 | PASS | macOS에서 `python3 scripts/check_harness.py`, `git diff --check` |
| 실제 데이터·주행 SDK 시나리오 | NOT_APPLICABLE | 목업 상태 전환 작업 |

## PR 통합 범위

이야기 듣기와 남기기 화면 전환을 현재 오브 홈에 연결한다. U-03·05·07은 화면 목업만 검토하고 실제 낭독·녹음·공개는 검증하지 않는다. PLEOS-UX-007, PLEOS-LIFECYCLE-002·006, PLEOS-SAFETY-004·008, PLEOS-PERF-008을 추적한다.
