# ourroad 작업 규칙

## 저장소와 제품 기준

- 이 저장소는 `Hyundai-MotorApp-Contest/unseen-roads`의 실제 앱 개발 저장소다. 하네스 문서와 앱 코드를 함께 관리한다. 앱의 빌드·실행 기준은 [개발 환경](docs/environment.md)에 있다. 서버 프로젝트는 아직 없다.
- 제품 요구의 기준은 `intent.md`다. 음성 사연 흐름·SDK 사용 계획·시스템 구조는 최신 논의인 `README.md`가 기준이다. 핵심은 **경로 중첩 → 길 위의 이야기 발견 → 말로 남기는 이야기 연결**이다. 기획 가설·제안 수치를 검증된 사실로 쓰지 않는다.
- 새 작업에서는 이 파일, `docs/project-context.md`, 관련 작업 문서만 우선 읽는다. 전체 문서·양식을 매번 읽거나 만들 필요는 없다.
- 구체적인 구현 요청은 수행한다. 제품 범위·공개 정책·데이터 보관·기술 버전 등 결과를 좌우하는 미정 결정은 필요한 시점에 질문하고, 답변과 무관한 작업은 계속한다. 작은 구현 선택은 기존 관례와 판단으로 처리한다.

## ourroad의 제품 제약

- 주행 중에는 화면 알림·팝업·피드·텍스트 열람·키보드 작성을 제공하지 않고, 음성 흐름(듣기·말하기·되묻기)만 계획한다. 주행 중 음성의 실제 허용 범위는 CarUxRestrictions와 운영사무국·Pleos 확인(`docs/pleos/implementation-roadmap.md` D6) 전까지 미확정이며, 확인 전 구현에서는 제한 상태로 둔다.
- 신호 대기·일시 정지는 주차로 보지 않는다. 목록·기록·상세 화면은 주차 확인과 플랫폼 UX 제한 해제를 모두 확인한 경우에만 연다. 상태 미상·신호 유실·권한 오류 시 제한 상태로 처리하고, 열린 화면과 진행 중 음성에도 상태 변경을 반영한다.
- 무응답·애매한 대답·인식 실패를 승인으로 보지 않는다. 운전자의 명시적 승인 없이 게시하지 않으며, 그 외에는 비공개 보관한다.
- 명시적 동의 전에는 경로를 수집하지 않는다. 철회·삭제는 수집 중단과 관련 저장 데이터 처리까지 검증한다.
- 타인의 원시 GPS 경로·정확한 통과 시각·출발지·목적지·차량 식별자를 공개하거나 공용 로그에 남기지 않는다. 민감 구간 제외·저장 기간·공간/시간 해상도는 검증할 결정이며 임의 숫자로 확정하지 않는다.
- 실제 통과한 공통 도로 구간을 기준으로 발견한다. 주변 좌표나 무작위 게시물을 경로 매칭 성공으로 위장하지 않는다. 방향·중첩·시간 조건은 `docs/decisions.md`에 확정 근거를 남긴다.
- 발견 없음은 정상 상태다. 초기 0~3개는 UX 가정이다. 시드·Mock 데이터는 명시하고, 실사용자 경험인 것처럼 표현하지 않는다.
- 실시간 추적·운전자 채팅·우회 유도·거리 보상·감정 추론·AI 자동 게시를 추가하지 않는다. 신고·숨김·삭제도 P0 범위에 포함한다.

## SDK와 구현

- Android Automotive OS / Pleos Connect 대상이다. 일반 Android Auto 투사 앱으로 간주하지 않는다.
- 차량 앱은 Kotlin을 사용한다. 임시 applicationId와 빌드 환경은 `docs/environment.md`와 `tasks/004-pleos-kotlin-skeleton.md`에 기록한다. FastAPI·PostgreSQL/PostGIS는 교체 가능한 기술 제안이다. 현재 없는 빌드 명령·서버·자동화를 있다고 쓰지 않는다.
- SDK 작업은 `docs/pleos/sdk-api-reference.md`와 `docs/pleos.md`의 조사일을 확인하고 사용할 버전의 공식 API·시그니처·권한·지원 상태를 다시 확인한다. 과거 예제나 기억으로 API를 만들어내지 않는다.
- 문서에 정의됨 / Workspace에서 사용 가능 / 설치 환경에서 지원 / 실제 호출 성공을 구분한다. 위치·주차 신호 접근 가능성을 먼저 확인한다.
- capability·null·비동기 실패·연결 해제·구독 정리를 처리한다. 기어 값만으로 UX 규제 해제를 판단하지 않는다. Mock·빌드 성공은 실제 차량 API 검증을 대신하지 않는다.
- 도메인 로직과 차량 신호·저장·네트워크 경계를 구분하되, 미정 아키텍처나 의존성을 선제 도입하지 않는다.

## Pleos 공식 가이드 준수

- Pleos 앱 기획·설계 가이드와 앱 개발·연동 가이드를 준수한다. 요구사항 ID·출처·검증 방식은 `docs/pleos/requirements.md`, 현재 구현 상태는 `docs/pleos/compliance-matrix.md`, 사용 절차는 `docs/pleos/README.md`다. 작업과 관련된 분류의 요구사항만 확인하고 매번 전체를 읽지 않는다.
- 구현 전: 관련 요구사항 ID를 작업 문서에 적는다. 쓸 API·퍼미션·지원 확인 방법을 `docs/pleos/sdk-api-reference.md`와 공식 원문에서 확인하고, 주행 규제 영향을 `docs/pleos/driving-safety.md`로 판단하며, 바뀔 파일과 매트릭스 행을 정한다.
- 구현 중: CarUxRestrictions를 주행 규제의 기준으로 삼고 진행 중 재생·녹음·공개 확인·화면에도 반영한다. DO 선언이나 카테고리 예시를 허용 근거로 쓰지 않는다. 차량 하드웨어에 직접 접근하지 않고 Pleos SDK·AAOS 표준 API만 쓰며 SDK 호출은 어댑터 경계(`docs/pleos/architecture.md`) 뒤에 둔다. 재생 전 오디오 포커스를 요청하고 포커스 변경·Suspend를 처리한다. capability·`isSupported()`·권한 거부·연결 실패·null·주행 제한을 각각 처리한다.
- 구현 후: macOS `scripts/pleos/check-compliance.sh`, Windows `py -3 scripts\pleos\check_compliance.py`를 실행하고, 관련 Emulator·리뷰 검증을 수행하거나 NOT_RUN·BLOCKED로 남긴다. compliance-matrix를 같은 변경에서 갱신하고 위반·미검증 항목을 작업 문서·PR에 적는다.
- SAFETY·AUDIO 분류의 MUST·MUST_NOT 위반은 BLOCKER다. 해결 전 머지·데모하지 않는다.
- 금지: 공식 문서에 없는 API·퍼미션·정책을 필수 규정이나 구현으로 만들기, 미승인 Pleos 퍼미션 선언, 차량 모델명 분기, 최상단 Back에서 앱 종료, 주행 중 애니메이션·자동 스크롤로 시선 유도, App Market 제출 빌드에 데모 데이터 포함. 확인하지 못한 문서 내용은 추측으로 채우지 않고 `docs/pleos/sources.md`에 UNVERIFIED로 남긴다.
- 요구사항 준수 상태(COMPLIANT·NON_COMPLIANT·PARTIAL·NOT_IMPLEMENTED·NOT_APPLICABLE·UNVERIFIED)와 검사 결과(PASS·FAIL·NOT_RUN·BLOCKED·NOT_APPLICABLE)를 섞지 않는다. 구현하지 않은 기능은 위반이 아니라 NOT_IMPLEMENTED로 쓴다.

## 협업과 검증

- `docs/workflow.md`의 작업 → 작은 변경 → 검증 → PR → 다른 팀원 리뷰 흐름을 따른다. 큰 작업은 `templates/task.md`로 `tasks/`에 범위·담당·완료 조건을 기록한다.
- 새 작업은 최신 `dev`에서 `feat/private-records` 같은 작업 브랜치로 시작한다. 브랜치명에 AI 도구 이름을 붙이지 않는다. 작업 PR은 `dev`로, 검증한 데모·제출 버전 PR은 `dev`에서 `main`으로 연다. 세부 기준은 [브랜치 규칙](docs/workflow.md#브랜치-규칙)을 따른다.
- 새 커밋은 [커밋 규칙](docs/workflow.md#커밋-규칙)을 따른다. 제목은 `type(scope): 변경 요약`으로 쓰고, 커밋 전에 스테이징한 변경을 확인한다.
- 시작 전 branch·기존 변경·겹치는 작업을 확인한다. 다른 사람의 변경을 보존하고 공유 브랜치 강제 푸시·임의 초기화를 하지 않는다.
- 팀·AI 사이 결정과 인수인계는 파일·이슈·PR에 남긴다. 개인 채팅이 모든 팀원에게 공유된다고 가정하지 않는다.
- 문서 하네스 검사는 macOS `python3 scripts/check_harness.py`, Windows `py -3 scripts/check_harness.py`다. 앱 프로젝트가 생기면 실제 Wrapper와 실행 명령을 `docs/environment.md`에 기록한다.
- ourroad 동작 변경 시 `docs/verification-scenarios.md`에서 관련 정상·실패·복귀 시나리오를 골라 검증한다. 문서 수정에 차량 테스트를 요구하지 않는다.
- 결과는 PASS / FAIL / NOT_RUN / BLOCKED / NOT_APPLICABLE로 표시하고 실제 명령·환경·결과·한계를 남긴다. 수행하지 않은 검증은 PASS로 쓰지 않는다.
- 키·토큰·서명 키·개인 경로·개인 동의서는 Git에 넣지 않는다. `.env.example`에는 값 없는 키나 가짜 예시만 둔다. OS별 경로·명령을 구분하고 개인 절대 경로를 공통 설정으로 고정하지 않는다.

## Code Review Rules

- 주행·일시 정지·상태 미상·신호 유실에서 화면 콘텐츠가 노출되거나, 노출된 화면이 제한 상태 전환 후 유지되는지 확인한다. 주행 중 음성 흐름이 허용 근거 없이 시작되거나 명시적 승인 없이 게시되는지 확인한다.
- 동의 전 수집, 철회 후 수집 지속, 삭제 누락, 응답·로그를 통한 타인 경로/시각 재식별 가능성을 확인한다.
- 도로 구간 매칭 없는 발견, 미지원 API를 성공으로 처리한 경로, 구독 누수, 신고·숨김·삭제 누락을 확인한다.
- SDK 버전·권한 근거와 실제 검증 수준을 대조한다. AI 리뷰를 다른 팀원 승인으로 기록하지 않는다.
- 변경에 걸린 Pleos 요구사항 ID, compliance-matrix 갱신 여부, `check-compliance` 결과와 NOT_RUN 항목을 확인한다.
