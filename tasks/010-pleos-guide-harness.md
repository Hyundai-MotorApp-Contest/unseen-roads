# 작업: Pleos 공식 가이드 하네스

- 상태: IN_REVIEW
- 담당자: Claude Code / 팀원 리뷰 미정
- 사용 AI 도구: Claude Code (Claude in Chrome로 공식 문서 열람)
- 관련 이슈·PR: 미정
- 시작일 / 갱신일: 2026-11-06 KST

## 목표와 완료 조건

- 해결할 문제: Pleos 앱 기획·설계 가이드와 앱 개발·연동 가이드의 요구사항이 개발 중에 추적·검증되지 않는다.
- 사용자가 확인한 요구: 두 가이드의 하위 문서를 직접 분석해 요구사항 ID·출처·검증 방법을 갖춘 문서 하네스와 자동 검사 도구를 만든다. 앱 기능·비즈니스 로직은 바꾸지 않는다.
- 관찰 가능한 완료 조건: `docs/pleos/` 11개 문서, `scripts/pleos/check-compliance.sh`, AGENTS.md 통합, 현재 앱과의 차이 분석, 자동·수동 검증 구분.
- intent.md의 P0/제품 원칙과 연결: 차량 UX·오디오·동의·신고 등 P0의 플랫폼 기준을 제공한다.
- 관련 ourroad 검증 시나리오 ID: U-01~03, U-05~07, U-14, U-18, U-19, U-21(요구사항 연결만, 실행 안 함)

## 범위

- 수정할 파일·기능: `docs/pleos/*`, `scripts/pleos/*`, `AGENTS.md`(Pleos 절 추가), `tasks/README.md`, `docs/pleos.md`·`docs/project-context.md`·`docs/environment.md`(안내 한 줄), `scripts/check_harness.py`(필수 파일 목록).
- 이번 작업에서 다루지 않을 항목: 앱 코드·Manifest·Gradle 변경, SDK 연동, Emulator 시나리오 실행, 디자인 자산 다운로드.
- 다른 작업과 겹치는 파일·담당자: AGENTS.md, tasks/README.md, docs/*.md에 다른 작업의 미커밋 변경이 있어 해당 줄만 추가했다.
- 의존 작업: 001(기술 가능성 확인)이 이 하네스의 요구사항 ID를 사용한다.

## 근거와 열린 질문

- 필요한 문서·API URL·버전: [docs/pleos/sources.md](../docs/pleos/sources.md).
- 확인된 결정: 없음. 카테고리·DO 범위·CCP·버전 고정·지역 정책·음성 기능 허용 근거는 [로드맵](../docs/pleos/implementation-roadmap.md)의 D1~D6으로 남겼다.
- 사용자 답변이 필요한 질문: D1~D6, CCP UX Guideline·디자인 자산 다운로드 여부(X-02·X-03).

## 실행·검증 기록

| 확인 항목 | 상태 | 명령/절차·환경·실제 결과·증거 |
| --- | --- | --- |
| 공식 문서 열람 | PASS | Chrome에서 렌더링 페이지 열람. 설계 원칙 10, 디자인 가이드 40(사이드바 34 + 사이트맵 6), 개발 가이드 11, SDK 프로그래밍 가이드 45, Gleo 6, 인접 문서 7. API Reference 1,816개는 색인만(2개 열람) |
| 접근 불가·미공개 구분 | PASS | sources.md X-01~X-15, 불일치 C-01~C-07 |
| 요구사항 추출 | PASS | 170개(MUST 104, MUST_NOT 21, SHOULD 45), H02 검사 |
| 준수 매트릭스 | PASS | 170개 전부 추적, H03 검사 |
| 준수 검사 전체 | 실행함(종료 코드 1) | `scripts/pleos/check-compliance.sh`: PASS 21, FAIL 3(C15 MAJOR, M12·M13 MINOR), NOT_RUN 3, BLOCKED 1(M14), NOT_APPLICABLE 16. 종료 코드 1은 앱의 MAJOR 위반(C15) 때문이며 검사기는 정상 |
| 검사기 자체 확인 | PASS | 복사본에 위반 15종을 넣어 전부 FAIL 검출. 문자열 안의 `//`를 주석으로 지우던 결함을 이 확인에서 발견해 수정 |
| 빌드·Lint | PASS | macOS arm64, Android Studio JBR, `./gradlew :app:assembleDebug :app:lintDebug`, 오류 0·경고 26 |
| 문서 하네스 | PASS | `python3 scripts/check_harness.py`, `git diff --check` |
| Windows 실행 | NOT_RUN | PowerShell 명령만 문서화 |
| Emulator·실차 검증 | NOT_RUN | 범위 밖 |

## 마무리

- 변경 요약: 공식 가이드 기반 요구사항·매트릭스·가이드 문서와 자동 검사기를 추가하고 AGENTS.md에 준수 절차를 넣었다.
- 남은 문제: C15 상태 저장(MAJOR), M12 백업·M13 아이콘(MINOR), 카테고리 미정(BLOCKED), CCP·Back·데모 문자열(NOT_RUN). 로드맵 참고.
- 다음 행동과 담당자: 팀원이 D1~D6을 논의하고 1단계(주행 규제 상태 관리) 작업 담당을 정한다.
- 리뷰한 다른 팀원·리뷰 근거: 미정
