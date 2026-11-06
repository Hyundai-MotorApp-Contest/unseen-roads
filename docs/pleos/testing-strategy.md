# 검증 전략

기준: 2026-11-06 KST. 검사 결과는 PASS·FAIL·NOT_RUN·BLOCKED·NOT_APPLICABLE로만 쓴다([docs/workflow.md](../workflow.md)). 실행하지 않은 검증을 PASS로 쓰지 않는다. Emulator 결과는 실차 결과를 대신하지 않고, Mock·Fake 결과는 SDK 호출 결과를 대신하지 않는다.

## 검증 단계

| 단계 | 무엇을 | 도구·명령 | 판정하는 요구사항 | 자동화 |
| --- | --- | --- | --- | --- |
| 1. 정적 검사 | Manifest·Gradle·소스 패턴·하네스 문서 일관성 | `scripts/pleos/check-compliance.sh --static-only` | AUTO 표시 항목 | 자동 |
| 2. 빌드 | 디버그 빌드, Android Lint | `./gradlew :app:assembleDebug :app:lintDebug` 또는 검사 스크립트 기본 실행 | PERF-007 일부, 정적 분석 | 자동 |
| 3. 단위 테스트 | 주행 규제 게이트 계산, SDK 오류 → 결과 범주 변환, 오디오 포커스 상태 기계, 동의·공개 확인 규칙, Gleo 응답 형식 | `./gradlew :app:testDebugUnitTest`(테스트 소스가 생긴 뒤) | UNIT 표시 항목 | 자동(테스트 작성 후) |
| 4. 차량 상태별 동작 | 기어·브레이크·탑승·도어 변경에 따른 앱 반응, 미지원·권한 오류 | Pleos Emulator + Virtual Console Emulation Controls | SDK-002·009~013, SDK-022 | 수동 |
| 5. 주행 규제 적용 | 규제 ON/OFF 중 화면·재생·녹음·공개 확인 변화 | `adb shell cmd hmg_car_service --service drivingstate driving_regulation on`/`off` | SAFETY-001~009·018, CATEGORY-004 | 수동(명령은 스크립트화 가능) |
| 6. 오디오 포커스 | Interaction Matrix 체크리스트, 통화·내비 안내·Gleo 호출 중 동작, Suspend·Resume | Emulator, 실차 | AUDIO-001~014 | 수동 |
| 7. SDK 호환성 | SDK·모드별 초기화, capability·isSupported, ON_DEVICE·HYBRID·SERVER 동작, 버전 | Emulator(대상 버전 기록), 실차 | SDK 전체 | 수동 |
| 8. 폼 팩터 | Connect-S·W·WF, Portrait·Landscape, window 크기 변경, IME | FormFactorConsole + Host Display Bridge | UX 전체, IME | 수동 |
| 9. 성능 | 시작 시간, 응답 시간, CPU·RAM | `adb shell am start -W`, `adb shell top`, `adb shell dumpsys meminfo <패키지>` | PERF-001~006 | 수동 |
| 10. 실차 | 텍스트 실측 높이, 주행 중 CPU, 코덱, Build.DEVICE, Suspend | 실차 | CAR 표시 항목 | 수동, 현재 BLOCKED(실차 없음) |

## 1·2단계: 자동 검사

```text
macOS·Linux·Git Bash : scripts/pleos/check-compliance.sh [--static-only] [--strict] [--list-manual]
Windows PowerShell   : py -3 scripts\pleos\check_compliance.py [--static-only] [--strict] [--list-manual]
```

| 검사군 | 내용 |
| --- | --- |
| H01~H04 | 하네스 파일 존재, 요구사항 ID·수준·출처, 매트릭스 추적성·요약 개수, 요구사항이 참조하는 검사 ID |
| G01~G05 | minSdk, Gradle 버전, Pleos Maven, SDK 좌표·버전, release debuggable |
| M01~M17 | Manifest: AAOS feature, Pleos 퍼미션, DO, 방향, CCP, exported, debuggable, cleartext, 백업, 아이콘, 카테고리, Music/Audio, Maps, Gleo 스키마 |
| C01~C17 | 소스: CarUxRestrictions, SDK 수명주기·리스너 짝, 오디오 포커스, 비밀값, 모델명 분기, 하드웨어 직접 접근, Back, http, URL 토큰, 상태 저장, DO 앱 애니메이션, 데모 문자열 |
| B01~B03 | assembleDebug, lintDebug, testDebugUnitTest |

- 종료 코드: 0은 BLOCKER·MAJOR FAIL 없음, 1은 있음(`--strict`이면 MINOR 포함), 2는 검사기 오류.
- FAIL 심각도는 연결된 요구사항의 수준으로 정한다. SAFETY·AUDIO의 MUST·MUST_NOT은 BLOCKER, 그 외 MUST·MUST_NOT은 MAJOR, SHOULD는 MINOR.
- 정적 검사는 패턴 검사다. C15처럼 휴리스틱인 항목이 오탐이면 근거를 남기고 [compliance-matrix.md](compliance-matrix.md)에서 판정한다. 검사를 끄기 위해 코드를 우회하지 않는다.
- EMU·CAR·REVIEW·EXT·UNIT 대상은 스크립트가 개수만 NOT_RUN으로 보여 준다(`--list-manual`로 ID 목록).
- Gradle 실행 시 `JAVA_HOME`이 없으면 macOS·Windows의 Android Studio 기본 JBR 경로를 시도한다. JDK를 찾지 못하면 B 검사는 BLOCKED다.

## 3단계: 단위 테스트 대상

SDK는 JVM에서 돌지 않으므로 [architecture.md](architecture.md)의 어댑터 경계 뒤에서 테스트한다.

| 대상 | 예시 사례 | 요구사항 |
| --- | --- | --- |
| 게이트 계산 | `NO_VIDEO`만 활성 → 영상만 제한. 알 수 없음 → 전부 제한. DO 불요 → 제한 없음 | SAFETY-001·005·018 |
| Vehicle 오류 변환 | ErrorCode 9종과 비 VehicleException이 결과 범주로 변환 | SDK-010·011 |
| TTS·STT 오류 변환 | ErrorType 12종 + 새 값 기본 분기 | SDK-017 |
| 위치 null 처리 | null·오류를 위치로 쓰지 않음 | SDK-022 |
| 포커스 상태 기계 | GAIN·LOSS·LOSS_TRANSIENT·CAN_DUCK 전이, Suspend 진입 시 abandon, Resume 자동 재생 없음 | AUDIO-004·010·011 |
| 공개 확인 | 무응답·STT 실패·변경 후 재확인 | AGENTS.md, U-16 |

현재 `app/src/test`가 없다. 테스트 소스를 추가하면 B03이 실행된다.

## 4·5단계: Emulator 절차

시작 전:

1. 다른 팀원·AI가 같은 AVD를 쓰고 있는지 확인한다(`adb devices`, 진행 중 작업 공유). 공용 AVD의 상태를 바꾸는 명령은 알리고 실행한다.
2. AVD 이름·Emulator 버전(2.1.0 이상), 앱 빌드, SDK 버전, Mode를 기록한다.
3. Virtual Console: `adb forward tcp:8088 tcp:8088` 후 `http://127.0.0.1:8088/`([docs/pleos.md](../pleos.md)). 외부 네트워크에 포트를 노출하지 않는다.

주행 규제 시나리오(SAFETY):

| 단계 | 행동 | 기록 |
| --- | --- | --- |
| 1 | 규제 OFF에서 앱 실행, 각 화면 진입 | 기준 화면 |
| 2 | `driving_regulation on` | 받은 `isRequiresDistractionOptimization`·ActiveRestrictions 값, 화면·재생·녹음 변화 |
| 3 | 규제 ON 중 제한 기능 시도(상세 화면, 키보드, 장문) | 차단 여부 |
| 4 | 재생·녹음·공개 확인 중 규제 ON | 즉시 반영 여부, 중복·무단 재개 없음 |
| 5 | `driving_regulation off` | 복귀 동작, 자동 재개 여부 |
| 6 | 앱 재시작·리스너 해제 상태에서 규제 ON | 알 수 없음 → 제한 |

Virtual Console에는 차속 카드가 없다. 기어 P·D 전환과 규제 명령을 함께 쓰고, 차속 기반 판정은 실차에서 다시 확인한다.

## UNSEEN 시나리오 연결

[docs/verification-scenarios.md](../verification-scenarios.md)의 시나리오와 요구사항을 연결한다.

| 시나리오 | 주요 요구사항 |
| --- | --- |
| U-01·02 동의·철회 | SECURITY-003·004·007 |
| U-03·05·06 주행 중 기능·제한 전환·상태 미상 | SAFETY-001~009·018·023, CATEGORY-004 |
| U-07 위치 null·미지원·권한 거부 | SDK-003·020~023 |
| U-11·12 재식별·삭제 | SECURITY-004~007·012 |
| U-14 앱 재시작·회전 | LIFECYCLE-001·002·006, UX-007, SDK-009·013·021 |
| U-15 네트워크 단절 | HW-004, SECURITY-015 |
| U-18·19 시작 제안·오디오 포커스 | AUDIO-001~006·009, SAFETY-023 |
| U-21 내비 전면 오브 | UX-005, SAFETY-008·009, X-12 |

## 결과 기록

작업 문서 또는 [검증 양식](../../templates/verification.md)에 다음을 남긴다.

- 실행한 명령과 종료 코드, 환경(OS·CPU·JDK·AVD·Emulator·SDK 버전·Mode), 초기 상태와 실제 결과.
- 관련 요구사항 ID와 [compliance-matrix.md](compliance-matrix.md)의 상태 변경.
- 실행하지 못한 항목은 NOT_RUN 또는 BLOCKED와 이유.

## 2026-11-06 실행 기록

| 항목 | 결과 | 명령·환경 |
| --- | --- | --- |
| 준수 검사 전체 | 종료 코드 1. PASS 21, FAIL 3(C15 MAJOR, M12·M13 MINOR), NOT_RUN 3(M08, C11, C17), BLOCKED 1(M14), NOT_APPLICABLE 16. 위반 목록은 [implementation-roadmap.md](implementation-roadmap.md) | `scripts/pleos/check-compliance.sh`, macOS arm64, Python 3.14 |
| 빌드·Lint(B01·B02) | PASS(Lint 오류 0, 경고 26) | 검사 스크립트가 `:app:assembleDebug`, `:app:lintDebug` 실행, Android Studio JBR |
| 검사기 자체 확인 | PASS | 작업 트리 복사본(scratchpad)에 위반 15종(Maven 미등록, 미정의 artifact, TTS 퍼미션 누락, 미사용 퍼미션, 방향 고정, exported service, cleartext, Gleo 스키마 오류, DO+CarUxRestrictions 없음, TTS 수명주기, 비밀값, finishAffinity, http URL, URL 토큰, DO+애니메이션)을 넣고 `--static-only` 실행. 15종 모두 FAIL로 검출 |
| Windows 실행 | NOT_RUN | Windows PC 미사용 |
| 단위 테스트 | NOT_APPLICABLE | 테스트 소스 없음 |
| Emulator 4~9단계 | NOT_RUN | 이번 작업은 하네스 구축 범위 |
| 실차 | BLOCKED | 실차 접근 없음 |
