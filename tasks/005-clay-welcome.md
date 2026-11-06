# 작업: 클레이모피즘 시작 화면

- 상태: IN_REVIEW
- 담당자: Codex / 팀원 리뷰 미정
- 시작일 / 갱신일: 2026-11-05 KST

> 이 문서는 이전 디자인 단계의 작업·검증 이력이다. 현재 앱의 진입 화면은 Prism Orb 홈이다.

## 목표와 범위

- 사용자가 선택한 서비스 소개와 시작 버튼을 Pleos 차량 앱에 표시한다.
- Kotlin Activity와 XML 화면에 라벤더 팔레트, 레코드 벡터, 입체 버튼을 적용한다.
- 시작 버튼은 듣기와 말하기 준비 안내로 전환하고 돌아가기를 제공한다. 음성·공개·차량 신호 연동은 별도 작업이다.
- 앱 뼈대와 실행 환경은 [작업 004](004-pleos-kotlin-skeleton.md)를 따른다. 작업은 현재 로컬 브랜치에서 진행했다.

## 실행·검증 기록

| 확인 항목 | 상태 | 명령·환경·결과 |
| --- | --- | --- |
| APK 빌드·Lint | PASS | macOS arm64 / JBR 25.0.3, `./gradlew --offline :app:assembleDebug :app:lintDebug`, 오류 0건·경고 6건 |
| 화면 표시·전환 | PASS | Pleos Connect v2.1.0 주차 상태, `adb install -r`, `adb shell am start -W`, `screencap`으로 화면 확인. `input tap`과 `uiautomator dump`로 시작·복귀 확인 |
| 차량 신호·음성·공개 시나리오 | NOT_APPLICABLE | 화면과 화면 전환 작업 |
| 문서 하네스 | PASS | macOS에서 `python3 scripts/check_harness.py`, `git diff --check` |
