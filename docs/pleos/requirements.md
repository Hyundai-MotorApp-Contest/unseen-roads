# Pleos 공식 가이드 요구사항

확인일: 2026-11-06 KST. 출처 원문과 상태는 [sources.md](sources.md)에 있다. 현재 구현 대조는 [compliance-matrix.md](compliance-matrix.md)다.

## 읽는 방법

- **ID**: `PLEOS-<분류>-<번호>`. 번호는 재사용하지 않는다. 폐기한 요구사항은 행을 지우지 않고 수준을 `RETIRED`로 바꾼다.
- **수준**: 원문의 표현으로 정한다. 원문에 없는 강도를 붙이지 않는다.

| 수준 | 원문 표현 | 위반 시 분류 |
| --- | --- | --- |
| MUST (필수) | 해야 합니다, 반드시, 필수, ~이어야 합니다 | SAFETY·AUDIO 분류는 BLOCKER, 나머지는 MAJOR |
| MUST_NOT (금지) | 안 됩니다, 금지, 하지 않습니다, 즉시 반려 | MUST와 같음 |
| SHOULD (권장) | 권장, 지양, 최소화, 서술형 설계 원칙 | MINOR |

- **조건**: `모든 앱`이 아니면 조건부 요구사항이다. 예외는 `준수 내용`에 "예외:"로 적는다.
- **방식**: 검증 방법. 하나의 요구사항에 여러 방식이 필요할 수 있다.

| 방식 | 의미 |
| --- | --- |
| AUTO(Mxx/Gxx/Cxx) | `scripts/pleos/check-compliance.sh`의 해당 검사. 정적 근거만 본다 |
| BUILD | Gradle 빌드·Lint |
| UNIT | JVM 단위 테스트로 검증 가능한 로직 |
| EMU | Pleos Connect Emulator·Virtual Console·ADB로 확인 |
| CAR | 실차에서만 확인 가능 |
| REVIEW | 코드·디자인·문구 수동 리뷰 |
| EXT | Pleos App Market·파트너·운영사무국 확인이 필요 |

AUTO 검사가 PASS여도 같은 행의 EMU·CAR·REVIEW·EXT 검증을 대신하지 않는다.

## SAFETY — 주행 안전과 주행 규제

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-SAFETY-001 | MUST | 모든 앱 | CarUxRestrictions를 기준으로 주행 중 제한 상태를 확인하고, 제한 대상 기능의 UI와 동작을 축소하거나 비활성화한다 | 규제 ON/OFF 전환 시 기능별 UI·동작 변화가 설계와 일치 | REVIEW, EMU | [P-02] [D-04] |
| PLEOS-SAFETY-002 | MUST | 모든 앱 | `CarUxRestrictionsManager.registerListener()`로 `onUxRestrictionsChanged()`를 받아 변경된 CarUxRestrictions를 반영한다 | 리스너 등록 코드 존재, 진행 중 화면·재생·녹음에도 즉시 반영 | AUTO(C01), EMU | [D-04] |
| PLEOS-SAFETY-003 | MUST | DO를 선언한 앱 | 규제 활성 시 `isRequiresDistractionOptimization()`과 `getActiveRestrictions()`로 기능별 UX 제한을 직접 적용한다. DO 선언만으로 기능이 허용되지 않는다 | DO 선언 Activity마다 플래그별 분기 존재 | AUTO(C01), EMU | [D-04] |
| PLEOS-SAFETY-004 | MUST | 제한 상태에서 실행할 Activity가 있는 앱 | 해당 `<activity>`에 `<meta-data android:name="distractionOptimized" android:value="true"/>`를 선언한다. DO 미선언 앱은 Parking에서만 사용 가능하고 Drive에서는 사용 불가다. 앱 전체가 DO가 되려면 모든 Activity가 가이드라인을 준수해야 한다 | 선언 위치와 Activity별 주행 중 동작 명세 일치 | AUTO(M06), REVIEW | [P-03] [L-07] |
| PLEOS-SAFETY-005 | MUST | 모든 앱 | 기능별 제한은 `onUxRestrictionsChanged()`로 받은 ActiveRestrictions를 기준으로 적용한다. 플랫폼이 지역 규제 사양에 맞는 값을 내려준다 | 앱이 자체 지역 표로 제한을 해제하지 않음 | REVIEW, EMU | [D-04] |
| PLEOS-SAFETY-006 | MUST | 지역별 UX 분기가 필요한 기능 | `product.sw.target_region`(KOR, EUR, USA, CAN)으로 지역을 구분해 지역별 UX 정책을 적용한다. SAFETY-005와 충돌 소지(C-01) | 분기 기능·근거 문서화, 파트너 확인 | EXT, REVIEW | [D-10] |
| PLEOS-SAFETY-007 | MUST_NOT | 영상 재생 기능이 있는 앱 | 주행 규제 상태에서 영상 재생을 시작하거나 재개하지 않는다(`UX_RESTRICTIONS_NO_VIDEO`) | 규제 ON에서 재생 시작·재개 불가 | REVIEW, EMU | [P-02] [D-04] |
| PLEOS-SAFETY-008 | MUST_NOT | 모든 앱 | 운전 중 동영상, 애니메이션 이미지, 텍스트 자동 스크롤로 운전자의 시선을 유도하지 않는다 | 주행 중 표시 화면에 해당 요소 없음 | AUTO(C16), REVIEW, EMU | [UI-01] |
| PLEOS-SAFETY-009 | SHOULD | 모든 앱 | 주행 중 시선을 끄는 동영상·애니메이션·움직이는 이미지를 최소화하거나 제한한다. 장식적·반복적 움직임과 자동 애니메이션·반복 화면 전환을 지양한다 | 주행 중 상태의 움직임 목록과 제한 방식 문서화 | REVIEW, EMU | [P-02] |
| PLEOS-SAFETY-010 | SHOULD | 모든 앱 | 운전에 필요한 정보를 짧은 시간 안에 이해하도록 간결하게 표시한다. 조건: HUD·클러스터·OSD 영역을 쓰면 해당 영역 정책을 따른다 | 화면별 핵심 정보 정의 | REVIEW | [P-02] |
| PLEOS-SAFETY-011 | SHOULD | 모든 앱 | 1~2초 안에 파악할 수 있는 핵심 정보만 노출하고 단순한 조작만 요구한다 | 화면별 정보량 리뷰 | REVIEW | [UI-01] |
| PLEOS-SAFETY-012 | SHOULD | 모든 앱 | 필요한 기능에 최소 조작으로 도달하도록 메뉴를 단순화하고, 자주 쓰는 기능은 첫 화면·상단에 두며 드롭다운·다단 메뉴를 남용하지 않는다 | 주요 기능까지 조작 단계 수 기록 | REVIEW | [P-02] |
| PLEOS-SAFETY-013 | SHOULD | 모든 앱 | 큰 터치 영역, 높은 대비, 텍스트 최소화 패턴을 적용하고 버튼 크기·간격은 UI Design Guide 기준을 따른다. 공개 문서에 dp 수치 없음(X-04) | 디자인 리뷰. 수치는 X-04 확인 후 | REVIEW | [P-02] [UI-12] |
| PLEOS-SAFETY-014 | SHOULD | 모든 앱 | 터치 조작을 최소화하고 음성 명령·물리 버튼 등 차량 입력 방식과 연계한다 | 주요 기능의 비터치 경로 존재 | REVIEW, EMU | [P-02] |
| PLEOS-SAFETY-015 | MUST_NOT | 모든 앱 | 게임, 인터넷 서핑, 피트니스 등 운전자의 주의를 분산시키는 활동을 유도하지 않는다 | 기능 목록 리뷰 | REVIEW | [P-02] |
| PLEOS-SAFETY-016 | SHOULD | 검색·로그인 등 입력 기능 | 키보드 입력보다 프리셋, 최근 항목, 음성 입력을 우선한다 | 입력 경로 리뷰 | REVIEW | [P-02] |
| PLEOS-SAFETY-017 | MUST | 모든 앱 | 한 손으로 조작할 수 있어야 하며 양손을 요구하거나 운전자의 손·발 움직임을 유도하지 않는다 | 조작 흐름 리뷰 | REVIEW | [UI-01] |
| PLEOS-SAFETY-018 | MUST | 해당 기능이 있는 앱 | 기능을 UX 제한 플래그에 대응시켜 제한한다: 키보드 `NO_KEYBOARD(8)`, 다이얼패드 `NO_DIALPAD(1)`, 검색 `NO_KEYBOARD`+`NO_FILTERING(2)`, 미디어 로그인·설정·차량 설정·QR 로그인 `NO_SETUP(64)`, 동영상 `NO_VIDEO(16)`, 매뉴얼 등 대용량 텍스트 `LIMIT_CONTENT(32)`+`LIMIT_STRING_LENGTH(4)`. 지역별 Driving 허용 범위는 [driving-safety.md](driving-safety.md) 표 | 기능-플래그 대응표 작성, 규제 ON에서 확인 | REVIEW, EMU | [P-03] [D-04] |
| PLEOS-SAFETY-019 | MUST | 표시 텍스트 | 영어 단일 메시지 120자 또는 24단어 이하, 일본어 단일 메시지 30자(영문 기준) 이하, 한 줄 80자(영문 기준) 이하, 문장·단락 3줄 이하. 한국어 기준은 미공개(X-15) | 문자열 리뷰 | REVIEW | [UI-01] |
| PLEOS-SAFETY-020 | MUST | 중요 정보 표시 | 속도·안내·경고 등 중요 정보의 기본 텍스트는 높이 5 mm 이상, 보조 텍스트는 3 mm 이상 | 대상 디스플레이에서 실측 | CAR, REVIEW | [UI-01] |
| PLEOS-SAFETY-021 | MUST | 모든 앱 | 아이콘·텍스트·이미지의 대비율은 4.5:1 이상 | 색 조합별 대비율 계산 | REVIEW | [UI-01] |
| PLEOS-SAFETY-022 | MUST | 알림을 보내는 앱 | 운전자의 필요와 관련된 경우에만 알림을 표시하고, 알림으로 광고나 텍스트 기반 광고를 표시하지 않는다 | 알림 목록 리뷰 | REVIEW | [UI-01] |
| PLEOS-SAFETY-023 | MUST | 음성 기반 통신·대화 기능 | 핸즈프리로 말하고 들을 수 있어야 하며, 음성 대화의 시작·종료·중단은 수동으로도 실행할 수 있어야 한다 | 음성 흐름별 시작·종료·중단 경로 존재 | REVIEW, EMU | [UI-01] |

## UX — 화면 구조·멀티 윈도우·디스플레이

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-UX-001 | MUST | 모든 앱. 예외: 가로 전용 게임처럼 한 모드로만 실행 가능한 앱 | Portrait(window 1개)와 Landscape(window 2개 확장) 레이아웃을 각각 설계한다 | 방향 고정 없음, 폼 팩터 테스트에서 두 방향 확인 | AUTO(M07), EMU | [P-05] [UI-02] |
| PLEOS-UX-002 | SHOULD | 모든 앱 | Portrait에는 핵심 기능과 주요 정보, Landscape에는 부가 정보와 확장 기능을 구분해 배치한다 | 방향별 화면 설계 리뷰 | REVIEW | [P-05] |
| PLEOS-UX-003 | MUST | 모든 앱 | window bar·제스처로 위치·크기가 바뀌어도 핵심 기능을 쓸 수 있도록 반응형 레이아웃으로 구성한다 | window 크기 변경 후 기능 동작 | EMU | [P-05] |
| PLEOS-UX-004 | MUST | 모든 앱 | `screenOrientation`이 window 수와 크기를 결정하므로 화면 설계와 함께 검토한다. 미지정은 Portrait·window 1개, landscape 지정은 window 2개 | Manifest 값과 설계 일치 | AUTO(M07), REVIEW | [P-05] |
| PLEOS-UX-005 | SHOULD | 모든 앱 | 앱은 Application window 안에서 동작하도록 설계한다. Driving view는 앱이 가리거나 종료할 수 없고, Status bar·GNB 패널·키보드 등 System UI는 앱 위 레이어라 앱을 일시적으로 가릴 수 있다 | 가려질 때 핵심 정보 손실 없음 | REVIEW, EMU | [P-05] [UI-02] |
| PLEOS-UX-006 | MUST | 모든 앱 | 레터박스 없이 디스플레이 영역을 채운다(미세 비율 보정용 한 방향 레터박스는 허용 가능). 호환성 모드로 실행되지 않는다 | 폼 팩터별 화면 확인 | EMU | [L-04] |
| PLEOS-UX-007 | MUST | 모든 앱 | 해상도·화면 전환 중 상태 손실·화면 이상(늘어짐, 블러, 터치 위치 불일치)이 없고 모든 화면 상태에서 기능 패리티를 유지한다 | 전환 전후 상태·기능 비교 | AUTO(C15), EMU | [L-04] |
| PLEOS-UX-008 | MUST | 모든 앱 | 디스플레이 컷아웃, 인셋, 소프트웨어 키보드, 시스템 표시줄 등 기기 안전 영역을 준수한다 | 인셋 처리 리뷰, 폼 팩터 확인 | REVIEW, EMU | [L-04] |
| PLEOS-UX-009 | SHOULD | 모든 앱 | Connect-S(17·14.6·12.9인치), Connect-W(Extended 포함), Connect-WF 등 다양한 해상도·비율에 대응한다 | 폼 팩터 프리셋별 확인 | EMU | [UI-01] [UI-02] [L-06] |
| PLEOS-UX-010 | SHOULD | 모든 앱 | 주간·야간 조명 환경에 따라 밝기와 대비를 조정해 가독성을 유지한다 | 테마 전환 확인 | EMU, REVIEW | [UI-01] |
| PLEOS-UX-011 | SHOULD | 모든 앱 | 다국어 지원에 적합한 UI로 설계한다 | 문자열 외부화·길이 여유 | REVIEW | [UI-01] |
| PLEOS-UX-012 | SHOULD | 모든 앱 | 새 앱 실행·window 전환 시 표시 위치·방향이 바뀔 수 있음을 고려한다(새 앱은 직전 orientation을 따름, Connect-W는 새 앱이 primary landscape) | 전환 시나리오 확인 | EMU | [P-05] [UI-03] |

## IME — 입력기와 window resize

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-IME-001 | MUST | EditText·TextField가 없는 window, Dialog, overlay | IME가 표시될 때 view가 밀려 올라가지 않도록 insets 적용 대상을 `statusBars() or navigationBars()`로 한정한다(`setFitInsetsTypes`). EditText가 있는 window는 밀려 올라가는 것이 사양 | IME 표시 시 해당 window 위치 고정 | REVIEW, EMU | [D-09] |
| PLEOS-IME-002 | SHOULD | IME 외 insets 갱신이 필요한 화면 | `View.OnApplyWindowInsetsListener`에서 개별 처리하고, Activity는 Google의 Handle input method visibility 기준으로 검토한다 | 인셋 처리 리뷰 | REVIEW | [D-09] |

## LIFECYCLE — 포그라운드·백그라운드·Suspend

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-LIFECYCLE-001 | MUST | 모든 앱 | 화면에서 사라진 앱은 종료되지 않고 백그라운드에 남을 수 있으며, 표시 중 앱이 종료되면 최근 앱 순으로 다시 foreground가 될 수 있음을 고려한다 | 재표시 시 상태·리소스 정상 | EMU | [P-05] [UI-03] |
| PLEOS-LIFECYCLE-002 | MUST | 모든 앱. 예외: 수 시간 이상 미사용 시 기본 상태 시작 가능 | 백그라운드에서 돌아오면 마지막 사용 상태를 복원하고 화면 크기·방향에 맞게 UI를 재구성한다 | 백그라운드→복귀 후 화면·진행 상태 동일 | AUTO(C15), EMU | [P-05] [L-04] |
| PLEOS-LIFECYCLE-003 | SHOULD | 모든 앱 | 백그라운드 전환 시 화면 갱신, 애니메이션, 불필요한 센서 수집처럼 즉시 멈출 수 있는 작업을 정리한다 | 백그라운드에서 렌더링·수집 중단 | REVIEW, EMU | [P-05] |
| PLEOS-LIFECYCLE-004 | SHOULD | 백그라운드 지속 작업이 있는 앱 | 오디오 재생·통신·다운로드 등은 작업별 실행 조건과 중단·재개 정책을 명확히 정의한다 | 작업별 정책 문서화 | REVIEW | [P-05] |
| PLEOS-LIFECYCLE-005 | SHOULD | 모든 앱 | 백그라운드의 불필요한 상주, 타이머, 폴링, 네트워크 요청을 최소화한다 | 백그라운드 동작 목록 리뷰 | REVIEW, EMU | [P-05] [P-10] |
| PLEOS-LIFECYCLE-006 | MUST | 모든 앱 | 앱 종료·프로세스 재생성 시에도 사용자 데이터와 진행 상태를 복원할 수 있도록 상태를 저장한다. `moveTaskToBack()` 후에도 프로세스는 종료될 수 있다 | 프로세스 종료 후 재실행 시 복원 | AUTO(C15), EMU | [P-05] [D-05] |
| PLEOS-LIFECYCLE-007 | MUST | 모든 앱 | Suspend(시동 OFF 후 저전력 상태)는 언제든 중단되고 다시 시작될 수 있는 상태로 간주한다. 앱 재시작과 시동·주행 세션을 혼동하지 않는다는 프로젝트 규칙과 함께 적용 | Suspend·Resume 후 상태 재평가 | EMU, CAR | [D-03] |

## CATEGORY — 앱 카테고리

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-CATEGORY-001 | MUST | 모든 앱 | 설계 시점부터 카테고리(Maps/Navigation, Mobility Services, Lifestyle, Entertainment, Business, Music/Audio, Kids, Social/Communication 등)를 명확히 정의한다 | 결정 기록 존재 | REVIEW | [P-04] |
| PLEOS-CATEGORY-002 | MUST | 모든 앱 | 개발·등록 시 카테고리를 선언하고 Manifest 선언과 App Market 등록 카테고리를 일치시킨다. 확인된 선언 방법: Maps/Navigation은 `android.intent.category.APP_MAPS`, Music/Audio는 `android:appCategory="audio"`. 그 외는 X-13 | Manifest와 등록 정보 일치 | AUTO(M14), EXT | [D-06] [D-07] |
| PLEOS-CATEGORY-003 | MUST_NOT | 모든 앱 | 선언 카테고리와 실제 기능이 불일치하지 않는다. 심사 중 기능이 바뀌면 재심사를 요청한다 | 기능 목록과 카테고리 대조 | REVIEW, EXT | [D-06] [D-07] [L-04] |
| PLEOS-CATEGORY-004 | MUST_NOT | 모든 앱 | 카테고리별 주행 중 허용 기능 예시(Music/Audio, Maps/Navigation, Mobility Services)를 허용 근거로 확정하지 않는다. 실제 적용은 주행 상태와 지역 정책에 따른다 | 주행 중 기능마다 ActiveRestrictions 근거 기록 | REVIEW, EMU | [P-04] |

## HW — 하드웨어 접근·네트워크·플랫폼 식별

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-HW-001 | MUST_NOT | 모든 앱 | 차량의 물리적 제어나 센서에 직접 접근하지 않는다 | 디바이스 파일·센서 직접 접근 없음 | AUTO(C10), REVIEW | [P-06] |
| PLEOS-HW-002 | MUST | 모든 앱 | 차량 상태 확인·제어는 Pleos Connect SDK의 표준 플랫폼 API로 하고, 속도·기어·외기 온도·연료 같은 표준 데이터는 AAOS Car API를 쓴다 | 차량 데이터 경로 리뷰 | REVIEW | [P-06] [P-07] |
| PLEOS-HW-003 | MUST | 모바일 API를 쓰는 앱 | Telephony, SMS, 자이로·근접 등 일부 센서, 캘린더, 주소록처럼 제한될 수 있는 모바일 API는 차량 지원 여부를 반드시 확인하고, 비필수 기능은 제거하거나 지원 확인 후 동작하는 Optional 구조로 만든다 | 사용 API 목록과 지원 확인 | REVIEW, EMU | [P-06] |
| PLEOS-HW-004 | SHOULD | 네트워크를 쓰는 앱 | 주행 중 연결 끊김·저대역폭에 대비해 최근 콘텐츠 캐싱, 재시도·타임아웃, 오프라인 안내 UI를 제공한다 | 오프라인 시나리오 | REVIEW, EMU | [P-06] |
| PLEOS-HW-005 | MUST_NOT | 모든 앱 | 차량 모델명으로 기능을 분기하지 않고 VehiclePropertyIds·CarPropertyManager로 실제 지원과 접근 가능 상태를 확인한다 | 모델명 비교 코드 없음 | AUTO(C09), REVIEW | [P-08] |
| PLEOS-HW-006 | MUST | SDK가 제공하지 않는 특수 프로퍼티가 필요한 앱 | 직접 구현하기 전에 기술 지원팀 검토를 요청한다 | 요청 기록 | EXT | [P-08] |
| PLEOS-HW-007 | MUST_NOT | 모든 앱 | 에뮬레이터에서 `Build.DEVICE`(CONNECT_S·CONNECT_L·CONNECT_W)로 플랫폼을 구분하지 않는다. 실차에서만 유효하다 | Build 값 분기 리뷰 | AUTO(C09), REVIEW | [D-10] |
| PLEOS-HW-008 | SHOULD | 클러스터와 같은 차속이 필요한 앱 | CarPropertyManager로 `PERF_VEHICLE_SPEED`를 읽는다. GPS 계산 속도는 클러스터와 어긋날 수 있다 | 속도 출처 리뷰 | REVIEW | [D-10] |
| PLEOS-HW-009 | MUST_NOT | 미디어 코덱을 쓰는 앱 | 에뮬레이터의 코덱 지원 결과를 실차에 그대로 적용하지 않는다(Connect-S/L·Connect-W·Emulator 코덱이 다름) | 코덱 선택 근거 | REVIEW, CAR | [D-10] |

## SDK — Pleos Connect SDK 사용

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-SDK-001 | SHOULD | 모든 앱 | 같거나 유사한 기능의 AAOS 표준 API가 있으면 표준 API를 쓴다. Pleos SDK는 중복 기능을 제공하지 않는다 | API 선택 근거 기록 | REVIEW | [P-07] [S-00] |
| PLEOS-SDK-002 | MUST | Vehicle SDK 사용 | 도메인별 `checkXxxCapabilities`로 지원 여부·AccessType(NONE·READ·WRITE·READ_WRITE)·AreaBounds를 확인하고, 미지원이면 UI 비활성 또는 대체 흐름으로 간다 | get/set 전 capability 확인 | AUTO(C02), EMU | [P-08] [S-00] [S-V-cap] |
| PLEOS-SDK-003 | MUST | FusedLocation SDK 사용 | 사용 전 `isSupported()`를 확인하고 미지원 환경에서는 위치 기능을 제공하지 않는다 | isSupported 분기 존재 | AUTO(C04), EMU | [P-08] [S-F] |
| PLEOS-SDK-004 | SHOULD | NaviHelper SDK 사용 | 내비 탑재 차량에서만 전체 호환이며, 전달되는 이벤트는 Navigation 앱 구현·상태에 따라 다를 수 있음을 처리한다. 탑재 확인 방법은 X-05 | 이벤트 미수신 경로 처리 | REVIEW, EMU | [P-08] [S-N] |
| PLEOS-SDK-005 | MUST | Pleos 퍼미션을 선언하는 앱 | 퍼미션은 Pleos App Market과 사전 협의·승인된 범위에서만 선언한다. 승인되지 않은 퍼미션 선언·API 호출은 심사를 통과할 수 없다 | 선언 퍼미션별 승인 근거 | AUTO(M02, M05), EXT | [S-00] [L-04] |
| PLEOS-SDK-006 | MUST | Pleos SDK 의존성 사용 | `settings.gradle(.kts)`에 `https://nexus-playground.pleos.ai/repository/maven-releases/`를 등록하고 `ai.pleos.playground:<SDK>:<버전>` 좌표를 쓴다. 문서 기준 버전: Vehicle 2.3.2, NaviHelper 3.0.5, TextToSpeech 3.3.1, SpeechToText 3.3.1, FusedLocation 1.1.3(팀 고정 버전은 미정, C-02) | 저장소·좌표 확인 | AUTO(G03, G04) | [S-V-setup] [S-N] [S-T] [S-S] [S-F] [L-01] |
| PLEOS-SDK-007 | MUST | 모든 앱 | minSdk는 Android API 28 이상이어야 한다(차량 사이버 보안 요건: 사이닝 키 로테이션·리니지) | `minSdk >= 28` | AUTO(G01) | [L-02] [L-01] |
| PLEOS-SDK-008 | MUST | 모든 앱 | Gradle 8.0 이상을 쓴다 | Wrapper 버전 | AUTO(G02) | [L-02] |
| PLEOS-SDK-009 | MUST | Vehicle SDK 사용 | `Vehicle(context)` 생성 → `initialize()` → `getXxx()`로만 도메인 객체 획득(직접 생성 금지) → 사용 종료 시 `release()`. 비동기 콜백이 끝나기 전에 `release()`하지 않는다 | 수명주기 코드·종료 순서 | AUTO(C02), UNIT, EMU | [S-V] [S-V-obj] [L-01] |
| PLEOS-SDK-010 | MUST | Vehicle SDK 사용 | 결과는 `onComplete`/`onFailed` 비동기 람다로 온다. `onFailed(Exception)`은 VehicleException이 아닐 수 있으므로 타입 확인 후 `errorCode`를 읽는다 | is VehicleException 분기 | AUTO(C02), UNIT | [S-V-err] |
| PLEOS-SDK-011 | SHOULD | Vehicle SDK 사용 | ErrorCode별 권장 처리: NOT_SUPPORTED_PROPERTY·AREA는 UI 비활성·대체, NOT_SUPPORTED_VALUE_TYPE은 파라미터 확인, SECURITY_PERMISSION은 퍼미션 확인, SERVICE_CONNECTION_FAILURE·INTERNAL_SERVICE는 initialize·재시도, DRIVING_RESTRICTED는 주행 제한 안내, EMPTY_RESULT는 빈 상태 UI, UNDEFINED는 로그·재시도 | 오류 코드별 처리 테스트 | UNIT, EMU | [S-V-err] |
| PLEOS-SDK-012 | MUST | Vehicle SDK 사용 | 조회는 `pleos.car.permission.CAR_*`, 제어는 `CONTROL_CAR_*`를 선언하되 쓰는 도메인 퍼미션만 선언하고 API별 `@RequiresPermission`을 확인한다(대표: Door CAR_DOORS, Window·Wiper CAR_WINDOWS, HVAC CAR_CLIMATE, DrivingMode·Brake·Odometer CAR_DRIVE, EvBattery·Fuel CAR_ENERGY, CarInfo·Display CAR_INFO, Light·TurnSignal CAR_LIGHTS, SideMirror CAR_MIRRORS, Safety·Tire CAR_SAFETY, Seat CAR_SEATS, Toll CAR_TOLL) | 사용 API와 선언 퍼미션 일치 | AUTO(M02, M03), REVIEW | [S-V-perm] |
| PLEOS-SDK-013 | MUST | Vehicle Callback 사용 | `registerXxx`/`unregisterXxx`에 같은 리스너 인스턴스를 써서 등록·해제한다 | register·unregister 짝 | AUTO(C03), UNIT | [S-V-cb] |
| PLEOS-SDK-014 | MUST | TextToSpeech SDK 사용 | `pleos.car.permission.TTS_SERVICE` 선언. `TextToSpeech(context, Mode)`(기본 HYBRID) → `initialize(OnInitializeListener)` → `addEventListener()` → 필요 시 `setLocale()` → 서버 모드는 `registerApp()` 후 `onConnected()` 이후 `speak()` → `stop()` → `removeEventListener()` → `release()`. release 후에는 새 객체를 만들어 다시 초기화한다 | 호출 순서·해제 | AUTO(M03, C05), EMU | [S-T] [S-T-perm] [S-T-init] [S-T-reg] |
| PLEOS-SDK-015 | MUST | SpeechToText SDK 사용 | `pleos.car.permission.STT_SERVICE` 선언. `SpeechToText(context, Mode)` → `initialize()` → `addListener(ResultListener)` → `setLocale()` → 서버 모드는 `registerApp()` → 리스너 등록 후 `request()` 또는 `sendAudio()` → `stop()` → `removeListener()` → `release()` | 호출 순서·해제 | AUTO(M03, C05), EMU | [S-S] [S-S-perm] |
| PLEOS-SDK-016 | MUST | STT `sendAudio()` 사용 | `sendAudio()`는 ON_DEVICE 모드에서만 쓰고 마지막 데이터 뒤 `completeAudioSend()`를 호출한다 | 모드·종료 호출 | AUTO(C05), EMU | [S-S-audio] |
| PLEOS-SDK-017 | SHOULD | TTS·STT 사용 | ErrorType 분기에 기본 분기를 둔다(새 타입이 추가될 수 있음). PERMISSION_DENIED, UNAUTHENTICATED, NETWORK_ERROR, TIMEOUT, UNAVAILABLE_LANGUAGE, NOT_SUPPORTED_API, ON_DEVICE_ENGINE_ERROR, MIC_ERROR, AUDIO_ERROR, INVALID_REQUEST_ERROR, SERVER_INTERNAL_ERROR, UNKNOWN을 처리한다 | when에 else 존재, 오류별 안내 | REVIEW, UNIT | [S-T-err] [S-S-err] |
| PLEOS-SDK-018 | MUST | TTS·STT 서버 기능(SERVER, HYBRID의 서버 경로) | Pleos Playground My Project의 Client ID·Client Secret으로 `registerApp()` 한다. ON_DEVICE만 쓰면 등록이 필요 없다. 자격 증명은 저장소에 넣지 않는다(SECURITY-023) | 등록 흐름, 비밀값 미포함 | AUTO(C08), EMU | [S-T-reg] [S-S] |
| PLEOS-SDK-019 | SHOULD | TTS·STT 사용 | 지원 locale은 ko-KR, en-US, fr-CA, es-US, es-MX이며 `setLocale()`은 `speak()`·`request()` 전에 호출한다 | 호출 순서 | REVIEW | [S-T-locale] [S-S] |
| PLEOS-SDK-020 | MUST | FusedLocation SDK 사용 | `pleos.car.permission.FUSED_LOCATION`(protection level dangerous)을 선언하고 앱의 런타임 권한 요청·결과 처리 흐름을 구현한다 | 선언·런타임 요청 | AUTO(M03), EMU | [S-F-perm] |
| PLEOS-SDK-021 | MUST | FusedLocation SDK 사용 | `isSupported()` → `initialize()` → `getLastKnownLocation()`/`registerFusedLocationCallback()` → `unregisterFusedLocationCallback()` → `release()`. unregister와 release는 서로 대체할 수 없고, release 후 같은 인스턴스를 호출하지 않는다 | 수명주기 코드 | AUTO(C04), UNIT | [S-F-init] |
| PLEOS-SDK-022 | MUST_NOT | FusedLocation SDK 사용 | null 위치를 유효 위치로 처리하지 않는다. `getLastKnownLocation()`은 새 위치를 계산하지 않는다. `onError(Exception)`을 처리하고 위치를 쓸 수 없음을 안내하거나 기능을 제한한다 | null·오류 경로 테스트 | UNIT, EMU | [S-F-data] [S-F-err] |
| PLEOS-SDK-023 | MUST_NOT | FusedLocation SDK 사용 | `showLocationAccessBlockedPopup()`을 접근 허용 API로 취급하지 않는다. Android ACCESS_COARSE·FINE·BACKGROUND_LOCATION 동작과 포그라운드·백그라운드 위치 정책을 Pleos 동작 보장으로 해석하지 않는다 | 위치 차단 시 기능 제한 | REVIEW, EMU | [S-F-perm] |
| PLEOS-SDK-024 | MUST | NaviHelper SDK 사용 | 쓰는 API에 맞춰 `NAVI_ROUTE`(get*Info, requestTrip), `NAVI_ROUTE_SEARCH`(경로·경유지 변경), `NAVI_CUSTOM_MAP`(requestNavigationScreen)을 선언한다. `initialize(onConnected, onFailed)`로 연결 후 리스너·API를 쓰고, 결과는 이벤트로 확인한다(`requestRoute()` 성공이 탐색 완료가 아님). `removeListener()` → `release()` | 퍼미션·연결·해제 | AUTO(M03), EMU | [S-N-perm] [S-N-init] [S-N-ev] |
| PLEOS-SDK-025 | MUST | 모든 앱 | Pleos가 공식 제공하고 허용한 API만 쓰고, 차량 API는 앱 기능과 직접 관련된 범위에서만 쓴다 | 사용 API 목록 리뷰 | REVIEW | [L-03] |

## AUDIO — 오디오 포커스·Suspend·음성 안내

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-AUDIO-001 | MUST | 소리를 내는 앱 | AAOS 오디오 포커스를 바탕으로 Pleos Connect Interaction Matrix v1.0(2026-01-28)을 따른다. 행렬은 [development-guidelines.md](development-guidelines.md)에 옮겨 두었다 | 컨텍스트별 기대 결과 확인 | EMU | [D-02] [P-10] [P-02] |
| PLEOS-AUDIO-002 | MUST | 소리를 내는 앱 | 각 오디오 스트림은 재생 시작 전에 `AudioAttributes`와 `AudioFocusRequest`로 포커스를 요청한다 | 재생 경로마다 요청 존재 | AUTO(C07), REVIEW | [D-02] |
| PLEOS-AUDIO-003 | MUST | 다른 스트림과 동시 재생(C)을 기대하는 스트림 | `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`을 쓴다 | gain 값 리뷰 | REVIEW, EMU | [D-02] |
| PLEOS-AUDIO-004 | MUST | 소리를 내는 앱 | 포커스 변경을 반드시 처리한다. 권장 매핑: GAIN 정상 재생, LOSS 즉시 중단·정지, LOSS_TRANSIENT 일시정지 후 복귀 가능, LOSS_TRANSIENT_CAN_DUCK 볼륨 감쇄. 운전에 중요한 오디오 안내가 나오면 일시정지·볼륨 조정으로 대응한다 | 이벤트별 동작 | AUTO(C07), EMU | [D-02] [P-02] [P-10] |
| PLEOS-AUDIO-005 | MUST | duck 대상 스트림 | CAN_DUCK 수신 시 볼륨을 감쇄하고 다른 스트림 종료·포커스 반환 시 원복한다. duck 중 pause하도록 구성하면 동시 재생(C)이 깨질 수 있다 | duck·원복 확인 | EMU | [D-02] |
| PLEOS-AUDIO-006 | MUST | 소리를 내는 앱 | Gleo 호출로 `AUDIOFOCUS_LOSS_TRANSIENT`를 받으면 즉시 재생을 멈추고 필요하면 포커스를 반납한다 | Gleo 호출 중 재생 중단 | EMU | [D-02] |
| PLEOS-AUDIO-007 | MUST_NOT | E(배타) 포커스가 필요한 컨텍스트(CALL, EMERGENCY 등) | 목적에 맞는 gain을 쓰되 포커스로 모든 것을 강제 제어하지 않는다 | gain 선택 근거 | REVIEW | [D-02] |
| PLEOS-AUDIO-008 | MUST | 포커스 중재를 구현하는 주체(C-07) | 다중 보유자 결과는 R > E > C 중 가장 보수적인 값을 적용하고, 매트릭스는 하드코딩 대신 설정으로 두며(권장), incoming context·gain·client id·holders·판정·duck 이벤트를 로그로 남긴다 | 해당 시 리뷰 | REVIEW | [D-02] |
| PLEOS-AUDIO-009 | MUST | 소리를 내는 앱 | 구현 후 체크리스트를 수행한다: MUSIC 중 NAVIGATION 요청은 C, CALL 중 MUSIC 요청은 R, MUSIC 중 CALL 요청은 E, EMERGENCY 중 요청은 R, MUSIC+NAVIGATION 동시 보유 중 VOICE_COMMAND는 보수적 판정 | 시나리오 결과 기록 | EMU | [D-02] |
| PLEOS-AUDIO-010 | MUST | 소리를 내는 앱 | Suspend 진입이 감지되면 진행 중 재생 즉시 중단 → 포커스 반납(abandon) → 재생 상태 저장 → player·track 등 오디오 리소스 해제 순서로 처리하며, 이 과정에서 새 재생·포커스 요청을 하지 않는다. 감지 API는 X-07 | Suspend 진입 후 무음·포커스 미보유 | EMU, CAR | [D-03] |
| PLEOS-AUDIO-011 | MUST | 소리를 내는 앱 | Resume 시 이전 오디오 상태를 그대로 복원하지 않는다. 포커스는 자동 복구되지 않으며 현재 상태를 재평가한 뒤 필요하면 다시 요청한다. 자동 재생 여부는 OEM·사용자 정책을 따른다 | Resume 직후 자동 재생 없음 | EMU, CAR | [D-03] |
| PLEOS-AUDIO-012 | MUST_NOT | 소리를 내는 앱. 예외: 통화·긴급 통신·안전 경고음·시스템 필수 안내는 시스템 정책상 제한적 허용 | Suspend 중 포커스를 유지하거나 백그라운드 재생을 계속하거나 Resume에 대비해 포커스를 선점·유지하지 않는다 | Suspend 중 포커스·재생 없음 | EMU, CAR | [D-03] |
| PLEOS-AUDIO-013 | SHOULD | TTS·녹음 음원 등 음성 안내를 재생하는 앱 | 재생 전 선행 묵음 150~200 ms(최소 150 ms, 200 ms 초과는 예외적)를 오디오 데이터 앞에 넣는다. 재생 지연 방식은 비권장. 한 발화 단위의 연속 음성에는 추가 묵음을 넣지 않고, 끊김·새 세션에는 다시 적용한다. Pleos TTS SDK 출력에 적용 가능한지는 X-08 | 첫 음절 손실 없음 | REVIEW, EMU | [D-08] |
| PLEOS-AUDIO-014 | MUST | 음성 안내를 재생하는 앱 | 재생 전에 출력 경로 초기화, 오디오 포커스·세션 확보, 디코더·플레이어·출력 장치 준비를 확인한다. 선행 묵음은 경로 초기화 문제를 대체하지 않는다 | 준비 확인 후 재생 | REVIEW, EMU | [D-08] |

## MEDIA — Music/Audio 카테고리 (조건부)

이 절은 앱을 Music/Audio 카테고리로 선언할 때만 적용한다.

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-MEDIA-001 | MUST | Music/Audio 앱 | 오디오 콘텐츠 재생이 주 기능이고, 주행 환경에 맞는 UI/UX를 제공하며, 백그라운드 재생과 차량 미디어 컨트롤 연동을 지원한다 | 등록 기준 대조 | REVIEW, EXT | [D-07] |
| PLEOS-MEDIA-002 | MUST_NOT | Music/Audio 앱 | 음악을 단순 보조 기능으로 제공하지 않는다(즉시 반려). 영상이 주 기능인 앱은 이 카테고리에 들지 않는다 | 기능 비중 리뷰 | REVIEW | [D-07] |
| PLEOS-MEDIA-003 | MUST | Music/Audio 앱 | Android MediaBrowserService를 구현하고 MediaSession으로 재생 상태·제어를 제공한다(미구현 시 배포 제외 가능). Media3 MediaSession·MediaLibraryService를 권장한다 | 서비스 선언·세션 동작 | AUTO(M15), EMU | [D-07] |
| PLEOS-MEDIA-004 | MUST | Music/Audio 앱 | `<application android:appCategory="audio">`와 `androidx.car.app.launchable=true` 메타데이터를 선언한다 | Manifest 확인 | AUTO(M15) | [D-07] |
| PLEOS-MEDIA-005 | MUST | Music/Audio 앱 | 하위 카테고리(음악·팟캐스트·오디오북·라디오)를 정의하고 미디어 앱 라이브러리 연동 시 전달하며 콘텐츠 유형별 브라우징 구조를 제공한다 | 브라우징 트리 확인 | REVIEW, EMU | [D-07] |
| PLEOS-MEDIA-006 | MUST | Music/Audio 앱 | 스티어링 휠 키 기본 재생 제어를 미디어 센터 연동 또는 MBS의 `MediaSessionCompat.Callback`(재생, 일시정지, 정지, 미디어 ID·검색 기반 재생, 가능하면 이전·다음)으로 지원한다 | 키 입력 확인 | EMU, CAR | [D-07] |
| PLEOS-MEDIA-007 | MUST_NOT | Music/Audio 앱 | 앱 연결 시·프로필 변경 시 자동 재생하지 않는다. 사용자 동작이나 시스템 재개 요청에서만 재생한다 | 연결·프로필 변경 후 무음 | EMU | [D-07] |
| PLEOS-MEDIA-008 | MUST | Music/Audio 앱 | 게임 실행 등으로 `AUDIOFOCUS_LOSS`를 받으면 MediaSession이 active로 남지 않도록 정지하거나 세션을 비활성화한다 | LOSS 후 세션 상태 | EMU | [D-07] |
| PLEOS-MEDIA-009 | MUST | Music/Audio 앱의 미디어 서비스 | `onCreate()`에서 `startService()`로 Started 상태를 만들고, `onStartCommand()`·재생 시점에 `startForeground()`를 호출하며, `onTaskRemoved()`는 stopMedia → stopSelf → super 순서로 처리하고, `onStop()`을 받으면 플레이어를 정지한다 | 서비스 수명주기 리뷰 | REVIEW, EMU | [D-07] |
| PLEOS-MEDIA-010 | SHOULD | Gleo 음성 검색·제어를 원하는 Music/Audio 앱 | Gleo Interface를 구현하거나 Media3 기반 표준 미디어 API(검색은 MediaLibraryService 필수)를 구현한다 | 연동 방식 결정 | REVIEW | [D-07] |

## NAV — Maps/Navigation 카테고리 (조건부)

ourroad는 내비게이션 앱이 아니므로 현재 적용 대상이 아니다. 카테고리를 바꾸면 다시 판정한다.

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-NAV-001 | MUST | Maps/Navigation 앱 | `android.intent.category.APP_MAPS` intent-filter로 카테고리를 선언하고 App Market 등록에도 반영한다 | Manifest 확인 | AUTO(M16), EXT | [D-06] |
| PLEOS-NAV-002 | MUST_NOT | Maps/Navigation 앱 | Inactive Route·Background 상태에서 경로 안내를 제공하지 않는다(단일 경로 정책, CarAppFocusManager 사용) | 상태별 안내 확인 | EMU | [D-06] |
| PLEOS-NAV-003 | MUST | Maps/Navigation 앱 | 경로 전환은 사용자의 명시적 선택이나 기존 앱 오류·종료 시에만 하고, 전환 안내를 제공하며 동의 없이 자동 전환하지 않는다 | 전환 시나리오 | EMU | [D-06] |
| PLEOS-NAV-004 | MUST_NOT | Maps/Navigation 앱 | 주행 중 3 depth 초과 메뉴, 긴 리스트 스크롤, 영상·애니메이션·광고, 전체 화면 팝업을 제공하지 않는다. 터치 영역 최소 크기와 주행 핵심 정보 상시 노출을 충족한다 | UX 심사 항목 | REVIEW, EMU | [D-06] |
| PLEOS-NAV-005 | MUST | Maps/Navigation 앱 | 최소 권한(위치 필수, 마이크는 음성 입력 시만), 공식 API만 사용, 위치 데이터 암호화, HTTPS, 개인정보 처리방침 제공, 불필요한 위치 저장 금지 | 권한·보안 리뷰 | REVIEW | [D-06] |
| PLEOS-NAV-006 | MUST | Maps/Navigation 앱 | 실행·전환 지연을 최소화하고 장시간 주행에도 안정적으로 동작한다(crash·freeze는 반려) | 장시간 실행 | EMU, CAR | [D-06] |

## CCP — Center Control Panel·Back 하드키

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-CCP-001 | MUST | CCP를 지원하지 않는 User-installed 앱 | 필수 조작을 터치만으로 완결할 수 있어야 한다 | 터치 전용 흐름 확인 | REVIEW, EMU | [D-05] |
| PLEOS-CCP-002 | MUST | CCP를 지원하지 않는 앱 | `<application>`에 `<meta-data android:name="com.hmg.rotary.ROTARY_SUPPORTED" android:value="false"/>`를 선언한다 | Manifest 확인 | AUTO(M08) | [D-05] |
| PLEOS-CCP-003 | MUST | 모든 앱 | 앱 최상단 포커스에서 CCP Back 하드키가 들어오면 앱을 종료하지 않고 `moveTaskToBack(true)`로 숨긴다. `finish()`·`finishAffinity()`로 종료하지 않는다. 하위 화면·팝업에서는 이전 화면 이동 또는 팝업 종료를 한다. 숨긴 뒤에도 허용된 백그라운드 기능은 계속 동작한다. 먼저 CCP Back이 Android 표준 Back 이벤트로 오는지 확인하고 `OnBackInvokedDispatcher` 등 적절한 API를 쓴다 | 화면 깊이별 Back 결과 | AUTO(C11), EMU | [D-05] |
| PLEOS-CCP-004 | MUST | Pre-installed 앱. 예외: 정적 콘텐츠·단순 정보 표시, WebView 등 기술 제약, 게임 등(계약서 명시) | CCP를 초기 릴리스부터 지원하고 CCP 심사를 통과한다 | 심사 | EXT | [D-05] |
| PLEOS-CCP-005 | MUST | CCP를 지원하는 앱 | 주요 UI의 포커스 이동, 회전 스크롤, 클릭 선택, 뒤로·취소 입력이 동작하고, 터치 전용 영역이 없으며, CCP 입력에도 운전 중 차단 요소가 지켜진다. 세부 기준은 CCP UX Guideline(X-02) | CCP 심사 항목 | EMU, EXT | [D-05] |

## GLEO — Gleo Interface 연동 (조건부)

이 절은 Gleo AI가 앱 기능을 음성으로 호출하도록 연동할 때만 적용한다.

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-GLEO-001 | MUST | Gleo 연동 앱 | App Schema를 `assets/schema.json`에 둔다. 필드: packageName, appDescription, version, actionCalls(최대 3개), 각 항목 type `function`, component `activity` 또는 `service`. 스키마 검토·승인 후 연동이 활성화된다 | 파일·필드 검증, 승인 기록 | AUTO(M17), EXT | [G-02] [G-05] |
| PLEOS-GLEO-002 | MUST | Gleo 연동 앱 | function name은 고유하고 대문자와 밑줄만 쓴다. description은 영어 1~2문장이며 Gleo 동작 조작·타 앱 억제 문구와 구체적 예시를 넣지 않는다. parameter type은 string·number·boolean·array 중 하나, enum은 문자열만, required에는 필수 parameter만 넣는다 | 스키마 검증 | AUTO(M17), REVIEW | [G-02] |
| PLEOS-GLEO-003 | MUST | Gleo 연동 앱 | function마다 Manifest에 action이 function name인 intent-filter(category DEFAULT)를 선언한다. activity는 startActivity, service는 sendBroadcast로 호출되며 `ACTION_PARAM` extra의 JSON을 파싱한다. 노출 컴포넌트의 입력 검증은 C-04 | intent-filter 대응 | AUTO(M17), EMU | [G-04] [G-05] |
| PLEOS-GLEO-004 | MUST | Gleo 연동 앱 | 응답은 `ai.pleos.playground.intent.action.APP_ACTION_RESPONSE` 브로드캐스트에 packageName과 response JSON(functionName 일치, status success·asking·fail, 영어 완전 문장 message, mode voice·beep·silent, data는 JSON 배열 문자열 권장)으로 보낸다 | 응답 형식 검증 | UNIT, EMU | [G-03] [G-04] |
| PLEOS-GLEO-005 | MUST | Gleo 연동 앱 | Gleo는 7초 뒤 Action을 취소하므로 시간 안에 응답하고, 시스템 오류·타임아웃도 `fail`로 응답한다 | 응답 시간 측정 | EMU | [G-03] |
| PLEOS-GLEO-006 | SHOULD | 자체 TTS·재생을 하는 Gleo 연동 앱 | 응답에 `mode: silent`(또는 `beep`)를 쓰고 data로 문맥을 넘긴다 | 응답 리뷰 | REVIEW | [G-03] |

## SECURITY — 보안·개인정보

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-SECURITY-001 | MUST | 계정·로그인이 있는 앱 | 차량 소유자 변경·운전자 교체 시 쉽게 로그아웃하고 계정을 전환할 수 있어야 한다 | 전환 흐름 | REVIEW, EMU | [P-09] |
| PLEOS-SECURITY-002 | MUST | 토큰을 저장하는 앱 | 소셜 로그인·OTT 계정 등 토큰은 Keystore 등 안전한 저장소에 둔다 | 저장 위치 리뷰 | REVIEW | [P-09] |
| PLEOS-SECURITY-003 | MUST | 위치·음성·계정 정보를 요구하는 앱 | 왜 필요한지 명확히 하고 동의 플로우를 간단하고 이해하기 쉽게 만든다 | 동의 화면·문구 리뷰 | REVIEW | [P-09] |
| PLEOS-SECURITY-004 | MUST | 모든 앱 | 불필요한 PII를 저장하지 않고, 로그에 개인정보가 포함되면 마스킹한다 | 저장 항목·로그 리뷰 | REVIEW | [P-09] [L-03] |
| PLEOS-SECURITY-005 | MUST | Telemetry·분석 이벤트를 보내는 앱 | 차량 ID·사용자 ID의 익명화·가명화 정책을 수립한다 | 정책 문서 | REVIEW | [P-09] |
| PLEOS-SECURITY-006 | MUST_NOT | 차량 외부로 데이터를 보내는 앱 | 운전 중 행동 데이터를 마케팅 목적으로 쓰지 않도록 구분한다 | 데이터 용도 문서 | REVIEW | [P-09] |
| PLEOS-SECURITY-007 | MUST | 민감정보를 저장하는 앱 | 기기에 저장하는 민감정보는 위치와 관계없이(내·외부 저장소, 캐시, SQLite, SharedPreferences) 앱 수준에서 암호화하고 임시 파일·사본에도 평문을 남기지 않는다 | 저장 경로별 암호화 | REVIEW | [L-03] |
| PLEOS-SECURITY-008 | MUST | 모든 앱 | 디버깅 로그에 민감정보가 기록되지 않게 하고 출시 전 관련 로그를 비활성화하거나 마스킹한다 | 로그 리뷰 | REVIEW | [L-03] |
| PLEOS-SECURITY-009 | SHOULD | 모든 앱 | 민감정보는 캐시에 두지 않는다(필요하면 암호화). 사용이 끝난 캐시·임시 파일은 삭제한다 | 캐시 리뷰 | REVIEW | [L-03] |
| PLEOS-SECURITY-010 | SHOULD | 모든 앱 | 쓰지 않는 자동 백업을 비활성화하거나 민감 파일을 백업 대상에서 제외한다(non-GAS AAOS에서는 Google 백업 미동작) | `android:allowBackup` 또는 백업 규칙 | AUTO(M12) | [L-03] |
| PLEOS-SECURITY-011 | SHOULD | 모든 앱 | 민감정보는 앱 전용 내부 저장소에 둔다. 외부 저장소에 둘 수밖에 없으면 암호화한다(MUST) | 저장 경로 리뷰 | REVIEW | [L-03] |
| PLEOS-SECURITY-012 | MUST_NOT | 네트워크를 쓰는 앱 | 인증 토큰·세션·비밀번호를 URL 경로·쿼리에 넣지 않는다. Authorization 헤더 등을 쓴다 | URL 구성 코드 | AUTO(C14), REVIEW | [L-03] |
| PLEOS-SECURITY-013 | SHOULD | 암호화를 쓰는 앱 | 검증된 표준 알고리즘(SHA-256, AES-GCM, RSA-OAEP)을 쓰고 MD5·SHA-1 보안 용도, ECB를 피하며, IV·nonce를 재사용하지 않고 암호학적 난수를 쓴다 | 암호 코드 리뷰 | REVIEW | [L-03] |
| PLEOS-SECURITY-014 | MUST | 암호화 키를 쓰는 앱 | 키는 Android Keystore 등으로 보호하고 소스·리소스·설정 파일 하드코딩이나 평문 저장을 하지 않는다 | 키 관리 리뷰 | AUTO(C08), REVIEW | [L-03] |
| PLEOS-SECURITY-015 | MUST | 외부와 통신하는 앱 | HTTPS 등 암호화 프로토콜을 쓰고 TLS 1.2 이상(1.3 권장)만 허용하며 SSL·TLS 1.0·1.1로 연결되지 않게 한다. 안전한 Cipher Suite를 권장한다 | cleartext 비허용, http URL 없음 | AUTO(M11, C13), REVIEW | [L-03] |
| PLEOS-SECURITY-016 | MUST | 외부와 통신하는 앱 | 신뢰할 수 있는 서버와만 통신하고, 서버 원격 명령은 출처·유효성·무결성을 확인하며 실행 범위를 제한한다 | 서버 목록·명령 처리 리뷰 | REVIEW | [L-03] |
| PLEOS-SECURITY-017 | MUST | 모든 앱 | 기능에 필요한 권한만 요청한다. 다른 환경과 같은 앱이어도 Pleos Connect에서 실제 필요한 권한 기준으로 관리한다 | 선언 권한 = 사용 기능 | AUTO(M04), REVIEW | [L-03] |
| PLEOS-SECURITY-018 | MUST | 모든 앱 | 외부 호출이 필요 없는 Activity·Service·BroadcastReceiver·ContentProvider를 외부에 노출하지 않는다 | exported 목록 | AUTO(M09) | [L-03] |
| PLEOS-SECURITY-019 | MUST | 민감정보를 다른 컴포넌트로 보내는 앱 | 수신 대상을 명시하고 암시적 브로드캐스트로 민감정보를 보내지 않는다 | Intent 구성 리뷰 | REVIEW | [L-03] |
| PLEOS-SECURITY-020 | MUST | 공개 컴포넌트가 있는 앱 | 민감정보 제공·보호 기능 실행 컴포넌트는 호출자 인증 상태와 접근 권한을 모두 확인한 경우에만 허용한다 | 접근 제어 리뷰 | REVIEW | [L-03] |
| PLEOS-SECURITY-021 | MUST | WebView를 쓰는 앱 | 로컬 파일 접근을 기능에 필요한 범위로 제한하거나 비활성화하고, 외부 콘텐츠가 앱 내부 민감 파일에 접근하지 못하게 한다 | WebView 설정 리뷰 | REVIEW | [L-03] |
| PLEOS-SECURITY-022 | SHOULD | 외부 URI·경로를 받는 앱 | 사용 전에 검증·정규화해 허용 범위 밖 파일·데이터 접근을 막는다 | 입력 검증 리뷰 | REVIEW | [L-03] |
| PLEOS-SECURITY-023 | MUST | 모든 앱 | 인증 토큰·비밀 키 등 비밀 정보를 소스·리소스·설정 파일에 하드코딩하지 않는다 | 비밀값 패턴 없음 | AUTO(C08) | [L-03] |
| PLEOS-SECURITY-024 | SHOULD | 서버를 쓰는 앱 | 서버 IP를 고정하지 말고 도메인 등 관리 가능한 방식으로 연결 대상을 둔다 | 설정 리뷰 | REVIEW | [L-03] |
| PLEOS-SECURITY-025 | SHOULD | 네이티브 라이브러리를 쓰는 앱 | RELRO, Stack Canary, NX 등 메모리 보호를 적용한다 | 빌드 설정 | REVIEW | [L-03] |
| PLEOS-SECURITY-026 | SHOULD | 모든 앱 | SDK·라이브러리의 알려진 취약점을 주기적으로 확인하고 패치된 버전을 유지한다 | 의존성 점검 기록 | REVIEW | [L-03] |
| PLEOS-SECURITY-027 | MUST | 배포 앱 | 테스트·디버그 인증서가 아닌 정식 배포용 서명 인증서로 서명한다 | release 서명 설정 | REVIEW, EXT | [L-03] |
| PLEOS-SECURITY-028 | MUST | 배포 앱 | 앱 디버깅, 디버그 빌드 설정, WebView 원격 디버깅을 비활성화한다 | debuggable 미설정, release 비디버그 | AUTO(M10, G05), BUILD | [L-03] |
| PLEOS-SECURITY-029 | MUST | 웹브라우저 앱 | 신뢰 엔진·HTTPS 우선·인증서 검증·SOP/CORS·CSP Level 2·샌드박스·개발 기능 제한·다운로드 제한 등 브라우저 추가 요구를 충족한다 | 해당 시 리뷰 | REVIEW | [L-03] |

## PERF — 성능·리소스·품질 심사

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-PERF-001 | SHOULD | 모든 앱 | Cold start 시 스플래시에 오래 머물지 않도록 필요한 최소한만 초기화하고 나머지는 지연 로딩한다 | 시작 시간 측정 | EMU | [P-10] |
| PLEOS-PERF-002 | MUST | 모든 앱 | CPU 사용률: 주행 중 내비게이션이 아닌 앱 5% 미만, 내비게이션 앱 15% 이하, 정차 중 모든 앱 25% 이하 | 주행·정차 상태별 측정 | EMU, CAR | [P-10] |
| PLEOS-PERF-003 | MUST | 모든 앱 | RAM 사용량을 400 MB 이하로 유지한다 | 메모리 측정 | EMU, CAR | [P-10] |
| PLEOS-PERF-004 | SHOULD | 모든 앱 | 백그라운드 장기 작업·불필요한 타이머·폴링을 줄이고 리사이클 뷰·캐시 전략으로 메모리를 최적화한다 | 코드 리뷰 | REVIEW | [P-10] |
| PLEOS-PERF-005 | MUST | 모든 앱 | 입력 후 시스템 응답(터치 효과 애니메이션 발생까지)은 0.25초를 넘지 않는다. 지연되면 응답 중 상태를 시각적으로 알린다 | 응답 시간 측정 | EMU, CAR | [UI-01] [L-04] |
| PLEOS-PERF-006 | MUST | 모든 앱 | 앱 실행 10초 이내, 콘텐츠 로드 10초 이내, 특정 버튼 반응 2초 이내. 2초 안에 못 하면 동작 중임을 UI로 알린다 | 시간 측정 | EMU | [L-04] |
| PLEOS-PERF-007 | MUST | 모든 앱 | 설치·삭제·실행·기능 사용 중 오류(강제 종료, 멈춤, 입력 무시)가 없고 미디어가 정상 출력되며, 스터터링·프레임 저하나 시스템 성능 저하가 없다 | 기능 회귀·안정성 | BUILD, EMU | [L-04] |
| PLEOS-PERF-008 | MUST_NOT | App Market 제출 빌드 | 테스트용 더미 데이터, 임시 파일, 임시 콘텐츠를 포함하지 않는다. 제출 앱은 실제 서비스 앱과 같아야 한다 | 제출 빌드 리소스 점검 | AUTO(C17), REVIEW | [L-04] |
| PLEOS-PERF-009 | MUST_NOT | 모든 앱 | 기기 재시작이나 핵심 기능과 무관한 시스템 설정 변경을 제안·요구하지 않고, 하드웨어·소프트웨어 손상이나 과점유를 일으키지 않는다 | 기능 리뷰 | REVIEW | [L-04] |

## DESIGN — UI Design Guide

| ID | 수준 | 조건 | 준수 내용 | 검증 기준 | 방식 | 출처 |
| --- | --- | --- | --- | --- | --- | --- |
| PLEOS-DESIGN-001 | MUST | 모든 앱 | 아이콘, 용어, 상호작용 패턴을 일관되게 제공하고 Pleos Connect 디자인 가이드로 기본 탑재 UI와 일관성을 유지한다 | 디자인 리뷰 | REVIEW | [UI-01] |
| PLEOS-DESIGN-002 | SHOULD | 모든 앱 | 국제적으로 통용되는 아이콘을 텍스트 라벨과 함께 쓰고, 터치 시 햅틱 또는 음성 피드백을 준다 | 디자인 리뷰 | REVIEW | [UI-01] |
| PLEOS-DESIGN-003 | SHOULD | 모든 앱 | 정보 위계별로 일관된 글자 크기를 쓰고 캐릭터가 강한 폰트를 지양한다. 최소 12sp, 실제 픽셀 높이 18px 이상을 권장한다 | 텍스트 크기 리뷰 | REVIEW | [UI-08] |
| PLEOS-DESIGN-004 | SHOULD | 아이콘 제작 | 48×48dp 기본 그리드로 만들고 Filled·Outlined를 상황에 따라 쓴다 | 아이콘 리뷰 | REVIEW | [UI-09] |
| PLEOS-DESIGN-005 | SHOULD | 모든 앱 | App Icon은 Foreground·Background 레이어를 분리한 Android Adaptive Icon으로 만든다(GNB·App library·App Market에서 사용) | 아이콘 리소스 존재 | AUTO(M13), REVIEW | [UI-10] |
| PLEOS-DESIGN-006 | SHOULD | 모든 앱 | 버튼에 Pressed·Disabled·Loading 상태를 제공하고 터치 요소에 축소(95%) 피드백을 준다 | 상태 리뷰 | REVIEW | [UI-12] [UI-14] |
| PLEOS-DESIGN-007 | SHOULD | 모든 앱 | 유지보수를 위해 OTA 업데이트를 고려한다 | 배포 계획 | REVIEW | [UI-01] |
| PLEOS-DESIGN-008 | SHOULD | 모든 앱 | Gleo AI 음성 인터랙션 활용으로 사용성과 주행 안전을 높일 수 있는지 검토한다 | 검토 기록 | REVIEW | [UI-01] |
| PLEOS-DESIGN-009 | SHOULD | Toast를 쓰는 앱 | App panel 안 side·bottom padding 24, Toast max width 792, 메시지 max width 712를 따른다 | 레이아웃 리뷰 | REVIEW | [UI-13] |

## 개수

| 분류 | 개수 |
| --- | --- |
| SAFETY | 23 |
| UX | 12 |
| IME | 2 |
| LIFECYCLE | 7 |
| CATEGORY | 4 |
| HW | 9 |
| SDK | 25 |
| AUDIO | 14 |
| MEDIA | 10 |
| NAV | 6 |
| CCP | 5 |
| GLEO | 6 |
| SECURITY | 29 |
| PERF | 9 |
| DESIGN | 9 |
| 합계 | 170 |

수준별 개수와 표의 일관성은 `scripts/pleos/check-compliance.sh`의 H 검사가 계산한다.

[P-02]: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driver-distraction-guideline
[P-03]: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driving-restiction-policy
[P-04]: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/app-category
[P-05]: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/app-structure-screen-model
[P-06]: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/platform-api-architecture
[P-07]: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/pleos-connect-sdk-api
[P-08]: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability
[P-09]: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/security-privacy
[P-10]: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/performance-resource-mgmt
[UI-01]: https://document.pleos.ai/docs/connect/guide/docs-design/Introduction/intro
[UI-02]: https://document.pleos.ai/docs/connect/guide/docs-design/getting-started/layout
[UI-03]: https://document.pleos.ai/docs/connect/guide/docs-design/getting-started/window
[UI-08]: https://document.pleos.ai/docs/connect/guide/docs-design/foundations/typography
[UI-09]: https://document.pleos.ai/docs/connect/guide/docs-design/foundations/contents-creation/iconography
[UI-10]: https://document.pleos.ai/docs/connect/guide/docs-design/foundations/contents-creation/app-icon
[UI-12]: https://document.pleos.ai/docs/connect/guide/docs-design/components/actions/basic-button
[UI-13]: https://document.pleos.ai/docs/connect/guide/docs-design/components/toast
[UI-14]: https://document.pleos.ai/docs/connect/guide/docs-design/foundations/interaction
[D-02]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/audio-focus-development-guide/audio-focus-development
[D-03]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/audio-focus-development-guide/suspend-mode-audio-focus
[D-04]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/driving-restriction-development-guide
[D-05]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/ccp-rotary-controller-development
[D-06]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/navigation-app-development-guide
[D-07]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/music-audio-app-developement-guide
[D-08]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/voice-guidance-preroll-silence-guide
[D-09]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/ime-window-resize-handling-guide
[D-10]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/vehicle-app-faq
[S-00]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/
[S-V]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro
[S-V-setup]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup
[S-V-perm]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission
[S-V-obj]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object
[S-V-cap]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability
[S-V-cb]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback
[S-V-err]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code
[S-N]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/intro
[S-N-perm]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/declaring-permissions
[S-N-init]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/initialization-and-release
[S-N-ev]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/event-listeners
[S-T]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro
[S-T-perm]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/declaring-permissions
[S-T-init]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/initialization-and-release
[S-T-reg]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/app-registration
[S-T-locale]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/set-locale
[S-T-err]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/error-code
[S-S]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro
[S-S-perm]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/declaring-permissions
[S-S-audio]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/sending-audio-data
[S-S-err]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/error-code
[S-F]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/intro
[S-F-perm]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/declaring-permissions
[S-F-init]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/initialization-and-release
[S-F-data]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/location-data-access
[S-F-err]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/error-handling
[G-02]: https://document.pleos.ai/docs/pleos-only/app-integration-guide/guide/app-capability
[G-03]: https://document.pleos.ai/docs/pleos-only/app-integration-guide/guide/action_callback
[G-04]: https://document.pleos.ai/docs/pleos-only/app-integration-guide/guide/examples
[G-05]: https://document.pleos.ai/docs/pleos-only/app-integration-guide/guide/deliver-schema
[L-01]: https://document.pleos.ai/docs/pleos-only/getting-started
[L-02]: https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator
[L-03]: https://document.pleos.ai/docs/pleos-only/app-registration-guide/app-security-guide
[L-04]: https://document.pleos.ai/docs/pleos-only/distribute/app-review-guide/review-functionality-performance-stability
[L-06]: https://document.pleos.ai/docs/pleos-only/app-test-guide/form-factor-test-guide
[L-07]: https://source.android.com/docs/automotive/driver_distraction/guidelines?hl=ko
