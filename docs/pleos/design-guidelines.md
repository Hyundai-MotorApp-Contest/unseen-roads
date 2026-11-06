# 화면·디자인 가이드라인

확인일: 2026-11-06 KST. 앱 설계 원칙([P-02], [P-05])과 Pleos Connect UI Design Guide([UI-01])를 화면 설계·리뷰 기준으로 정리했다. 주행 규제 자체는 [driving-safety.md](driving-safety.md)를 본다.

## 디스플레이와 window

| 항목 | 내용 | 요구사항 |
| --- | --- | --- |
| 화면 구성 | Status bar, Driving view(운전자 쪽 고정, 앱이 가릴 수 없음), GNB, Application window(앱 1개 또는 2개) | UX-005 |
| 레이어 | Status bar·GNB 패널(App library, 공조)·키보드 등 System UI가 앱 위. 일시적으로 앱을 가릴 수 있음 | UX-005 |
| 화면 유형 | Connect-S(17·14.6·12.9인치, 12.9는 기존 Connect-L), Connect-W(Wide, 세로 확장형 Connect-W-Extended), Connect-WF(Waterfall) | UX-009 |
| Portrait | 앱 영역의 window 하나. 두 앱을 같이 띄우면 보통 portrait | UX-001 |
| Landscape | 앱 하나가 두 window 영역을 사용 | UX-001 |
| 방향 지정 | `screenOrientation` 미지정이면 portrait·window 1개, landscape 지정이면 window 2개 | UX-004 |

Window Policy([UI-03]) 요약:

| 항목 | Connect-S | Connect-W |
| --- | --- | --- |
| 구성 | 앱 2개 1:1, 앱 1개 2:0(기본 1:1) | 2:1 또는 1:2(기본 2:1) |
| 기본 화면 | Primary: Navigation 앱 | Primary: Navigation, Secondary: Media(재탑승 시 마지막 Media 앱) |
| 새 앱 | 직전 앱의 orientation을 따름. landscape 하나면 landscape, portrait 둘이면 primary에 portrait | 항상 landscape로 primary. 기존 primary는 secondary로, secondary는 사라짐 |
| 화면에서 사라진 앱 | 종료가 아님. 표시 앱이 종료되면 최근 앱 순으로 다시 표시 | 같음 |
| 위치·크기 변경 | 3 핑거 스와이프, window bar 탭·드래그, 앱 window 버튼 | 같음 |
| 종료 | 3 핑거 스와이프. Navigation 앱은 종료 불가 | 같음 |

Connect-W는 최대 앱 2개, App library가 운전석·조수석 모두에 있고 조수석에서 실행한 앱은 조수석 window에서만 실행된다. Deep link로 앱 B를 실행하면 앱 A를 덮고, B가 끝나면 A로 돌아간다.

설계 규칙:

- Portrait와 Landscape를 각각 설계한다. Portrait는 핵심 기능·주요 정보, Landscape는 부가 정보·확장 기능(UX-001·002).
- window 크기가 바뀌어도 핵심 기능을 쓸 수 있는 반응형 레이아웃(UX-003). 레터박스 최소화, 호환성 모드 금지(UX-006).
- 해상도·방향 전환 중 상태 손실·화면 이상이 없고 기능 패리티를 지킨다(UX-007).
- 컷아웃·인셋·키보드·시스템 표시줄 안전 영역을 지킨다(UX-008).

## 주행을 고려한 화면

| 원칙 | 기준 | 요구사항 |
| --- | --- | --- |
| Easy & Safe | 운전 중 동영상·애니메이션 이미지·텍스트 자동 스크롤로 시선 유도 금지. 한 손 조작, 양손·손발 움직임 유도 금지 | SAFETY-008, 017 |
| Glanceability | 1~2초 안에 파악. 핵심 정보만. 입력 후 0.25초 안에 응답, 늦으면 진행 중 표시 | SAFETY-011, PERF-005 |
| 구조 | 자주 쓰는 기능은 첫 화면·상단. 드롭다운·다단 메뉴 남용 금지 | SAFETY-012 |
| 터치 | 큰 터치 영역, 높은 대비, 텍스트 최소화. 버튼 크기·간격은 디자인 가이드 기준 | SAFETY-013 |
| 입력 | 프리셋·최근 항목·음성 우선 | SAFETY-016 |
| 일관성 | 아이콘·용어·상호작용 패턴을 기본 탑재 UI와 일관되게 | DESIGN-001 |

버튼·터치 영역의 dp 수치는 공개 텍스트에 없다(X-04). Basic button 크기 이미지는 Large·Medium·Small만 보여 준다. 수치가 필요하면 디자인 자산(X-03)을 사용자 승인 후 내려받아 확인하고 [sources.md](sources.md)를 갱신한다. 그 전까지 수치 기준을 만들어 쓰지 않는다.

## 텍스트

| 항목 | 기준 | 요구사항 |
| --- | --- | --- |
| 단일 메시지 | 영어 120자 또는 24단어 이하, 일본어 30자(영문 기준) 이하 | SAFETY-019 |
| 한 줄 | 80자(영문 기준) 이하 | SAFETY-019 |
| 문장·단락 | 3줄 이하 | SAFETY-019 |
| 높이 | 중요 정보 기본 텍스트 5 mm 이상, 보조 3 mm 이상 | SAFETY-020 |
| 크기 | 최소 12sp 권장, 실제 픽셀 높이 18px 이상 권장. 위계별 일관 크기 | DESIGN-003 |
| 서체 | 중립적·균일한 시인성. 캐릭터가 강한 폰트 지양. System UI 폰트는 Asta Sans(한국어·영어) | DESIGN-003 |

한국어 길이 기준은 문서에 없다(X-15). 영어 기준을 한국어 글자 수로 환산해 확정하지 않는다.

## 색·대비·테마

- 아이콘·텍스트·이미지 대비율 4.5:1 이상(SAFETY-021). 중요한 정보는 강한 대비로, 색을 과하게 쓰지 않는다.
- 주간·야간 조명에 맞춰 밝기·대비를 조정한다(UX-010).
- Colors 문서는 Grayscale·Alpha·Static·System Color(Background, Button, Driving, Regulation, Text 등) 토큰 체계를 제시하지만 값은 이미지다. 토큰 값을 옮겨 적지 않았다.

## 아이콘·피드백·상태

| 항목 | 기준 | 요구사항 |
| --- | --- | --- |
| 아이콘 | 국제 통용, 텍스트 라벨과 함께. 48×48dp 그리드, Filled·Outlined | DESIGN-002, 004 |
| App Icon | Android Adaptive Icon(Foreground·Background 분리). GNB·App library·App Market에서 사용. 브랜드별 마스킹 | DESIGN-005 |
| 터치 피드백 | 햅틱 또는 음성 피드백. 터치 요소는 눌림 시 95% 축소 + 배경색 변화, 영역 밖으로 나가면 취소 | DESIGN-002, 006 |
| 버튼 상태 | Pressed, Disabled, Loading(로더 표시·조작 불가) | DESIGN-006 |
| 애니메이션 | 상태 설명 목적. Linear(정적 로딩), Tension(긴 로딩) | SAFETY-009 |

## 알림·팝업

- 알림은 운전자 필요와 관련될 때만, 광고 금지(SAFETY-022).
- Toast: App panel 안 side·bottom padding 24, 전체 max width 792, 메시지 max width 712(DESIGN-009). Toast Popup은 24dp 간격.
- 주행 중 전체 화면 팝업 금지는 Maps/Navigation 심사 항목이다(NAV-004). 다른 카테고리에서도 SAFETY-011·012 관점에서 피한다.

## Gleo AI 경험

Gleo는 GNB 다음 하위 레이어에 표시되어 다른 앱 화면을 일시적으로 가릴 수 있다. 대화 상태는 Sleep, Listening_Passive, Listening_Active, Thinking, Asking, Speaking, Standby로 구분되고 chrome(Lottie)으로 표시된다([UI-15]). 음성 대화 앱은 "듣는 중"과 "응답 중"을 명확히 구분해 보여 주는 이 원칙을 참고할 수 있다. Gleo의 브랜딩·chrome을 앱이 재사용해도 되는지는 문서에 없으므로 쓰지 않는다.

## UNSEEN 적용 메모

| 항목 | 현재 | 판단 |
| --- | --- | --- |
| Prism Orb 애니메이션 | 표시 중 매 프레임 셰이더 갱신, 화면에 보이지 않으면 갱신 중단 | 주차 중 화면은 문제 없음. DO 선언 시 SAFETY-008·009 적용, 주행 중 정책 결정 필요 |
| 글래스 패널 효과 | `GlassPanel` 페이드 애니메이션 | 같음 |
| 사용 레이아웃 글자 크기 | 최소 20sp | DESIGN-003 충족(정적) |
| 버튼 | `PrismAction` minHeight 72dp, minWidth 220dp | 기준 수치 미확인(X-04) |
| 방향 | 미지정, 폭 560dp 이상에서 버튼 가로 배치 | 폼 팩터 테스트 NOT_RUN |
| 야간 테마 | `values-night` 없음 | UX-010 NOT_IMPLEMENTED |
| 앱 아이콘 | 없음 | DESIGN-005 NOT_IMPLEMENTED |
| 예시·데모 문구 | 21개(C17) | 데모 표기는 프로젝트 규칙상 필요, App Market 제출 빌드에서는 제거(PERF-008) |

## 디자인 리뷰 체크리스트

- [ ] Portrait·Landscape 화면이 모두 있고 window 크기 변경에도 핵심 기능이 남는다.
- [ ] 주행 중 표시될 화면에 동영상·자동 스크롤·장식 애니메이션이 없다.
- [ ] 화면마다 1~2초 안에 읽을 핵심 정보만 있다. 문장은 3줄 이하.
- [ ] 주요 기능까지 최소 조작이며 한 손으로 된다. 음성·물리 버튼 경로가 있다.
- [ ] 대비율 4.5:1 이상, 주간·야간 모두 확인했다.
- [ ] 아이콘에 텍스트 라벨이 있고 Pressed·Disabled·Loading 상태가 있다.
- [ ] 수치를 모르는 항목(X-04, X-15)에 임의 기준을 쓰지 않았다.

[P-02]: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driver-distraction-guideline
[P-05]: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/app-structure-screen-model
[UI-01]: https://document.pleos.ai/docs/connect/guide/docs-design/Introduction/intro
[UI-03]: https://document.pleos.ai/docs/connect/guide/docs-design/getting-started/window
[UI-15]: https://document.pleos.ai/docs/connect/guide/docs-design/Experiences/gleo-ai
