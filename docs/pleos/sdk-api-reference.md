# Pleos Connect SDK·AAOS API 참조

확인일: 2026-11-06 KST. 공식 프로그래밍 가이드에서 확인한 범위만 적는다. 메서드 시그니처를 추측해 쓰지 않으며, 쓰기 전에 해당 API Reference 페이지(`@RequiresPermission` 포함)를 다시 연다. 요구사항 ID는 [requirements.md](requirements.md)의 SDK·AUDIO 분류다.

## 검증 수준을 섞지 않는다

| 수준 | 뜻 | 근거 위치 |
| --- | --- | --- |
| 문서에 정의됨 | 공식 문서에 API가 있음 | 이 문서 |
| Workspace에서 사용 가능 | Pleos Playground 프로젝트 콘솔 표시 | [docs/pleos.md](../pleos.md) 2026-11-01 절 |
| 설치 환경에서 지원 | Emulator·차량에서 capability·`isSupported()` 확인 | 작업 문서, [testing-strategy.md](testing-strategy.md) |
| 실제 호출 성공 | 콜백 결과를 로그·화면으로 확인 | 작업 문서 |
| 앱의 사용·배포 허용 | App Market 퍼미션 승인·심사 | EXT |

| SDK | 문서 기준 버전 | 문서에 정의됨 | Workspace 표시(2026-11-01) | 설치 환경 지원 | 실제 호출 | 사용·배포 허용 |
| --- | --- | --- | --- | --- | --- | --- |
| Vehicle | 2.3.2 | 예 | Vehicle State 11개 사용 가능, Vehicle Control 8개 준비 중 | NOT_RUN | NOT_RUN | 퍼미션 승인 필요(SDK-005) |
| NaviHelper | 3.0.5 | 예 | 2개 사용 가능 | NOT_RUN | NOT_RUN | 퍼미션 승인 필요 |
| TextToSpeech | 3.3.1 | 예 | 준비 중 | NOT_RUN | NOT_RUN | UNVERIFIED |
| SpeechToText | 3.3.1 | 예 | 준비 중 | NOT_RUN | NOT_RUN | UNVERIFIED |
| FusedLocation | 1.1.3 | 예 | 준비 중 | NOT_RUN | NOT_RUN | UNVERIFIED |

콘솔의 "준비 중" 표시를 호출 실패로 단정하지 않고, "사용 가능" 표시를 호출 성공으로 보지 않는다.

## 공통 설정

| 항목 | 내용 | 요구사항 |
| --- | --- | --- |
| Maven 저장소 | `https://nexus-playground.pleos.ai/repository/maven-releases/`를 `settings.gradle(.kts)`의 `dependencyResolutionManagement.repositories`에 추가 | SDK-006 |
| 좌표 | `ai.pleos.playground:Vehicle:2.3.2`, `:NaviHelper:3.0.5`, `:TextToSpeech:3.3.1`, `:SpeechToText:3.3.1`, `:FusedLocation:1.1.3` | SDK-006 |
| 개발 환경 | minSdk API 28 이상(필수, 차량 사이버 보안), Gradle 8.0 이상, Java·Kotlin, Emulator 2.1.0, Virtual Console은 Emulator 2.1.0 이상 | SDK-007, 008 |
| API 정책 | AAOS에 같은 기능이 있으면 AAOS 표준 API 우선. Pleos SDK API가 AAOS로 편입돼도 최소 2개 메이저 버전 하위 호환 | SDK-001 |
| 차종별 호환 | Vehicle·FusedLocation은 모델에 따라 다름(capability·isSupported로 확인). NaviHelper는 내비 탑재 차량만. TTS·STT는 전체 호환으로 표기 | SDK-002~004 |
| 퍼미션 승인 | Pleos App Market과 사전 협의·승인된 퍼미션만 선언. 미승인 선언·호출은 심사 불가 | SDK-005 |

L-01 빠른 시작 예시는 Vehicle 2.3.1이고 상세 문서는 2.3.2다(C-02). 버전을 고정할 때 [docs/decisions.md](../decisions.md)에 근거를 남긴다. 프로젝트의 현재 `settings.gradle.kts`는 `repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)`이므로 저장소는 settings 파일에만 추가한다.

## Vehicle SDK

| 항목 | 내용 |
| --- | --- |
| 역할 | 차량 정보 조회·제어. 3rd party 앱은 Vehicle Service(v2.3.21 기준)에 IPC로 연결 |
| 진입점 | `Vehicle(context)` → `initialize()` → `getDoor()`·`getBrake()` 등 `getXxx()`로 도메인 객체 획득(직접 생성 금지) → `release()` |
| 도메인 | Brake, CarInfo, Display, Door, DrivingMode, EvBattery, Fuel, HVAC, Light, Odometer, Safety, Seat, SideMirror, Steeringwheel, Tire, Toll, TurnSignal, Window, Wiper |
| API 타입 | Capability `checkXxxCapabilities(onComplete(config, areaMap), onFailed)`, Get `getCurrentXxx(onComplete(값), onFailed)`, Set `setXxx(..., onFailed)`, Callback `registerXxx(listener)`/`unregisterXxx(listener)` 리스너 `onXxxUpdated`/`onFailed` |
| Capability 모델 | `CapabilityConfig.accessType`: NONE, READ, WRITE, READ_WRITE. `AreaBounds(minValue, maxValue)`, `Map<Area, AreaBounds>` |
| 비동기 | 결과는 즉시 반환되지 않는다. 콜백이 끝나기 전에 `release()`하지 않는다. 콜백 스레드는 문서에 없다(UNVERIFIED) |
| 퍼미션 | 조회 `pleos.car.permission.CAR_*`, 제어 `CONTROL_CAR_*`. 대표: Door CAR_DOORS, Window·Wiper CAR_WINDOWS, HVAC CAR_CLIMATE, DrivingMode·Brake·Odometer CAR_DRIVE, EvBattery·Fuel CAR_ENERGY, CarInfo·Display CAR_INFO, Light·TurnSignal CAR_LIGHTS, SideMirror CAR_MIRRORS, Safety·Tire CAR_SAFETY, Seat CAR_SEATS, Toll CAR_TOLL. API별로 `@RequiresPermission`이 다를 수 있다 |
| 오류 | `onFailed(Exception)`. VehicleException이면 `errorCode`(ErrorCode enum, `ai.pleos.playground.vehicle.constant`). 모든 실패가 VehicleException은 아니다 |
| 지원 환경 | 모델별 상이. Emulator와 Virtual Console로 개발·검증 |

ErrorCode와 권장 처리(SDK-011):

| ErrorCode | 뜻 | 권장 처리 |
| --- | --- | --- |
| ERROR_NOT_SUPPORTED_PROPERTY | 기능 미지원 | UI 비활성·대체 흐름 |
| ERROR_NOT_SUPPORTED_AREA | 영역 미지원 | 영역 선택 변경 |
| ERROR_NOT_SUPPORTED_VALUE_TYPE | 값 타입 미지원 | 파라미터·타입 확인 |
| ERROR_SECURITY_PERMISSION | 퍼미션 부족 | Manifest·`@RequiresPermission` 확인 |
| ERROR_SERVICE_CONNECTION_FAILURE | 서비스 연결 실패 | `initialize()`·에뮬레이터 상태 확인 |
| ERROR_INTERNAL_SERVICE | 내부 오류 | 재시도·일시 오류 안내 |
| ERROR_DRIVING_RESTRICTED | 주행 중 기능 제한 | 주행 제한 안내 |
| ERROR_EMPTY_RESULT | 결과 없음 | 빈 상태 UI |
| ERROR_UNDEFINED | 미정의 | 로그·재시도 |

UNSEEN과 관련해 API Reference에서 직접 연 API:

| API | 시그니처(원문) | 퍼미션 |
| --- | --- | --- |
| `Brake.getCurrentBrakePedalStatus` | `getCurrentBrakePedalStatus(onComplete: (Boolean?) -> Unit, onFailed: (Exception) -> Unit)`. true면 페달을 밟고 있음 | `CAR_DRIVE`(normal) |
| `Seat.getCurrentSeatOccupancy` | `getCurrentSeatOccupancy(area: SeatArea, onComplete: (Boolean?) -> Unit, onFailed: (Exception) -> Unit)` | `CAR_SEATS`(normal) |

같은 Reference 목록에 `checkSeatOccupancyCapabilities`, `registerSeatOccupancy`/`unregisterSeatOccupancy`, `checkBrakePedalStatusCapabilities`, `registerBrakePedalStatus`/`unregisterBrakePedalStatus`가 있다(본문 미열람). Brake 소개는 주차 브레이크 조회를 언급하지만 Reference의 Brake에는 브레이크 페달 API만 보인다(C-05).

## TextToSpeech SDK

| 항목 | 내용 |
| --- | --- |
| 역할 | 텍스트를 Speech Service가 합성해 차량 오디오로 재생 |
| 진입점 | `ai.pleos.playground.tts.TextToSpeech(applicationContext, Mode)`. Mode 생략 시 HYBRID |
| 모드 | HYBRID(기본, 서버 우선·네트워크 실패 시 On-device 자동 전환, 서버 사용 시 앱 등록 필요), SERVER(서버만, 실패 시 오류 이벤트), ON_DEVICE(네트워크·앱 등록 불필요) |
| 순서 | 생성 → `initialize(OnInitializeListener)` → `addEventListener(EventListener)` → `setLocale(Locale)` → (서버 모드) `registerApp(clientID, clientSecret, OnServerConnectionListener)` 후 `onConnected()` → `speak(text, isFinal = true)` → `stop()` → `removeEventListener()` → `release()` |
| 이벤트 | `onReady()`, `onStart()`, `onUpdatedRms(rms)`, `onDone()`, `onStop()`, `onError(ErrorType)` |
| 분할 발화 | 마지막 조각을 제외하고 `isFinal = false` |
| Locale | ko-KR, en-US, fr-CA, es-US, es-MX. 미설정 시 이전 설정 → 시스템 locale |
| 퍼미션 | `pleos.car.permission.TTS_SERVICE`(초기화·리스너·등록·합성 요청에 필요) |
| 재사용 | 컴포넌트 수명주기에 맞춰 재사용. `release()` 뒤에는 새 객체·재초기화 |
| 미확인 | Speech Service의 오디오 포커스 요청·CarAudioContext(X-08), 앱 측 선행 묵음 적용 가능 여부 |

## SpeechToText SDK

| 항목 | 내용 |
| --- | --- |
| 역할 | 발화를 텍스트로 반환. **명령을 실행하지 않는다.** 앱 기능 실행이 필요하면 Gleo Interface 검토 |
| 진입점 | `ai.pleos.playground.stt.SpeechToText(applicationContext, Mode)`. Mode 생략 시 HYBRID |
| 모드 | TTS와 같은 HYBRID·SERVER·ON_DEVICE. SERVER는 음성 데이터를 원격 Speech Service로 전송 |
| 순서 | 생성 → `initialize(OnInitializeListener)` → `addListener(ResultListener)` → `setLocale()` → (서버 모드) `registerApp()` → `request()`(차량 마이크) 또는 `sendAudio(ByteArray)` → `stop()` → `removeListener()` → `release()` |
| 리스너 | `onReady()`, `onStartedRecognition()`, `onUpdated(stt, completed)`, `onUpdatedEpdData(on, off)`(입력 버퍼 byte offset), `onUpdatedRms(rms)`(dB 아님), `onEndedRecognition()`, `onError(ErrorType)`. `request()`·`sendAudio()` 전에 등록 |
| 오디오 전송 | `sendAudio()`는 ON_DEVICE 전용. 16 kHz·16-bit·mono PCM 예시는 100 ms당 3,200 byte. 마지막에 `completeAudioSend()` |
| 퍼미션 | `pleos.car.permission.STT_SERVICE`. Android `RECORD_AUDIO` 필요 여부는 문서에 없음(X-09) |
| 미확인 | 주행 중 마이크 입력 허용 범위, 실제 인식 품질 |

TTS·STT ErrorType(SDK-017): AUDIO_ERROR, INVALID_REQUEST_ERROR, MIC_ERROR, NETWORK_ERROR, NOT_SUPPORTED_API, ON_DEVICE_ENGINE_ERROR, PERMISSION_DENIED, SERVER_INTERNAL_ERROR, TIMEOUT, UNAUTHENTICATED, UNAVAILABLE_LANGUAGE, UNKNOWN. 새 타입이 추가될 수 있으므로 기본 분기를 둔다. STT의 MIC_ERROR 안내는 "마이크 상태와 센서 개인정보 보호 설정 확인"이다.

## FusedLocation SDK

| 항목 | 내용 |
| --- | --- |
| 역할 | 차량 위치 데이터 수신(FusedLocation Service) |
| 진입점 | `FusedLocation(context)`. 문서 예시는 Application 범위의 인스턴스 1개 |
| 순서 | `isSupported()` → `initialize()` → `getLastKnownLocation()` 또는 `registerFusedLocationCallback(FusedLocationListener)` → `unregisterFusedLocationCallback(listener)` → `release()` |
| 기타 API | `showLocationAccessBlockedPopup()`(차단 안내만, 허용 API 아님), `addProximityAlert()`/`removeProximityAlert()`(영역 진입·이탈 알림) |
| 데이터 | `LocationData`: 위도, 경도, 고도, 진행 방향, 신뢰도, 타임스탬프. 콜백 인자는 null일 수 있음 |
| 리스너 | `onChanged(LocationData?)`, `onError(Exception)` |
| 퍼미션 | `pleos.car.permission.FUSED_LOCATION`(protection level dangerous). 런타임 요청 흐름 필요 |
| 주의 | `getLastKnownLocation()`은 새 위치를 계산하지 않는다. release 후 같은 인스턴스 호출 금지. Android ACCESS_*_LOCATION·백그라운드 위치 정책은 이 SDK가 보장하지 않는다 |

UNSEEN 규칙과 함께 적용: 명시적 동의 전에는 위치 수집을 시작하지 않고, null·오래된 위치로 가짜 경로를 만들지 않는다(AGENTS.md, U-07).

## NaviHelper SDK

| 항목 | 내용 |
| --- | --- |
| 역할 | 내장 Navigation 앱(Navi Service)과 목적지·경유지·경로·정보 조회 연동 |
| 진입점 | `ai.pleos.playground.navi.helper.NaviHelper(applicationContext)` → `initialize(onConnected, onFailed)`(권장) 또는 `initialize()` |
| 리스너 | `NaviHelperEventListener`: 경로(onRouteStarted, onRoutingCompleted, onRouteCancelled, onReRouteEnded, onRouteOptionChanged), 목적지·경유지(onDestinationChanged, onDestinationArrived, onWaypointChanged, onWayPointArrived), 정보(onBookmarkInfo, onRecentDestinationInfo, onCurrentLocationInfo, onDestinationInfo, onWayPointInfo, onRouteStateInfo, onTBTInfo, onTripInfo, onNavigationStatus, onDrivingInfo, onCurrentRoadSpeedLimit, onCameraAlert, onCautionPointInfo 등), `onError(NaviErrorCode)` |
| 요청 | `requestRoute`, `requestRouteWithWaypoints`, `cancelRoute`, `requestReRoute`, `changeRouteOption`, `addWaypoint`/`removeWaypoint`(최대 2개, FIRST·SECOND·ALL), `get*Info`, `requestNavigationScreen` |
| 퍼미션 | `NAVI_ROUTE`(get*Info, requestTrip), `NAVI_ROUTE_SEARCH`(경로·경유지 변경), `NAVI_CUSTOM_MAP`(requestNavigationScreen) |
| 오류 | 초기화 실패는 `onFailed(throwable)`, 내비 오류는 `onError`: CAUSE_LOW_BATTERY, CAUSE_SEARCH_ROUTE_OVER_DISTANCE, CAUSE_SEARCH_ROUTE_TOO_SHORT_DISTANCE, CAUSE_SEARCH_ROUTE |
| 주의 | 이벤트는 Navigation 앱 구현·상태에 따라 다르다. `requestRoute()` 성공은 탐색 완료가 아니다. 내비 탑재 확인 방법은 비공개(X-05) |

UNSEEN 규칙: 우회 유도·경로 변경 기능을 넣지 않는다(AGENTS.md). 안내 예정 경로를 실제 통과 이력으로 쓰지 않는다. 내비 안내 중인지 판단하는 데 쓸 수 있는 이벤트(`onNavigationStatus`, `onTBTInfo` 등)의 실제 전달 여부는 EMU로 확인한다.

## Gleo Interface

| 항목 | 내용 |
| --- | --- |
| 역할 | Gleo AI(LLM function calling)가 앱 function을 Intent로 호출하고 앱이 Broadcast로 응답 |
| 스키마 | `assets/schema.json`. packageName, appDescription, version, actionCalls(최대 3). 부팅·설치 시 Gleo가 스캔, 검토·승인 후 활성 |
| 호출 | `component: activity`는 `startActivity`, `service`는 `sendBroadcast`. Intent action = function name, extra `ACTION_PARAM`에 JSON 문자열 |
| 응답 | `Intent("ai.pleos.playground.intent.action.APP_ACTION_RESPONSE")`에 `packageName`, `response`(functionName, status success·asking·fail, message, mode voice·beep·silent, data) |
| 제한 | 7초 후 Action 취소. description·message는 영어 |

## AAOS·Android 표준 API

| API | 쓰임 | 요구사항 |
| --- | --- | --- |
| `CarUxRestrictionsManager.registerListener()` / `onUxRestrictionsChanged()` | 주행 규제 상태 수신 | SAFETY-002 |
| `CarUxRestrictions.isRequiresDistractionOptimization()` / `getActiveRestrictions()` | 기능별 제한 판단 | SAFETY-003 |
| `<meta-data android:name="distractionOptimized" android:value="true"/>` | Activity DO 선언 | SAFETY-004 |
| `CarPropertyManager` + `VehiclePropertyIds.PERF_VEHICLE_SPEED` | 클러스터와 같은 차속 | HW-008 |
| `AudioManager.requestAudioFocus(AudioFocusRequest)` / `abandonAudioFocusRequest()` / `OnAudioFocusChangeListener` | 오디오 포커스 | AUDIO-002~006 |
| `CarAppFocusManager.requestAppFocus()` | 내비 앱 경로 소유권 | NAV-002 |
| `Activity.moveTaskToBack(true)`, `OnBackInvokedDispatcher` | CCP Back 숨김 | CCP-003 |
| `WindowManager.LayoutParams.setFitInsetsTypes()`, `View.OnApplyWindowInsetsListener` | IME insets 제외 | IME-001·002 |
| `android.os.Build.DEVICE` | 실차 플랫폼 구분(CONNECT_S·L·W). 에뮬레이터에서는 무효 | HW-007 |
| 시스템 프로퍼티 `product.sw.target_region` | 지역 구분(KOR·EUR·USA·CAN). 앱에서 읽는 방법은 문서에 없음 | SAFETY-006 |

AAOS API의 정확한 시그니처·권한(예: CarPropertyManager 접근 권한)은 이번에 열람하지 않았다(X-10). 사용할 때 Android Developers 원문을 확인한다.

## 문서에 없는 것

아래 API는 공식 문서에서 찾지 못했다. 있다고 가정하고 구현하지 않는다.

- Suspend 진입·Resume 감지 API(X-07)
- 시동·탑승·주행 세션 시작/종료 이벤트 전용 API
- 다른 앱(내비) 위 오버레이 표시 허용(X-12)
- 내비 탑재 여부 조회(X-05)
- Speech Service의 오디오 컨텍스트(X-08)
- LLM SDK: Emulator 2.1.0에서 제공 종료(docs/pleos.md 2026-10-29 조사)
