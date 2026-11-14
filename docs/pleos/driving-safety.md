# 주행 안전과 주행 규제

확인일: 2026-11-06 KST. 이 문서의 요구사항은 [requirements.md](requirements.md)의 SAFETY·AUDIO·CATEGORY 분류다. **SAFETY·AUDIO 분류의 MUST·MUST_NOT 위반은 BLOCKER**이며 머지·데모 전에 해결한다.

## 플랫폼이 정하는 것

공식 문서가 밝힌 플랫폼 동작이다. 앱이 바꿀 수 없다.

| 사실 | 출처 |
| --- | --- |
| 주행 규제는 속도·기어·브레이크 등 하드웨어 신호와 지역별 안전 규정으로 적용된다. 플랫폼 버전·양산 차량마다 다를 수 있다 | [P-03] |
| DO(Distraction Optimization)를 선언하지 않은 앱은 Parking에서 사용 가능, Drive에서 사용 불가 | [P-03] |
| UX 제한이 활성화되면 매니페스트에 DO로 태그된 Activity만 계속 실행되거나 새로 실행될 수 있다. 플랫폼은 선언만 확인하고 실제 준수는 심사로 집행된다 | [L-07] |
| 플랫폼이 지역 규제 사양에 맞는 ActiveRestrictions를 내려주며, 앱은 `onUxRestrictionsChanged()`로 받은 값을 반영한다 | [D-04] |
| Driving view는 앱이 가리거나 종료할 수 없다. Status bar·GNB 패널·키보드는 앱 위에 표시된다 | [P-05] [UI-02] |
| UX 규제 피그마 사양서는 공개하지 않는다 | [D-04] |

## 앱이 해야 하는 것

1. **CarUxRestrictions가 기준이다.** `CarUxRestrictionsManager.registerListener()`로 변경을 받고, `isRequiresDistractionOptimization()`·`getActiveRestrictions()`로 기능별 제한을 적용한다(SAFETY-001~003, 005). 기어·속도 값만으로 제한을 해제하지 않는다(프로젝트 규칙, AGENTS.md).
2. **DO 선언은 허가가 아니다.** DO는 제한 상태에서 실행을 허용받는 선언일 뿐이다. 선언한 Activity는 모든 제한 플래그를 직접 처리해야 한다(SAFETY-003·004).
3. **진행 중인 것에도 적용한다.** 제한이 켜지면 진행 중 재생·녹음·공개 확인·화면에도 즉시 반영한다(SAFETY-002, AGENTS.md).
4. **모르면 제한한다(프로젝트 규칙).** 리스너 등록 전, 서비스 연결 실패, 권한 오류, 신호 유실은 제한 상태로 취급한다. 이것은 공식 문서 문장이 아니라 AGENTS.md의 규칙이며, 공식 문서와 충돌하지 않는 보수적 기본값이다.
5. **신호 대기·일시 정지는 주차가 아니다(프로젝트 규칙).** 주차 판정은 CarUxRestrictions의 제한 해제와 함께 확인한다.

## UX 제한 플래그와 지역별 Driving 허용 범위

[P-03]의 표를 기능별로 다시 정리했다. Parking에서는 모든 기능이 허용된다. "음성한정"은 음성으로만 사용 가능하다는 뜻이다. 표의 "검색"은 웹 검색이 아니라 인포테인먼트 통합 검색이다.

| 기능 | 플래그(값) | 북미 Driving | 유럽 Driving | 그외 지역 Driving |
| --- | --- | --- | --- | --- |
| 키보드 | `UX_RESTRICTIONS_NO_KEYBOARD`(8) | 제한 | 허용 | 허용 |
| 다이얼패드 | `UX_RESTRICTIONS_NO_DIALPAD`(1) | 제한 | 허용 | 허용 |
| 검색 | `NO_KEYBOARD`(8) + `NO_FILTERING`(2) | 음성한정 | 허용 | 허용 |
| 미디어 앱 로그인·설정·검색 | `NO_SETUP`(64) + `NO_KEYBOARD`(8) + `NO_FILTERING`(2) | 제한 | 제한 | 허용 |
| 디지털키·도어·시트·차량 설정 | `NO_SETUP`(64) | 제한 | 제한 | 제한 |
| 동영상 시청 | `NO_VIDEO`(16) | 제한 | 제한 | 제한 |
| 내비게이션 검색 | `NO_KEYBOARD`(8) + `NO_FILTERING`(2) | 제한 | 허용 | 허용 |
| QR 로그인 | `NO_SETUP`(64) | 제한 | 제한 | 제한 |
| 매뉴얼(대용량 텍스트) | `LIMIT_CONTENT`(32) + `LIMIT_STRING_LENGTH`(4) | 제한 | 제한 | 제한 |

주의:

- 한국이 "그외 지역"에 해당하는지는 문서에 없다(X-14). FAQ의 지역 코드는 KOR·EUR·USA·CAN이다([D-10]).
- 본문은 Parking·Idling·Driving을 언급하지만 표에는 Idling 행이 없다(C-06).
- 이 표로 앱이 직접 제한을 해제하지 않는다. 실제 값은 플랫폼의 ActiveRestrictions다(SAFETY-005). FAQ는 지역 코드로 UX 정책을 적용하라고도 해서 충돌한다(C-01, SAFETY-006).
- InfoLINK용 앱은 지역별 키보드 분기가 필요할 수 있으며 partnership@pleos.ai 문의 사항이다(X-06).

## 주의 분산 방지 원칙 요약

| 영역 | 원칙 | 요구사항 |
| --- | --- | --- |
| 움직임 | 운전 중 동영상·애니메이션 이미지·텍스트 자동 스크롤로 시선 유도 금지. 장식·반복 움직임 지양 | SAFETY-008(금지), 009 |
| 정보 | 1~2초 안에 파악할 핵심 정보만. 문장·단락 3줄 이하 | SAFETY-010, 011, 019 |
| 조작 | 한 손, 최소 조작, 단순한 메뉴, 큰 터치 영역, 음성·물리 버튼 연계 | SAFETY-012~014, 017 |
| 입력 | 키보드보다 프리셋·최근 항목·음성 | SAFETY-016 |
| 음성 대화 | 핸즈프리로 말하고 듣기. 시작·종료·중단은 수동으로도 가능 | SAFETY-023 |
| 오디오 | 운전에 중요한 안내가 오면 포커스 정책에 따라 일시정지·감쇄 | AUDIO-004 |
| 알림 | 운전자 필요와 관련될 때만, 광고 금지 | SAFETY-022 |
| 활동 | 게임·인터넷 서핑·피트니스 유도 금지 | SAFETY-015 |

## 카테고리 예외

[P-04]는 Music/Audio(단순 시각 정보, 재생·일시정지·곡 넘기기, 스트리밍 청취, 라디오 채널 변경, 오디오북), Maps/Navigation, Mobility Services의 주행 중 제한적 허용 예시를 든다. **예시이며, 실제 적용은 주행 상태와 지역 정책에 따른다.** 이 예시를 허용 근거로 확정하지 않는다(CATEGORY-004). UNSEEN의 카테고리는 아직 정하지 않았다(CATEGORY-001).

## UNSEEN 기능별 판단

검토한 로컬 UI 목업은 DO를 선언하지 않았으므로 주행 중에는 플랫폼이 앱 화면 사용을 막는다. 아래는 제안된 주행 중 음성 기능을 검토할 때의 판단이다. 제품 기획의 기준은 [intent.md](../../intent.md)에서 관리한다. "미확인"은 공식 문서에서 찾지 못한 내용이며 기본값은 제한이다.

| 기능 | 관련 요구사항 | 공식 문서 근거 | 미확인 | 구현 전 조건 |
| --- | --- | --- | --- | --- |
| 출발 후 시작 제안(음성 질문) | SAFETY-023, AUDIO-001~004, 013 | 음성 대화는 핸즈프리·수동 종료 가능해야 함 | 자동 실행·출발 감지 허용, Speech Service 포커스(X-08) | 카테고리·실행 정책 확인, 포커스 시나리오 EMU 검증 |
| 이야기 낭독(TTS) | AUDIO-001~014, CATEGORY-004 | 오디오 포커스 정책 필수. Music/Audio 예시에 오디오북·스트리밍 청취 | Pleos TTS의 오디오 컨텍스트(X-08), 선행 묵음 적용 가능 여부 | 포커스 상실·통화·내비 안내 복귀 검증 |
| 대화 입력(STT) | SAFETY-014, 016, 023 | 음성 입력 우선을 권장 | 주행 중 마이크 입력 허용, `RECORD_AUDIO` 필요 여부(X-09) | 공식 확인 전 주행 중 녹음 시작 안 함 |
| 공개 전 음성 확인 | SAFETY-023, 018 | 키보드·설정 화면은 제한 대상 | 음성 확인 자체의 허용 근거 없음 | 무응답·STT 실패를 승인으로 처리하지 않음(AGENTS.md) |
| 내비 위 오브 표시 | UX-005, SAFETY-008·009 | Driving view는 가릴 수 없음 | 다른 앱 위 표시 허용(X-12) | 공식 근거 확보 전 구현하지 않음 |
| 청취 시작 무음 오브 반응 | SAFETY-008·009 | 운전 중 애니메이션 이미지로 시선 유도 금지 | 짧은 상태 표시 움직임의 허용 범위 | 주행 중 움직임 정책을 결정 기록에 남기고 리뷰 |
| 개인 기록·상세 화면 | SAFETY-011, 018, 019 | 대용량 텍스트는 `LIMIT_CONTENT`·`LIMIT_STRING_LENGTH` 대상 | - | 주행 중 상세 화면 진입 차단 |
| 다음 주행 응답 수락 | LIFECYCLE-007, AUDIO-010·011 | Suspend는 언제든 중단·재시작 가능 상태. Resume 시 자동 재생 금지 | Suspend 감지 API(X-07), 주차 브레이크 신호(C-05) | 앱 재시작을 다음 주행으로 오인하지 않음 |

Vehicle SDK의 `getCurrentSeatOccupancy`(`CAR_SEATS`)와 `getCurrentBrakePedalStatus`(`CAR_DRIVE`)는 API Reference에 있다. 탑승·주행 세션 판정 근거로 쓰려면 capability와 Virtual Console 결과를 함께 확인한다(SDK-002).

## 현재 앱의 안전 관련 상태

| 항목 | 상태 | 근거 |
| --- | --- | --- |
| CarUxRestrictions 연동 | NOT_IMPLEMENTED | 1차 목업은 구독하지 않는다. 주행은 시연 제어 토글 |
| DO 선언 | 없음 | M06. 현재는 주행 중 앱 화면 사용 불가 |
| `DadeuriView` 주행 중 움직임 | NOT_APPLICABLE | 표시 중 매 프레임 셰이더 갱신, 시연 주행 토글에서는 정지. DO 선언 시 SAFETY-008 BLOCKER 후보 |
| 오디오 | NOT_APPLICABLE | 소리 출력 없음. 대사는 자막으로 표시 |

## 검증 수단

| 수단 | 쓰임 | 출처 |
| --- | --- | --- |
| `adb shell cmd hmg_car_service --service drivingstate driving_regulation on` / `off` | 주행 규제 활성·해제. 모든 차종 공통 | [D-11] |
| Virtual Console Emulation Controls | 기어(P·R·N·D), 브레이크 페달, Seat Occupancy, Telltales 등 | [L-05] |
| 폼 팩터 테스트 | Connect-S·W·WF, Portrait·Landscape | [L-06] |

Virtual Console 카드 목록에는 차속 카드가 없다. 차속 기반 동작은 규제 명령과 CarUxRestrictions 결과로 검증하고, 실차에서 다시 확인한다. 상세 절차는 [testing-strategy.md](testing-strategy.md)에 있다.

[P-03]: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driving-restiction-policy
[P-04]: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/app-category
[P-05]: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/app-structure-screen-model
[UI-02]: https://document.pleos.ai/docs/connect/guide/docs-design/getting-started/layout
[D-04]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/driving-restriction-development-guide
[D-10]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/vehicle-app-faq
[D-11]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/adb-command-reference-guide
[L-05]: https://document.pleos.ai/docs/pleos-only/app-test-guide/virtual-console-guide
[L-06]: https://document.pleos.ai/docs/pleos-only/app-test-guide/form-factor-test-guide
[L-07]: https://source.android.com/docs/automotive/driver_distraction/guidelines?hl=ko

### 2026-11-15 기획 기준

최신 기획([README](../../README.md), [결정 D-013](../decisions.md))은 주행 중 화면을 줄이고 음성 흐름(이야기 듣기·말로 남기기·되묻기)만 유지하는 것을 계획한다. 위 기능별 판단 표의 "미확인" 항목과 [로드맵](implementation-roadmap.md) D6가 해결되기 전에는 구현에서 제한 상태를 기본값으로 둔다. 1차 기획 제출은 SDK 미개방으로 기획만 다루며, 이 판단의 실제 검증은 SDK 사용이 열린 뒤 수행한다. [음성 기획](../ourroad-voice.md).

1차 목업은 실제 CarUxRestrictions를 구독하지 않는다. 주행 여부는 "시연 제어" 토글로 흉내 내며, 주행으로 바꾸면 자막·사연 정보를 숨기고 다드리의 움직임을 멈춘다. DO를 선언하지 않았으므로 실제 주행 규제가 켜지면 플랫폼이 앱 화면을 막는다.
