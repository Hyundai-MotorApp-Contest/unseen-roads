# 개발 환경과 실행 명령

기준일: 2026-11-05 KST. 설치 완료 전달과 실제 실행 확인을 구분한다.

## 현재 실행 가능 범위

| 항목 | 상태 |
| --- | --- |
| 문서 검사 | Python 3.9+ / 표준 라이브러리만 사용. macOS와 Windows 명령은 README 참고 |
| Android 앱 / Gradle Wrapper | 저장소 루트의 Gradle Wrapper와 `:app` 모듈. 아래 실행 절차 참고 |
| 서버 / DB / 마이그레이션 | 아직 없음. 서버 실행·테스트·DB 초기화 명령 미등록 |
| SDK 호출·차량 상태 시나리오 | NOT_RUN |
| IDE·시뮬레이터 | 팀 설치 완료 전달. 각 PC의 버전·연결 상태는 미확인 |

## Kotlin 차량 앱 빌드와 실행

- 저장소 루트를 Android Studio에서 열고 Gradle 동기화를 실행한다.
- Gradle Wrapper 9.6.0, AGP 9.4.1의 내장 Kotlin, Android SDK Platform 37.0을 사용한다. 로컬 검증 환경은 macOS arm64와 Android Studio 내장 JBR 25.0.3이다.
- 앱의 최소 API는 28, 대상 API는 Pleos Connect v2.1.0 이미지와 같은 34다. 컴파일 SDK는 개발 PC에 설치된 37을 사용한다. [Pleos 개발 환경](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator) · [AGP 호환 표](https://developer.android.com/build/releases/about-agp)
- Android Studio 밖에서는 호환 JDK의 `JAVA_HOME`과 SDK의 `ANDROID_HOME`을 설정한다. Android Studio는 로컬의 `local.properties`로도 SDK를 찾을 수 있다.

| 작업 | macOS | Windows PowerShell |
| --- | --- | --- |
| 디버그 빌드 | `./gradlew :app:assembleDebug` | `.\gradlew.bat :app:assembleDebug` |
| Android Lint | `./gradlew :app:lintDebug` | `.\gradlew.bat :app:lintDebug` |
| Pleos 가이드 준수 검사 | `scripts/pleos/check-compliance.sh` | `py -3 scripts\pleos\check_compliance.py` |

준수 검사는 정적 검사 뒤 위 빌드·Lint와 단위 테스트(소스가 있을 때)를 실행한다. Gradle을 건너뛰려면 `--static-only`를 붙인다. 판정 기준은 [Pleos 하네스](pleos/README.md)에 있다.

APK 경로는 `app/build/outputs/apk/debug/app-debug.apk`다. Device Manager에서 `Pleos_Connect_v2.1.0`을 부팅한 뒤 Run 대상 모듈을 `app`, 기기를 Pleos AVD로 선택한다. 앱을 실행하면 Prism Orb 홈이 표시된다. 단계별 검증 결과는 [작업 004](../tasks/004-pleos-kotlin-skeleton.md)에 기록한다.

## 팀 환경 확인표

### 2026-11-01 로컬 확인 — macOS arm64 1대

다른 팀원의 PC 상태로 일반화하지 않으며 계정별 OS 대응은 확정하지 않았다. 설치·설정 조회와 실제 앱 실행을 구분한다.

| 확인 항목 | 결과 / 근거 |
| --- | --- |
| Android Studio의 현재 프로젝트 | PASS — `unseen-roads`가 열려 있음. `Add Configuration` 표시, Run 비활성화 |
| Android 앱 구성 | PASS — 저장소에서 Gradle 파일·Wrapper·AndroidManifest.xml·앱 모듈을 확인하지 못함. 아직 문서 저장소 상태 |
| Pleos 시스템 이미지 | PASS — 설치된 `source.properties`에서 API 34, arm64-v8a, android-automotive 확인 |
| Pleos AVD | PASS — SDK의 `emulator -list-avds` 결과에 `Pleos_Connect_v2.1.0` 표시 |
| Pleos AVD 설정 | PASS — `config.ini`에서 RAM 4096MB, Cold boot 활성화·Fast boot 비활성화 확인 |
| ADB | PASS — platform-tools 설치 확인. 로컬 ADB 서버 시작 후 `adb devices -l` 성공, 연결된 기기는 없음 |
| 터미널 PATH | `adb`와 `emulator`가 PATH에 없음. 이번 조회는 설치된 Android SDK 내부 실행 파일의 전체 경로로 수행 |
| 에뮬레이터 부팅 / Virtual Console | NOT_RUN — 이번 확인에서는 에뮬레이터를 실행하지 않음 |
| Android build / Pleos SDK 호출 | NOT_RUN — 앱 프로젝트 미생성 |

이 PC에서는 Pleos 시스템 이미지 재설치가 필요하다는 근거가 없다. 에뮬레이터는 PC의 Android SDK에 설치되어 있고, 앱에서 사용할 Vehicle·STT·TTS·FusedLocation 라이브러리는 향후 앱 프로젝트의 Gradle 의존성으로 별도 연결한다. applicationId·UI·빌드 버전은 아직 결정하지 않았다. 개인 SDK 절대 경로는 공통 설정에 기록하지 않는다.

### 계정별 확인표

계정별 OS 배정은 아직 확인하지 않았다. 개인 SDK 경로·자격 증명은 기록하지 않는다.

| 항목 | 2019100743 | seoddhh | dionypark |
| --- | --- | --- | --- |
| OS·CPU / 개발 역할 | 미확인 | 미확인 | 미확인 |
| Python 3.9+ / 문서 검사 | 미확인 | 미확인 | 미확인 |
| Android Studio·JDK | 미확인 | 미확인 | 미확인 |
| Gradle·AGP·Android SDK | 미확인 | 미확인 | 미확인 |
| Emulator·Vehicle Service·AVD | 미확인 | 미확인 | 미확인 |
| ADB 연결·Virtual Console | NOT_RUN | NOT_RUN | NOT_RUN |
| Workspace 권한·SDK 실제 호출 | NOT_RUN | NOT_RUN | NOT_RUN |
| AI 규칙 로딩 | NOT_RUN | NOT_RUN | NOT_RUN |

## 이후 검증 기록

- 서버·DB를 추가하면 의존성 설치·테스트·개발 데이터 초기화·종료 방법을 등록한다.
- SDK 작업에는 [API 계약 양식](../templates/api-contract.md)을 쓰고 사용 버전·권한·지원·실제 호출을 분리한다.
- `local.properties`, `.env`, 키·서명·개인 경로는 로컬에서 관리한다. 공유 가능한 설정 키만 `.env.example`로 제공한다.
