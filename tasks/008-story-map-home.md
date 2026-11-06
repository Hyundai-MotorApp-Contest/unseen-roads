# 작업: Pleos 내비와 이야기 지도

- 상태: IN_REVIEW
- 담당자: Codex / 팀원 리뷰 미정
- 시작일 / 갱신일: 2026-11-05 KST

> 이 문서는 이전 디자인 단계의 작업·검증 이력이다. 현재 앱의 진입 화면은 Prism Orb 홈이다.

## 합의한 방향과 목업 범위

- Pleos 내비가 주행 안내를 맡는다. UNSEEN은 내비 옆의 별도 앱 창을 목표로 하고, 음성의 백그라운드 동작은 후속 연동에서 검증한다.
- UNSEEN 홈은 내 기록 경로와 공개 이야기가 연결된 구간을 표시한다. 다른 사람의 원시 경로·정확한 통과 시각·출발지·목적지는 공개 지도 모델에 포함하지 않는다.
- 밝은 유리와 현대 네이비·시안을 사용한다. 홈에서 사용자가 작은 오브를 눌러 예시 AI DJ를 시작한다. 이야기 남기기는 예시 문장·구간 확인으로 끝난다.
- 지도는 고정된 합성 그래픽이다. 실제 내비 화면 합성, 위치 수집, 음성 재생·녹음, 게시, 백그라운드 서비스는 별도 작업이다.
- [Pleos 앱 구조](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/app-structure-screen-model)는 앱 두 개를 표시할 수 있는 환경과 백그라운드 전환을 설명한다. 실제 창 배치는 차량·디스플레이 구성에 의존한다. 음성 기능별 허용과 포커스는 [오디오 포커스 정책](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/audio-focus-development-guide/audio-focus-development)에 따라 검증한다.

## 검증

| 항목 | 결과 | 근거 |
| --- | --- | --- |
| 빌드·Lint | PASS | macOS arm64 / JBR 25.0.3, `./gradlew --offline :app:assembleDebug :app:lintDebug`, 오류 0건·경고 10건 |
| Pleos 지도 홈 | PASS | Pleos v2.1.0에 디버그 APK 설치·실행. `adb exec-out screencap -p`로 지도·경로·핀·오브 표시 확인 |
| 버튼 흐름 | BLOCKED | 지도 홈 확인 후 ADB가 offline으로 전환되어 오브·이야기 확인 흐름의 전체 검증 중단 |
| 문서 하네스 | PASS | `python3 scripts/check_harness.py`, `git diff --check` |
| 내비 동시 표시·실제 백그라운드 동작 | NOT_RUN | UNSEEN의 멀티윈도우 태스크 표시만 확인. 내비와의 동시 표시 및 실제 음성·위치 연동은 미검증 |
| 제품 시나리오 U-01·03·05·07·08·16 | NOT_APPLICABLE | 합성 데이터 화면 목업. SDK와 수집·게시 동작은 별도 검증 |
