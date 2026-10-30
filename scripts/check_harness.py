#!/usr/bin/env python3
"""Check this repo's harness files with Python 3.9+, without dependencies."""

import re
import sys
from pathlib import Path
from urllib.parse import unquote, urlsplit


ROOT = Path(__file__).resolve().parents[1]
REQUIRED = (
    "README.md", "AGENTS.md", "CLAUDE.md", "intent.md", "PROMPTS.md",
    "docs/project-context.md", "docs/decisions.md", "docs/environment.md",
    "docs/workflow.md", "docs/onboarding.md", "docs/verification-scenarios.md",
    "docs/harness-migration.md", "docs/validation.md", "docs/pleos.md",
    "docs/competition.md", "tasks/README.md", "tasks/000-harness.md",
    "tasks/001-feasibility.md", "templates/README.md", "templates/task.md",
    "templates/api-contract.md", "templates/verification.md", "templates/handoff.md",
    "templates/decision.md", ".github/pull_request_template.md",
    ".github/ISSUE_TEMPLATE/task.md", ".gitignore", ".gitattributes", ".editorconfig",
    "scripts/check_harness.py",
)


def outside_code(source):
    """Keep line numbers, skipping fenced blocks and inline code examples."""
    fence = None
    result = []
    for line in source.splitlines():
        marker = re.match(r"^\s{0,3}(`{3,}|~{3,})(.*)$", line)
        if marker:
            run, suffix = marker.groups()
            if fence is None:
                fence = run
            elif run[0] == fence[0] and len(run) >= len(fence) and not suffix.strip():
                fence = None
            result.append("")
        elif fence:
            result.append("")
        else:
            result.append(re.sub(r"(`+).*?\1", "", line))
    if fence:
        raise ValueError("unclosed Markdown fence")
    return "\n".join(result)


def anchors(source):
    counts = {}
    found = set()
    for heading in re.findall(r"^#{1,6}\s+(.+?)\s*#*\s*$", outside_code(source), re.M):
        heading = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", heading).lower()
        slug = "".join(c for c in heading if c.isalnum() or c in "_- ").replace(" ", "-")
        count = counts.get(slug, 0)
        counts[slug] = count + 1
        found.add(f"{slug}-{count}" if count else slug)
    return found


def main():
    errors = []
    texts = {}
    links = 0
    for rel in REQUIRED:
        path = ROOT / rel
        if not path.is_file():
            errors.append(f"{rel}: missing required file")
            continue
        try:
            data = path.read_bytes()
            source = data.decode("utf-8")
        except UnicodeError:
            errors.append(f"{rel}: not UTF-8")
            continue
        texts[path] = source
        if b"\r" in data or not source.endswith("\n"):
            errors.append(f"{rel}: expected LF and final newline")
        # Preserve user-authored Markdown's intentional two-space hard breaks.
        if rel != "intent.md":
            for line_no, line in enumerate(source.splitlines(), 1):
                if line != line.rstrip():
                    errors.append(f"{rel}:{line_no}: trailing whitespace")

    documents = {ROOT / "README.md", ROOT / "AGENTS.md", ROOT / "CLAUDE.md", ROOT / "PROMPTS.md"}
    for directory in ("docs", "tasks", "templates", ".github"):
        documents.update((ROOT / directory).rglob("*.md"))
    for path in sorted(documents):
        if not path.is_file():
            continue
        rel = path.relative_to(ROOT)
        try:
            source = texts.get(path)
            if source is None:
                source = path.read_text(encoding="utf-8")
            prose = outside_code(source)
        except (UnicodeError, ValueError) as exc:
            errors.append(f"{rel}: {exc}")
            continue
        for match in re.finditer(r"!?\[[^\]\n]*\]\(([^)\s]+)\)", prose):
            target = match.group(1).strip("<>")
            url = urlsplit(target)
            if url.scheme or url.netloc:
                continue
            links += 1
            resolved = (path.parent / unquote(url.path)).resolve() if url.path else path
            if not resolved.is_relative_to(ROOT):
                errors.append(f"{rel}: local link leaves repository: {target}")
            elif not resolved.exists():
                errors.append(f"{rel}: broken local link: {target}")
            elif url.fragment and resolved.suffix == ".md":
                try:
                    headings = anchors(resolved.read_text(encoding="utf-8"))
                    if unquote(url.fragment) not in headings:
                        errors.append(f"{rel}: missing heading: {target}")
                except (UnicodeError, ValueError) as exc:
                    errors.append(f"{rel}: invalid link target {target}: {exc}")

    claude = texts.get(ROOT / "CLAUDE.md", "")
    agents = texts.get(ROOT / "AGENTS.md", "")
    if claude.splitlines()[:1] != ["@AGENTS.md"]:
        errors.append("CLAUDE.md: first line must import @AGENTS.md")
    if len(agents.encode("utf-8")) >= 32768:
        errors.append("AGENTS.md: exceeds default 32 KiB instruction budget")
    if "Hyundai-MotorApp-Contest/unseen-roads" not in agents:
        errors.append("AGENTS.md: missing app repository identity")
    for stale in ("앱 코드·Gradle 프로젝트·CI·에뮬레이터 설치를 추가하지 않는다", "이 저장소는 AI 규칙·작업 문서용 하네스다"):
        if stale in agents:
            errors.append("AGENTS.md: stale harness-only restriction")
    if errors:
        print("FAIL: harness checks")
        for error in errors:
            print(f"- {error}")
        return 1
    print(f"PASS: {len(REQUIRED)} required files, {len(documents)} Markdown documents, {links} local links; format, fences, anchors, import and repository scope")
    return 0


if __name__ == "__main__":
    sys.exit(main())
