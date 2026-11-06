# Pleos 요구사항 준수 매트릭스

기준: 2026-11-06 KST, 분리 전 로컬 UI 목업 작업 트리. 이 문서 PR과 별도로 앱을 통합하며, 아래 표는 전체 목업을 대상으로 한 스냅샷이다. 요구사항 원문은 [requirements.md](requirements.md)에 있다.

## 상태 값

| 상태 | 의미 |
| --- | --- |
| COMPLIANT | 요구사항 충족. 근거 수준(정적·빌드·EMU·실차)을 Verification에 적는다 |
| NON_COMPLIANT | 요구사항 위반. SAFETY·AUDIO 분류의 MUST·MUST_NOT 위반은 BLOCKER |
| PARTIAL | 일부만 충족 |
| NOT_IMPLEMENTED | 제품에 필요하지만 아직 구현하지 않음. 위반으로 보지 않는다 |
| NOT_APPLICABLE | 조건 미충족으로 현재 앱에 적용되지 않음 |
| UNVERIFIED | 증거나 검증 환경이 부족함 |

이 값은 요구사항의 **현재 상태**다. 검사 실행 결과(PASS·FAIL·NOT_RUN·BLOCKED·NOT_APPLICABLE)는 [testing-strategy.md](testing-strategy.md)와 작업 문서에 따로 기록한다. 자동 검사 PASS만으로 COMPLIANT를 쓰지 않는다. Verification 열의 `Mxx`·`Gxx`·`Cxx`는 `scripts/pleos/check-compliance.sh`의 검사 ID다.

## 현재 근거

| 근거 | 결과 |
| --- | --- |
| 저장소 조사 | 단일 `:app` 모듈, Kotlin, `Activity` 1개(`MainActivity`)가 View 표시 전환으로 HOME·RECORDS·RADIO·RADIO_PAUSED·CAPTURE·REVIEW·DONE 화면을 보여 줌. `PrismOrbView`(AGSL 셰이더, 표시 중 매 프레임 갱신), `GlassPanel`. Pleos SDK·AAOS Car API·오디오·네트워크·저장소·테스트 소스 없음 |
| Manifest | `android.hardware.type.automotive` required, LAUNCHER Activity 1개(exported). 권한·DO·카테고리·CCP 선언·아이콘·`allowBackup`·`screenOrientation` 없음 |
| Gradle | AGP 9.4.1, Gradle Wrapper 9.6.0, compileSdk 37, minSdk 28, targetSdk 34. 의존성 없음 |
| 빌드 | 2026-11-06 macOS arm64, Android Studio JBR, `./gradlew :app:assembleDebug :app:lintDebug` PASS. Lint 오류 0, 경고 26(MissingApplicationIcon 1, UnusedResources 15 등) |
| Emulator·실차 | 이번 작업에서 실행하지 않음(NOT_RUN) |

## 요약

| 상태 | 개수 |
| --- | --- |
| COMPLIANT | 17 |
| NON_COMPLIANT | 3 |
| PARTIAL | 5 |
| NOT_IMPLEMENTED | 15 |
| NOT_APPLICABLE | 99 |
| UNVERIFIED | 31 |
| 합계 | 170 |

### 주요 위반과 미구현

| 항목 | 분류 | 내용 |
| --- | --- | --- |
| UX-007, LIFECYCLE-002 | NON_COMPLIANT (MAJOR, 정적 근거) | `MainActivity.screen`을 저장하지 않고(`onSaveInstanceState` 없음, `configChanges` 없음) window 크기 변경·방향 전환으로 Activity가 재생성되면 HOME으로 돌아간다. EMU 재현은 NOT_RUN |
| SECURITY-010 | NON_COMPLIANT (MINOR, 권장) | `android:allowBackup` 미지정(기본 true), 백업 규칙 없음 |
| CCP-003 | PARTIAL (MAJOR) | Back 처리 코드 없음. 하위 화면(RECORDS·RADIO·CAPTURE·REVIEW·DONE)에서 Back이 이전 화면으로 가지 않는다. 최상단 동작과 CCP Back 이벤트 전달은 EMU 미확인 |
| SAFETY-001·002·005·018·023 | NOT_IMPLEMENTED (BLOCKER 후보) | CarUxRestrictions 연동이 없다. DO 미선언이라 현재는 주행 중 플랫폼이 앱 사용을 막는다. 제품 목표인 주행 중 음성 경험을 넣기 전에 반드시 구현 |
| SAFETY-008·009 | NOT_APPLICABLE (DO 선언 시 BLOCKER 후보) | `PrismOrbView`는 표시 중 계속 애니메이션한다. DO를 선언하는 순간 주행 중 동작 정책이 필요하다 |
| CATEGORY-001·002 | NOT_IMPLEMENTED (MAJOR) | 카테고리 미정. 주행 중 허용 범위와 MEDIA 조건부 요구사항이 이 결정에 달려 있다 |
| DESIGN-005 | NOT_IMPLEMENTED (MINOR) | `android:icon` 없음(Lint MissingApplicationIcon) |

## SAFETY

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-SAFETY-001 | [P-02] [D-04] | `MainActivity.kt`, 향후 주행 규제 상태 관리 | NOT_IMPLEMENTED | C01, EMU `driving_regulation on/off` |
| PLEOS-SAFETY-002 | [D-04] | 향후 CarUxRestrictions 어댑터 | NOT_IMPLEMENTED | C01, EMU |
| PLEOS-SAFETY-003 | [D-04] | DO 선언 Activity | NOT_APPLICABLE | DO 미선언. C01 |
| PLEOS-SAFETY-004 | [P-03] [L-07] | `AndroidManifest.xml` | NOT_IMPLEMENTED | M06. 제품 목표상 필요, 선언 범위 결정 전 |
| PLEOS-SAFETY-005 | [D-04] | 향후 주행 규제 상태 관리 | NOT_IMPLEMENTED | REVIEW, EMU |
| PLEOS-SAFETY-006 | [D-10] | 미정 | UNVERIFIED | C-01. 지역 분기 필요 여부 미정 |
| PLEOS-SAFETY-007 | [P-02] [D-04] | - | NOT_APPLICABLE | 영상 기능 없음 |
| PLEOS-SAFETY-008 | [UI-01] | `PrismOrbView.kt`, `GlassPanel.kt` | NOT_APPLICABLE | DO 미선언. 선언 시 C16, EMU |
| PLEOS-SAFETY-009 | [P-02] | `PrismOrbView.kt` | NOT_APPLICABLE | DO 미선언. 선언 시 REVIEW, EMU |
| PLEOS-SAFETY-010 | [P-02] | `res/layout/*` | UNVERIFIED | REVIEW 미실시 |
| PLEOS-SAFETY-011 | [UI-01] | `res/layout/*`, `strings.xml` | UNVERIFIED | REVIEW 미실시 |
| PLEOS-SAFETY-012 | [P-02] | `prism_home.xml` | UNVERIFIED | REVIEW 미실시 |
| PLEOS-SAFETY-013 | [P-02] [UI-12] | `values/prism_actions.xml`(minHeight 72dp) | UNVERIFIED | 기준 수치 X-04 미확인 |
| PLEOS-SAFETY-014 | [P-02] | 향후 STT·Gleo 연동 | NOT_IMPLEMENTED | REVIEW, EMU |
| PLEOS-SAFETY-015 | [P-02] | 기능 목록 | COMPLIANT | REVIEW(정적): 게임·브라우징·피트니스 기능 없음 |
| PLEOS-SAFETY-016 | [P-02] | - | NOT_APPLICABLE | 검색·로그인 없음 |
| PLEOS-SAFETY-017 | [UI-01] | `res/layout/*` | UNVERIFIED | REVIEW 미실시 |
| PLEOS-SAFETY-018 | [P-03] [D-04] | 향후 기능-플래그 대응표 | NOT_IMPLEMENTED | REVIEW, EMU |
| PLEOS-SAFETY-019 | [UI-01] | `values/strings.xml` | UNVERIFIED | 한국어 기준 X-15 |
| PLEOS-SAFETY-020 | [UI-01] | `res/layout/*` | UNVERIFIED | CAR |
| PLEOS-SAFETY-021 | [UI-01] | `values/colors.xml`, drawable | UNVERIFIED | 대비율 계산 미실시 |
| PLEOS-SAFETY-022 | [UI-01] | - | NOT_APPLICABLE | 알림 없음 |
| PLEOS-SAFETY-023 | [UI-01] | 향후 음성 대화 흐름 | NOT_IMPLEMENTED | REVIEW, EMU |

## UX

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-UX-001 | [P-05] [UI-02] | `AndroidManifest.xml`, `values-w560dp/` | PARTIAL | M07: 방향 고정 없음. 폭 560dp 이상 가로 버튼 배치만 있음. 폼 팩터 테스트 NOT_RUN |
| PLEOS-UX-002 | [P-05] | `res/layout/*` | UNVERIFIED | REVIEW |
| PLEOS-UX-003 | [P-05] | `res/layout/*` | UNVERIFIED | EMU window 크기 변경 |
| PLEOS-UX-004 | [P-05] | `AndroidManifest.xml` | PARTIAL | M07: 미지정(기본 portrait 1 window). landscape 2 window 설계 근거 미기록 |
| PLEOS-UX-005 | [P-05] [UI-02] | `res/layout/*` | UNVERIFIED | EMU |
| PLEOS-UX-006 | [L-04] | 앱 전체 | UNVERIFIED | 폼 팩터 테스트 |
| PLEOS-UX-007 | [L-04] | `MainActivity.kt` | NON_COMPLIANT | 정적 근거: 화면 상태 미저장. C15, EMU 재현 필요 |
| PLEOS-UX-008 | [L-04] | `res/layout/*` | UNVERIFIED | REVIEW, EMU |
| PLEOS-UX-009 | [UI-01] [UI-02] [L-06] | 앱 전체 | UNVERIFIED | 폼 팩터 테스트 |
| PLEOS-UX-010 | [UI-01] | `res/values*` | NOT_IMPLEMENTED | `values-night` 없음 |
| PLEOS-UX-011 | [UI-01] | `values/strings.xml` | PARTIAL | 문자열 외부화됨, 한국어만 |
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
| PLEOS-LIFECYCLE-002 | [P-05] [L-04] | `MainActivity.kt` | NON_COMPLIANT | 정적 근거: 화면 상태 미저장. C15, EMU |
| PLEOS-LIFECYCLE-003 | [P-05] | `PrismOrbView.kt`, `GlassPanel.kt` | UNVERIFIED | 정적: 오브는 `isShown`일 때만 다음 프레임 요청, 패널은 detach 시 정리. EMU 미확인 |
| PLEOS-LIFECYCLE-004 | [P-05] | - | NOT_APPLICABLE | 백그라운드 작업 없음 |
| PLEOS-LIFECYCLE-005 | [P-05] [P-10] | 앱 전체 | COMPLIANT | REVIEW(정적): 상주 서비스·타이머·폴링·네트워크 없음 |
| PLEOS-LIFECYCLE-006 | [P-05] [D-05] | `MainActivity.kt` | NOT_IMPLEMENTED | C15 |
| PLEOS-LIFECYCLE-007 | [D-03] | 향후 세션·오디오 관리 | NOT_IMPLEMENTED | 감지 API X-07 |

## CATEGORY

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-CATEGORY-001 | [P-04] | `docs/decisions.md` | NOT_IMPLEMENTED | 결정 기록 없음 |
| PLEOS-CATEGORY-002 | [D-06] [D-07] | `AndroidManifest.xml` | NOT_IMPLEMENTED | M14 BLOCKED(카테고리 미정) |
| PLEOS-CATEGORY-003 | [D-06] [D-07] [L-04] | - | UNVERIFIED | 카테고리 미정 |
| PLEOS-CATEGORY-004 | [P-04] | - | NOT_APPLICABLE | 주행 중 기능 없음 |

## HW

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-HW-001 | [P-06] | `app/src/main/java/**` | COMPLIANT | C10 PASS(정적) |
| PLEOS-HW-002 | [P-06] [P-07] | - | NOT_APPLICABLE | 차량 데이터 미사용 |
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
| PLEOS-SDK-006 | [S-V-setup] [L-01] | `settings.gradle.kts`, `app/build.gradle.kts` | NOT_APPLICABLE | 의존성 없음. G03, G04 |
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
| PLEOS-AUDIO-001 | [D-02] [P-10] | - | NOT_APPLICABLE | EMU 예정 |
| PLEOS-AUDIO-002 | [D-02] | - | NOT_APPLICABLE | C07 |
| PLEOS-AUDIO-003 | [D-02] | - | NOT_APPLICABLE | REVIEW |
| PLEOS-AUDIO-004 | [D-02] [P-02] | - | NOT_APPLICABLE | C07, EMU |
| PLEOS-AUDIO-005 | [D-02] | - | NOT_APPLICABLE | EMU |
| PLEOS-AUDIO-006 | [D-02] | - | NOT_APPLICABLE | EMU |
| PLEOS-AUDIO-007 | [D-02] | - | NOT_APPLICABLE | E 컨텍스트 미사용 |
| PLEOS-AUDIO-008 | [D-02] | - | NOT_APPLICABLE | 포커스 중재 주체 아님(C-07) |
| PLEOS-AUDIO-009 | [D-02] | - | NOT_APPLICABLE | EMU 예정 |
| PLEOS-AUDIO-010 | [D-03] | - | NOT_APPLICABLE | X-07 |
| PLEOS-AUDIO-011 | [D-03] | - | NOT_APPLICABLE | EMU, CAR |
| PLEOS-AUDIO-012 | [D-03] | - | NOT_APPLICABLE | EMU, CAR |
| PLEOS-AUDIO-013 | [D-08] | - | NOT_APPLICABLE | X-08 |
| PLEOS-AUDIO-014 | [D-08] | - | NOT_APPLICABLE | EMU |

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
| PLEOS-CCP-001 | [D-05] | `res/layout/*` | UNVERIFIED | CCP 지원 여부 미정. 현재 흐름은 터치로 완결 |
| PLEOS-CCP-002 | [D-05] | `AndroidManifest.xml` | NOT_IMPLEMENTED | M08 NOT_RUN(결정 필요) |
| PLEOS-CCP-003 | [D-05] | `MainActivity.kt` | PARTIAL | C11: `finish()` 없음, Back 처리 없음. 하위 화면 Back 미처리. EMU 미확인 |
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
| PLEOS-SECURITY-003 | [P-09] | 향후 동의 흐름 | NOT_APPLICABLE | 위치·음성 미수집. 수집 기능과 함께 P0 동의 흐름 필수 |
| PLEOS-SECURITY-004 | [P-09] [L-03] | `app/src/main/java/**` | COMPLIANT | REVIEW(정적): PII 저장·로그 없음 |
| PLEOS-SECURITY-005 | [P-09] | - | NOT_APPLICABLE | Telemetry 없음 |
| PLEOS-SECURITY-006 | [P-09] | - | NOT_APPLICABLE | 외부 전송 없음 |
| PLEOS-SECURITY-007 | [L-03] | - | NOT_APPLICABLE | 저장 데이터 없음 |
| PLEOS-SECURITY-008 | [L-03] | `PrismOrbView.kt` | COMPLIANT | REVIEW(정적): 로그는 셰이더 실패 1건, 민감정보 없음 |
| PLEOS-SECURITY-009 | [L-03] | - | NOT_APPLICABLE | 캐시 없음 |
| PLEOS-SECURITY-010 | [L-03] | `AndroidManifest.xml` | NON_COMPLIANT | M12 FAIL(MINOR): allowBackup 미지정 |
| PLEOS-SECURITY-011 | [L-03] | - | NOT_APPLICABLE | 저장 데이터 없음 |
| PLEOS-SECURITY-012 | [L-03] | - | NOT_APPLICABLE | 네트워크 없음. C14 |
| PLEOS-SECURITY-013 | [L-03] | - | NOT_APPLICABLE | 암호화 없음 |
| PLEOS-SECURITY-014 | [L-03] | - | NOT_APPLICABLE | 키 없음. C08 |
| PLEOS-SECURITY-015 | [L-03] | `AndroidManifest.xml` | NOT_APPLICABLE | 네트워크 없음. M11·C13 PASS |
| PLEOS-SECURITY-016 | [L-03] | - | NOT_APPLICABLE | 서버 없음 |
| PLEOS-SECURITY-017 | [L-03] | `AndroidManifest.xml` | COMPLIANT | M04 PASS: 권한 선언 없음 |
| PLEOS-SECURITY-018 | [L-03] | `AndroidManifest.xml` | COMPLIANT | M09 PASS: LAUNCHER Activity만 exported |
| PLEOS-SECURITY-019 | [L-03] | - | NOT_APPLICABLE | 컴포넌트 간 전달 없음 |
| PLEOS-SECURITY-020 | [L-03] | - | NOT_APPLICABLE | 공개 컴포넌트는 LAUNCHER뿐 |
| PLEOS-SECURITY-021 | [L-03] | - | NOT_APPLICABLE | WebView 없음 |
| PLEOS-SECURITY-022 | [L-03] | - | NOT_APPLICABLE | 외부 URI 입력 없음 |
| PLEOS-SECURITY-023 | [L-03] | 저장소 전체 | COMPLIANT | C08 PASS |
| PLEOS-SECURITY-024 | [L-03] | - | NOT_APPLICABLE | 서버 없음 |
| PLEOS-SECURITY-025 | [L-03] | - | NOT_APPLICABLE | 네이티브 코드 없음 |
| PLEOS-SECURITY-026 | [L-03] | `app/build.gradle.kts` | UNVERIFIED | 의존성 점검 기록 없음 |
| PLEOS-SECURITY-027 | [L-03] | `app/build.gradle.kts` | NOT_IMPLEMENTED | release 서명 설정 없음(배포 전 필요) |
| PLEOS-SECURITY-028 | [L-03] | `AndroidManifest.xml`, `app/build.gradle.kts` | COMPLIANT | M10·G05 PASS(정적). release 빌드 미생성 |
| PLEOS-SECURITY-029 | [L-03] | - | NOT_APPLICABLE | 브라우저 앱 아님 |

## PERF

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-PERF-001 | [P-10] | `MainActivity.kt` | UNVERIFIED | 시작 시간 미측정 |
| PLEOS-PERF-002 | [P-10] | `PrismOrbView.kt` | UNVERIFIED | 연속 셰이더 애니메이션의 CPU 미측정. EMU `adb shell top` |
| PLEOS-PERF-003 | [P-10] | 앱 전체 | UNVERIFIED | EMU `dumpsys meminfo` |
| PLEOS-PERF-004 | [P-10] | 앱 전체 | COMPLIANT | REVIEW(정적): 백그라운드 작업·리스트 없음 |
| PLEOS-PERF-005 | [UI-01] [L-04] | 앱 전체 | UNVERIFIED | EMU 측정 |
| PLEOS-PERF-006 | [L-04] | 앱 전체 | UNVERIFIED | 작업 004의 `am start -W` 성공 기록만 있고 시간 기준 판정 없음 |
| PLEOS-PERF-007 | [L-04] | 앱 전체 | UNVERIFIED | BUILD PASS. 장시간·기능 회귀 EMU NOT_RUN |
| PLEOS-PERF-008 | [L-04] | `values/strings.xml` | NOT_APPLICABLE | App Market 제출 빌드 아님. 데모·예시 문자열 존재(C17). 제출 전 제거 필수 |
| PLEOS-PERF-009 | [L-04] | 앱 전체 | COMPLIANT | REVIEW(정적): 재시작·설정 변경 요구 없음 |

## DESIGN

| Requirement ID | Source | Target | Status | Verification |
| --- | --- | --- | --- | --- |
| PLEOS-DESIGN-001 | [UI-01] | `res/drawable/*`, `res/layout/*` | UNVERIFIED | 글래스·오브 자체 스타일과 Pleos 컴포넌트 대조 미실시 |
| PLEOS-DESIGN-002 | [UI-01] | `res/layout/*` | UNVERIFIED | REVIEW |
| PLEOS-DESIGN-003 | [UI-08] | `activity_main.xml`, `prism_home.xml`, `private_records.xml` | COMPLIANT | REVIEW(정적): 사용 레이아웃 최소 20sp. 미사용 `map_home.xml`은 15sp |
| PLEOS-DESIGN-004 | [UI-09] | - | NOT_APPLICABLE | 자체 아이콘 없음 |
| PLEOS-DESIGN-005 | [UI-10] | `AndroidManifest.xml` | NOT_IMPLEMENTED | M13 FAIL(MINOR), Lint MissingApplicationIcon |
| PLEOS-DESIGN-006 | [UI-12] [UI-14] | `res/animator/glass_press.xml` | PARTIAL | 누름 피드백 있음, Disabled·Loading 상태 미사용 |
| PLEOS-DESIGN-007 | [UI-01] | - | NOT_APPLICABLE | 배포 계획 없음 |
| PLEOS-DESIGN-008 | [UI-01] | 향후 음성 흐름 | UNVERIFIED | 검토 기록 없음 |
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
