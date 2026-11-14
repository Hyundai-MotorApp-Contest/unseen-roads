# Pleos 공식 문서 출처

확인일: **2026-11-06 KST**. Chrome 브라우저(Claude in Chrome)로 실제 렌더링된 페이지를 열어 본문·표·코드·경고를 읽었다. 로그인 없이 열람했다.

상태 값:

| 상태 | 의미 |
| --- | --- |
| READ | 렌더링된 본문을 읽고 요구사항을 추출함 |
| READ_IMAGE | 표·수치가 이미지여서 이미지를 직접 확인함 |
| INDEXED | URL·제목만 확인함. 본문은 읽지 않음 |
| NOT_READ | 링크만 확인했거나 다운로드 파일이라 열지 않음 |
| UNVERIFIED | 공개되지 않았거나 문서에 없어서 확인할 수 없음. 추측으로 채우지 않음 |

요구사항 ID는 [requirements.md](requirements.md), 현재 구현과의 대조는 [compliance-matrix.md](compliance-matrix.md)에 있다.

## 수집 범위 요약

사이드바와 `https://document.pleos.ai/sitemap.xml`(한국어 경로 4,331개)로 하위 문서를 열거했다.

| 범위 | 사이드바 문서 | 읽은 문서 | 비고 |
| --- | --- | --- | --- |
| 앱 기획·설계 가이드 · 앱 설계 원칙 | 10 | 10 | 전부 READ |
| 앱 기획·설계 가이드 · 디자인 가이드 | 34 | 34 | 사이트맵에만 있는 8개 중 6개 추가 READ |
| 앱 개발·연동 가이드 · 앱 개발 가이드 | 11 | 11 | 전부 READ |
| 앱 개발·연동 가이드 · SDK 사용 가이드(프로그래밍 가이드) | 50 | 45 | Overview + SDK별 소개·설정·권한·수명주기·오류. 나머지 5개는 본문 없는 API Reference 루트(`entry`) |
| 앱 개발·연동 가이드 · SDK API Reference(Dokka) | 사이트맵 1,816 | 2 | 나머지는 INDEXED. 필요 시 API 단위로 연다 |
| 앱 개발·연동 가이드 · Gleo Interface | 6 | 6 | 전부 READ |
| 두 가이드에서 링크된 외부·인접 문서 | - | 7 | 아래 L 표 |

API Reference 사이트맵 개수: Vehicle 1,168 · NaviHelper 486 · TextToSpeech 54 · SpeechToText 63 · FusedLocation 45.

## P — 앱 기획·설계 가이드 · 앱 설계 원칙

| ID | 제목 | 상태 | 관련 요구사항 |
| --- | --- | --- | --- |
| [P-01](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/intro) | Pleos Connect 앱 설계 가이드 | READ | 목차 |
| [P-02](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driver-distraction-guideline) | 주행 안전을 고려한 설계 | READ | SAFETY |
| [P-03](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driving-restiction-policy) | 지역별 주행 규제 정책 | READ | SAFETY |
| [P-04](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/app-category) | 앱 카테고리 | READ | CATEGORY |
| [P-05](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/app-structure-screen-model) | 앱 구조 및 동작 모델 | READ | UX, LIFECYCLE |
| [P-06](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/platform-api-architecture) | 하드웨어 접근 정책 | READ | HW |
| [P-07](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/pleos-connect-sdk-api) | SDK API 정책 | READ | SDK |
| [P-08](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability) | API 호환성 및 가용성 | READ | SDK, HW |
| [P-09](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/security-privacy) | 보안 및 개인정보 보호를 고려한 설계 | READ | SECURITY |
| [P-10](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/performance-resource-mgmt) | 성능 및 리소스 효율을 고려한 설계 | READ | PERF, AUDIO |

P-03의 URL 철자(`restiction`)는 원문 그대로다.

## UI — 앱 기획·설계 가이드 · 디자인 가이드 (Pleos Connect UI Design Guide)

공통 경로: `https://document.pleos.ai/docs/connect/guide/docs-design/`

| ID | 문서 | 상태 | 관련 요구사항 |
| --- | --- | --- | --- |
| [UI-01](https://document.pleos.ai/docs/connect/guide/docs-design/Introduction/intro) | Pleos Connect UI Design Guide | READ | SAFETY, DESIGN, UX |
| [UI-02](https://document.pleos.ai/docs/connect/guide/docs-design/getting-started/layout) | Display Layout | READ | UX |
| [UI-03](https://document.pleos.ai/docs/connect/guide/docs-design/getting-started/window) | Window Policy | READ | UX, LIFECYCLE |
| [UI-04](https://document.pleos.ai/docs/connect/guide/docs-design/components/status-bar) | Status bar | READ | UX |
| [UI-05](https://document.pleos.ai/docs/connect/guide/docs-design/components/gnb) | Global Navigation Bar(GNB) | READ | UX |
| [UI-06](https://document.pleos.ai/docs/connect/guide/docs-design/components/app-library) | App library | READ | UX |
| [UI-07](https://document.pleos.ai/docs/connect/guide/docs-design/foundations/colors) | Colors | READ | DESIGN. 토큰 값은 이미지 |
| [UI-08](https://document.pleos.ai/docs/connect/guide/docs-design/foundations/typography) | Typography | READ | DESIGN |
| [UI-09](https://document.pleos.ai/docs/connect/guide/docs-design/foundations/contents-creation/iconography) | Iconography | READ | DESIGN |
| [UI-10](https://document.pleos.ai/docs/connect/guide/docs-design/foundations/contents-creation/app-icon) | App Icon | READ | DESIGN |
| [UI-11](https://document.pleos.ai/docs/connect/guide/docs-design/foundations/animation) | Animation | READ | DESIGN |
| [UI-12](https://document.pleos.ai/docs/connect/guide/docs-design/components/actions/basic-button) | Basic button | READ_IMAGE | SAFETY-013. 크기 이미지에 dp 수치 없음 |
| [UI-13](https://document.pleos.ai/docs/connect/guide/docs-design/components/toast) | Toast · Toast Popup | READ | DESIGN-009 |
| [UI-14](https://document.pleos.ai/docs/connect/guide/docs-design/foundations/interaction) | Interaction (사이드바 밖, 사이트맵) | READ | DESIGN-006 |
| [UI-15](https://document.pleos.ai/docs/connect/guide/docs-design/Experiences/gleo-ai) | Gleo AI 경험 (사이드바 밖) | READ | GLEO, AUDIO |

위 표의 문서와 Icon button·System Popup·System Notification은 렌더링 화면으로 읽었다(규범 문장 없음). 다음 17개는 본문 텍스트를 가져와 규범 표현(주행·금지·해야·필수·최소·권장·dp)으로 스캔했고, 구성요소·상태 이름만 있을 뿐 규범 문장은 없었다: Shadow, Toggle button, Text button, Keypad, Bar, Controllers, Indicator, Dropdown, Text Field, Number Field, Page Navigation, Segmented menu, Checkbox, Radio, Switches, Spinner, Widgets. 사이드바 밖의 Designing apps for driving, Driving view, Theme, Vehicle Notification은 READ했고 UI-01과 같은 원칙만 반복한다. 3D contents, Design token은 NOT_READ다.

## D — 앱 개발·연동 가이드 · 앱 개발 가이드

| ID | 제목 | 상태 | 관련 요구사항 |
| --- | --- | --- | --- |
| [D-01](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/intro) | Pleos Connect 앱 개발 가이드 | READ | 목차 |
| [D-02](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/audio-focus-development-guide/audio-focus-development) | 오디오 포커스 개발 가이드 | READ, READ_IMAGE | AUDIO. Interaction Matrix v1.0 이미지 확인 |
| [D-03](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/audio-focus-development-guide/suspend-mode-audio-focus) | Suspend 모드 - 오디오 포커스 개발 가이드 | READ | AUDIO, LIFECYCLE |
| [D-04](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/driving-restriction-development-guide) | 주행 규제 개발 가이드 | READ | SAFETY |
| [D-05](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/ccp-rotary-controller-development) | CCP 개발 가이드 | READ | CCP |
| [D-06](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/navigation-app-development-guide) | Maps/Navigation 앱 개발 가이드 | READ | NAV, CATEGORY |
| [D-07](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/music-audio-app-developement-guide) | Music/Audio 앱 개발 가이드 | READ | MEDIA, CATEGORY |
| [D-08](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/voice-guidance-preroll-silence-guide) | TTS / 음성 안내 선행 묵음 적용 가이드 | READ | AUDIO |
| [D-09](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/ime-window-resize-handling-guide) | IME 동작에 따른 window resize 처리 가이드 | READ | IME |
| [D-10](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/vehicle-app-faq) | FAQ - 앱 실행 환경 및 플랫폼 리소스 | READ | HW, SAFETY |
| [D-11](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/adb-command-reference-guide) | FAQ - 디버깅을 위한 주요 ADB Command 활용법 | READ | 테스트 전략 |

## S — 앱 개발·연동 가이드 · SDK 사용 가이드

공통 경로: `https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/`

| ID | SDK / 문서 기준 버전 | READ한 프로그래밍 가이드 | 관련 요구사항 |
| --- | --- | --- | --- |
| [S-00](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/) | Overview | 모듈 목록, API 정책, 퍼미션 승인 | SDK |
| [S-V](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro) | Vehicle 2.3.2 (Vehicle Service 2.3.21) | intro, project-setup, vehicle-permission, obtain-domain-object, check-capability, get-property, set-property, callback, vehicle-error-code | SDK |
| [S-N](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/intro) | NaviHelper 3.0.5 | intro, importing-sdk, declaring-permissions, initialization-and-release, event-listeners, destinations, routes, waypoint-settings, route-with-waypoints, error-handling | SDK |
| [S-T](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro) | TextToSpeech 3.3.1 | intro, importing-sdk, declaring-permissions, initialization-and-release, event-listeners, set-locale, app-registration, start-and-stop, error-code | SDK, AUDIO |
| [S-S](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro) | SpeechToText 3.3.1 | intro, importing-sdk, declaring-permissions, initialization-and-release, result-listeners, app-registration, set-locale, start-and-stop, sending-audio-data, error-code | SDK |
| [S-F](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/intro) | FusedLocation 1.1.3 | intro, project-setup, declaring-permissions, initialization-and-release, location-data-access, error-handling | SDK |

각 SDK의 `entry` 페이지는 API Reference 루트이며 본문이 없다. API Reference 중 READ한 것은 Vehicle의 `getCurrentBrakePedalStatus`(`CAR_DRIVE`, normal)와 `getCurrentSeatOccupancy`(`CAR_SEATS`, normal) 두 개다.

## G — 앱 개발·연동 가이드 · Gleo Interface 구현 가이드

공통 경로: `https://document.pleos.ai/docs/pleos-only/app-integration-guide/`

| ID | 문서 | 상태 |
| --- | --- | --- |
| [G-01](https://document.pleos.ai/docs/pleos-only/app-integration-guide/guide/core-concepts) | Gleo Interface (Core Concepts) | READ |
| [G-02](https://document.pleos.ai/docs/pleos-only/app-integration-guide/guide/app-capability) | App Schema Writing Guide | READ |
| [G-03](https://document.pleos.ai/docs/pleos-only/app-integration-guide/guide/action_callback) | Callback Schema Writing Guide | READ |
| [G-04](https://document.pleos.ai/docs/pleos-only/app-integration-guide/guide/examples) | Code Details with Examples | READ |
| [G-05](https://document.pleos.ai/docs/pleos-only/app-integration-guide/guide/deliver-schema) | Delivering the Schema to Gleo | READ |
| [G-06](https://document.pleos.ai/docs/pleos-only/app-integration-guide/use-cases/radio-app) | How to Integrate Radio App with Gleo AI | READ |

## L — 두 가이드에서 링크된 인접 문서

| ID | 제목 | 상태 | 이유 |
| --- | --- | --- | --- |
| [L-01](https://document.pleos.ai/docs/pleos-only/getting-started) | Pleos Connect 앱 개발 시작하기 | READ | minSdk·Maven·첫 호출 |
| [L-02](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator) | Pleos Connect Emulator 설치 및 실행 | READ | minSdk 28 필수·Gradle 8.0 |
| [L-03](https://document.pleos.ai/docs/pleos-only/app-registration-guide/app-security-guide) | 앱 보안 가이드 | READ | P-09가 상세 기준으로 링크 |
| [L-04](https://document.pleos.ai/docs/pleos-only/distribute/app-review-guide/review-functionality-performance-stability) | 앱 리뷰 가이드라인 · 앱 기능 및 성능 안정성 | READ | S-00이 퍼미션 심사로 링크 |
| [L-05](https://document.pleos.ai/docs/pleos-only/app-test-guide/virtual-console-guide) | Pleos Connect Virtual Console 사용 가이드 | READ | D-11이 기어 변경으로 링크 |
| [L-06](https://document.pleos.ai/docs/pleos-only/app-test-guide/form-factor-test-guide) | 폼 팩터 테스트 가이드 | READ | UX 검증 |
| [L-07](https://source.android.com/docs/automotive/driver_distraction/guidelines?hl=ko) | AAOS 운전자 주의 분산 행동 가이드라인 | READ | P-02가 링크. DO 선언 방법 |

## X — 접근하지 못했거나 문서에 없는 항목

아래는 추측으로 채우지 않는다. 필요해지면 해당 경로로 확인하고 이 표를 갱신한다.

| ID | 항목 | 상태 | 확인 경로 |
| --- | --- | --- | --- |
| X-01 | UX 규제 피그마 사양서 | UNVERIFIED | D-04가 "Pleos Playground에 공개하지 않습니다"라고 명시 |
| X-02 | Pleos Connect CCP UX Guideline | NOT_READ | D-05의 다운로드 파일. 다운로드는 사용자 승인 후 |
| X-03 | Pleos Connect display guide · design assets(Figma 템플릿) | NOT_READ | UI-01·UI-10의 다운로드 파일 |
| X-04 | 버튼 크기·터치 영역 간격의 수치 | UNVERIFIED | UI-12 크기 이미지에 Large/Medium/Small만 있고 dp 값이 없음. X-03에 있을 수 있음 |
| X-05 | 차량의 내비게이션 탑재 여부 확인 방법 | UNVERIFIED | D-10: 퍼블릭 미공개, partnership@pleos.ai 문의 |
| X-06 | InfoLINK 지역별 키보드 분기 | UNVERIFIED | P-03: partnership@pleos.ai 문의 |
| X-07 | Suspend 진입·Resume을 앱이 감지하는 API | UNVERIFIED | D-03 예제는 `onSuspendEntering()` 같은 앱 함수만 보여 주고 감지 API를 명시하지 않음 |
| X-08 | TTS·STT(Speech Service)가 쓰는 오디오 포커스·CarAudioContext | UNVERIFIED | S-T·S-S에 언급 없음. Emulator에서 측정 필요 |
| X-09 | STT의 Android 마이크 권한(`RECORD_AUDIO`) 필요 여부 | UNVERIFIED | S-S는 `STT_SERVICE`만 안내. MIC_ERROR는 "센서 개인정보 보호 설정"을 언급 |
| X-10 | 링크된 AAOS·Android 문서(VehiclePropertyIds, CarPropertyManager, CarAppFocusManager, Handle input method visibility, AAOS 미디어 앱, 자동차 오디오 구성, Adaptive Icon) | NOT_READ | 해당 API를 쓰는 작업에서 열람 |
| X-11 | 앱 리뷰 가이드라인의 나머지 페이지, 앱 등록 가이드(태깅·업데이트) | NOT_READ | 배포 준비 단계에서 열람 |
| X-12 | 내비 위 오버레이(다른 앱 위 표시) 허용 여부 | UNVERIFIED | 두 가이드 어디에도 없음. UNSEEN의 내비 위 오브 목표와 직결 |
| X-13 | Maps·Music/Audio 외 카테고리의 매니페스트 선언 방법 | UNVERIFIED | D-06·D-07은 각자 카테고리만 안내 |
| X-14 | 지역별 규제 표에서 한국의 분류 | UNVERIFIED | P-03은 북미·유럽·그외만 표시. D-10은 지역 코드 KOR을 정의 |
| X-15 | 한국어 텍스트 길이 기준 | UNVERIFIED | UI-01은 영어·일본어 기준만 제시 |

## 문서 간 불일치

| ID | 내용 | 처리 |
| --- | --- | --- |
| C-01 | D-04: 플랫폼이 지역별 ActiveRestrictions를 내려주므로 앱은 수신값만 반영. D-10: 앱은 `product.sw.target_region`으로 지역을 구분해 지역별 UX 정책을 적용해야 함 | SAFETY-005를 기본으로 하고 SAFETY-006은 지역 분기가 필요한 기능에서만 적용. 파트너 확인 전 확정하지 않음 |
| C-02 | L-01 예시는 Vehicle 2.3.1, S-V는 2.3.2 | 팀이 버전을 고정할 때 설치 환경과 대조 |
| C-03 | D-07: Gleo 연동 시 핵심 기능 3~5개 추가 음성 기능. G-02: `actionCalls` 최대 3개 | 스키마는 G-02의 최대 3개를 따른다 |
| C-04 | G-05는 function별 exported intent-filter를 요구. L-03은 불필요한 컴포넌트 노출 금지·호출자 접근 제어를 요구 | Gleo 연동 시 수신 컴포넌트에 입력 검증·접근 제어 설계를 함께 리뷰 |
| C-05 | S-V intro는 Brake 도메인이 "주차 브레이크 상태 조회"를 포함한다고 쓰지만 API Reference의 Brake에는 brake pedal status API만 있음 | 주차 브레이크 신호는 UNVERIFIED. API Reference 전체 검색 후 사용 |
| C-06 | P-03 본문은 Parking·Idling·Driving을 언급하지만 표에는 Parking·Driving 행만 있음 | Idling의 기능 허용 범위는 UNVERIFIED |
| C-07 | D-02의 다중 포커스 보유자 판정·매트릭스 설정화·로그 항목은 포커스 중재를 구현하는 주체 기준으로 쓰여 있음 | 3rd-party 앱에는 자기 스트림 요청·결과 처리와 검증 체크리스트만 적용(AUDIO-008) |

## 이전 조사와의 관계

[docs/pleos.md](../pleos.md)는 2026-10-29·2026-11-01 조사 스냅샷이며 콘솔의 SDK 표시 상태(사용 가능/준비 중)를 담고 있다. 이 디렉터리는 2026-11-06 기준 공식 가이드의 요구사항 하네스다. 두 문서가 다르면 원문을 다시 열어 확인하고 이 파일의 확인일을 갱신한다.

## 2026-11-09 · 음성 작업 추가 확인

> 이 절의 프로토타입 코드는 2026-11-15 1차 기획 목업으로 대체하며 제거했다. 조사·확인 기록으로만 남긴다.

웹 도구로 G-01~05의 Gleo 개념·앱 스키마·Callback·Intent 예제·승인 전달, S-T·S-S의 3.3.1 소개, SDK 사용 신청 안내를 다시 읽었다. Android Car·VehiclePropertyIds·CarPropertyManager·CarUxRestrictionsManager·SpeechRecognizer·TextToSpeech·Voice도 읽었다. [정확한 원문 링크와 해석](../ourroad-voice.md), [실행 결과](../../tasks/011-ourroad-voice.md). 2026-11-06의 Chrome 수집과 이번 웹 수집은 별개다.

| ID | 항목 | 상태 | 확인 경로 |
| --- | --- | --- | --- |
| X-16 | Gleo 수신 인증·정확한 응답 대상 패키지/권한·제한 중 silent 보장 | UNVERIFIED | G-04·G-05 공개 예제만으로 보안 보장 불가. 담당자/실환경 확인 전 export하지 않음 |
| X-17 | Pleos 이미지의 android.car 공유 라이브러리 선언 | UNVERIFIED | 공식 배포 계약은 못 찾음. 2.1.0 로컬 이미지에서 필수 선언 설치 실패·optional 선언 후 공개 Car API 실제 콜백 성공. 임의 vendor 내부 API 사용 안 함 |
| X-18 | 기기 내 STT·TTS 한국어 지원 | UNVERIFIED | 표준 API 지원은 이미지별 확인. 이번 이미지 on-device STT=false, Android TTS init=-1. Pleos Speech SDK 실패로 일반화하지 않음 |

X-10 중 이번에 사용한 Car/속성/UX API는 READ로 보완했고 나머지 링크는 기존 상태다. X-07(Suspend)·X-08(Pleos Speech 오디오 포커스)·X-09(Pleos STT RECORD_AUDIO 필요 여부)는 여전히 UNVERIFIED다.
