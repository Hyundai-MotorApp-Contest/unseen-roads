# Pleos 요구사항 구현 로드맵

기준: 2026-11-06 KST. 아직 아무 단계도 시작하지 않았다. 순서는 ① 운전자 안전·주행 규제 → ② 플랫폼 필수 → ③ SDK 연동·호환성 → ④ 보안·개인정보 → ⑤ UI/UX → ⑥ 성능·안정성이다. 각 단계는 [tasks/](../../tasks/README.md)의 작업으로 쪼개고 담당자를 정한 뒤 시작한다. 제품 범위 결정은 [intent.md](../../intent.md)와 [docs/decisions.md](../decisions.md)를 따른다.

## 먼저 필요한 결정

아래 결정이 없으면 해당 단계의 요구사항을 판정할 수 없다. 결정자와 근거를 docs/decisions.md에 남긴다.

| 결정 | 영향 요구사항 | 필요한 확인 |
| --- | --- | --- |
| D1 앱 카테고리 | CATEGORY-001~004, MEDIA 전체, 주행 중 허용 범위 | Music/Audio·Lifestyle·Social 등 후보와 심사 위험. 카테고리 선언 방법(X-13) |
| D2 DO 선언 범위 | SAFETY-003·004·008·009 | 어떤 Activity가 주행 중 실행되는지. 오브 움직임의 주행 중 정책 |
| D3 CCP 지원 여부 | CCP-001·002·005 | 지원하면 포커스 이동 설계, 미지원이면 `ROTARY_SUPPORTED=false` |
| D4 SDK 버전 고정 | SDK-006 | 문서 버전과 설치 환경 대조(C-02) |
| D5 지역 정책 처리 | SAFETY-005·006 | C-01 해소. 필요하면 파트너 문의 |
| D6 음성 기능의 주행 중 허용 근거 | SAFETY-023, AUDIO 전체 | TTS·STT·음성 공개 확인이 주행 중 허용되는지(X-08, X-09). 운영사무국·파트너 확인 |

## 1단계 — 운전자 안전·주행 규제 (BLOCKER)

| 작업 | 요구사항 | 현재 | 완료 기준 |
| --- | --- | --- | --- |
| 1-1 주행 규제 상태 관리와 게이트 계산 | SAFETY-001·002·005·018 | NOT_IMPLEMENTED | CarUxRestrictions 리스너, 알 수 없음 → 제한, 게이트 단위 테스트 PASS, EMU 규제 ON/OFF 시나리오 PASS |
| 1-2 DO 선언 범위 결정 후 선언 | SAFETY-003·004 | NOT_IMPLEMENTED | D2 기록, M06·C01 PASS, 선언 Activity마다 플래그 처리 |
| 1-3 주행 중 움직임 정책 | SAFETY-008·009 | DO 선언 전 NOT_APPLICABLE | 오브·패널의 제한 상태 동작 정의, C16 PASS, EMU 확인 |
| 1-4 음성 대화 수동 시작·종료·중단 | SAFETY-023 | NOT_IMPLEMENTED | 모든 음성 흐름에 수동 중단 경로 |
| 1-5 오디오 포커스 기본 처리 | AUDIO-001~006·009 | NOT_APPLICABLE(소리 없음) | 소리를 내기 전에 구현. C07 PASS, 체크리스트 5개 EMU 결과 기록 |
| 1-6 Suspend·Resume 처리 | AUDIO-010~012, LIFECYCLE-007 | NOT_IMPLEMENTED | 감지 수단 확인(X-07) 후 구현, 미확인이면 BLOCKED로 기록 |

## 2단계 — 플랫폼 필수

| 작업 | 요구사항 | 현재 | 완료 기준 |
| --- | --- | --- | --- |
| 2-1 화면 상태 저장·복원 | UX-007, LIFECYCLE-002·006 | NON_COMPLIANT | window 크기 변경·회전·프로세스 재생성 후 같은 화면, C15 PASS, EMU 확인 |
| 2-2 Back 동작 | CCP-003 | PARTIAL | 하위 화면은 이전 화면, 최상단은 `moveTaskToBack(true)`, EMU에서 CCP Back 확인 |
| 2-3 카테고리 선언 | CATEGORY-002 | NOT_IMPLEMENTED | D1 후 M14 PASS, Music/Audio면 M15 PASS |
| 2-4 CCP 선언 | CCP-002 | NOT_IMPLEMENTED | D3 후 M08 PASS |
| 2-5 Portrait·Landscape·폼 팩터 | UX-001~006·008·009·012 | PARTIAL·UNVERIFIED | 폼 팩터 테스트 결과 기록 |

## 3단계 — SDK 연동·호환성

ourroad P0에 필요한 순서를 제안한다. 실제 순서는 작업 001(기술 가능성 확인)에서 정한다.

| 작업 | 요구사항 | 완료 기준 |
| --- | --- | --- |
| 3-1 ON_DEVICE TTS 최소 호출 | SDK-006·014·017·018·019, AUDIO-013·014 | 설치 환경 지원·실제 호출 결과 기록, Speech Service 포커스(X-08) 측정 |
| 3-2 ON_DEVICE STT 최소 호출 | SDK-015~017, SAFETY-023 | 마이크 권한 필요 여부(X-09) 확인, 실패 경로 기록 |
| 3-3 FusedLocation(동의 후) | SDK-003·020~023 | isSupported·권한·null 처리, 동의 전 미수집 검증(U-01) |
| 3-4 Vehicle 보조 신호 | SDK-002·009~013 | 쓰는 도메인만 퍼미션 선언, capability 결과 기록, 주차 브레이크 신호(C-05) 확인 |
| 3-5 NaviHelper(필요 시) | SDK-004·024 | 내비 안내 중 판정에 쓸 이벤트의 실제 전달 확인 |
| 3-6 퍼미션 승인 근거 | SDK-005 | 선언 퍼미션별 App Market 협의 기록 |

각 SDK는 "문서·Workspace·설치 환경·실제 호출·사용 허용" 다섯 수준을 [sdk-api-reference.md](sdk-api-reference.md) 표에 갱신한다.

## 4단계 — 보안·개인정보

| 작업 | 요구사항 | 현재 | 완료 기준 |
| --- | --- | --- | --- |
| 4-1 동의 흐름(위치·음성) | SECURITY-003 | 수집 기능 없음 | 수집 기능과 같은 작업에서 구현. 이유 설명·간단한 흐름 |
| 4-2 백업 정책 | SECURITY-010 | NON_COMPLIANT(MINOR) | `allowBackup=false` 또는 제외 규칙, M12 PASS |
| 4-3 저장 암호화·Keystore | SECURITY-002·007·011·014 | 저장 없음 | 저장소 도입 시 함께 |
| 4-4 통신 보안 | SECURITY-012·015·016 | 네트워크 없음 | 서버 도입 시 HTTPS·TLS 1.2+, C13·C14 PASS |
| 4-5 로그·Telemetry | SECURITY-004·005·008 | 해당 없음 | 로그 마스킹, 가명화 정책 |
| 4-6 배포 서명·디버그 비활성 | SECURITY-027·028 | 배포 계획 없음 | 제출 전 release 서명, G05·M10 PASS |

## 5단계 — UI/UX

| 작업 | 요구사항 | 현재 | 완료 기준 |
| --- | --- | --- | --- |
| 5-1 앱 아이콘 | DESIGN-005 | NOT_IMPLEMENTED | Adaptive Icon, M13 PASS |
| 5-2 야간 테마 | UX-010 | NOT_IMPLEMENTED | `values-night` 또는 테마 전환, 대비율 확인 |
| 5-3 디자인 리뷰 | SAFETY-010~013·017·019·021, DESIGN-001·002·006 | UNVERIFIED | [design-guidelines.md](design-guidelines.md) 체크리스트 기록 |
| 5-4 수치 기준 확보 | SAFETY-013, X-03·X-04 | UNVERIFIED | 디자인 자산 확인 후 sources.md 갱신 |

## 6단계 — 성능·안정성

| 작업 | 요구사항 | 현재 | 완료 기준 |
| --- | --- | --- | --- |
| 6-1 시작·응답 시간 | PERF-001·005·006 | UNVERIFIED | `am start -W`와 입력 응답 측정 기록 |
| 6-2 CPU·RAM | PERF-002·003 | UNVERIFIED | 오브 애니메이션 포함 정차·주행(규제 ON) 측정 |
| 6-3 장시간 안정성 | PERF-007 | UNVERIFIED | 장시간 실행·전환 반복에서 crash·freeze 없음 |
| 6-4 제출 빌드 정리 | PERF-008 | 데모 문자열 21개 | App Market 제출 시 데모·예시 콘텐츠 분리 또는 제거, C17 PASS |

## 현재 자동 검사 위반 목록

`scripts/pleos/check-compliance.sh --static-only`(2026-11-06) 기준:

| 검사 | 결과 | 연결 작업 |
| --- | --- | --- |
| C15 Activity 상태 저장 | FAIL(MAJOR) | 2-1 |
| M12 자동 백업 | FAIL(MINOR) | 4-2 |
| M13 앱 아이콘 | FAIL(MINOR) | 5-1 |
| M14 카테고리 | BLOCKED | D1, 2-3 |
| M08 CCP 선언 | NOT_RUN | D3, 2-4 |
| C11 Back 처리 | NOT_RUN | 2-2 |
| C17 데모 문자열 | NOT_RUN | 6-4 |

검사 실행 기록은 [testing-strategy.md](testing-strategy.md)와 [작업 010](../../tasks/010-pleos-guide-harness.md)에 있다.
