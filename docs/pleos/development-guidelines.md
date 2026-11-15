# 개발·연동 가이드라인

확인일: 2026-11-06 KST. 공식 앱 개발 가이드([D-01])와 SDK 사용 가이드를 구현 절차로 옮겼다. API 세부는 [sdk-api-reference.md](sdk-api-reference.md), 주행 규제는 [driving-safety.md](driving-safety.md)를 본다. 코드 예시는 공식 문서의 흐름을 줄여 보인 것이며 그대로 복사하지 않는다.

## 1. 프로젝트 설정

| 항목 | 기준 | 현재 저장소 | 요구사항 |
| --- | --- | --- | --- |
| minSdk | API 28 이상 필수 | 28 | SDK-007 |
| Gradle | 8.0 이상 | Wrapper 9.6.0 | SDK-008 |
| Pleos Maven | settings에 `https://nexus-playground.pleos.ai/repository/maven-releases/` | 없음(의존성 없음) | SDK-006 |
| SDK 좌표 | `ai.pleos.playground:<SDK>:<버전>` | 없음 | SDK-006 |
| 버전 고정 | 문서 기준 버전과 다르면 [docs/decisions.md](../decisions.md)에 근거 | 미정 | SDK-006 |

## 2. AndroidManifest 체크리스트

`scripts/pleos/check-compliance.sh`가 대부분을 정적으로 확인한다(M 검사).

| 항목 | 규칙 | 검사 |
| --- | --- | --- |
| AAOS 기능 | `android.hardware.type.automotive` required(프로젝트 기준) | M01 |
| Pleos 퍼미션 | 쓰는 SDK·도메인 퍼미션만. 승인 범위 내에서만 | M02~M05 |
| DO | 제한 상태에서 실행할 Activity에만 `distractionOptimized=true`. `<application>`이 아니라 `<activity>`에 | M06 |
| 화면 방향 | Portrait·Landscape 모두 지원. 고정하지 않음 | M07 |
| CCP | 미지원이면 `<application>`에 `com.hmg.rotary.ROTARY_SUPPORTED=false` | M08 |
| exported | LAUNCHER·미디어 서비스·Gleo function 외 노출 금지 | M09 |
| debuggable | Manifest에 하드코딩 금지 | M10 |
| 평문 통신 | `usesCleartextTraffic=true` 금지 | M11 |
| 백업 | 쓰지 않으면 `allowBackup=false` 또는 민감 데이터 제외 규칙 | M12 |
| 아이콘 | Adaptive Icon `android:icon` | M13 |
| 카테고리 | Music/Audio `android:appCategory="audio"`, Maps `APP_MAPS` intent category | M14~M16 |
| Gleo | `assets/schema.json`과 function별 intent-filter | M17 |

## 3. 주행 규제 연동

공식 흐름([D-04]):

```kotlin
// 개념 예시. 실제 클래스·생명주기 위치는 architecture.md를 따른다.
uxRestrictionsManager.registerListener { restrictions ->
    val active = restrictions.activeRestrictions
    if (restrictions.isRequiresDistractionOptimization &&
        active and CarUxRestrictions.UX_RESTRICTIONS_NO_VIDEO != 0) {
        stopPlayingVideo()
    }
}
```

- 기능별 플래그는 [driving-safety.md](driving-safety.md)의 표를 따른다. 지역별 값은 플랫폼이 내려준다.
- 등록 전·연결 실패·리스너 해제 후에는 제한 상태로 둔다(AGENTS.md 규칙).
- 진행 중 재생·녹음·공개 확인에도 즉시 반영한다.
- CarUxRestrictionsManager 획득 방법(`Car.createCar` 등)과 필요한 Android 권한은 Android Developers 원문으로 확인한다(X-10). 문서에서 확인하지 않은 생성 코드를 하네스에 적지 않는다.

## 4. 오디오 포커스

### 앱이 구현할 것

1. 스트림마다 재생 전에 `AudioAttributes` + `AudioFocusRequest`로 요청한다(AUDIO-002).
2. 다른 스트림과 공존(C)을 기대하면 `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`(AUDIO-003).
3. 포커스 변경을 반드시 처리한다(AUDIO-004).

| 이벤트 | 권장 처리 |
| --- | --- |
| `AUDIOFOCUS_GAIN` | 정상 재생 |
| `AUDIOFOCUS_LOSS` | 즉시 중단·정지 |
| `AUDIOFOCUS_LOSS_TRANSIENT` | 일시정지 후 복귀 가능. Gleo 호출이면 즉시 멈추고 필요 시 abandon(AUDIO-006) |
| `AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK` | 볼륨 감쇄, 해제 조건에서 원복(AUDIO-005) |

Music/Audio 가이드의 예시는 `LOSS_TRANSIENT`와 `LOSS_TRANSIENT_CAN_DUCK`을 모두 일시정지로 처리한다([D-07]). duck 중 pause는 C 공존을 깰 수 있으므로([D-02]) 스트림 성격에 맞게 정하고 결정 기록에 남긴다.

### Pleos Connect Interaction Matrix v1.0 (2026-01-28)

[D-02]의 이미지를 옮겼다. 행은 현재 포커스 보유자, 열은 새 요청이다. E는 새 요청이 포커스를 얻고 기존 보유자는 잃음, C는 공존(보통 duck), R은 새 요청 거부다. 보유자가 여럿이면 R > E > C로 가장 보수적인 결과가 적용된다.

| 현재＼요청 | MUSIC | NAV | VOICE_CMD | CALL_RING | CALL | ALARM | NOTIF | SYS_SOUND | EMERG | SAFETY | VEH_STATUS | ANNOUNCE |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| MUSIC | E | C | E | E | E | C | C | C | E | C | C | C |
| NAVIGATION | C | C | E | C | C | C | C | C | E | C | C | C |
| VOICE_COMMAND | R | R | E | E | E | R | R | R | E | R | C | E |
| CALL_RING | R | C | C | C | C | R | R | C | E | C | C | R |
| CALL | R | C | R | C | C | C | C | C | E | C | C | R |
| ALARM | C | C | C | E | E | C | C | C | E | R | C | C |
| NOTIFICATION | C | C | E | C | C | C | C | C | E | C | C | C |
| SYSTEM_SOUND | C | C | C | C | C | C | C | C | E | C | C | C |
| EMERGENCY | R | R | R | R | R | R | R | R | C | R | R | R |
| SAFETY | C | C | E | C | C | E | C | C | E | C | C | C |
| VEHICLE_STATUS | C | C | C | C | C | C | C | C | E | C | C | C |
| ANNOUNCEMENT | C | C | E | E | E | C | C | C | E | C | C | E |

정책 변경에 대비해 매트릭스를 하드코딩하지 말고 설정으로 두라는 권장과 로그 필수 항목은 포커스를 중재하는 주체 기준이다(C-07, AUDIO-008). 3rd party 앱은 자기 스트림의 요청·결과·duck 이벤트를 로그로 남기는 정도로 적용한다.

CarAudioContext와 AttributeUsage 대응([D-02]):

| CarAudioContext | AttributeUsages |
| --- | --- |
| MUSIC | MUSIC, UNKNOWN, GAME, MEDIA |
| NAVIGATION | NAVIGATION, ASSISTANCE_NAVIGATION_GUIDANCE |
| VOICE_COMMAND | VOICE_COMMAND, ASSISTANT |
| CALL_RING | CALL_RING, NOTIFICATION_RINGTONE |
| CALL | CALL, VOICE_COMMUNICATION, VOICE_COMMUNICATION_SIGNALING |
| ALARM | ALARM |
| NOTIFICATION | NOTIFICATION, NOTIFICATION_* |
| SYSTEM_SOUND | SYSTEM_SOUND, ASSISTANCE_SONIFICATION, ASSISTANCE_ACCESSIBILITY |
| EMERGENCY · SAFETY · VEHICLE_STATUS · ANNOUNCEMENT | 같은 이름의 usage. 시스템 컨텍스트는 Android 11 이상 |

ourroad 낭독이 어떤 usage로 재생되는지는 Pleos TTS SDK 문서에 없다(X-08). 앱이 직접 재생하는 경우 usage 선택과 매트릭스상의 결과(예: MUSIC 중 NAVIGATION은 C, CALL 중 MUSIC은 R)를 결정 기록에 남긴다.

### 필수 검증 체크리스트(AUDIO-009)

1. MUSIC 재생 중 NAVIGATION 요청 → C(음악 duck, 내비 재생)
2. CALL 중 MUSIC 요청 → R(요청 실패)
3. MUSIC 중 CALL 요청 → E(통화가 음악 선점)
4. EMERGENCY 재생 중 MUSIC·NOTIFICATION 요청 → R
5. MUSIC + NAVIGATION 동시 보유 중 VOICE_COMMAND 요청 → 보유자별 결과 중 보수적 결과

## 5. Suspend·Resume

[D-03] 기준. Suspend는 시동 OFF 후 저전력 유지 상태이며 언제든 중단·재시작될 수 있다(LIFECYCLE-007).

| 시점 | 순서 |
| --- | --- |
| 진입 | 새 작업 시작 금지 → 재생 즉시 중단 → 포커스 abandon → 재생 상태(여부·위치·콘텐츠) 저장 → player·track·타이머·센서·리스너 정리 |
| Resume | 상태 복원 → 현재 시스템·사용자 조건 재평가 → 필요할 때만 포커스 재요청. 자동 재생은 기본 false(OEM·사용자 정책) |
| 금지 | Suspend 중 포커스 유지, 백그라운드 재생, Resume 대비 포커스 선점 |

진입을 감지하는 API는 문서에 없다(X-07). 감지 수단을 확인하기 전에는 Suspend 대응이 구현됐다고 쓰지 않는다. ourroad의 "다음 주행" 판정은 앱 재시작이나 Resume만으로 하지 않는다(AGENTS.md).

## 6. 음성 안내 선행 묵음

[D-08] 기준(AUDIO-013·014).

- 음성 안내 시작 전 150~200 ms 묵음. 150 ms 미만은 첫 음절 손실 가능, 200 ms 초과는 체감 지연.
- 우선순위: PCM·WAV 데이터 앞 silence padding → TTS 결과 후처리 → 재생 지연(비권장).
- 한 발화 단위의 연속 음성에는 추가 묵음을 넣지 않는다. 스트림이 끊기거나 새 세션이면 다시 적용한다.
- 출력 경로 초기화, 포커스·세션 확보, 플레이어 준비를 먼저 확인한다. 묵음은 초기화 문제를 대체하지 않는다.
- Pleos TTS SDK는 Speech Service가 직접 재생하므로 앱이 묵음을 넣을 수 있는지 확인이 필요하다(X-08).

## 7. 생명주기·Back

| 상황 | 규칙 | 요구사항 |
| --- | --- | --- |
| 화면에서 사라짐 | 종료가 아니라 백그라운드일 수 있음. 다시 foreground 될 수 있음 | LIFECYCLE-001 |
| 백그라운드 전환 | 화면 갱신·애니메이션·불필요 센서 수집 중단 | LIFECYCLE-003 |
| 복귀 | 마지막 상태 복원, 크기·방향에 맞춰 UI 재구성(수 시간 미사용 예외) | LIFECYCLE-002 |
| 프로세스 재생성 | 사용자 데이터·진행 상태 복원 | LIFECYCLE-006 |
| window 크기·방향 변경 | 상태 손실·화면 이상 없음, 기능 패리티 | UX-007 |
| 최상단에서 CCP Back | `moveTaskToBack(true)`로 숨김. `finish()`·`finishAffinity()` 금지 | CCP-003 |
| 하위 화면·팝업에서 Back | 이전 화면 이동 또는 팝업 종료 | CCP-003 |

CCP Back이 Android 표준 Back 이벤트로 전달되는지 먼저 Emulator에서 확인하고, 표준 이벤트라면 `OnBackInvokedDispatcher` 등 플랫폼이 지원하는 Back API를 쓴다([D-05]).

## 8. IME

- EditText·TextField가 있는 window: IME가 view를 밀어 올리는 것이 사양이다.
- 없는 window·Dialog·overlay: insets 적용 대상을 `statusBars() or navigationBars()`로 한정해 밀려 올라가지 않게 한다(`setFitInsetsTypes`). IME 외 insets는 `OnApplyWindowInsetsListener`에서 처리한다(IME-001·002).

## 9. 카테고리별 개발

| 카테고리 | 핵심 규칙 | 요구사항 |
| --- | --- | --- |
| Music/Audio | 오디오 재생이 주 기능. MediaBrowserService·MediaSession(Media3 권장), `appCategory="audio"`, `androidx.car.app.launchable`, 하위 카테고리, 스티어링 휠 키, 자동 재생 금지, LOSS 시 세션 비활성, 서비스 수명주기 | MEDIA-001~010 |
| Maps/Navigation | `APP_MAPS`, 단일 경로·CarAppFocusManager, 사용자 동의 없는 경로 전환 금지, 주행 중 조작 금지 항목, 최소 권한·위치 암호화 | NAV-001~006 |
| 그 외 | 선언 방법 미확인(X-13). 카테고리-기능 일치 | CATEGORY-001~004 |

ourroad는 카테고리를 결정하기 전이다. Music/Audio를 택하면 MBS·MediaSession·하위 카테고리(라디오·팟캐스트 등)와 스티어링 휠 키 지원이 필수 범위로 들어오며, 대화·공개 확인 같은 기능이 "오디오 재생이 주 기능" 기준과 맞는지 심사 위험을 함께 검토한다.

## 10. Gleo Interface

1. `assets/schema.json` 작성(최대 3개 function, 대문자·밑줄 이름, 영어 description, 단일 parameter type).
2. function마다 intent-filter(action = function name, category DEFAULT).
3. `ACTION_PARAM` JSON을 파싱해 실행하고 7초 안에 `APP_ACTION_RESPONSE` 브로드캐스트로 응답.
4. 앱이 직접 TTS·재생하면 `mode: silent` 또는 `beep`.
5. 노출되는 수신 컴포넌트는 입력을 검증한다(C-04, SECURITY-020).
6. Gleo 호출 시 앱 오디오는 `AUDIOFOCUS_LOSS_TRANSIENT`를 받는다(AUDIO-006).

## 11. 디버깅·ADB

[D-11]의 명령이다. 여러 기기가 연결되면 `adb -s <SERIAL>`로 대상을 지정한다. 같은 AVD를 다른 팀원이나 AI가 쓰고 있지 않은지 먼저 확인한다.

| 목적 | 명령 |
| --- | --- |
| 기기 확인 | `adb devices` |
| 설치 | `adb install -d -r <apk>` (실패 시 `adb push` 후 `adb shell pm install -d -r /data/local/tmp/<apk>`) |
| 실행 | `adb shell monkey -p <패키지> -c android.intent.category.LAUNCHER 1` 또는 `adb shell am start -n <패키지>/<Activity>` |
| 종료·삭제 | `adb shell am force-stop <패키지>`, `adb uninstall <패키지>`, `adb shell pm clear <패키지>` |
| 텍스트 입력 | `adb shell input text "<텍스트>"` |
| 주행 규제 ON/OFF | `adb shell cmd hmg_car_service --service drivingstate driving_regulation on` / `off` |
| 화면 캡처 | `adb exec-out screencap -p > <파일>.png` |
| 화면 녹화 | `adb exec-out screenrecord --output-format=h264 - > <파일>.h264` 후 ffmpeg로 mp4 변환 |

주의:

- CCS 연결 명령(`hmg_is_ccs_account_linked`, `hmg_provisioning_status`)은 CCS 차량 전용이며 Devbox·Emulator에서 실행하지 않는다.
- 유틸리티 모드 명령(`/dev/vcs_simulator_rx` 쓰기)은 차속 0·주차 브레이크 On·P단을 먼저 만든 뒤에만 쓴다. 공용 AVD 상태를 바꾸므로 사전에 공유한다.
- 화면 캡처·녹화 경로는 개인 경로를 공통 문서·설정에 고정하지 않는다(AGENTS.md).
- `Build.DEVICE`는 에뮬레이터에서 유효하지 않고, Emulator 코덱 지원은 실차와 다르다([D-10]).

## 12. 금지 패턴

| 패턴 | 이유 | 요구사항 |
| --- | --- | --- |
| 기어·속도 값만으로 주행 UI·음성 허용 | 기준은 CarUxRestrictions | SAFETY-001, AGENTS.md |
| DO 선언 후 플래그 미처리 | DO는 허가가 아님 | SAFETY-003 |
| 카테고리 예시를 허용 근거로 사용 | 예시일 뿐 | CATEGORY-004 |
| 주행 중 애니메이션·자동 스크롤로 시선 유도 | 금지 | SAFETY-008 |
| 차량 센서·디바이스 파일 직접 접근 | 금지 | HW-001 |
| 차량 모델명 분기, 에뮬레이터에서 `Build.DEVICE` 분기 | 실제 지원 확인 필요 | HW-005, 007 |
| capability·`isSupported()` 없이 SDK 호출 | 차종별 지원 상이 | SDK-002, 003 |
| 콜백 완료 전 `release()`, 리스너 미해제 | 누수·오류 | SDK-009, 013, 021 |
| null 위치를 유효 위치로 사용 | 가짜 경로 | SDK-022 |
| 포커스 요청 없이 재생, 포커스 이벤트 무시 | 정책 위반 | AUDIO-002, 004 |
| Suspend 중 포커스 유지·재생, Resume 자동 재생 | 정책 위반 | AUDIO-010~012 |
| 최상단 Back에서 `finish()` | 심사 반려 대상 | CCP-003 |
| 미승인 Pleos 퍼미션 선언 | 심사 불가 | SDK-005 |
| Client Secret·키 하드코딩, URL에 토큰 | 보안 위반 | SECURITY-012, 014, 023 |
| 제출 빌드에 데모·더미 데이터 | 심사 기준 위반 | PERF-008 |
| 문서에 없는 API·퍼미션 사용 | 추측 금지 | SDK-025 |

[D-01]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/intro
[D-02]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/audio-focus-development-guide/audio-focus-development
[D-03]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/audio-focus-development-guide/suspend-mode-audio-focus
[D-04]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/driving-restriction-development-guide
[D-05]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/ccp-rotary-controller-development
[D-07]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/music-audio-app-developement-guide
[D-08]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/voice-guidance-preroll-silence-guide
[D-10]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/vehicle-app-faq
[D-11]: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/adb-command-reference-guide
