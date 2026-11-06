# 작업: Kotlin 차량 앱 뼈대

- 상태: IN_REVIEW
- 담당자: Codex / 팀원 리뷰 미정
- 사용 AI 도구: Codex
- 관련 이슈·PR: 미정
- 시작일 / 갱신일: 2026-11-05 KST

## 목표와 완료 조건

- Kotlin 기반 차량용 앱을 Android Studio에서 열 수 있도록 구성한다.
- Gradle Wrapper로 디버그 APK를 빌드하고 Pleos Connect AVD에서 시작 화면을 확인한다.
- 제품 기능 검증은 [작업 001](001-feasibility.md)과 [검증 시나리오](../docs/verification-scenarios.md)에서 이어간다.

## 범위

- 단일 `:app` 모듈, AAOS Manifest, Kotlin Activity, XML 시작 화면, Gradle Wrapper, 실행 문서.
- 이름은 현재 제품 표기인 UNSEEN을 사용한다. `io.github.hyundaimotorappcontest.unseen`은 개발용 applicationId다. XML은 뼈대의 시작 화면에 사용했다.
- Pleos SDK·내비게이션 호출, 실제 차량 신호, 위치 수집·음성·게시, 모바일 앱, 백엔드는 후속 범위다.

## 근거

- [Pleos 시작 가이드](https://document.pleos.ai/docs/pleos-only/getting-started)는 Kotlin Automotive 프로젝트와 API 28 이상을 안내한다.
- 로컬에 설치된 Android SDK 37을 컴파일에 쓰고 Pleos AVD의 API 34를 대상으로 설정했다. 빌드 도구는 [개발 환경](../docs/environment.md)에 기록했다.

## 실행·검증 기록

| 확인 항목 | 상태 | 명령·환경·결과 |
| --- | --- | --- |
| Gradle Wrapper 생성 | PASS | macOS arm64 / Android Studio 내장 JBR 25.0.3 / Gradle 9.6.0의 `wrapper` |
| Android 빌드·Lint | PASS | `./gradlew :app:assembleDebug :app:lintDebug`. 디버그 APK 생성, Lint 오류 0건·경고 4건 |
| Pleos AVD 설치·실행 | PASS | `adb emu avd name`으로 Pleos_Connect_v2.1.0 확인. `adb install -r`, `adb shell am start -W` 성공. `uiautomator dump`로 앱 이름과 시작 안내 문구 확인 |
| 문서 하네스 | PASS | macOS에서 `python3 scripts/check_harness.py`, `git diff --check` 통과 |
| Windows 빌드 | NOT_RUN | PowerShell 실행 명령은 개발 환경 문서에 기재 |
| SDK 호출·주행 시나리오·실차 | NOT_RUN | 시작 화면만 실행 |
| 새 로컬 브랜치 생성 | BLOCKED | `git switch -c codex/pleos-kotlin-skeleton`에서 .git 쓰기 권한 제한. 기존 작업 브랜치의 미커밋 변경과 함께 보존 |

## 마무리

다음 작업은 [Pleos 기술 가능성 확인](001-feasibility.md)이다. 팀 리뷰어는 미정이다.

## PR 통합 범위

Gradle·AAOS Manifest와 Prism Orb 홈을 통합한다. Pleos 요구사항은 PLEOS-SDK-007·008, PLEOS-SAFETY-004·008, PLEOS-UX-001·004, PLEOS-PERF-007을 추적한다.
