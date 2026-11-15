#!/usr/bin/env python3
"""Check ourroad against the Pleos guide harness. Python 3.9+, standard library only.

Only statically verifiable items are judged here. Items that need the Pleos
Emulator, a real vehicle, a human review or Pleos/App Market confirmation are
listed as NOT_RUN and never reported as PASS.

Usage:
  python3 scripts/pleos/check_compliance.py [--static-only] [--strict] [--list-manual]
  py -3 scripts\\pleos\\check_compliance.py [...]          (Windows)
  scripts/pleos/check-compliance.sh [...]                  (macOS/Linux/Git Bash)

Exit codes: 0 no BLOCKER/MAJOR failure, 1 BLOCKER/MAJOR failure (or any failure
with --strict), 2 the checker itself could not run.
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DOCS = ROOT / "docs" / "pleos"
APP = ROOT / "app"
MANIFEST = APP / "src" / "main" / "AndroidManifest.xml"
ANDROID = "{http://schemas.android.com/apk/res/android}"

STATUSES = ("PASS", "FAIL", "NOT_RUN", "BLOCKED", "NOT_APPLICABLE")
COMPLIANCE_STATUSES = (
    "COMPLIANT", "NON_COMPLIANT", "PARTIAL", "NOT_IMPLEMENTED", "NOT_APPLICABLE", "UNVERIFIED",
)
LEVELS = ("MUST", "MUST_NOT", "SHOULD", "RETIRED")
SAFETY_CATEGORIES = ("SAFETY", "AUDIO")
ALLOWED_SOURCE_HOSTS = ("https://document.pleos.ai/", "https://source.android.com/")
REQUIRED_FILES = (
    "docs/pleos/README.md", "docs/pleos/sources.md", "docs/pleos/requirements.md",
    "docs/pleos/architecture.md", "docs/pleos/design-guidelines.md",
    "docs/pleos/development-guidelines.md", "docs/pleos/sdk-api-reference.md",
    "docs/pleos/driving-safety.md", "docs/pleos/compliance-matrix.md",
    "docs/pleos/testing-strategy.md", "docs/pleos/implementation-roadmap.md",
    "scripts/pleos/check-compliance.sh", "scripts/pleos/check_compliance.py",
)

# Documented SDK artifacts and versions (docs/pleos/sdk-api-reference.md, checked 2026-11-06).
PLEOS_SDKS = {
    "Vehicle": "2.3.2",
    "NaviHelper": "3.0.5",
    "TextToSpeech": "3.3.1",
    "SpeechToText": "3.3.1",
    "FusedLocation": "1.1.3",
}
PLEOS_MAVEN = "nexus-playground.pleos.ai/repository/maven-releases"
VEHICLE_DOMAINS = (
    "DOORS", "WINDOWS", "CLIMATE", "DRIVE", "ENERGY", "INFO", "LIGHTS", "MIRRORS",
    "SAFETY", "SEATS", "TOLL",
)
KNOWN_PLEOS_PERMISSIONS = {f"CAR_{d}" for d in VEHICLE_DOMAINS} | {
    "TTS_SERVICE", "STT_SERVICE", "FUSED_LOCATION", "NAVI_ROUTE", "NAVI_ROUTE_SEARCH",
    "NAVI_CUSTOM_MAP",
}
SDK_IMPORTS = {
    "Vehicle": "ai.pleos.playground.vehicle",
    "NaviHelper": "ai.pleos.playground.navi",
    "TextToSpeech": "ai.pleos.playground.tts",
    "SpeechToText": "ai.pleos.playground.stt",
    "FusedLocation": "ai.pleos.playground.location",
}
FIXED_ORIENTATIONS = {
    "portrait", "landscape", "sensorPortrait", "sensorLandscape", "reversePortrait",
    "reverseLandscape", "userPortrait", "userLandscape", "locked", "nosensor",
}


@dataclass
class Requirement:
    rid: str
    category: str
    level: str
    methods: str
    sources: list = field(default_factory=list)


@dataclass
class Result:
    cid: str
    title: str
    status: str
    detail: str
    reqs: tuple = ()
    severity: str = ""


class Context:
    def __init__(self):
        self.requirements: dict = {}
        self.manifest = None
        self.manifest_error = ""
        self.app_gradle = read(first_existing(APP / "build.gradle.kts", APP / "build.gradle"))
        self.settings_gradle = read(first_existing(ROOT / "settings.gradle.kts", ROOT / "settings.gradle"))
        self.version_catalog = read(ROOT / "gradle" / "libs.versions.toml")
        self.sources = load_sources()
        self.code = "\n".join(strip_comments(text) for text in self.sources.values())
        self.dependencies = find_pleos_dependencies(self.app_gradle + "\n" + self.version_catalog)
        if MANIFEST.is_file():
            try:
                self.manifest = ET.parse(MANIFEST).getroot()
            except ET.ParseError as exc:
                self.manifest_error = str(exc)
        else:
            self.manifest_error = "app/src/main/AndroidManifest.xml not found"

    def application(self):
        return None if self.manifest is None else self.manifest.find("application")

    def components(self):
        app = self.application()
        if app is None:
            return []
        return [c for c in app if c.tag in ("activity", "activity-alias", "service", "receiver", "provider")]

    def activities(self):
        return [c for c in self.components() if c.tag in ("activity", "activity-alias")]

    def permissions(self):
        if self.manifest is None:
            return []
        return [p.get(ANDROID + "name", "") for p in self.manifest.findall("uses-permission")]

    def pleos_permissions(self):
        prefix = "pleos.car.permission."
        return [p[len(prefix):] for p in self.permissions() if p.startswith(prefix)]

    def used_sdks(self):
        used = set(self.dependencies)
        for sdk, package in SDK_IMPORTS.items():
            if re.search(r"import\s+" + re.escape(package) + r"\b", self.code):
                used.add(sdk)
        return used

    def do_activities(self):
        found = []
        for activity in self.activities():
            for meta in activity.findall("meta-data"):
                if meta.get(ANDROID + "name") == "distractionOptimized" and meta.get(ANDROID + "value") == "true":
                    found.append(activity.get(ANDROID + "name", "?"))
        return found


def read(path):
    if path is None or not path.is_file():
        return ""
    try:
        return path.read_text(encoding="utf-8")
    except UnicodeError:
        return ""


def first_existing(*paths):
    for path in paths:
        if path.is_file():
            return path
    return None


def strip_comments(text):
    """Remove // and /* */ comments from Kotlin/Java, keeping string literals intact."""
    out = []
    i, n = 0, len(text)
    while i < n:
        ch = text[i]
        if text.startswith('"""', i):
            end = text.find('"""', i + 3)
            end = n if end < 0 else end + 3
            out.append(text[i:end])
            i = end
        elif ch in "\"'":
            j = i + 1
            while j < n and text[j] != ch and text[j] != "\n":
                j += 2 if text[j] == "\\" else 1
            out.append(text[i:j + 1])
            i = j + 1
        elif text.startswith("//", i):
            end = text.find("\n", i)
            i = n if end < 0 else end
        elif text.startswith("/*", i):
            end = text.find("*/", i + 2)
            i = n if end < 0 else end + 2
        else:
            out.append(ch)
            i += 1
    return "".join(out)


def load_sources():
    found = {}
    for module in [p for p in ROOT.iterdir() if (p / "src" / "main").is_dir()]:
        for directory in ("java", "kotlin"):
            base = module / "src" / "main" / directory
            if base.is_dir():
                for path in sorted(base.rglob("*")):
                    if path.suffix in (".kt", ".java") and path.is_file():
                        found[path.relative_to(ROOT).as_posix()] = read(path)
    return found


def find_pleos_dependencies(text):
    deps = {}
    for artifact, version in re.findall(r"ai\.pleos\.playground:([A-Za-z]+):([\w.\-]+)", text):
        deps[artifact] = version
    for artifact in re.findall(r'module\s*=\s*"ai\.pleos\.playground:([A-Za-z]+)"', text):
        deps.setdefault(artifact, "catalog")
    return deps


def tracked_files():
    try:
        out = subprocess.run(
            ["git", "ls-files", "--cached", "--others", "--exclude-standard", "-z"],
            cwd=ROOT, capture_output=True, check=True,
        ).stdout.decode("utf-8", "replace")
        return [ROOT / name for name in out.split("\0") if name]
    except (OSError, subprocess.CalledProcessError):
        skip = {".git", ".gradle", "build", ".idea", ".kotlin"}
        return [p for p in ROOT.rglob("*") if p.is_file() and not skip.intersection(p.relative_to(ROOT).parts)]


def files_with(pattern, ctx):
    regex = re.compile(pattern)
    return sorted({path for path, text in ctx.sources.items() if regex.search(strip_comments(text))})


# --------------------------------------------------------------------------- harness

def parse_requirements():
    text = read(DOCS / "requirements.md")
    reqs = {}
    rows = []
    for line in text.splitlines():
        if line.startswith("| PLEOS-"):
            cells = [c.strip() for c in line.strip().strip("|").split("|")]
            rows.append(cells)
    refs = dict(re.findall(r"(?m)^\[([A-Za-z0-9\-]+)\]:\s+(\S+)\s*$", text))
    return text, rows, refs, reqs


def check_harness(ctx):
    results = []
    missing = [f for f in REQUIRED_FILES if not (ROOT / f).is_file()]
    results.append(Result("H01", "하네스 필수 파일", "FAIL" if missing else "PASS",
                          "없음: " + ", ".join(missing) if missing else f"{len(REQUIRED_FILES)}개 존재"))

    text, rows, refs, _ = parse_requirements()
    problems = []
    for cells in rows:
        if len(cells) != 7:
            problems.append(f"{cells[0]}: 열 {len(cells)}개(7개 필요)")
            continue
        rid, level, _cond, _body, _crit, methods, source = cells
        match = re.fullmatch(r"PLEOS-([A-Z]+)-(\d{3})", rid)
        if not match:
            problems.append(f"{rid}: ID 형식 오류")
            continue
        if rid in ctx.requirements:
            problems.append(f"{rid}: 중복 ID")
        if level not in LEVELS:
            problems.append(f"{rid}: 수준 '{level}' 미정의")
        keys = re.findall(r"\[([A-Za-z0-9\-]+)\]", source)
        if not keys:
            problems.append(f"{rid}: 출처 없음")
        for key in keys:
            url = refs.get(key)
            if url is None:
                problems.append(f"{rid}: 출처 [{key}] 정의 없음")
            elif not url.startswith(ALLOWED_SOURCE_HOSTS):
                problems.append(f"{rid}: 출처 [{key}]가 공식 문서가 아님")
        ctx.requirements[rid] = Requirement(rid, match.group(1), level, methods, [refs.get(k, "") for k in keys])
    declared = {}
    in_count = False
    for line in text.splitlines():
        if line.startswith("## "):
            in_count = line.strip() == "## 개수"
        elif in_count:
            m = re.match(r"\|\s*([A-Z]+|합계)\s*\|\s*(\d+)\s*\|", line)
            if m:
                declared[m.group(1)] = int(m.group(2))
    actual = {}
    for req in ctx.requirements.values():
        actual[req.category] = actual.get(req.category, 0) + 1
    actual["합계"] = len(ctx.requirements)
    if declared != actual:
        problems.append(f"개수 표 불일치: 문서 {declared} / 실제 {actual}")
    levels = {}
    for req in ctx.requirements.values():
        levels[req.level] = levels.get(req.level, 0) + 1
    detail = f"{len(ctx.requirements)}개, 수준 {levels}"
    results.append(Result("H02", "요구사항 ID·수준·출처", "FAIL" if problems else "PASS",
                          "; ".join(problems[:12]) + (" ..." if len(problems) > 12 else "") if problems else detail))

    matrix = read(DOCS / "compliance-matrix.md")
    seen = {}
    m_problems = []
    for line in matrix.splitlines():
        if line.startswith("| PLEOS-"):
            cells = [c.strip() for c in line.strip().strip("|").split("|")]
            if len(cells) != 5:
                m_problems.append(f"{cells[0]}: 열 {len(cells)}개(5개 필요)")
                continue
            rid, _src, _target, status, _verify = cells
            if rid in seen:
                m_problems.append(f"{rid}: 매트릭스 중복")
            seen[rid] = status
            if status not in COMPLIANCE_STATUSES:
                m_problems.append(f"{rid}: 상태 '{status}' 미정의")
    for rid in ctx.requirements:
        if rid not in seen:
            m_problems.append(f"{rid}: 매트릭스에 없음")
    for rid in seen:
        if rid not in ctx.requirements:
            m_problems.append(f"{rid}: 요구사항에 없음")
    counted = {}
    for status in seen.values():
        counted[status] = counted.get(status, 0) + 1
    summary = {}
    in_summary = False
    for line in matrix.splitlines():
        if line.startswith("## "):
            in_summary = line.strip() == "## 요약"
        elif in_summary:
            m = re.match(r"\|\s*([A-Z_]+|합계)\s*\|\s*(\d+)\s*\|", line)
            if m:
                summary[m.group(1)] = int(m.group(2))
    expected = {k: v for k, v in counted.items()}
    expected["합계"] = len(seen)
    if {k: v for k, v in summary.items() if v} != {k: v for k, v in expected.items() if v}:
        m_problems.append(f"요약 표 불일치: 문서 {summary} / 실제 {expected}")
    results.append(Result("H03", "준수 매트릭스 추적성", "FAIL" if m_problems else "PASS",
                          "; ".join(m_problems[:12]) + (" ..." if len(m_problems) > 12 else "")
                          if m_problems else f"{len(seen)}개 추적, 상태 {counted}"))

    known = set(CHECKS)
    bad = []
    for req in ctx.requirements.values():
        for group in re.findall(r"AUTO\(([^)]*)\)", req.methods):
            for cid in re.split(r"[,\s]+", group.strip()):
                if cid and cid not in known:
                    bad.append(f"{req.rid}: {cid}")
    results.append(Result("H04", "요구사항이 참조하는 자동 검사 ID", "FAIL" if bad else "PASS",
                          "정의되지 않은 검사: " + ", ".join(bad) if bad else "모두 정의됨"))
    return results


# --------------------------------------------------------------------------- gradle

def check_gradle(ctx):
    results = []
    m = re.search(r"minSdk(?:Version)?\s*(?:=|\(|\s)\s*(\d+)", ctx.app_gradle)
    if not m:
        results.append(Result("G01", "minSdk >= 28", "NOT_RUN", "app 빌드 파일에서 minSdk를 찾지 못함"))
    else:
        value = int(m.group(1))
        results.append(Result("G01", "minSdk >= 28", "PASS" if value >= 28 else "FAIL", f"minSdk={value}"))

    props = read(ROOT / "gradle" / "wrapper" / "gradle-wrapper.properties")
    m = re.search(r"gradle-(\d+)\.(\d+)(?:\.\d+)?-(?:bin|all)\.zip", props)
    if not m:
        results.append(Result("G02", "Gradle >= 8.0", "NOT_RUN", "Wrapper 버전을 찾지 못함"))
    else:
        major, minor = int(m.group(1)), int(m.group(2))
        ok = (major, minor) >= (8, 0)
        results.append(Result("G02", "Gradle >= 8.0", "PASS" if ok else "FAIL", f"Gradle {major}.{minor}"))

    if not ctx.dependencies:
        results.append(Result("G03", "Pleos Maven 저장소", "NOT_APPLICABLE", "Pleos SDK 의존성 없음"))
        results.append(Result("G04", "Pleos SDK 좌표·버전", "NOT_APPLICABLE", "Pleos SDK 의존성 없음"))
    else:
        has_repo = PLEOS_MAVEN in ctx.settings_gradle or PLEOS_MAVEN in ctx.app_gradle
        results.append(Result("G03", "Pleos Maven 저장소", "PASS" if has_repo else "FAIL",
                              "등록됨" if has_repo else f"{PLEOS_MAVEN} 미등록"))
        unknown = [a for a in ctx.dependencies if a not in PLEOS_SDKS]
        differs = [f"{a} {v}(문서 {PLEOS_SDKS[a]})" for a, v in ctx.dependencies.items()
                   if a in PLEOS_SDKS and v != PLEOS_SDKS[a]]
        if unknown:
            results.append(Result("G04", "Pleos SDK 좌표·버전", "FAIL", "문서에 없는 artifact: " + ", ".join(unknown)))
        elif differs:
            results.append(Result("G04", "Pleos SDK 좌표·버전", "NOT_RUN",
                                  "문서 기준과 다른 버전: " + ", ".join(differs) + ". 근거를 docs/decisions.md에 기록"))
        else:
            results.append(Result("G04", "Pleos SDK 좌표·버전", "PASS",
                                  ", ".join(f"{a} {v}" for a, v in ctx.dependencies.items())))

    release = re.search(r"(?:getByName\(\"release\"\)|release)\s*\{(.*?)\n\s*\}", ctx.app_gradle, re.S)
    debuggable = release and re.search(r"(isDebuggable\s*=\s*true|debuggable\s+true)", release.group(1))
    results.append(Result("G05", "release 빌드 비디버그", "FAIL" if debuggable else "PASS",
                          "release에 debuggable=true" if debuggable else "release debuggable 설정 없음(기본 false)"))
    return results


# --------------------------------------------------------------------------- manifest

def check_manifest(ctx):
    if ctx.manifest is None:
        return [Result(cid, "AndroidManifest", "BLOCKED", ctx.manifest_error) for cid in
                ("M01", "M02", "M03", "M04", "M05", "M06", "M07", "M08", "M09", "M10", "M11", "M12",
                 "M13", "M14", "M15", "M16", "M17")]
    results = []
    app = ctx.application()

    automotive = any(f.get(ANDROID + "name") == "android.hardware.type.automotive"
                     and f.get(ANDROID + "required", "true") == "true"
                     for f in ctx.manifest.findall("uses-feature"))
    results.append(Result("M01", "AAOS automotive feature(프로젝트 기준)", "PASS" if automotive else "FAIL",
                          "android.hardware.type.automotive required" if automotive else "선언 없음"))

    pleos = ctx.pleos_permissions()
    if not pleos:
        results.append(Result("M02", "Pleos 퍼미션 이름", "NOT_APPLICABLE", "pleos.car.permission.* 선언 없음"))
    else:
        unknown = [p for p in pleos if p not in KNOWN_PLEOS_PERMISSIONS
                   and not (p.startswith("CONTROL_CAR_") and p[len("CONTROL_"):] in KNOWN_PLEOS_PERMISSIONS)]
        results.append(Result("M02", "Pleos 퍼미션 이름", "NOT_RUN" if unknown else "PASS",
                              "문서 대표 목록에 없음(API Reference @RequiresPermission 확인): " + ", ".join(unknown)
                              if unknown else ", ".join(pleos)))

    used = ctx.used_sdks()
    needs = {
        "TextToSpeech": lambda ps: "TTS_SERVICE" in ps,
        "SpeechToText": lambda ps: "STT_SERVICE" in ps,
        "FusedLocation": lambda ps: "FUSED_LOCATION" in ps,
        "NaviHelper": lambda ps: any(p.startswith("NAVI_") for p in ps),
        "Vehicle": lambda ps: any(p.startswith(("CAR_", "CONTROL_CAR_")) for p in ps),
    }
    if not used:
        results.append(Result("M03", "SDK 사용 시 필수 퍼미션", "NOT_APPLICABLE", "Pleos SDK 사용 없음"))
    else:
        lacking = [sdk for sdk in sorted(used) if sdk in needs and not needs[sdk](pleos)]
        results.append(Result("M03", "SDK 사용 시 필수 퍼미션", "FAIL" if lacking else "PASS",
                              "퍼미션 없음: " + ", ".join(lacking) if lacking else "사용 SDK: " + ", ".join(sorted(used))))

    owner = {
        "TTS_SERVICE": "TextToSpeech", "STT_SERVICE": "SpeechToText", "FUSED_LOCATION": "FusedLocation",
    }
    orphan = []
    for p in pleos:
        sdk = owner.get(p) or ("NaviHelper" if p.startswith("NAVI_") else "Vehicle")
        if sdk not in used:
            orphan.append(p)
    others = [p for p in ctx.permissions() if not p.startswith("pleos.car.permission.")]
    if orphan:
        results.append(Result("M04", "사용하지 않는 퍼미션", "FAIL", "SDK 사용 없이 선언: " + ", ".join(orphan)))
    elif others:
        results.append(Result("M04", "사용하지 않는 퍼미션", "NOT_RUN", "Android 권한 용도 수동 확인: " + ", ".join(others)))
    else:
        results.append(Result("M04", "사용하지 않는 퍼미션", "PASS", "불필요 퍼미션 없음(선언 " + str(len(pleos)) + "개)"))

    if pleos:
        control = [p for p in pleos if p.startswith("CONTROL_CAR_")]
        results.append(Result("M05", "Pleos 퍼미션 App Market 승인", "NOT_RUN",
                              "승인 근거 확인 필요(EXT)" + ("; 제어 퍼미션: " + ", ".join(control) if control else "")))
    else:
        results.append(Result("M05", "Pleos 퍼미션 App Market 승인", "NOT_APPLICABLE", "Pleos 퍼미션 없음"))

    misplaced = [m for m in app.findall("meta-data") if m.get(ANDROID + "name") == "distractionOptimized"] if app is not None else []
    bad_values = []
    for activity in ctx.activities():
        for meta in activity.findall("meta-data"):
            if meta.get(ANDROID + "name") == "distractionOptimized" and meta.get(ANDROID + "value") not in ("true", "false"):
                bad_values.append(activity.get(ANDROID + "name", "?"))
    do = ctx.do_activities()
    if misplaced or bad_values:
        results.append(Result("M06", "DO 메타데이터 선언", "FAIL",
                              ("<application>에 선언됨(Activity에 선언해야 함) " if misplaced else "") +
                              ("값 오류: " + ", ".join(bad_values) if bad_values else "")))
    else:
        results.append(Result("M06", "DO 메타데이터 선언", "PASS",
                              "DO Activity: " + ", ".join(do) if do else "DO 선언 없음: 주행 중 플랫폼이 앱 사용을 막음"))

    fixed = [f"{a.get(ANDROID + 'name', '?')}={a.get(ANDROID + 'screenOrientation')}" for a in ctx.activities()
             if a.get(ANDROID + "screenOrientation") in FIXED_ORIENTATIONS]
    values = sorted({a.get(ANDROID + "screenOrientation", "미지정") for a in ctx.activities()})
    results.append(Result("M07", "화면 방향 고정 없음", "FAIL" if fixed else "PASS",
                          "고정: " + ", ".join(fixed) if fixed else "screenOrientation: " + ", ".join(values) +
                          ". 두 방향 레이아웃은 폼 팩터 테스트로 확인"))

    rotary = [m.get(ANDROID + "value") for m in (app.findall("meta-data") if app is not None else [])
              if m.get(ANDROID + "name") == "com.hmg.rotary.ROTARY_SUPPORTED"]
    if rotary:
        results.append(Result("M08", "CCP 지원 선언", "PASS", f"ROTARY_SUPPORTED={rotary[0]}"))
    else:
        results.append(Result("M08", "CCP 지원 선언", "NOT_RUN",
                              "선언 없음. CCP 지원 여부를 결정하고 미지원이면 ROTARY_SUPPORTED=false 선언"))

    gleo_actions = gleo_function_names()
    exposed = []
    for comp in ctx.components():
        if comp.get(ANDROID + "exported") != "true" or comp.get(ANDROID + "permission"):
            continue
        actions = {a.get(ANDROID + "name") for f in comp.findall("intent-filter") for a in f.findall("action")}
        categories = {c.get(ANDROID + "name") for f in comp.findall("intent-filter") for c in f.findall("category")}
        if "android.intent.action.MAIN" in actions and "android.intent.category.LAUNCHER" in categories:
            continue
        if actions & {"android.media.browse.MediaBrowserService", "androidx.media3.session.MediaLibraryService",
                      "androidx.media3.session.MediaSessionService"}:
            continue
        if gleo_actions and actions and actions <= gleo_actions:
            continue
        exposed.append(f"{comp.tag}:{comp.get(ANDROID + 'name', '?')}")
    results.append(Result("M09", "불필요한 exported 컴포넌트", "FAIL" if exposed else "PASS",
                          "노출: " + ", ".join(exposed) if exposed else "LAUNCHER·미디어·Gleo 외 노출 없음"))

    debuggable = app is not None and app.get(ANDROID + "debuggable") == "true"
    results.append(Result("M10", "Manifest debuggable 하드코딩 없음", "FAIL" if debuggable else "PASS",
                          "android:debuggable=true" if debuggable else "미설정"))

    cleartext = app is not None and app.get(ANDROID + "usesCleartextTraffic") == "true"
    nsc = app is not None and app.get(ANDROID + "networkSecurityConfig")
    if cleartext:
        results.append(Result("M11", "평문 통신 비허용", "FAIL", "usesCleartextTraffic=true"))
    elif nsc:
        results.append(Result("M11", "평문 통신 비허용", "NOT_RUN", f"networkSecurityConfig={nsc} 수동 확인"))
    else:
        results.append(Result("M11", "평문 통신 비허용", "PASS", "평문 허용 설정 없음(targetSdk 28+ 기본 차단)"))

    backup = app.get(ANDROID + "allowBackup") if app is not None else None
    rules = app is not None and (app.get(ANDROID + "dataExtractionRules") or app.get(ANDROID + "fullBackupContent"))
    if backup == "false":
        results.append(Result("M12", "자동 백업 비활성·제외", "PASS", "allowBackup=false"))
    elif rules:
        results.append(Result("M12", "자동 백업 비활성·제외", "NOT_RUN", "백업 규칙 파일의 민감 데이터 제외 수동 확인"))
    else:
        results.append(Result("M12", "자동 백업 비활성·제외", "FAIL",
                              f"allowBackup={'미지정(기본 true)' if backup is None else backup}, 백업 규칙 없음"))

    icon = app is not None and app.get(ANDROID + "icon")
    results.append(Result("M13", "앱 아이콘", "PASS" if icon else "FAIL", f"icon={icon}" if icon else "android:icon 없음"))

    app_category = app.get(ANDROID + "appCategory") if app is not None else None
    maps = any(c.get(ANDROID + "name") == "android.intent.category.APP_MAPS"
               for comp in ctx.components() for f in comp.findall("intent-filter") for c in f.findall("category"))
    if app_category == "audio" or maps:
        results.append(Result("M14", "카테고리 선언", "PASS", "Music/Audio" if app_category == "audio" else "Maps/Navigation"))
    elif app_category:
        results.append(Result("M14", "카테고리 선언", "NOT_RUN", f"appCategory={app_category}: Pleos 선언 방법 미확인(X-13)"))
    else:
        results.append(Result("M14", "카테고리 선언", "BLOCKED", "카테고리 미정(PLEOS-CATEGORY-001 결정 필요)"))

    if app_category == "audio":
        media = any(a.get(ANDROID + "name") in ("android.media.browse.MediaBrowserService",
                                                 "androidx.media3.session.MediaLibraryService",
                                                 "androidx.media3.session.MediaSessionService")
                    for comp in ctx.components() if comp.tag == "service"
                    for f in comp.findall("intent-filter") for a in f.findall("action"))
        launchable = any(m.get(ANDROID + "name") == "androidx.car.app.launchable" and m.get(ANDROID + "value") == "true"
                         for m in app.findall("meta-data"))
        missing = [n for n, ok in (("MediaBrowserService/MediaLibraryService", media),
                                   ("androidx.car.app.launchable", launchable)) if not ok]
        results.append(Result("M15", "Music/Audio 필수 선언", "FAIL" if missing else "PASS",
                              "없음: " + ", ".join(missing) if missing else "MBS·launchable 선언"))
    else:
        results.append(Result("M15", "Music/Audio 필수 선언", "NOT_APPLICABLE", "Music/Audio 카테고리 아님"))

    results.append(Result("M16", "Maps/Navigation 조건부 요구", "NOT_RUN" if maps else "NOT_APPLICABLE",
                          "APP_MAPS 선언: NAV 요구사항 수동 확인" if maps else "내비게이션 앱 아님"))

    results.append(check_gleo_schema(ctx, gleo_actions))
    return results


def gleo_function_names():
    schema = APP / "src" / "main" / "assets" / "schema.json"
    if not schema.is_file():
        return set()
    try:
        data = json.loads(read(schema))
        return {c.get("function", {}).get("name") for c in data.get("actionCalls", []) if isinstance(c, dict)}
    except (ValueError, AttributeError):
        return set()


def check_gleo_schema(ctx, actions):
    schema = APP / "src" / "main" / "assets" / "schema.json"
    if not schema.is_file():
        return Result("M17", "Gleo App Schema", "NOT_APPLICABLE", "assets/schema.json 없음")
    problems = []
    try:
        data = json.loads(read(schema))
    except ValueError as exc:
        return Result("M17", "Gleo App Schema", "FAIL", f"JSON 오류: {exc}")
    for key in ("packageName", "appDescription", "version", "actionCalls"):
        if key not in data:
            problems.append(f"{key} 없음")
    calls = data.get("actionCalls", [])
    if not isinstance(calls, list) or len(calls) > 3:
        problems.append("actionCalls는 최대 3개 배열")
    names = []
    for call in calls if isinstance(calls, list) else []:
        fn = call.get("function", {}) if isinstance(call, dict) else {}
        name = fn.get("name", "")
        names.append(name)
        if call.get("type") != "function":
            problems.append(f"{name}: type은 function")
        if not re.fullmatch(r"[A-Z][A-Z0-9_]*", name or ""):
            problems.append(f"{name}: 이름은 대문자·밑줄")
        if fn.get("component", "activity") not in ("activity", "service"):
            problems.append(f"{name}: component는 activity/service")
        params = fn.get("parameters", {})
        if params.get("type") != "object":
            problems.append(f"{name}: parameters.type은 object")
        props = params.get("properties", {}) or {}
        for pname, spec in props.items():
            if not isinstance(spec, dict) or spec.get("type") not in ("string", "number", "boolean", "array"):
                problems.append(f"{name}.{pname}: type은 string/number/boolean/array 하나")
            if "enum" in spec and not all(isinstance(v, str) for v in spec["enum"]):
                problems.append(f"{name}.{pname}: enum은 문자열만")
        for req in params.get("required", []) or []:
            if req not in props:
                problems.append(f"{name}: required '{req}'가 properties에 없음")
    if len(names) != len(set(names)):
        problems.append("function 이름 중복")
    declared = {a.get(ANDROID + "name") for comp in ctx.components() for f in comp.findall("intent-filter")
                for a in f.findall("action")}
    missing = [n for n in names if n and n not in declared]
    if missing:
        problems.append("intent-filter 없음: " + ", ".join(missing))
    return Result("M17", "Gleo App Schema", "FAIL" if problems else "PASS",
                  "; ".join(problems) if problems else f"function {len(names)}개. 스키마 승인 여부는 EXT")


# --------------------------------------------------------------------------- code

def check_code(ctx):
    results = []
    code = ctx.code
    do = ctx.do_activities()

    if do:
        needed = ["CarUxRestrictionsManager", "registerListener"]
        missing = [n for n in needed if n not in code]
        if not re.search(r"getActiveRestrictions|activeRestrictions", code):
            missing.append("getActiveRestrictions")
        results.append(Result("C01", "CarUxRestrictions 연동", "FAIL" if missing else "PASS",
                              "DO 선언 앱인데 없음: " + ", ".join(missing) if missing else "리스너·ActiveRestrictions 사용"))
    else:
        results.append(Result("C01", "CarUxRestrictions 연동", "NOT_APPLICABLE",
                              "DO 선언 없음(주행 중 앱 사용 불가 상태). 주행 중 기능 도입 시 필수"))

    used = ctx.used_sdks()
    if "Vehicle" in used:
        missing = [n for n, p in (("initialize()", r"\.initialize\("), ("release()", r"\.release\("),
                                  ("checkXxxCapabilities", r"check\w+Capabilities\("),
                                  ("VehicleException 분기", r"VehicleException")) if not re.search(p, code)]
        results.append(Result("C02", "Vehicle 수명주기·capability·오류", "FAIL" if missing else "PASS",
                              "없음: " + ", ".join(missing) if missing else "initialize/release/capability/VehicleException"))
        regs = set(re.findall(r"(?<![A-Za-z])register([A-Z]\w*)\(", code))
        unregs = set(re.findall(r"unregister([A-Z]\w*)\(", code))
        unpaired = sorted(r for r in regs - unregs if not r.startswith("App"))
        results.append(Result("C03", "Vehicle 리스너 등록·해제 짝", "FAIL" if unpaired else "PASS",
                              "해제 없음: " + ", ".join(unpaired) if unpaired else f"register {len(regs)}종 모두 해제"))
    else:
        results.append(Result("C02", "Vehicle 수명주기·capability·오류", "NOT_APPLICABLE", "Vehicle SDK 사용 없음"))
        results.append(Result("C03", "Vehicle 리스너 등록·해제 짝", "NOT_APPLICABLE", "Vehicle SDK 사용 없음"))

    if "FusedLocation" in used:
        missing = [n for n, p in (("isSupported()", r"isSupported\("), ("initialize()", r"\.initialize\("),
                                  ("release()", r"\.release\(")) if not re.search(p, code)]
        if "registerFusedLocationCallback" in code and "unregisterFusedLocationCallback" not in code:
            missing.append("unregisterFusedLocationCallback()")
        results.append(Result("C04", "FusedLocation 지원 확인·수명주기", "FAIL" if missing else "PASS",
                              "없음: " + ", ".join(missing) if missing else "isSupported/initialize/release"))
    else:
        results.append(Result("C04", "FusedLocation 지원 확인·수명주기", "NOT_APPLICABLE", "FusedLocation 사용 없음"))

    speech = [s for s in ("TextToSpeech", "SpeechToText") if s in used]
    if speech:
        missing = []
        if not re.search(r"\.initialize\(", code):
            missing.append("initialize()")
        if not re.search(r"\.release\(", code):
            missing.append("release()")
        if "addEventListener(" in code and "removeEventListener(" not in code:
            missing.append("removeEventListener()")
        if re.search(r"\.addListener\(", code) and "removeListener(" not in code:
            missing.append("removeListener()")
        if "sendAudio(" in code and "completeAudioSend(" not in code:
            missing.append("completeAudioSend()")
        if "sendAudio(" in code and "ON_DEVICE" not in code:
            missing.append("sendAudio는 Mode.ON_DEVICE 전용")
        results.append(Result("C05", "TTS·STT 수명주기", "FAIL" if missing else "PASS",
                              "없음: " + ", ".join(missing) if missing else ", ".join(speech)))
    else:
        results.append(Result("C05", "TTS·STT 수명주기", "NOT_APPLICABLE", "TTS·STT 사용 없음"))

    players = sorted(set(re.findall(r"\b(MediaPlayer|AudioTrack|ExoPlayer|SoundPool|RingtoneManager|"
                                    r"android\.speech\.tts\.TextToSpeech)\b", code)))
    if players:
        missing = [n for n in ("AudioFocusRequest", "requestAudioFocus") if n not in code]
        if not re.search(r"OnAudioFocusChangeListener|setOnAudioFocusChangeListener", code):
            missing.append("OnAudioFocusChangeListener")
        results.append(Result("C07", "재생 전 오디오 포커스 요청·변경 처리", "FAIL" if missing else "PASS",
                              f"재생 API {players}; 없음: " + ", ".join(missing) if missing else f"재생 API {players}"))
    elif "TextToSpeech" in used:
        results.append(Result("C07", "재생 전 오디오 포커스 요청·변경 처리", "NOT_RUN",
                              "Pleos TTS만 사용. Speech Service의 포커스 처리(X-08)를 EMU로 확인"))
    else:
        results.append(Result("C07", "재생 전 오디오 포커스 요청·변경 처리", "NOT_APPLICABLE", "오디오 재생 없음"))

    results.append(check_secrets())

    hits = files_with(r"\bBuild\.(MODEL|DEVICE|PRODUCT|BOARD)\b", ctx)
    results.append(Result("C09", "모델명·Build 값 분기", "NOT_RUN" if hits else "PASS",
                          "수동 리뷰: " + ", ".join(hits) if hits else "Build.MODEL/DEVICE 사용 없음"))

    hits = files_with(r"\"/(dev|sys)/|\bSensorManager\b|android\.hardware\.camera2?\b", ctx)
    results.append(Result("C10", "하드웨어 직접 접근", "NOT_RUN" if hits else "PASS",
                          "수동 리뷰: " + ", ".join(hits) if hits else "디바이스 파일·센서 직접 접근 없음"))

    finish_on_back = re.search(r"fun\s+onBackPressed\s*\([^)]*\)[^{]*\{(?:[^{}]|\{[^{}]*\})*?\bfinish\(\)", code)
    if "finishAffinity(" in code or finish_on_back:
        results.append(Result("C11", "CCP Back: 종료 대신 숨김", "FAIL",
                              "finishAffinity() 또는 onBackPressed 안의 finish() 사용. 최상단은 moveTaskToBack(true)로 숨김"))
    elif re.search(r"OnBackPressedCallback|OnBackInvokedCallback|onBackPressed|moveTaskToBack", code):
        results.append(Result("C11", "CCP Back: 종료 대신 숨김", "PASS",
                              "Back 처리 있음, finish 없음. 화면 깊이별 동작은 EMU 확인"))
    else:
        results.append(Result("C11", "CCP Back: 종료 대신 숨김", "NOT_RUN",
                              "Back 처리 코드 없음. 하위 화면 Back·최상단 숨김을 EMU로 확인"))

    http = files_with(r"\"http://(?!localhost|127\.0\.0\.1|10\.0\.2\.2)", ctx)
    results.append(Result("C13", "평문 http URL", "FAIL" if http else "PASS",
                          ", ".join(http) if http else "http:// URL 없음"))

    tokens = files_with(r"[?&](access_token|token|session|password|apikey|api_key)=", ctx)
    results.append(Result("C14", "URL에 토큰·세션·비밀번호", "FAIL" if tokens else "PASS",
                          ", ".join(tokens) if tokens else "없음"))

    stateless = []
    for path, text in ctx.sources.items():
        body = strip_comments(text)
        for m in re.finditer(r"class\s+(\w+)\s*(?:\([^)]*\))?\s*:\s*[\w.]*Activity\s*\(", body):
            if re.search(r"\bprivate\s+var\b|\bvar\s+\w+\s*=", body) and not re.search(
                    r"onSaveInstanceState|SavedStateHandle|ViewModel|rememberSaveable", body):
                stateless.append(f"{path}:{m.group(1)}")
    results.append(Result("C15", "Activity 상태 저장(휴리스틱)", "FAIL" if stateless else "PASS",
                          "가변 상태가 있으나 저장 코드 없음: " + ", ".join(stateless) +
                          ". 오탐이면 근거를 남기고 compliance-matrix에서 판정" if stateless else "저장 코드 있음 또는 가변 상태 없음"))

    animated = files_with(r"postInvalidateOnAnimation|ValueAnimator|ObjectAnimator|AnimatedVectorDrawable|"
                          r"LottieAnimationView|Choreographer", ctx)
    if do:
        gated = "CarUxRestrictions" in code
        results.append(Result("C16", "DO 앱의 주행 중 애니메이션 제한", "PASS" if gated or not animated else "FAIL",
                              "애니메이션: " + ", ".join(animated) + ("; CarUxRestrictions 참조 있음(동작은 EMU 확인)" if gated
                                                                     else "; 주행 규제 분기 없음")))
    else:
        results.append(Result("C16", "DO 앱의 주행 중 애니메이션 제한", "NOT_APPLICABLE",
                              "DO 선언 없음. 애니메이션 파일: " + (", ".join(animated) if animated else "없음") +
                              ". DO 선언 전 주행 중 정책 필요"))

    strings = read(APP / "src" / "main" / "res" / "values" / "strings.xml")
    demo = re.findall(r'<string name="(\w+)">([^<]*)</string>', strings)
    markers = [n for n, v in demo if n.startswith(("demo_", "map_sample")) or re.search(r"예시|데모|시뮬레이션|Mock|mock|sample", v)]
    results.append(Result("C17", "제출 빌드의 더미·예시 콘텐츠", "NOT_RUN" if markers else "PASS",
                          f"예시·데모 문자열 {len(markers)}개. App Market 제출 빌드 전 제거 필요" if markers else "표시 없음"))
    return results


def check_secrets():
    patterns = [
        r"(?i)client_?secret\s*[=:]\s*[\"'][^\"'\s]{8,}[\"']",
        r"(?i)(api_?key|secret_?key|access_?token)\s*[=:]\s*[\"'][A-Za-z0-9_\-]{16,}[\"']",
        r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----",
        r"AKIA[0-9A-Z]{16}", r"AIza[0-9A-Za-z\-_]{35}", r"ghp_[0-9A-Za-z]{36}", r"xox[baprs]-[0-9A-Za-z\-]{10,}",
    ]
    regexes = [re.compile(p) for p in patterns]
    risky_names = re.compile(r"(^|/)(\.env(\..+)?|local\.properties|key\.properties|keystore\.properties)$|\.(jks|keystore)$")
    hits = []
    self_path = Path(__file__).resolve()
    for path in tracked_files():
        rel = path.relative_to(ROOT).as_posix() if path.is_absolute() else str(path)
        if rel.endswith(".env.example"):
            continue
        if risky_names.search(rel):
            hits.append(f"{rel}(비밀 파일)")
            continue
        if path.resolve() == self_path or not path.is_file() or path.stat().st_size > 1_000_000:
            continue
        text = read(path)
        if text and any(r.search(text) for r in regexes):
            hits.append(rel)
    return Result("C08", "비밀값·키 하드코딩", "FAIL" if hits else "PASS",
                  "의심: " + ", ".join(hits[:10]) if hits else "추적·미무시 파일에서 비밀값 패턴 없음")


# --------------------------------------------------------------------------- build

def gradle_command():
    if os.name == "nt":
        return [str(ROOT / "gradlew.bat")]
    return [str(ROOT / "gradlew")]


def gradle_env():
    env = dict(os.environ)
    if not env.get("JAVA_HOME"):
        for candidate in ("/Applications/Android Studio.app/Contents/jbr/Contents/Home",
                          r"C:\Program Files\Android\Android Studio\jbr"):
            if Path(candidate).is_dir():
                env["JAVA_HOME"] = candidate
                break
    return env


def run_gradle(tasks):
    cmd = gradle_command() + tasks + ["--console=plain"]
    if not Path(cmd[0]).is_file():
        return None, "Gradle Wrapper 없음"
    try:
        proc = subprocess.run(cmd, cwd=ROOT, env=gradle_env(), capture_output=True, text=True, timeout=1800)
    except (OSError, subprocess.TimeoutExpired) as exc:
        return None, str(exc)
    tail = "\n".join((proc.stdout + proc.stderr).strip().splitlines()[-6:])
    return proc.returncode, tail


def env_problem(text):
    return re.search(r"JAVA_HOME|Unable to locate a Java Runtime|SDK location not found|Could not resolve", text or "")


def check_build(ctx, static_only):
    if static_only:
        return [Result(cid, title, "NOT_RUN", "--static-only") for cid, title in
                (("B01", "디버그 빌드"), ("B02", "Android Lint"), ("B03", "단위 테스트"))]
    results = []
    code, tail = run_gradle([":app:assembleDebug"])
    if code is None or (code != 0 and env_problem(tail)):
        results.append(Result("B01", "디버그 빌드", "BLOCKED", tail))
    else:
        results.append(Result("B01", "디버그 빌드", "PASS" if code == 0 else "FAIL", ":app:assembleDebug" if code == 0 else tail))

    code, tail = run_gradle([":app:lintDebug"])
    report = APP / "build" / "reports" / "lint-results-debug.xml"
    if code is None or (code != 0 and env_problem(tail)):
        results.append(Result("B02", "Android Lint", "BLOCKED", tail))
    else:
        xml = read(report)
        errors = len(re.findall(r'severity="(?:Error|Fatal)"', xml))
        warnings = len(re.findall(r'severity="Warning"', xml))
        ok = code == 0 and errors == 0
        results.append(Result("B02", "Android Lint", "PASS" if ok else "FAIL",
                              f"오류 {errors}, 경고 {warnings} ({report.relative_to(ROOT).as_posix()})" if xml else tail))

    tests = [p for p in ROOT.glob("*/src/test") if p.is_dir()]
    if not tests:
        results.append(Result("B03", "단위 테스트", "NOT_APPLICABLE", "단위 테스트 소스 없음"))
    else:
        code, tail = run_gradle([":app:testDebugUnitTest"])
        if code is None or (code != 0 and env_problem(tail)):
            results.append(Result("B03", "단위 테스트", "BLOCKED", tail))
        else:
            results.append(Result("B03", "단위 테스트", "PASS" if code == 0 else "FAIL", ":app:testDebugUnitTest" if code == 0 else tail))
    return results


# --------------------------------------------------------------------------- runner

CHECKS = {
    "H01": (), "H02": (), "H03": (), "H04": (),
    "G01": ("PLEOS-SDK-007",), "G02": ("PLEOS-SDK-008",), "G03": ("PLEOS-SDK-006",),
    "G04": ("PLEOS-SDK-006",), "G05": ("PLEOS-SECURITY-028",),
    "M01": (), "M02": ("PLEOS-SDK-005", "PLEOS-SDK-012"),
    "M03": ("PLEOS-SDK-012", "PLEOS-SDK-014", "PLEOS-SDK-015", "PLEOS-SDK-020", "PLEOS-SDK-024"),
    "M04": ("PLEOS-SECURITY-017",), "M05": ("PLEOS-SDK-005",), "M06": ("PLEOS-SAFETY-004",),
    "M07": ("PLEOS-UX-001", "PLEOS-UX-004"), "M08": ("PLEOS-CCP-002",), "M09": ("PLEOS-SECURITY-018",),
    "M10": ("PLEOS-SECURITY-028",), "M11": ("PLEOS-SECURITY-015",), "M12": ("PLEOS-SECURITY-010",),
    "M13": ("PLEOS-DESIGN-005",), "M14": ("PLEOS-CATEGORY-002",),
    "M15": ("PLEOS-MEDIA-003", "PLEOS-MEDIA-004"), "M16": ("PLEOS-NAV-001",),
    "M17": ("PLEOS-GLEO-001", "PLEOS-GLEO-002", "PLEOS-GLEO-003"),
    "C01": ("PLEOS-SAFETY-002", "PLEOS-SAFETY-003"),
    "C02": ("PLEOS-SDK-002", "PLEOS-SDK-009", "PLEOS-SDK-010"), "C03": ("PLEOS-SDK-013",),
    "C04": ("PLEOS-SDK-003", "PLEOS-SDK-021"), "C05": ("PLEOS-SDK-014", "PLEOS-SDK-015", "PLEOS-SDK-016"),
    "C07": ("PLEOS-AUDIO-002", "PLEOS-AUDIO-004"),
    "C08": ("PLEOS-SECURITY-023", "PLEOS-SECURITY-014", "PLEOS-SDK-018"),
    "C09": ("PLEOS-HW-005", "PLEOS-HW-007"), "C10": ("PLEOS-HW-001",), "C11": ("PLEOS-CCP-003",),
    "C13": ("PLEOS-SECURITY-015",), "C14": ("PLEOS-SECURITY-012",),
    "C15": ("PLEOS-UX-007", "PLEOS-LIFECYCLE-002", "PLEOS-LIFECYCLE-006"),
    "C16": ("PLEOS-SAFETY-008",), "C17": ("PLEOS-PERF-008",),
    "B01": ("PLEOS-PERF-007",), "B02": ("PLEOS-PERF-007",), "B03": (),
}


def severity(result, ctx):
    levels = []
    for rid in result.reqs:
        req = ctx.requirements.get(rid)
        if req:
            levels.append((req.category, req.level))
    if any(cat in SAFETY_CATEGORIES and lvl in ("MUST", "MUST_NOT") for cat, lvl in levels):
        return "BLOCKER"
    if any(lvl in ("MUST", "MUST_NOT") for _, lvl in levels) or not levels:
        return "MAJOR"
    return "MINOR"


def manual_summary(ctx, show_ids):
    groups = {m: [] for m in ("EMU", "CAR", "REVIEW", "EXT", "UNIT")}
    for req in ctx.requirements.values():
        for method in groups:
            if re.search(r"\b" + method + r"\b", req.methods):
                groups[method].append(req.rid)
    lines = ["", "자동 검사로 판정하지 않는 검증 (모두 NOT_RUN, compliance-matrix.md에서 관리):"]
    labels = {"EMU": "Emulator·Virtual Console", "CAR": "실차", "REVIEW": "수동 리뷰", "EXT": "외부 확인", "UNIT": "단위 테스트"}
    for method, ids in groups.items():
        lines.append(f"  [NOT_RUN] {method:<6} {labels[method]}: {len(ids)}개")
        if show_ids and ids:
            lines.append("           " + ", ".join(ids))
    return lines


def main():
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser(description="Pleos guide compliance checks (static + build).")
    parser.add_argument("--static-only", action="store_true", help="Gradle 빌드·Lint·테스트를 실행하지 않음")
    parser.add_argument("--strict", action="store_true", help="MINOR FAIL도 실패 종료 코드로 처리")
    parser.add_argument("--list-manual", action="store_true", help="수동·환경 검증 대상 요구사항 ID 출력")
    args = parser.parse_args()

    try:
        ctx = Context()
        results = check_harness(ctx) + check_gradle(ctx) + check_manifest(ctx) + check_code(ctx) + \
            check_build(ctx, args.static_only)
    except Exception as exc:  # the checker itself failed; never report PASS
        print(f"BLOCKED: checker error: {exc!r}")
        return 2

    order = {cid: i for i, cid in enumerate(CHECKS)}
    results.sort(key=lambda r: order.get(r.cid, 999))
    counts = {s: 0 for s in STATUSES}
    failing = False
    print(f"Pleos 가이드 준수 검사 — {ROOT.name} (요구사항 {len(ctx.requirements)}개)")
    for r in results:
        r.reqs = CHECKS.get(r.cid, ())
        label = r.status
        if r.status == "FAIL":
            r.severity = severity(r, ctx)
            label = f"FAIL:{r.severity}"
            if r.severity in ("BLOCKER", "MAJOR") or args.strict:
                failing = True
        counts[r.status] += 1
        ids = " ".join(x.replace("PLEOS-", "") for x in r.reqs)
        print(f"[{label}] {r.cid} {r.title}" + (f" ({ids})" if ids else "") + f" — {r.detail}")
    for line in manual_summary(ctx, args.list_manual):
        print(line)
    print("")
    print("요약: " + ", ".join(f"{s} {n}" for s, n in counts.items()))
    print("결과: " + ("FAIL (BLOCKER/MAJOR 위반 또는 --strict)" if failing else "PASS (BLOCKER/MAJOR 위반 없음)") +
          ". 자동 검사 PASS는 EMU·실차·리뷰 검증을 대신하지 않음")
    return 1 if failing else 0


if __name__ == "__main__":
    sys.exit(main())
