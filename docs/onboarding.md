# 팀원 시작 안내

## 1. 같은 앱 저장소 받기

GitHub에서 이 Private 저장소에 접근할 수 있는 각자의 계정으로 인증한다. 토큰을 공유하지 않는다.
다음 Git 명령은 macOS 터미널과 Windows PowerShell에서 동일하다.

```sh
git clone https://github.com/Hyundai-MotorApp-Contest/unseen-roads.git
cd unseen-roads
```

이미 clone했다면 중복 clone하지 않는다. `git status`로 미커밋 변경을 확인하고 정리한 뒤 최신 `dev`를 받는다.
머지 전 PR을 검토할 때는 해당 PR의 브랜치를 선택한다. 새 작업은 최신 `dev`에서 [작업 브랜치](workflow.md#브랜치-규칙)를 만든다. `main`은 검증한 데모·제출 버전 기준이다.

## 2. 제품과 규칙 확인

- [제품 기획](../intent.md): 무엇을 왜 만드는지
- [프로젝트 맥락](project-context.md) / [결정 기록](decisions.md): 현재 상태와 미정 선택
- [AGENTS.md](../AGENTS.md): 주행 중 비개입·동의·경로 보호·SDK·협업 기준

Codex에서는 이 저장소를 작업 폴더로 선택해 새 채팅을 시작한다.
Claude Code도 이 저장소에서 시작한다. `CLAUDE.md`가 `@AGENTS.md`를 가져온다.
별도 하네스 저장소를 clone하거나 전역 개인 설정을 덮어쓸 필요는 없다.

새 AI 세션에서 다음으로 실제 적용 여부를 확인한다.

```text
파일은 수정하지 말고 현재 적용되는 규칙 파일과 저장소 역할을 확인해줘.
ourroad의 핵심 흐름, 주행/상태 미상에서 콘텐츠 처리,
동의·삭제 원칙, 현재 실행 가능한 검사와 아직 없는 앱 실행 환경을 설명해줘.
기술 제안과 확정된 제품 요구도 구분해줘.
```

기대 결과: 앱 개발 가능한 저장소임을 알고, 주차·UX 제한 확인 전 콘텐츠를 차단하며,
실제 SDK 검증과 앱 빌드는 아직 수행되지 않았다고 설명한다.
전역 개인 규칙·상위 override·다른 도구 버전의 영향도 있을 수 있으므로 팀원별 실제 세션에서 확인한다.
Claude Code에서는 `/memory`로 규칙 로딩을 점검할 수 있다.

## 3. 문서 검사와 첫 작업

Python 3.9 이상에서 macOS는 `python3 scripts/check_harness.py`, Windows는 `py -3 scripts/check_harness.py`를 실행한다.
외부 패키지가 필요 없으며 이 검사는 앱·SDK 동작을 검증하지 않는다.

[작업 목록](../tasks/README.md)에서 담당을 정한다. 첫 개발 후보는 [기술 가능성 확인](../tasks/001-feasibility.md)이다.
아직 담당자가 정해지지 않았으며 팀원 계정에 임의 할당하지 않았다.

## 공유되는 것

커밋·푸시한 규칙·기획·코드·검증 기록은 같은 저장소를 받는 팀원과 공유된다.
AI 대화, 개인 메모, 환경 설치, 로그인 권한은 자동 공유되지 않는다.
다음 담당자를 위한 결정은 파일·이슈·PR에 남긴다.

## 도구 근거

- [Codex AGENTS.md 안내](https://learn.chatgpt.com/docs/agent-configuration/agents-md)
- [Codex 로컬 스킬 위치](https://learn.chatgpt.com/docs/build-skills): 반복 절차가 필요해지면 이 저장소의 `.agents/skills/`에 추가한다. 현재 별도 스킬은 만들지 않았다.
- [Claude Code 메모리와 import 안내](https://code.claude.com/docs/en/memory)
