# Pleos 공식 가이드 하네스

ourroad를 Pleos Connect 공식 가이드에 맞춰 개발하기 위한 요구사항·검증 체계다. 기준일 2026-11-06 KST에 [앱 기획·설계 가이드](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/intro)와 [앱 개발·연동 가이드](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/intro)의 하위 문서를 직접 열람해 만들었다.

공식 문서를 요약한 자료가 아니라, 작업마다 **어떤 요구사항이 걸리는지 찾고 → 지키며 구현하고 → 검증해 기록하는** 데 쓴다.

## 파일

| 파일 | 쓰는 때 |
| --- | --- |
| [sources.md](sources.md) | 출처·확인일·열람 상태, 접근하지 못한 문서(X), 문서 간 불일치(C) |
| [requirements.md](requirements.md) | 170개 요구사항(ID·수준·조건·검증 기준·방식·출처) |
| [compliance-matrix.md](compliance-matrix.md) | 요구사항별 현재 구현 상태와 검증 근거 |
| [driving-safety.md](driving-safety.md) | 주행 규제·DO·UX 제한 플래그·지역 표, ourroad 기능별 판단 |
| [design-guidelines.md](design-guidelines.md) | 화면·window·텍스트·색·아이콘·디자인 리뷰 체크리스트 |
| [development-guidelines.md](development-guidelines.md) | Manifest·오디오 포커스(Interaction Matrix)·Suspend·Back·IME·Gleo·ADB·금지 패턴 |
| [sdk-api-reference.md](sdk-api-reference.md) | Pleos SDK·AAOS API의 역할·퍼미션·수명주기·오류, 검증 수준 표 |
| [architecture.md](architecture.md) | 계층 책임, 주행 규제 상태 관리, SDK 어댑터 계약 |
| [testing-strategy.md](testing-strategy.md) | 정적·빌드·단위·Emulator·실차 검증 절차와 실행 기록 |
| [implementation-roadmap.md](implementation-roadmap.md) | 필요한 결정과 우선순위별 작업 |
| `scripts/pleos/check-compliance.sh` | 자동 검사(정적 + Gradle). Windows는 `py -3 scripts\pleos\check_compliance.py` |

## 작업 절차

모든 파일을 매번 읽지 않는다. 작업과 관련된 요구사항만 찾는다.

### 구현 전

1. 작업이 건드리는 영역(화면, 주행 중 기능, 오디오, SDK, 저장·통신, Manifest)을 정한다.
2. [requirements.md](requirements.md)에서 해당 분류의 요구사항 ID를 고르고 작업 문서에 적는다. 주행 중 동작이 있으면 SAFETY·AUDIO를 반드시 포함한다.
3. 쓸 API를 [sdk-api-reference.md](sdk-api-reference.md)에서 확인하고, 문서 확인일이 오래됐거나 시그니처가 필요하면 공식 원문과 API Reference를 다시 연다. 문서에 없는 API·퍼미션은 쓰지 않는다.
4. [driving-safety.md](driving-safety.md)로 주행 규제 영향을 판단한다. 근거가 없으면 제한이 기본이다.
5. 변경할 파일과 [compliance-matrix.md](compliance-matrix.md)에서 바뀔 행을 적는다.

### 구현 중

- 확인된 공식 API만 쓴다. 차량·SDK 호출은 [architecture.md](architecture.md)의 어댑터 경계 뒤에 둔다.
- 미지원·권한 거부·연결 실패·null·주행 제한을 각각 처리한다.
- 주행 규제 상태 변경을 진행 중 재생·녹음·공개 확인·화면에 반영한다.
- 리스너·포커스·SDK 인스턴스는 등록한 곳에서 해제한다.

### 구현 후

1. `scripts/pleos/check-compliance.sh`를 실행한다(Gradle을 건너뛰려면 `--static-only`).
2. 관련 EMU·REVIEW 검증을 [testing-strategy.md](testing-strategy.md)에 따라 수행하거나 NOT_RUN·BLOCKED로 남긴다.
3. [compliance-matrix.md](compliance-matrix.md)의 해당 행을 갱신한다. 정적 근거만으로 EMU·CAR가 필요한 항목을 COMPLIANT로 바꾸지 않는다.
4. 위반·미검증 항목을 작업 문서·PR에 적는다. SAFETY·AUDIO의 MUST·MUST_NOT 위반은 BLOCKER이며 해결 전 머지·데모하지 않는다.

## 상태 값 두 가지

| 용도 | 값 | 위치 |
| --- | --- | --- |
| 요구사항 준수 상태 | COMPLIANT, NON_COMPLIANT, PARTIAL, NOT_IMPLEMENTED, NOT_APPLICABLE, UNVERIFIED | compliance-matrix.md |
| 검사·검증 실행 결과 | PASS, FAIL, NOT_RUN, BLOCKED, NOT_APPLICABLE(기존 [workflow.md](../workflow.md)) | 검사 출력, 작업 문서, PR |

검사 FAIL에는 심각도 BLOCKER·MAJOR·MINOR가 붙는다. 미구현 항목은 위반이 아니라 NOT_IMPLEMENTED다.

## 기존 하네스와의 관계

| 항목 | 처리 |
| --- | --- |
| [docs/pleos.md](../pleos.md) | 2026-10-29·11-01 조사와 콘솔 SDK 표시 상태를 보존한다. 공식 가이드 요구사항의 기준은 이 디렉터리다 |
| [docs/verification-scenarios.md](../verification-scenarios.md) | ourroad 제품 시나리오(U-xx)의 원본. testing-strategy.md가 요구사항과 연결만 한다 |
| [templates/api-contract.md](../../templates/api-contract.md) | SDK 작업의 API 확인 기록. sdk-api-reference.md의 검증 수준 표와 함께 쓴다 |
| `scripts/check_harness.py` | 문서 형식·링크 검사. 이 디렉터리의 파일도 필수 파일로 확인한다 |

요청 형식과 기존 규칙이 충돌한 지점과 처리:

| 충돌 | 처리 |
| --- | --- |
| 검사 스크립트를 `.sh`로 요청받았으나 팀에 Windows 사용자가 있고 기존 검사기는 Python 표준 라이브러리만 사용 | `check-compliance.sh`는 얇은 래퍼, 검사 로직은 `check_compliance.py`(Python 3.9+) |
| 준수 상태 값(COMPLIANT 등)과 기존 결과 값(PASS 등)이 다름 | 위 표처럼 용도를 나눠 둘 다 쓴다 |
| 아키텍처 문서를 요청받았으나 AGENTS.md는 미정 아키텍처·의존성의 선제 도입을 금지 | architecture.md는 책임 경계만 정하고 모듈·DI·의존성은 결정 기록으로 넘긴다 |
| App Review는 더미 데이터 포함을 금지하나 현재 앱은 명시된 데모 데이터로 동작 | 데모 표기는 유지하고(AGENTS.md), App Market 제출 빌드에서 제거하는 것을 PERF-008로 관리 |
| "개발 전 참조 문서"를 요청받았으나 AGENTS.md는 매번 전체 문서를 읽지 않도록 함 | 이 README와 관련 요구사항 ID만 읽는 절차로 정했다 |

## 갱신

- 공식 문서를 다시 열람하면 [sources.md](sources.md)의 확인일과 상태를 바꾸고, 달라진 요구사항은 행을 고치거나 `RETIRED`로 표시한다(번호 재사용 금지).
- 요구사항을 추가·변경하면 같은 변경에서 compliance-matrix.md 행과 요약 개수를 갱신한다. `check-compliance.sh`의 H 검사가 불일치를 FAIL로 알린다.
