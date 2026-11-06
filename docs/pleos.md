# Pleos 공식 문서 조사

> 2026-10-29 조사 스냅샷을 UNSEEN으로 이관했다. 2026-10-30에 최신성을 재검증한 자료가 아니다. 버전·권한·일정은 사용 직전 공식 원문을 확인한다. 이 파일의 “이번 조사/이번 세션”은 원본 조사 당시를 뜻한다. 출처는 [하네스 이관 기록](harness-migration.md)을 참고한다.

확인일: **2026-10-29 KST**. 문서 기준 정보이며, 팀의 실제 설치 상태·호출 결과와 구분한다.
현재 docs 사이트와 릴리스 노트를 기준으로 정리했다. 이전 검색 결과의 모듈 목록을 그대로 재사용하지 않는다.

## 플랫폼 구분

| 구분 | 의미 |
| --- | --- |
| Pleos Playground | 문서·SDK·프로젝트 관리를 제공하는 개발자 포털 |
| Pleos Connect | 앱이 실행되는 차량 인포테인먼트 플랫폼 |
| Pleos Connect SDK | 차량 상태·내비·음성·위치 API |
| Pleos Connect Emulator | Android Studio 등에서 사용하는 가상 실행 환경 |
| Virtual Console | 에뮬레이터의 차량 상태·이벤트를 조작해 앱 반응을 검증하는 도구 |

사용자가 설명한 ‘실차 없이 주행 상황을 돌려보는 환경’은 Emulator와 Virtual Console의 조합에 해당한다.
실차의 모든 동작·성능이 검증된다는 뜻은 아니다.
[대회 개발 환경 안내](https://appdevchallenge.auto-hmg.io/guide/development), [Emulator 설명](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)

Connect SDK는 **Android Automotive OS(AAOS)** 기반이다. 스마트폰 투사 방식의 Android Auto용 앱이라고 가정하지 않는다.
현재 SDK Overview의 모듈은 Vehicle, NaviHelper, TextToSpeech, SpeechToText, FusedLocation이다.
[SDK Overview](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/)

## 문서상 개발 환경과 버전

| 항목 | 공식 문서 기준 | 팀 실제 상태 |
| --- | --- | --- |
| Minimum SDK | API 28 이상 | 미확인 |
| 개발 언어 | Java 또는 Kotlin | 선택 미정 |
| Gradle | 8.0 이상 | 고정 버전 미정 |
| 에뮬레이터 이미지 | API 34, Pleos Connect v2.1.0 | 설치 완료 전달, 정확한 버전 미확인 |
| AVD 설정 | Cold boot, RAM 4GB 안내 | 미확인 |

공식 Mac용 system image URL은 arm64용이며 Windows/Linux는 별도 URL이다.
Mac 2대의 CPU 종류는 아직 확인하지 않았다. OS가 같다는 이유로 CPU·이미지까지 같다고 간주하지 않는다.
[설치·실행 가이드](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)

문서상 Cold boot가 필요하며 Quick boot에서는 VHAL을 사용할 수 없다고 안내한다.
기존 설치를 삭제·재설치하거나 설정을 변경하지 않았다.
[Emulator 설정](https://document.pleos.ai/en/docs/connect/guide/getting-started/application-development/configure-pleos-connect-emulator)

2026-10-19 릴리스 노트의 기준 버전은 다음과 같다. **이 표가 팀의 버전 고정 결정은 아니다.**

| SDK | 버전 |
| --- | --- |
| Vehicle | 2.3.2 |
| NaviHelper | 3.0.5 |
| TextToSpeech | 3.3.1 |
| SpeechToText | 3.3.1 |
| FusedLocation | 1.1.3 |

같은 릴리스에서 `VehicleSpeedListener` 삭제, NaviHelper 좌표 생성자 순서 변경 등 호환성 변경이 있다.
따라서 이전 예제를 무조건 복사하지 않고 사용할 버전의 API를 확인해야 한다.
[SDK 릴리스 노트](https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015)

Emulator v2.1.0 릴리스에는 **LLM SDK·CRN·블루투스 동글 연동 제공 종료**가 명시되어 있다.
Gleo Interface와 LLM SDK를 같은 기능으로 취급하지 않는다.
[Emulator 릴리스 노트](https://document.pleos.ai/docs/pleos-only/release-note/emulator-261015)

## 문서 불일치·제공 상태 주의

| 확인 사항 | 근거 | 처리 |
| --- | --- | --- |
| Quick Start의 Vehicle 예시는 2.3.1 | [빠른 시작](https://document.pleos.ai/docs/pleos-only/getting-started) | 상세 문서·설치 환경과 대조 |
| Vehicle 상세·가져오기 문서는 2.3.2 | [Importing Vehicle SDK](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup) | 팀 확인 후 사용할 버전 고정 |
| 사용 신청 문서에 SDK별 상태 차이 | [프로젝트·사용 신청](https://document.pleos.ai/docs/pleos-only/create-my-project) | 로그인 Workspace의 실제 상태 확인 |

사용 신청 문서는 Vehicle State와 NaviHelper를 신청 없이 사용 가능으로, Vehicle Control·TTS·STT·FusedLocation을 준비 중으로 안내한다.
동시에 해당 SDK의 상세 API 문서와 릴리스 버전도 제공된다. 문서 존재·프로젝트 사용 권한·환경 지원·실제 호출 성공을 별도로 기록해야 한다.
프로젝트 생성 후 팀 멤버 관리가 가능하며, 서버 기반 음성 SDK에는 프로젝트 자격 증명이 필요할 수 있다.
자격 증명을 Git이나 공용 채팅에 복사하지 않는다. CRN은 v2.1.0 이후 입력이 필요 없다고 안내한다.
[프로젝트 생성 및 SDK 사용 신청](https://document.pleos.ai/docs/pleos-only/create-my-project)

### UNSEEN 프로젝트 콘솔의 제공 상태 (2026-11-05 확인)

Pleos Playground 콘솔에서 로그인한 상태로 UNSEEN 프로젝트의 **SDK** 및 **API 권한 관리** 메뉴를 읽기만 했다. 권한 신청·설정 변경은 하지 않았다.
이 표는 **Workspace의 제공 상태**다. 설치 환경 지원·실제 호출 성공은 별도로 확인해야 한다.

| SDK / API | 콘솔 상태 | 콘솔에 표시된 항목 |
| --- | --- | --- |
| Vehicle State | 사용 가능 (11/11) | 제원, 주행 정보(브레이크 페달·드라이브 모드·크루즈·주행 거리), 도어·트렁크·프렁크, EV·연료, 공조, 라이트, 안전 경고등, 좌석, 사이드미러, 통행료 시스템, 창문 |
| NaviHelper | 사용 가능 (2/2) | 경로 정보 조회(현재 경로·위치 정보·TBT), 경로 탐색 설정 |
| FusedLocation | 준비 중 | 정밀 위치 정보 조회 |
| TextToSpeech | 준비 중 | 음성 합성·재생 |
| SpeechToText | 준비 중 | 음성 인식 |
| Vehicle Control | 준비 중 (0/8) | 도어, 공조, 주행 정보 설정, EV 충전, 창문, 좌석, 사이드미러, 라이트 제어 |
| REST · Vehicle Data | 신청 전, 유럽 지역 한정 | 위치, 운행 상태(시동·주차 상태), 속도, 배터리·파워트레인 등 10개 |
| REST · Fleet | 제휴 계약 필요 | 차량 관제 |

UNSEEN 영향:

- 경로 수집 근거로 가정했던 FusedLocation을 현재 프로젝트에서 사용할 수 없다. NaviHelper 경로 정보의 위치 값과 Android 표준 위치 API를 대안 후보로 확인해야 한다. 둘 다 미검증이다.
- 콘솔의 Vehicle State 주행 정보 설명에는 기어가 명시되어 있지 않다. 주차 판단에 쓸 신호는 SDK 클래스와 `CarUxRestrictionsManager`로 확인한다.
- Vehicle Data REST의 주차 상태·위치는 유럽 한정이라 국내 MVP 근거로 쓰지 않는다.
- P1 주차 중 음성 입력은 STT 제공 전까지 진행할 수 없다.

## SDK별 개발 시 확인점

### Vehicle

차량의 Door·HVAC·EV 배터리·시트·조명 등 도메인을 조회·제어한다.
진입점은 `Vehicle`이며 `getXxx()`로 도메인을 얻는다.
`initialize()` 후 capability/get/set/구독을 사용하고, 구독과 리소스를 해제한다.
IPC 기반 비동기 처리이므로 완료 전에 `release()`하지 않는다.
[Vehicle 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)

Maven 저장소는 `https://nexus-playground.pleos.ai/repository/maven-releases/`이고 상세 문서의 좌표는 `ai.pleos.playground:Vehicle:2.3.2`다.
이번에는 실제 Gradle 파일이나 의존성을 생성하지 않았다.
[가져오기](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)

조회 `CAR_*`와 제어 `CONTROL_CAR_*` 권한을 구분한다.
예: 도어 조회 `pleos.car.permission.CAR_DOORS`.
도메인 이름만으로 권한을 추정하지 않고 API별 `@RequiresPermission`을 확인한다.
[권한 문서](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)

`onFailed`는 `Exception`을 받는다. 모든 오류를 `VehicleException`으로 가정하지 않는다.
기능·영역 미지원, 권한 부족, 연결 실패, 주행 제한, 빈 결과를 구분한다.
[오류 처리](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)

### NaviHelper

Connect 내장 Navigation 앱과 목적지·경유지·경로 요청을 연동한다.
연결 초기화, 리스너 등록, 요청, 콜백 처리, 리스너 해제·release 흐름을 확인한다.
앱이 이벤트를 받는지는 Navigation 앱의 구현·상태에도 영향을 받는다.
[NaviHelper 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/intro)

### TextToSpeech / SpeechToText

TTS는 텍스트의 음성 합성·재생, STT는 발화를 텍스트로 반환한다.
STT 자체가 인식한 명령의 앱 동작을 실행하지는 않는다.
각 SDK는 HYBRID / SERVER / ON_DEVICE 모드를 설명한다.
서버 기반 기능은 등록·연결 조건을 확인하고, ON_DEVICE는 네트워크가 필요 없는 모드로 설명된다.
실제 프로젝트에서 사용 가능한 상태인지는 별도로 확인해야 한다.
[TTS 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro), [STT 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro)

### FusedLocation

마지막 위치 조회·위치 업데이트·영역 진입/이탈 알림 등을 제공한다.
`isSupported()`와 `pleos.car.permission.FUSED_LOCATION`을 확인한다.
마지막 위치 조회가 새 위치 계산을 보장하지 않으며 결과가 null일 수 있다.
[FusedLocation 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/intro)

차량 모델마다 API 지원이 다르므로 Vehicle capability와 FusedLocation 지원 확인을 사용한다.
에뮬레이터에서 한 번 성공했다고 모든 차종에 적용된다고 결론 내리지 않는다.
[API 호환성과 가용성](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability)

## Virtual Console과 재현 가능한 검증

지원 최소 Emulator 버전은 v2.1.0이다. 공식 연결 절차는 다음과 같다.

```text
adb forward tcp:8088 tcp:8088
http://127.0.0.1:8088/
```

위는 공식 절차의 안내이며 이번 세션에서 실행한 명령이 아니다.
복수 디바이스가 연결되어 있다면 실제 대상 serial을 확인하고 `adb -s <SERIAL> ...`로 지정한다.
제공된 로컬 접속 절차를 사용하며 팀 공유를 위해 외부 네트워크에 포트를 노출하지 않는다.
[Console 셋업](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/use-vehicle-emulator)

Emulation Controls에서 Domain·카드·영역·값을 선택하고 앱 반응을 확인한다.
기어·브레이크·도어·조명·배터리·공조 등 다양한 상태를 바꿀 수 있다.
각 API의 지원 여부까지 자동으로 보장하는 것은 아니므로 실제 callback과 화면 결과를 기록한다.
[Console 사용 가이드](https://document.pleos.ai/docs/pleos-only/app-test-guide/virtual-console-guide)

아래는 향후 앱 기능에 맞춰 고를 **검증 후보**이며 실행 결과가 아니다.

| 후보 | 기록할 근거 |
| --- | --- |
| 초기 연결과 상태 읽기 | AVD·서비스 버전, capability, get 결과·오류 |
| 차량 상태 변경 반영 | Console Domain·카드·초기/변경 값, callback, 화면 |
| 주차→주행 제한→해제 | 수신한 UX restrictions, 제한 기능, 복귀 동작 |
| 미지원·권한 부족·빈 값 | 실제 오류와 앱의 대체 동작 |
| 앱 종료·재시작 | 구독 정리·재연결 결과 |

공식 Console 문서에서 임의 JSON 시나리오 재생 REST API를 확인하지 않았다.
존재 여부를 확인하지 않은 자동화 API나 주행 시나리오 실행기를 이번 하네스에 만들지 않았다.

## 주행 UX와 화면 검증

주행 중 기능 허용은 속도·기어·브레이크·지역·앱 카테고리의 영향을 받는다.
DO 미선언 앱은 주차 시 사용 가능, 주행 시 사용 불가로 안내된다.
[주행 규제 정책](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driving-restiction-policy)

앱은 `CarUxRestrictionsManager`의 변경 콜백과 `ActiveRestrictions`를 반영해야 한다.
기어만 보거나 DO 선언만으로 모든 기능이 허용된다고 처리하지 않는다.
[주행 규제 구현](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/driving-restriction-development-guide)

주행 시 짧게 이해할 수 있는 정보·적은 조작·큰 터치 영역·높은 대비를 고려하고 영상·주의를 끄는 동작을 제한한다.
[주행 안전 설계](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driver-distraction-guideline)

폼 팩터 검증은 별도 가상 디스플레이와 FormFactorConsole을 사용한다.
가이드에는 Go 1.22 이상·Host Display Bridge·8095/8096 포트가 필요하다고 안내된다.
별도 Emulator 창에서 실행해야 하며 Android Studio 내장 창이나 headless 모드는 사용하지 않도록 안내한다.
가로·세로·화면 크기별 UI를 검사한다. Go 설치나 Bridge 실행은 이번 범위에 포함하지 않았다.
[폼 팩터 테스트](https://document.pleos.ai/docs/pleos-only/app-test-guide/form-factor-test-guide)

## 팀이 다음에 확인할 것

실제 앱 구현을 시작할 때 앱 요구·SDK·설치 버전·Workspace 제공 상태를 확인하고 [API 확인 양식](../templates/api-contract.md)에 기록한다.
현재 실제 SDK 호출과 Emulator 시나리오 검증 상태는 **NOT_RUN**이다.
