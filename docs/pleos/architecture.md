# Pleos 정책에 맞춘 앱 구조

기준: 2026-11-06 KST. 이 문서는 **책임 경계**를 정한다. 모듈 분리, DI 프레임워크, 새 의존성은 정하지 않는다. AGENTS.md는 "미정 아키텍처나 의존성을 선제 도입하지 않는다"고 하므로, 아래 구조는 첫 SDK 연동 작업에서 필요한 만큼만 코드로 옮긴다.

## 로컬 UI 목업의 구조

| 위치 | 역할 | 계층 |
| --- | --- | --- |
2026-11-15 기준 1차 기획 목업이다(Jetpack Compose).

| 위치 | 역할 | 계층 |
| --- | --- | --- |
| `app/src/main/java/.../MainActivity.kt` | 단일 `ComponentActivity`. 프레임 시계로 `Tick`을 보내 대화 시간을 흘리고, Back은 대화를 멈춘다. 연결·시연 설정을 `rememberSaveable`로 복원 | Presentation |
| `.../CarScreen.kt` | 차량 화면(다드리·상태 한 줄·대답 버튼)과 발표용 시연 제어 | Presentation |
| `.../OurroadTheme.kt` | 색·서체(Pretendard)·자간 토큰 | Presentation |
| `.../DadeuriView.kt` | AGSL 셰이더 다드리. IDLE·LISTENING·THINKING·SPEAKING, `still`이면 정지. API 33 미만은 정적 그라데이션 | Presentation |
| `.../DadeuriFlow.kt` | 대화 상태기계(Android 타입 없음). 단위 테스트 대상 | Domain |
| `.../DadeuriScript.kt` | 예시 대사·사연·발화 | Data(목업) |
| 차량·SDK·오디오·저장·네트워크 | 없음. 시연 제어 값으로 흉내 냄 | - |

차량 상태·음성·서버 판단을 받는 경로가 없다. SDK를 붙일 때 `DadeuriFlow`의 이벤트를 어댑터가 보내도록 바꾼다.

## 공식 정책이 구조에 요구하는 것

| 정책 | 구조적 의미 | 요구사항 |
| --- | --- | --- |
| 차량 하드웨어 직접 접근 금지, 표준 플랫폼 API만 | 차량 접근은 한 계층(Vehicle Integration)에 모으고 UI가 SDK를 직접 부르지 않는다 | HW-001·002 |
| 차종별 지원 상이, capability·isSupported 확인 | 기능은 "지원됨/미지원/권한 없음/제한/실패"를 구분해 받는다 | SDK-002·003, HW-005 |
| CarUxRestrictions를 기준으로 기능별 제한, 진행 중 기능에도 반영 | 주행 규제 상태의 단일 출처가 있고 화면·재생·녹음이 같은 값을 구독한다 | SAFETY-001~005 |
| 오디오 포커스 필수 처리, Suspend 시 반납 | 재생 주체마다 포커스 상태 기계가 있고 수명주기 이벤트를 받는다 | AUDIO-001~012 |
| 포그라운드·백그라운드 빈번 전환, 프로세스 재생성 | 화면 상태는 저장·복원 가능해야 하고 백그라운드 작업은 실행 조건이 명시된다 | LIFECYCLE-001~006, UX-007 |
| 네트워크 불안정 | 원격 데이터는 캐시·재시도·오프라인 상태를 가진다 | HW-004 |
| 민감정보 최소 수집·암호화 | 동의 전 수집 차단과 저장 암호화는 Data 계층 경계에서 강제한다 | SECURITY-003·007, AGENTS.md |

## 계층과 책임

```text
Presentation ──▶ Domain ◀── Data
     │             ▲          │
     │             │          ▼
     │      Driving Restriction State ◀── Vehicle Integration ──▶ SDK Adapters ──▶ Pleos SDK / AAOS
     └────────────(구독)────────┘
```

화살표는 의존 방향이다. Domain은 Android·Pleos 타입을 모른다. SDK 타입은 SDK Adapter 밖으로 나가지 않는다.

| 계층 | 책임 | 하지 않는 것 |
| --- | --- | --- |
| Presentation | 화면 렌더링, 사용자 입력, 화면 상태 저장·복원, Portrait·Landscape 레이아웃. 주행 규제 상태를 구독해 기능을 숨기거나 비활성화 | SDK 직접 호출, 제한 여부 판단 로직 보유 |
| Domain | ourroad 규칙(동의·시작 제안 조건·공개 확인·응답 전달 시점)과 기능 게이트 판정. 순수 Kotlin으로 단위 테스트 | Android·Pleos 클래스 참조 |
| Data | 이야기·응답·동의 상태·개인 기록의 저장소와 원격 API. 동의 전 수집 차단, 민감정보 암호화, 캐시·재시도 | 차량 신호 해석 |
| Vehicle Integration | 차량·플랫폼 신호를 도메인 값으로 변환: 주행 규제, 위치, 탑승·브레이크 등 차량 상태, 내비 상태, 오디오 포커스, 수명주기·Suspend | UI 결정 |
| SDK Adapter | SDK별 1개. 초기화·release, capability·isSupported, 리스너 등록·해제, 오류 변환, 스레드 정리. Fake로 대체 가능 | 정책 판단 |
| Driving Restriction State Management | CarUxRestrictions와 연결 상태로 기능별 허용 여부를 계산하는 단일 출처 | 지역 표를 앱이 직접 해석해 제한 해제 |

## 주행 규제 상태 관리

입력과 출력을 분리한다.

| 입력 | 출처 | 비고 |
| --- | --- | --- |
| `isRequiresDistractionOptimization()`, `getActiveRestrictions()` | CarUxRestrictionsManager | 기준값 |
| 연결 상태 | 어댑터 | 등록 전·연결 실패·해제 후는 "알 수 없음" |
| 보조 차량 신호(기어·브레이크·탑승) | Vehicle SDK·Virtual Console | 제한 해제 근거로 쓰지 않음 |

출력은 기능 게이트다. 플래그 대응은 [driving-safety.md](driving-safety.md)를 따른다.

| 게이트 | 제한 조건 | 근거 |
| --- | --- | --- |
| 영상 | `NO_VIDEO` | SAFETY-007 |
| 키보드 입력 | `NO_KEYBOARD` | SAFETY-018 |
| 검색·필터 | `NO_KEYBOARD`, `NO_FILTERING` | SAFETY-018 |
| 설정·로그인 | `NO_SETUP` | SAFETY-018 |
| 장문·상세 화면 | `LIMIT_CONTENT`, `LIMIT_STRING_LENGTH` | SAFETY-018 |
| 장식·상태 애니메이션 | 제한 상태 전체(정책 결정 필요) | SAFETY-008·009 |
| 음성 낭독·음성 입력·공개 음성 확인 | 주행 중 음성 흐름은 기획상 계획(README, D-013). 공식 허용 근거 확인 전에는 제한 상태에서 시작하지 않음 | X-08, X-09, 로드맵 D6 |

규칙:

- 상태가 "알 수 없음"이면 모든 게이트를 제한으로 둔다(AGENTS.md).
- 게이트가 바뀌면 진행 중인 재생·녹음·공개 확인을 Domain 규칙에 따라 멈추거나 보류한다. 무응답·인식 실패를 승인으로 바꾸지 않는다.
- 이 계산은 순수 함수로 두어 UNIT으로 검증한다(플래그 조합 → 게이트).

## SDK Adapter 계약

모든 어댑터는 같은 결과 범주로 도메인에 알린다.

| 범주 | Vehicle | TTS·STT | FusedLocation | NaviHelper |
| --- | --- | --- | --- | --- |
| 지원 안 됨 | capability NONE, ERROR_NOT_SUPPORTED_* | NOT_SUPPORTED_API, UNAVAILABLE_LANGUAGE | `isSupported()` false | 초기 연결 실패, 내비 미탑재(X-05) |
| 권한 없음 | ERROR_SECURITY_PERMISSION | PERMISSION_DENIED, UNAUTHENTICATED | 런타임 권한 거부, 위치 접근 차단 | 퍼미션 누락 |
| 주행 제한 | ERROR_DRIVING_RESTRICTED | - | - | - |
| 일시 실패 | ERROR_SERVICE_CONNECTION_FAILURE, ERROR_INTERNAL_SERVICE | NETWORK_ERROR, TIMEOUT, SERVER_INTERNAL_ERROR | onError(Exception) | onError(NaviErrorCode) |
| 값 없음 | ERROR_EMPTY_RESULT, null | - | null LocationData | 이벤트 미수신 |
| 알 수 없음 | ERROR_UNDEFINED, 비 VehicleException | UNKNOWN, 새 ErrorType | 기타 Exception | - |

어댑터 규칙:

1. 수명주기 소유자를 하나로 정한다. FusedLocation 문서는 Application 범위 인스턴스 예시를 든다. 화면 회전으로 SDK를 다시 초기화하지 않는다.
2. `initialize()` 실패와 이후 호출 실패를 구분한다. release 후 같은 객체를 재사용하지 않는다(TTS·STT·FusedLocation 문서).
3. 등록한 리스너는 같은 인스턴스로 해제한다. 비동기 콜백이 끝나기 전에 release하지 않는다(Vehicle).
4. 콜백 스레드는 문서에 없으므로(UNVERIFIED) 어댑터가 UI 스레드 전달을 책임진다.
5. 어댑터마다 Fake를 두어 Domain·Presentation을 SDK 없이 테스트한다. Fake·Mock 결과를 실제 SDK 검증으로 기록하지 않는다.

## 오디오와 수명주기

- 재생 주체(앱이 직접 재생하는 플레이어 또는 Pleos TTS)마다 포커스 상태(없음·보유·일시 상실·duck·영구 상실)를 관리한다. Pleos TTS가 포커스를 스스로 요청하는지는 X-08이므로 EMU로 확인한 뒤 앱 측 요청 여부를 정한다.
- 포커스 상실·통화·내비 안내 후 복귀는 Domain 규칙(중복 재생·녹음·게시 금지)과 함께 처리한다.
- Suspend 진입·Resume을 받을 수단이 확인되면(X-07) Vehicle Integration에서 이벤트로 바꿔 오디오 주체가 [development-guidelines.md](development-guidelines.md) 5절 순서를 따르게 한다. 앱 프로세스 재시작은 "다음 주행"이 아니다.
- 화면 상태(현재 화면, 진행 중 단계)는 저장·복원한다. 현재 `MainActivity.screen`은 저장되지 않는다(UX-007 NON_COMPLIANT).

## 데이터 경계

| 데이터 | 경계 규칙 | 근거 |
| --- | --- | --- |
| 위치·주행 구간 | 동의 전 수집·저장·전송 없음. 원시 경로·정확한 시각은 공개 데이터와 분리 | AGENTS.md, SECURITY-003·004 |
| 음성·전사 | 비공개 대화와 공개 이야기 분리. 공개는 명시적 음성 확인 후 | AGENTS.md |
| 토큰·자격 증명 | Keystore. 저장소에 넣지 않음 | SECURITY-002·014·023 |
| 로그 | PII 마스킹, 출시 전 디버그 로그 정리 | SECURITY-004·008 |
| Telemetry | 차량·사용자 ID 가명화 정책 | SECURITY-005 |

보관 기간·해상도·민감 구간 처리는 아직 정하지 않은 결정이다. 이 문서에서 값을 정하지 않는다.

## 적용 순서 제안

1. 첫 SDK 작업에서 `:app` 안에 패키지로만 경계를 만든다(예: `ui`, `domain`, `data`, `vehicle`). 모듈 분리는 빌드·테스트 필요가 생길 때 결정한다.
2. 주행 규제 상태 관리와 게이트 계산을 먼저 만든다. 차량 기능보다 앞선다([implementation-roadmap.md](implementation-roadmap.md) 1단계).
3. SDK 어댑터는 실제로 쓰는 SDK만 추가한다. 쓰지 않는 SDK 의존성과 퍼미션을 미리 넣지 않는다(SECURITY-017).
4. 패키지 구조나 상태 관리 방식(ViewModel 등)을 정하면 [docs/decisions.md](../decisions.md)에 기록한다.

## 2026-11-09 프로토타입 기록

2026-11-09에 ParkedAccess(AAOS Car)·VoiceSession·AndroidVoice(기기 내 STT·TTS) 프로토타입을 만들었으나 2026-11-15에 1차 기획 목업으로 대체하며 제거했다. 조사 내용은 [음성 기획](../ourroad-voice.md)과 [작업 011](../../tasks/011-ourroad-voice.md)에 남아 있다.
