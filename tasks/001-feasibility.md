# 001 · Pleos에서 ourroad가 가능한지 확인

- 상태: TODO
- 담당자: 미정
- 관련 기획: [intent.md](../intent.md) §12 Phase 0
- 의존: 프로젝트 생성 전 [열린 결정](../docs/decisions.md) 확인
- 수정 후보: `docs/environment.md`, API 계약 기록, 이후 확정될 Android 프로젝트

## 목표

실제 Pleos 환경에서 동의 기반 경로 수집과 주차 후 콘텐츠 게이팅에 필요한 신호를 얻을 수 있는지 확인한다.
가용성 확인 없이 핵심 경험·백엔드·소셜 UI부터 완성하지 않는다.

## 완료 조건

1. 프로젝트 생성에 필요한 applicationId·기술/버전 결정과 각 실제 환경을 기록한다.
2. 주차 상태·UX restrictions·위치 데이터의 정확한 API·권한·capability·null/오류·구독 해제를 [API 계약 양식](../templates/api-contract.md)에 기록한다.
3. 문서상 존재, Workspace 사용 가능, 환경 지원, 실제 호출을 각각 확인한다. 접근 불가는 BLOCKED로 기록한다.
4. Emulator·Virtual Console에서 주행/일시 정지/주차/상태 미상·신호 유실을 가능한 범위에서 재현하고 callback·앱의 제한 상태를 확인한다.
5. 동의 전 수집 없음, 철회 후 중단, 주차·UX 제한이 모두 확인되기 전 콘텐츠 차단을 검증한다. [U-01·03~07](../docs/verification-scenarios.md)과 연결한다.
6. 실제 위치 접근 불가 등 핵심 제약이 있으면 대안·제품 영향·다음 결정을 기록한다. Mock 성공으로 대체하지 않는다.

## 검증 기록

| 수준 | 상태 | 이유 |
| --- | --- | --- |
| 공식 문서·실제 버전 대조 | NOT_RUN | 담당 작업 시작 전 |
| 프로젝트·Workspace·SDK 지원 | NOT_RUN | 담당자와 설치 환경 미확인 |
| Android build / SDK 호출 / 상태 시나리오 | NOT_RUN | 앱 프로젝트 미생성 |

공용 기록에는 합성 경로와 비식별 로그만 남긴다. 결과가 나오면 환경·결정·검증 보고서를 함께 갱신한다.
