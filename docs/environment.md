# 개발 환경과 실행 명령

기준일: 2026-10-30 KST. 설치 완료 전달과 실제 실행 확인을 구분한다.

## 현재 실행 가능 범위

| 항목 | 상태 |
| --- | --- |
| 문서 검사 | Python 3.9+ / 표준 라이브러리만 사용. macOS와 Windows 명령은 README 참고 |
| Android 앱 / Gradle Wrapper | 아직 없음. build·unit test·APK 생성 명령 미등록 |
| 서버 / DB / 마이그레이션 | 아직 없음. 서버 실행·테스트·DB 초기화 명령 미등록 |
| SDK 호출·차량 상태 시나리오 | NOT_RUN |
| IDE·시뮬레이터 | 팀 설치 완료 전달. 각 PC의 버전·연결 상태는 미확인 |

## 팀 환경 확인표

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

## Pleos 가이드 검사

macOS는 `scripts/pleos/check-compliance.sh`, Windows는 `py -3 scripts\pleos\check_compliance.py`를 실행한다. 앱이 없는 문서 브랜치에서는 `--static-only`로 하네스 구조를 확인한다. [Pleos 하네스](pleos/README.md)

## 앱 프로젝트가 생성되면

1. 실제 프로젝트 경로·JDK·Gradle Wrapper·AGP·SDK 버전·호환 근거를 기록한다.
2. macOS와 Windows에서 각각 실제 실행한 build·단위 테스트 명령과 결과를 기록한다. Wrapper가 없는데 명령만 지어내지 않는다.
3. 서버·DB를 추가하면 의존성 설치·테스트·개발 데이터 초기화·종료 방법을 등록한다. 테스트는 합성 경로를 쓴다.
4. SDK 작업에는 [API 계약 양식](../templates/api-contract.md)을 쓰고 사용 버전·권한·지원·실제 호출을 분리한다.

OS별 명령은 macOS `./gradlew`, PowerShell `.\gradlew.bat`처럼 구분하되, **지금 실행 가능한 앱 명령이 아니다.**
`local.properties`, `.env`, 키·서명·개인 경로는 로컬에서 관리한다. 공유 가능한 설정 키만 `.env.example`로 제공한다.
