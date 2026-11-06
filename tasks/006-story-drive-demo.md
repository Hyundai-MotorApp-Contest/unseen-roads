# 작업: 파스텔 옐로우 홈과 이야기 남기기 데모

- 상태: IN_REVIEW
- 담당자: Codex / 팀원 리뷰 미정
- 시작일 / 갱신일: 2026-11-05 KST

> 이 문서는 이전 디자인 단계의 작업·검증 이력이다. 현재 앱의 진입 화면은 Prism Orb 홈이다.

## 범위와 완료 조건

- Pleos의 차량 앱 화면에 파스텔 옐로우 시작 화면과 홈을 표시한다.
- 주차 상태에서 합성 주행 데모를 열어 예시 발화, 문장·구간 확인, 취소·홈 복귀를 시연한다.
- 예시 경로는 벡터 일러스트다. 마이크·차량 위치·네트워크 권한과 주행 허용 선언을 추가하지 않았다. 예시 초안은 저장·게시하지 않는다.

## 지도·음성 확인

- [NaviHelper 3.0.5](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/intro)는 내장 내비게이션 안내 정보 조회·제어용이다. [공개 SDK 목록](https://document.pleos.ai/)에서 앱 화면에 지도를 렌더링하는 별도 Pleos SDK는 확인되지 않았다.
- [SDK 신청 안내](https://document.pleos.ai/docs/pleos-only/create-my-project)는 NaviHelper를 신청 없이 사용 가능으로, FusedLocation과 STT를 준비 중으로 표시한다. 프로젝트별 Workspace 상태·사용 승인은 별도 확인이 필요하다. [호환성 문서](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability)는 FusedLocation의 기기 지원을 `isSupported`로 확인하도록 안내한다.
- [주행 규제 정책](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driving-restiction-policy)과 [카테고리 가이드](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/app-category)에서 이 앱의 실주행 녹음 허용 근거는 확인되지 않았다.

## 검증

| 항목 | 결과 | 근거 |
| --- | --- | --- |
| 빌드·Lint | PASS | macOS arm64 / JBR 25.0.3, `./gradlew --offline :app:assembleDebug :app:lintDebug`, 오류 0건·경고 7건 |
| SDK 배포 정보 | PASS | 공식 Maven POM을 curl로 조회, NaviHelper 3.0.5와 FusedLocation 1.1.3 모두 HTTP 200 |
| 에뮬레이터 권한 등록 | PASS | Pleos v2.1.0에서 `adb shell pm list permissions`로 FUSED_LOCATION·STT_SERVICE·TTS_SERVICE 확인. 앱 권한 부여·호출 여부와 별개 |
| Pleos 화면·데모 | PASS | 호스트 그래픽 오류 후 SwiftShader로 콜드 부팅. `adb input tap`과 `uiautomator dump`로 홈·데모 시작·취소·예시 구간 확인·완료·홈 복귀 확인 |
| 최종 보조 버튼 스타일 | PASS | 대비 수정 후 APK 재빌드·재설치, `screencap`으로 최종 홈 표시 확인. 최종 빌드의 전체 자동 재검증은 UI 계층 조회 실패로 중단 |
| 문서 하네스 | PASS | macOS에서 `python3 scripts/check_harness.py`, `git diff --check` |
| 실제 SDK 호출·차량 주행 | NOT_RUN | Workspace 가용성, 앱 권한 및 기능별 주행 허용은 [작업 001](001-feasibility.md)에서 확인. U-01·03·05·07·08·16의 실기능 검증 미실행 |
