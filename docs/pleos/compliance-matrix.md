# Pleos 요구사항 준수 매트릭스

기준: 2026-11-15 KST, ourroad 차량 1차 기획 목업(다드리 시나리오 1) 작업 트리. 변경 근거와 실행 결과는 [작업 011](../../tasks/011-ourroad-voice.md)에 있다. 요구사항 원문은 [requirements.md](requirements.md)에 있다.

## 상태 값

| 상태 | 의미 |
| --- | --- |
| COMPLIANT | 17 |
| NON_COMPLIANT | 1 |
| PARTIAL | 8 |
| NOT_IMPLEMENTED | 13 |
| NOT_APPLICABLE | 100 |
| UNVERIFIED | 31 |

이 값은 요구사항의 **현재 상태**다. 검사 실행 결과(PASS·FAIL·NOT_RUN·BLOCKED·NOT_APPLICABLE)는 [testing-strategy.md](testing-strategy.md)와 작업 문서에 따로 기록한다. 자동 검사 PASS만으로 COMPLIANT를 쓰지 않는다. Verification 열의 `Mxx`·`Gxx`·`Cxx`는 `scripts/pleos/check-compliance.sh`의 검사 ID다.

## PR 단계별 확인

- 1차 기획 목업이다. 음성·차량 신호·서버 판단은 예시 대사와 시연 제어로 흉내 내며 SDK를 호출하지 않는다. 이전 Android 음성 프로토타입(2026-11-09)은 2026-11-15에 제거했다.

## 전체 UI 스냅샷의 근거

| 근거 | 결과 |
| --- | --- |
| 저장소 조사 | 단일 Kotlin :app, Jetpack Compose. `MainActivity` → `CarScreen`(차량 화면·시연 제어), `DadeuriFlow`(순수 Kotlin 대화 상태기계), `DadeuriScript`(예시 대사), `DadeuriView`(AGSL 셰이더). 네트워크·DB·경로 수집·소리 출력 없음 |
| Manifest | automotive required, LAUNCHER 1개(exported), 권한 없음, allowBackup=false. DO·카테고리·아이콘 없음 |
| Gradle | AGP 9.4.1(내장 Kotlin 2.2.10), Gradle Wrapper 9.6.0, compileSdk 37, minSdk 28, targetSdk 34. Compose 컴파일러 2.2.10, Compose BOM 2026.09.00, activity-compose 1.12.0, JUnit 4.13.2(test only) |
| 빌드 | 2026-11-15 macOS arm64·Android Studio JBR, assembleDebug·testDebugUnitTest·lintDebug PASS. Lint 오류 0·경고 6, 단위 테스트 11개. check-compliance PASS(M13 MINOR, M14 BLOCKED) |
| Emulator·실차 | Pleos 2.1.0 AVD에 설치·첫 화면 표시만 확인. 흐름 조작 EMU NOT_RUN. 실차 NOT_RUN |

## 요약

| 상태 | 개수 |
| --- | --- |
| COMPLIANT | 20 |
| NON_COMPLIANT | 0 |
| PARTIAL | 26 |
| NOT_IMPLEMENTED | 8 |
| NOT_APPLICABLE | 83 |
| UNVERIFIED | 33 |
| 합계 | 170 |

### 주요 위반과 미구현

| 항목 | 분류 | 내용 |
| --- | --- | --- |
| SAFETY-001·002·005·018 | NOT_IMPLEMENTED (BLOCKER 후보) | CarUxRestrictions 연동이 없다. 주행은 시연 토글이다. DO 미선언이라 실제 주행 규제에서는 플랫폼이 앱을 막는다. 주행 중 음성 경험을 실제로 넣기 전에 반드시 구현 |
| SAFETY-008·009 | NOT_APPLICABLE (DO 선언 시 BLOCKER 후보) | `DadeuriView`는 표시 중 계속 애니메이션한다. 시연 주행 토글에서는 멈추지만 실제 규제 신호와 연결되지 않았다 |
| UX-011 | NON_COMPLIANT (MINOR) | 목업 대사·문구가 Kotlin에 직접 들어 있다 |
| UX-007, LIFECYCLE-002 | PARTIAL | 연결·시연 설정 복원, 대화 자동 재개 없음. EMU 재생성 NOT_RUN |
| CATEGORY-001·002 | NOT_IMPLEMENTED (MAJOR) | 카테고리 미정. 주행 중 허용 범위와 MEDIA 조건부 요구사항이 이 결정에 달려 있다 |
| DESIGN-005 | NOT_IMPLEMENTED (MINOR) | `android:icon` 없음(Lint MissingApplicationIcon) |

## SAFETY

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-SAFETY-001 | [P-02] [D-04] | `MainActivity`·`CarScreen` | NOT_IMPLEMENTED | 1차 목업은 CarUxRestrictions를 구독하지 않는다. 주행 여부는 시연 제어 토글. C01, EMU 예정 |
| PLEOS-SAFETY-002 | [D-04] | - | NOT_IMPLEMENTED | CarUxRestrictions 어댑터 없음. 이전 프로토타입은 2026-11-15 제거 |
| PLEOS-SAFETY-003 | [D-04] | DO 선언 Activity | NOT_APPLICABLE | DO 미선언. C01 |
| PLEOS-SAFETY-004 | [P-03] [L-07] | `AndroidManifest.xml` | NOT_IMPLEMENTED | M06. 제품 목표상 필요, 선언 범위 결정 전 |
| PLEOS-SAFETY-005 | [D-04] | - | NOT_IMPLEMENTED | 주행 규제 상태 관리 없음 |
| PLEOS-SAFETY-006 | [D-10] | 미정 | UNVERIFIED | C-01. 지역 분기 필요 여부 미정 |
| PLEOS-SAFETY-007 | [P-02] [D-04] | - | NOT_APPLICABLE | 영상 기능 없음 |
| PLEOS-SAFETY-008 | [UI-01] | `DadeuriView.kt` | NOT_APPLICABLE | DO 미선언. 시연 주행 토글 시 움직임 정지(still). 선언 시 C16, EMU |
| PLEOS-SAFETY-009 | [P-02] | `DadeuriView.kt` | NOT_APPLICABLE | DO 미선언. 시연 주행 토글 시 정지. 선언 시 REVIEW, EMU |
| PLEOS-SAFETY-010 | [P-02] | `CarScreen.kt` | UNVERIFIED | REVIEW 미실시 |
| PLEOS-SAFETY-011 | [UI-01] | `CarScreen.kt`, `DadeuriScript.kt` | UNVERIFIED | REVIEW 미실시. 주행 토글 시 자막·사연 정보 숨김 |
| PLEOS-SAFETY-012 | [P-02] | `CarScreen.kt` | UNVERIFIED | REVIEW 미실시 |
| PLEOS-SAFETY-013 | [P-02] [UI-12] | `CarScreen.kt`(버튼 최소 88dp) | UNVERIFIED | 기준 수치 X-04 미확인 |
| PLEOS-SAFETY-014 | [P-02] | `DadeuriFlow.kt` | NOT_IMPLEMENTED | 음성 입력은 예시 발화로 흉내 냄. STT 미연동 |
| PLEOS-SAFETY-015 | [P-02] | 기능 목록 | COMPLIANT | REVIEW(정적): 게임·브라우징·피트니스 기능 없음 |
| PLEOS-SAFETY-016 | [P-02] | - | NOT_APPLICABLE | 검색·로그인 없음 |
| PLEOS-SAFETY-017 | [UI-01] | `CarScreen.kt` | UNVERIFIED | REVIEW 미실시 |
| PLEOS-SAFETY-018 | [P-03] [D-04] | - | NOT_IMPLEMENTED | 기능-플래그 대응 없음. 목업은 시연 토글로 자막·정보만 숨김 |
| PLEOS-SAFETY-019 | [UI-01] | `DadeuriScript.kt` | UNVERIFIED | 한국어 기준 X-15. 자막은 4줄 제한 |
| PLEOS-SAFETY-020 | [UI-01] | `CarScreen.kt` | UNVERIFIED | CAR |
| PLEOS-SAFETY-021 | [UI-01] | `OurroadTheme.kt` | UNVERIFIED | 대비율 계산 미실시 |
| PLEOS-SAFETY-022 | [UI-01] | - | NOT_APPLICABLE | 알림 없음 |
| PLEOS-SAFETY-023 | [UI-01] | `DadeuriFlow.kt`·`CarScreen.kt` | PARTIAL | 버튼 시작·중단, 명시적 승인만 게시, 무응답·애매함 비게시 UNIT PASS. 실제 음성 미연동 |

## UX

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-UX-001 | [P-05] [UI-02] | `AndroidManifest.xml`, `CarScreen.kt` | PARTIAL | M07: 방향 고정 없음. 폭 1100dp 미만이면 세로 스크롤 배치. 폼 팩터 테스트 NOT_RUN |
| PLEOS-UX-002 | [P-05] | `CarScreen.kt` | UNVERIFIED | REVIEW |
| PLEOS-UX-003 | [P-05] | `CarScreen.kt` | UNVERIFIED | EMU window 크기 변경 |
| PLEOS-UX-004 | [P-05] | `AndroidManifest.xml` | PARTIAL | M07: 미지정(기본 portrait 1 window). landscape 2 window 설계 근거 미기록 |
| PLEOS-UX-005 | [P-05] [UI-02] | `CarScreen.kt` | UNVERIFIED | EMU |
| PLEOS-UX-006 | [L-04] | 앱 전체 | UNVERIFIED | 폼 팩터 테스트 |
| PLEOS-UX-007 | [L-04] | `MainActivity` | PARTIAL | rememberSaveable: 연결·시연 설정 복원. 진행 중 대화는 자동 재개하지 않음. C15 PASS, 재생성 EMU NOT_RUN |
| PLEOS-UX-008 | [L-04] | `CarScreen.kt` | UNVERIFIED | REVIEW, EMU |
| PLEOS-UX-009 | [UI-01] [UI-02] [L-06] | 앱 전체 | UNVERIFIED | 폼 팩터 테스트 |
| PLEOS-UX-010 | [UI-01] | `OurroadTheme.kt` | NOT_IMPLEMENTED | 다크 테마 없음 |
| PLEOS-UX-011 | [UI-01] | `DadeuriScript.kt`, `CarScreen.kt` | NON_COMPLIANT | MINOR(SHOULD): 목업 대사·화면 문구가 Kotlin에 한국어로 직접 들어 있음. 문자열 외부화 필요 |
| PLEOS-UX-012 | [P-05] [UI-03] | 앱 전체 | UNVERIFIED | EMU |

## IME

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-IME-001 | [D-09] | - | NOT_APPLICABLE | Dialog·overlay·IME 사용 없음 |
| PLEOS-IME-002 | [D-09] | - | NOT_APPLICABLE | 해당 화면 없음 |

## LIFECYCLE

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-LIFECYCLE-001 | [P-05] [UI-03] | `MainActivity.kt` | UNVERIFIED | EMU |
| PLEOS-LIFECYCLE-002 | [P-05] [L-04] | `MainActivity` | PARTIAL | 연결·시연 설정 복원, 대화 자동 재개 없음. 재생성 EMU NOT_RUN |
| PLEOS-LIFECYCLE-003 | [P-05] | `DadeuriView.kt`, `MainActivity.kt` | UNVERIFIED | 정적: isShown일 때만 다음 프레임 요청. 대화 시간은 프레임 시계로 흘러 화면이 안 보이면 멈춤. EMU 미확인 |
| PLEOS-LIFECYCLE-004 | [P-05] | - | NOT_APPLICABLE | 백그라운드 작업 없음 |
| PLEOS-LIFECYCLE-005 | [P-05] [P-10] | 앱 전체 | COMPLIANT | REVIEW(정적): 상주 서비스·타이머·폴링·네트워크 없음 |
| PLEOS-LIFECYCLE-006 | [P-05] [D-05] | `MainActivity` | PARTIAL | Saver로 비민감 상태만 저장. C15 PASS, EMU 재생성 NOT_RUN |
| PLEOS-LIFECYCLE-007 | [D-03] | - | NOT_IMPLEMENTED | 세션·오디오 없음. 감지 API X-07 |

## CATEGORY

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-CATEGORY-001 | [P-04] | `docs/decisions.md` | NOT_IMPLEMENTED | 결정 기록 없음 |
| PLEOS-CATEGORY-002 | [D-06] [D-07] | `AndroidManifest.xml` | NOT_IMPLEMENTED | M14 BLOCKED(카테고리 미정) |
| PLEOS-CATEGORY-003 | [D-06] [D-07] [L-04] | - | UNVERIFIED | 카테고리 미정 |
| PLEOS-CATEGORY-004 | [P-04] | - | NOT_APPLICABLE | 실제 주행 중 기능 없음(시연 토글만) |

## HW

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-HW-001 | [P-06] | `app/src/main/java/**` | COMPLIANT | C10 PASS(정적) |
| PLEOS-HW-002 | [P-06] [P-07] | - | NOT_APPLICABLE | 차량 데이터 미사용. 차량 상황은 시연 토글 |
| PLEOS-HW-003 | [P-06] | - | NOT_APPLICABLE | 모바일 전용 API 미사용 |
| PLEOS-HW-004 | [P-06] | - | NOT_APPLICABLE | 네트워크 없음 |
| PLEOS-HW-005 | [P-08] | `app/src/main/java/**` | COMPLIANT | C09 PASS(정적) |
| PLEOS-HW-006 | [P-08] | - | NOT_APPLICABLE | 특수 프로퍼티 없음 |
| PLEOS-HW-007 | [D-10] | `app/src/main/java/**` | COMPLIANT | C09 PASS(정적) |
| PLEOS-HW-008 | [D-10] | - | NOT_APPLICABLE | 차속 미사용 |
| PLEOS-HW-009 | [D-10] | - | NOT_APPLICABLE | 코덱 미사용 |

## SDK

SDK 조건부 요구사항은 SDK를 도입하기 전까지 NOT_APPLICABLE이다. 제품상 필요한 도입 순서는 [implementation-roadmap.md](implementation-roadmap.md)에 있다.

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-SDK-001 | [P-07] [S-00] | - | NOT_APPLICABLE | 차량 API 미사용 |
| PLEOS-SDK-002 | [P-08] [S-00] [S-V-cap] | - | NOT_APPLICABLE | Vehicle SDK 미도입. C02 |
| PLEOS-SDK-003 | [P-08] [S-F] | - | NOT_APPLICABLE | FusedLocation 미도입. C04 |
| PLEOS-SDK-004 | [P-08] [S-N] | - | NOT_APPLICABLE | NaviHelper 미도입 |
| PLEOS-SDK-005 | [S-00] [L-04] | `AndroidManifest.xml` | NOT_APPLICABLE | 퍼미션 없음. M02, M05 |
| PLEOS-SDK-006 | [S-V-setup] [L-01] | `settings.gradle.kts`, `app/build.gradle.kts` | NOT_APPLICABLE | Pleos SDK 의존성 없음(Compose만). G03, G04 |
| PLEOS-SDK-007 | [L-02] [L-01] | `app/build.gradle.kts` | COMPLIANT | G01 PASS, BUILD PASS |
| PLEOS-SDK-008 | [L-02] | `gradle/wrapper/gradle-wrapper.properties` | COMPLIANT | G02 PASS(9.6.0) |
| PLEOS-SDK-009 | [S-V] [S-V-obj] | - | NOT_APPLICABLE | C02 |
| PLEOS-SDK-010 | [S-V-err] | - | NOT_APPLICABLE | C02 |
| PLEOS-SDK-011 | [S-V-err] | - | NOT_APPLICABLE | UNIT 예정 |
| PLEOS-SDK-012 | [S-V-perm] | - | NOT_APPLICABLE | M02, M03 |
| PLEOS-SDK-013 | [S-V-cb] | - | NOT_APPLICABLE | C03 |
| PLEOS-SDK-014 | [S-T] [S-T-init] | - | NOT_APPLICABLE | TTS 미도입. M03, C05 |
| PLEOS-SDK-015 | [S-S] [S-S-perm] | - | NOT_APPLICABLE | STT 미도입. M03, C05 |
| PLEOS-SDK-016 | [S-S-audio] | - | NOT_APPLICABLE | C05 |
| PLEOS-SDK-017 | [S-T-err] [S-S-err] | - | NOT_APPLICABLE | UNIT 예정 |
| PLEOS-SDK-018 | [S-T-reg] [S-S] | - | NOT_APPLICABLE | C08 |
| PLEOS-SDK-019 | [S-T-locale] [S-S] | - | NOT_APPLICABLE | REVIEW |
| PLEOS-SDK-020 | [S-F-perm] | - | NOT_APPLICABLE | M03 |
| PLEOS-SDK-021 | [S-F-init] | - | NOT_APPLICABLE | C04 |
| PLEOS-SDK-022 | [S-F-data] [S-F-err] | - | NOT_APPLICABLE | UNIT 예정 |
| PLEOS-SDK-023 | [S-F-perm] | - | NOT_APPLICABLE | REVIEW |
| PLEOS-SDK-024 | [S-N-perm] [S-N-init] | - | NOT_APPLICABLE | M03 |
| PLEOS-SDK-025 | [L-03] | `app/src/main/java/**` | COMPLIANT | REVIEW(정적): 비공개·미허용 API 사용 없음 |

## AUDIO

현재 앱은 소리를 내지 않는다. 낭독·청취 화면은 무음 시뮬레이션이다.

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-AUDIO-001 | [D-02] [P-10] | - | NOT_APPLICABLE | 소리 출력 없음. 목업은 대사를 자막으로 표시 |
| PLEOS-AUDIO-002 | [D-02] | - | NOT_APPLICABLE | 소리 출력 없음. 목업은 대사를 자막으로 표시 |
| PLEOS-AUDIO-003 | [D-02] | - | NOT_APPLICABLE | 소리 출력 없음. 목업은 대사를 자막으로 표시 |
| PLEOS-AUDIO-004 | [D-02] [P-02] | - | NOT_APPLICABLE | 소리 출력 없음. 목업은 대사를 자막으로 표시 |
| PLEOS-AUDIO-005 | [D-02] | - | NOT_APPLICABLE | 소리 출력 없음. 목업은 대사를 자막으로 표시 |
| PLEOS-AUDIO-006 | [D-02] | - | NOT_APPLICABLE | 소리 출력 없음. 목업은 대사를 자막으로 표시 |
| PLEOS-AUDIO-007 | [D-02] | - | NOT_APPLICABLE | E 컨텍스트 미사용 |
| PLEOS-AUDIO-008 | [D-02] | - | NOT_APPLICABLE | 포커스 중재 주체 아님(C-07) |
| PLEOS-AUDIO-009 | [D-02] | - | NOT_APPLICABLE | 소리 출력 없음. 목업은 대사를 자막으로 표시 |
| PLEOS-AUDIO-010 | [D-03] | - | NOT_APPLICABLE | 소리 출력 없음. 목업은 대사를 자막으로 표시 |
| PLEOS-AUDIO-011 | [D-03] | - | NOT_APPLICABLE | 소리 출력 없음. 목업은 대사를 자막으로 표시 |
| PLEOS-AUDIO-012 | [D-03] | - | NOT_APPLICABLE | 소리 출력 없음. 목업은 대사를 자막으로 표시 |
| PLEOS-AUDIO-013 | [D-08] | - | NOT_APPLICABLE | 소리 출력 없음. 목업은 대사를 자막으로 표시 |
| PLEOS-AUDIO-014 | [D-08] | - | NOT_APPLICABLE | 소리 출력 없음. 목업은 대사를 자막으로 표시 |

## MEDIA

카테고리가 정해지지 않았다. Music/Audio로 정하면 전부 다시 판정한다.

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-MEDIA-001 | [D-07] | - | NOT_APPLICABLE | 카테고리 미정 |
| PLEOS-MEDIA-002 | [D-07] | - | NOT_APPLICABLE | 카테고리 미정 |
| PLEOS-MEDIA-003 | [D-07] | - | NOT_APPLICABLE | M15 |
| PLEOS-MEDIA-004 | [D-07] | - | NOT_APPLICABLE | M15 |
| PLEOS-MEDIA-005 | [D-07] | - | NOT_APPLICABLE | 카테고리 미정 |
| PLEOS-MEDIA-006 | [D-07] | - | NOT_APPLICABLE | 카테고리 미정 |
| PLEOS-MEDIA-007 | [D-07] | - | NOT_APPLICABLE | 카테고리 미정 |
| PLEOS-MEDIA-008 | [D-07] | - | NOT_APPLICABLE | 카테고리 미정 |
| PLEOS-MEDIA-009 | [D-07] | - | NOT_APPLICABLE | 카테고리 미정 |
| PLEOS-MEDIA-010 | [D-07] | - | NOT_APPLICABLE | 카테고리 미정 |

## NAV

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-NAV-001 | [D-06] | - | NOT_APPLICABLE | 내비 앱 아님. M16 |
| PLEOS-NAV-002 | [D-06] | - | NOT_APPLICABLE | 내비 앱 아님 |
| PLEOS-NAV-003 | [D-06] | - | NOT_APPLICABLE | 내비 앱 아님 |
| PLEOS-NAV-004 | [D-06] | - | NOT_APPLICABLE | 내비 앱 아님 |
| PLEOS-NAV-005 | [D-06] | - | NOT_APPLICABLE | 내비 앱 아님 |
| PLEOS-NAV-006 | [D-06] | - | NOT_APPLICABLE | 내비 앱 아님 |

## CCP

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-CCP-001 | [D-05] | `CarScreen.kt` | UNVERIFIED | CCP 지원 여부 미정. 현재 흐름은 터치로 완결 |
| PLEOS-CCP-002 | [D-05] | `AndroidManifest.xml` | NOT_IMPLEMENTED | M08 NOT_RUN(결정 필요) |
| PLEOS-CCP-003 | [D-05] | `MainActivity.kt` | PARTIAL | BackHandler: 대화 중 Back은 멈추고 대기로, 최상단 Back은 소비하고 종료 안 함. CCP 실제 입력 NOT_RUN |
| PLEOS-CCP-004 | [D-05] | - | NOT_APPLICABLE | Pre-installed 앱 아님 |
| PLEOS-CCP-005 | [D-05] | - | UNVERIFIED | CCP 지원 여부 미정 |

## GLEO

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-GLEO-001 | [G-02] [G-05] | - | NOT_APPLICABLE | 연동 없음. M17 |
| PLEOS-GLEO-002 | [G-02] | - | NOT_APPLICABLE | M17 |
| PLEOS-GLEO-003 | [G-04] [G-05] | - | NOT_APPLICABLE | M17 |
| PLEOS-GLEO-004 | [G-03] [G-04] | - | NOT_APPLICABLE | 연동 없음 |
| PLEOS-GLEO-005 | [G-03] | - | NOT_APPLICABLE | 연동 없음 |
| PLEOS-GLEO-006 | [G-03] | - | NOT_APPLICABLE | 연동 없음 |

## SECURITY

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-SECURITY-001 | [P-09] | - | NOT_APPLICABLE | 계정 없음 |
| PLEOS-SECURITY-002 | [P-09] | - | NOT_APPLICABLE | 토큰 없음 |
| PLEOS-SECURITY-003 | [P-09] | - | NOT_APPLICABLE | 수집·권한 요청 없음 |
| PLEOS-SECURITY-004 | [P-09] [L-03] | - | NOT_APPLICABLE | 저장·로그·공개 데이터 없음 |
| PLEOS-SECURITY-005 | [P-09] | - | NOT_APPLICABLE | Telemetry 없음 |
| PLEOS-SECURITY-006 | [P-09] | - | NOT_APPLICABLE | 외부 전송 없음 |
| PLEOS-SECURITY-007 | [L-03] | - | NOT_APPLICABLE | 저장 데이터 없음 |
| PLEOS-SECURITY-008 | [L-03] | `DadeuriView.kt` | COMPLIANT | REVIEW(정적): 로그는 셰이더 실패 1건, 민감정보 없음 |
| PLEOS-SECURITY-009 | [L-03] | - | NOT_APPLICABLE | 캐시 없음 |
| PLEOS-SECURITY-010 | [L-03] | `AndroidManifest.xml` | COMPLIANT | M12 PASS: allowBackup=false |
| PLEOS-SECURITY-011 | [L-03] | - | NOT_APPLICABLE | 저장 데이터 없음 |
| PLEOS-SECURITY-012 | [L-03] | - | NOT_APPLICABLE | 네트워크 없음. C14 |
| PLEOS-SECURITY-013 | [L-03] | - | NOT_APPLICABLE | 암호화 없음 |
| PLEOS-SECURITY-014 | [L-03] | - | NOT_APPLICABLE | 키 없음. C08 |
| PLEOS-SECURITY-015 | [L-03] | `AndroidManifest.xml` | NOT_APPLICABLE | 네트워크 없음. M11·C13 PASS |
| PLEOS-SECURITY-016 | [L-03] | - | NOT_APPLICABLE | 서버 없음 |
| PLEOS-SECURITY-017 | [L-03] | `AndroidManifest.xml` | COMPLIANT | REVIEW(정적): 권한 선언 없음. M04 NOT_RUN |
| PLEOS-SECURITY-018 | [L-03] | `AndroidManifest.xml` | COMPLIANT | M09 PASS: LAUNCHER Activity만 exported |
| PLEOS-SECURITY-019 | [L-03] | - | NOT_APPLICABLE | 컴포넌트 간 전달 없음 |
| PLEOS-SECURITY-020 | [L-03] | - | NOT_APPLICABLE | 공개 컴포넌트는 LAUNCHER뿐 |
| PLEOS-SECURITY-021 | [L-03] | - | NOT_APPLICABLE | WebView 없음 |
| PLEOS-SECURITY-022 | [L-03] | - | NOT_APPLICABLE | 외부 URI 입력 없음 |
| PLEOS-SECURITY-023 | [L-03] | 저장소 전체 | COMPLIANT | C08 PASS |
| PLEOS-SECURITY-024 | [L-03] | - | NOT_APPLICABLE | 서버 없음 |
| PLEOS-SECURITY-025 | [L-03] | - | NOT_APPLICABLE | 네이티브 코드 없음 |
| PLEOS-SECURITY-026 | [L-03] | `app/build.gradle.kts` | UNVERIFIED | Compose BOM 2026.09.00, activity-compose 1.12.0 의존성 점검 기록 없음 |
| PLEOS-SECURITY-027 | [L-03] | `app/build.gradle.kts` | NOT_IMPLEMENTED | release 서명 설정 없음(배포 전 필요) |
| PLEOS-SECURITY-028 | [L-03] | `AndroidManifest.xml`, `app/build.gradle.kts` | COMPLIANT | M10·G05 PASS(정적). release 빌드 미생성 |
| PLEOS-SECURITY-029 | [L-03] | - | NOT_APPLICABLE | 브라우저 앱 아님 |

## PERF

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-PERF-001 | [P-10] | `MainActivity.kt` | UNVERIFIED | 시작 시간 미측정 |
| PLEOS-PERF-002 | [P-10] | `DadeuriView.kt` | UNVERIFIED | 연속 셰이더 애니메이션의 CPU 미측정. EMU `adb shell top` |
| PLEOS-PERF-003 | [P-10] | 앱 전체 | UNVERIFIED | EMU `dumpsys meminfo` |
| PLEOS-PERF-004 | [P-10] | 앱 전체 | COMPLIANT | REVIEW(정적): 백그라운드 작업·리스트 없음 |
| PLEOS-PERF-005 | [UI-01] [L-04] | 앱 전체 | UNVERIFIED | EMU 측정 |
| PLEOS-PERF-006 | [L-04] | 앱 전체 | UNVERIFIED | 작업 004의 `am start -W` 성공 기록만 있고 시간 기준 판정 없음 |
| PLEOS-PERF-007 | [L-04] | 앱 전체 | UNVERIFIED | BUILD PASS. 장시간·기능 회귀 EMU NOT_RUN |
| PLEOS-PERF-008 | [L-04] | `DadeuriScript.kt` | NOT_APPLICABLE | App Market 제출 빌드 아님. 예시 사연·발화와 시연 제어 존재. 제출 전 제거 필수 |
| PLEOS-PERF-009 | [L-04] | 앱 전체 | COMPLIANT | REVIEW(정적): 재시작·설정 변경 요구 없음 |

## DESIGN

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-DESIGN-001 | [UI-01] | `CarScreen.kt`, `OurroadTheme.kt` | UNVERIFIED | Apple 스타일 자체 디자인과 Pleos 컴포넌트 대조 미실시 |
| PLEOS-DESIGN-002 | [UI-01] | `CarScreen.kt` | UNVERIFIED | REVIEW |
| PLEOS-DESIGN-003 | [UI-08] | `OurroadTheme.kt` | COMPLIANT | REVIEW(정적): 위계별 고정 스타일, 최소 18sp. Pretendard |
| PLEOS-DESIGN-004 | [UI-09] | - | NOT_APPLICABLE | 자체 아이콘 없음 |
| PLEOS-DESIGN-005 | [UI-10] | `AndroidManifest.xml` | NOT_IMPLEMENTED | M13 FAIL(MINOR), Lint MissingApplicationIcon |
| PLEOS-DESIGN-006 | [UI-12] [UI-14] | `CarScreen.kt` | PARTIAL | 누름 리플·Disabled(투명도) 있음, Loading 상태 없음 |
| PLEOS-DESIGN-007 | [UI-01] | - | NOT_APPLICABLE | 배포 계획 없음 |
| PLEOS-DESIGN-008 | [UI-01] | `DadeuriFlow.kt`, `DadeuriScript.kt` | UNVERIFIED | 검토 기록 없음 |
| PLEOS-DESIGN-009 | [UI-13] | - | NOT_APPLICABLE | Toast 없음 |

## 갱신 규칙

- 코드나 Manifest가 바뀌면 관련 행의 Target·Status·Verification을 같은 변경에서 고친다.
- COMPLIANT로 바꿀 때는 근거 수준을 적는다. EMU나 CAR가 필요한 요구사항을 정적 근거만으로 COMPLIANT로 바꾸지 않는다.
- 요약 표의 개수는 `scripts/pleos/check-compliance.sh`의 H03 검사가 다시 센다.

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
[S-T]: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro
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
