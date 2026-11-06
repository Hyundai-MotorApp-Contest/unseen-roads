# UNSEEN 하네스 검증 결과

실행일: 2026-10-30 KST. 하네스 문서와 도구에 대한 검증이며 앱 동작 결과가 아니다.

| 확인 항목 | 상태 | 근거 |
| --- | --- | --- |
| 원본 commit / 원격 일치 | PASS | 원본 로컬 clean, GitHub main과 SHA 일치. 이관 기록 참고 |
| 사용자 intent.md 보존 | PASS | 이관 전후 SHA256 일치. 원문은 편집하지 않음 |
| 문서·규칙·파일 형식 검사 | PASS | python3 scripts/check_harness.py: 필수 파일 30개, Markdown 25개, 로컬 링크 70개 통과 |
| 실패 탐지 확인 | PASS | 임시 복사본에서 없는 파일·없는 heading·저장소 밖 링크·누락 import가 각각 exit 1. 복원 후 exit 0 |
| 변경 공백 검사 | PASS | git diff --check 및 git diff --cached --check -- . ':!intent.md' 통과. 사용자 원문 줄바꿈 보존 |
| GitHub PR 공유 | PASS | codex/unseen-harness 푸시 및 Draft PR #1 생성. main 미머지 |
| Windows에서 검사 실행 | PASS | 2026-11-05 Windows 11 / Python 3.12.10, py -3 scripts/check_harness.py. 처음에는 core.autocrlf=true로 LF 규칙 밖 파일이 CRLF가 되어 FAIL. .gitattributes 기본값을 eol=lf로 바꾼 뒤 재체크아웃하여 PASS |
| 각 팀원 Codex·Claude 규칙 로딩 | NOT_RUN | 팀원 세션을 실행하지 않음 |
| Android build·SDK·Emulator·서버 | NOT_RUN | 앱·서버 프로젝트 미생성 |
| UNSEEN 제품 검증 시나리오 | NOT_RUN | 계획만 작성, 실제 앱 없음 |
| CI·브랜치 보호·필수 승인 | NOT_RUN | 이번 작업에서 설정하지 않음 |

## 검사 범위와 한계

`scripts/check_harness.py`는 필수 파일, 로컬 Markdown 링크와 heading anchor, UTF-8/LF·줄 끝 공백,
Markdown fence, Claude import, 이전 저장소 전용 규칙 잔존을 검사한다.
코드 블록 속 예시 링크와 외부 URL 접근·비밀값 유무·AI 규칙 준수·SDK 가용성은 자동 검사하지 않는다.
원본 조사 문서의 외부 링크는 조사 당시 출처이며 이번에 최신성·접속 성공을 재검증하지 않았다.

## 실행 환경과 결과

- 환경: macOS / arm64 / Python 3.14.7. Python 3.9에서의 실행은 별도 수행하지 않았다.
- `python3 scripts/check_harness.py` → exit 0, 필수 파일·링크·anchor·fence·규칙 import·현재 저장소 역할 검사 통과.
- 임시 디렉터리에 복사한 뒤 4종 결함을 각각 주입 → exit 1과 해당 오류 메시지 확인. 복원 후 PASS. 실제 작업 파일에는 결함을 넣지 않았다.
- `intent.md` 원문 SHA256 일치 확인 → PASS. Markdown의 의도된 두 칸 줄바꿈을 보존했다.
- `git diff --check` → PASS. 사용자 원문 intent.md는 공백을 정리하지 않는다.

## GitHub 공유

- [Draft PR #1](https://github.com/Hyundai-MotorApp-Contest/unseen-roads/pull/1)
- 브랜치: `codex/unseen-harness` → `main`
- 구현 커밋: `60c02d1` (공유 기록 갱신은 후속 커밋)
- 다른 팀원 승인·main 머지·원본 저장소 Archive/삭제는 수행하지 않았다.
