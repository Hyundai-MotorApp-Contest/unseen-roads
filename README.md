# UNSEEN · unseen-roads

같은 길, 다른 시간. 서로 다른 시간에 같은 도로를 지나간 사람들의 경험을 연결하는 차량 기반 비동기 소셜 서비스.

**제품 상태: Discovery / 구현 전.** 현대자동차그룹 차량용 앱 개발 공모전의 Pleos 기반 MVP를 준비한다.
제품 가설과 범위는 [intent.md](intent.md)에 있으며, 차량 앱 빌드·실행 방법은 [개발 환경](docs/environment.md)에 있다.

## 팀원이 처음 시작할 때

1. 이 저장소를 clone하고 [온보딩](docs/onboarding.md)을 따른다.
2. [제품 기획](intent.md), [프로젝트 맥락](docs/project-context.md), [결정·미정 사항](docs/decisions.md)을 확인한다.
3. Codex 또는 Claude Code를 **이 저장소 폴더에서** 시작하고 공통 규칙 로딩을 확인한다.
4. [작업 목록](tasks/README.md)에서 담당 범위를 정하고 작업별 브랜치 → 검증 → PR → 다른 팀원 리뷰로 공유한다.

이 저장소의 문서가 UNSEEN 작업 기준이다. 별도 `pleos-dev-harness` clone·설치·자동 동기화는 필요 없다.
AI 대화와 개인 메모는 자동 공유되지 않으므로 결정과 진행 상황은 저장소·이슈·PR에 남긴다.

## 문서와 하네스

| 파일 | 역할 |
| --- | --- |
| [AGENTS.md](AGENTS.md) / [CLAUDE.md](CLAUDE.md) | AI 공통 규칙과 Claude Code 진입점 |
| [프로젝트 맥락](docs/project-context.md) / [결정 기록](docs/decisions.md) | 제품 기준·확정/제안/미정 구분 |
| [개발 환경](docs/environment.md) / [협업 흐름](docs/workflow.md) | OS별 시작과 작업 공유 |
| [Pleos 조사](docs/pleos.md) / [공모전 조사](docs/competition.md) | 2026-10-29 조사 스냅샷; 사용 전 공식 원문 재확인 |
| [UNSEEN 검증 시나리오](docs/verification-scenarios.md) | 동의·차량 상태·공유 구간·프라이버시·콘텐츠 검증 |
| [작업 목록](tasks/README.md) / [양식](templates/README.md) / [프롬프트](PROMPTS.md) | 작업·API 계약·검증·인수인계 |
| [이관 기록](docs/harness-migration.md) / [검증 결과](docs/validation.md) | 원본 출처·폐기 기준·이번 확인 수준 |

## 지금 실행할 수 있는 검사

Python 3.9 이상, 외부 패키지 없이 실행한다.

macOS:

```sh
python3 scripts/check_harness.py
```

Windows PowerShell:

```powershell
py -3 scripts/check_harness.py
```

문서 링크·규칙 파일·파일 형식을 검사한다. 앱 빌드·차량 SDK 동작·규칙 준수를 검증하는 명령은 아니다.
Android·서버 명령은 실제 프로젝트 구성 후 [개발 환경](docs/environment.md)에 추가한다.
