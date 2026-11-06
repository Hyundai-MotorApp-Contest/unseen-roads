# UNSEEN 프로젝트 맥락

기준일: 2026-10-30 KST. 제품 요구의 원문은 [intent.md](../intent.md)다.

| 항목 | 현재 기준 |
| --- | --- |
| 앱 이름 / 저장소 | UNSEEN / Hyundai-MotorApp-Contest/unseen-roads |
| 저장소 역할 | 실제 앱 코드와 프로젝트 전용 하네스를 함께 관리하는 Private 저장소 |
| 목표 | 현대차 차량용 앱 공모전 / Pleos Connect·Android Automotive OS 기반 MVP |
| 제품 핵심 | 동의한 주행 세션 → 공통 도로 구간 → 주차 후 발견 → Road Threads 읽기·이어쓰기 |
| P0 | 옵트인·철회·삭제, 세션 기록, 도로 구간 매핑, Route Resonance, Road Threads, 주차 후 Discovery, 신고·숨김·삭제 |
| P1 | Time Layers, 관련도 정렬, 주차 중 음성 입력, 개인 주행 요약 |
| 제외 | 실시간 추적·채팅, 주행 중 소셜 콘텐츠, 우회 유도, 감정 추론, AI 자동 게시 |
| 현재 구현 상태 | Kotlin AAOS 앱과 Gradle Wrapper, Prism Orb 홈 목업. 백엔드·DB·자동 빌드 파이프라인 미구성 |
| 제안된 기술 | Kotlin / FastAPI / PostgreSQL + PostGIS. 의도 수준의 제안이며 고정 버전·구조 미정 |
| 팀 환경 | 이전 확인: macOS 2명·Windows 1명, Codex·Claude Code, Android Studio·시뮬레이터 설치 완료 전달 |
| GitHub 접근 | 2026-10-30 확인: 2019100743·seoddhh·dionypark 모두 Admin. OS와 계정의 대응·개발 역할은 미정 |

## 제품 기준과 의사결정

- 제품 의도와 P0 범위는 `intent.md`를 기준으로 구현하되 시장 수요·KPI·SDK 가용성까지 입증된 것으로 표현하지 않는다.
- `0~3개` 발견 카드, 파일럿 수치, 기술 구성은 가정/제안이다. 팀이 바꾸면 [결정 기록](decisions.md)과 관련 기획을 함께 갱신한다.
- 주차 여부와 UX 제한을 함께 확인한다. 상태를 모르면 제한한다. 수집·공유·삭제·재식별 방지는 UI와 데이터 경계를 모두 검증한다.
- 다음 순서는 [기술 가능성 확인](../tasks/001-feasibility.md) → 핵심 경험 → 신뢰·완성도다. 이 순서는 기획서의 단계이며 아직 실행하지 않았다.

## 문서 선택

- 환경·시작: [온보딩](onboarding.md), [환경](environment.md)
- 구현·리뷰: [결정 기록](decisions.md), [협업](workflow.md), [검증 시나리오](verification-scenarios.md)
- SDK·제출: [Pleos 조사](pleos.md), [공모전 조사](competition.md). 두 문서는 과거 조사이며 사용 직전 재확인한다.
- Pleos 공식 가이드 준수: [Pleos 하네스](pleos/README.md). 요구사항 ID·준수 매트릭스·자동 검사.
- 이관·폐기: [원본 이관 기록](harness-migration.md). 기존 하네스 저장소를 실행 중에 참조하지 않는다.
