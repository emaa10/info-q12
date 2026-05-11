#!/usr/bin/env python3
import re
from pathlib import Path
from collections import defaultdict

SRC = Path("src/main/java/com/pixeldrift")
OUT = Path("docs/pixeldrift.puml")


def visibility(modifier_str):
    if "private" in modifier_str:
        return "-"
    if "protected" in modifier_str:
        return "#"
    return "+"


def bare_type(t):
    return re.sub(r"<.*>", "", t).strip()


def generic_arg(t):
    m = re.search(r"<([\w]+)>", t)
    return m.group(1) if m else None


def format_params(raw):
    if not raw.strip():
        return ""
    parts = []
    for p in raw.split(","):
        p = p.strip()
        tokens = p.rsplit(" ", 1)
        if len(tokens) == 2:
            parts.append(f"{tokens[1]} : {tokens[0]}")
        else:
            parts.append(p)
    return ", ".join(parts)


classes = {}
packages = defaultdict(list)

for java_file in sorted(SRC.rglob("*.java")):
    src = java_file.read_text()

    pkg_m = re.search(r"^package com\.pixeldrift\.?(\w*);", src, re.M)
    pkg = pkg_m.group(1) if pkg_m and pkg_m.group(1) else "root"

    cls_m = re.search(
        r"(?:public\s+)?(?P<abstract>abstract\s+)?"
        r"(?P<kind>class|interface|enum)\s+(?P<name>\w+)"
        r"(?:\s+extends\s+(?P<ext>[\w<>]+))?"
        r"(?:\s+implements\s+(?P<impl>[\w<>, ]+))?"
        r"\s*\{",
        src,
        re.S,
    )
    if not cls_m:
        continue

    name = cls_m.group("name")
    kind = cls_m.group("kind")
    is_abstract = bool(cls_m.group("abstract"))
    extends = cls_m.group("ext")
    impl_raw = cls_m.group("impl")

    enum_vals = []
    if kind == "enum":
        body_m = re.search(r"\{([^}]+)\}", src, re.S)
        if body_m:
            enum_vals = [
                v.strip()
                for v in body_m.group(1).split(",")
                if re.match(r"^\s*[A-Z_][A-Z_0-9]*\s*$", v)
            ]

    fields = []
    if kind != "enum":
        for m in re.finditer(
            r"^\s+(private|protected|public)\s+"
            r"(?:static\s+)?(?:final\s+)?"
            r"([\w<>\[\], ]+?)\s+(\w+)\s*[;=]",
            src,
            re.M,
        ):
            fields.append(
                {
                    "vis": visibility(m.group(1)),
                    "type": m.group(2).strip(),
                    "name": m.group(3),
                }
            )

    methods = []
    if kind != "enum":
        for m in re.finditer(
            r"^\s+(?:@\w+\s+)*"
            r"(?P<mods>(?:public|protected|private)"
            r"(?:\s+(?:abstract|static|final|override))*)\s+"
            r"(?P<ret>void|[\w<>\[\]]+)\s+"
            r"(?P<mname>\w+)\s*\((?P<params>[^)]*)\)",
            src,
            re.M,
        ):
            mods = m.group("mods")
            methods.append(
                {
                    "vis": visibility(mods),
                    "abstract": "abstract" in mods,
                    "ret": m.group("ret").strip(),
                    "name": m.group("mname"),
                    "params": format_params(m.group("params")),
                }
            )

    classes[name] = {
        "pkg": pkg,
        "kind": kind,
        "abstract": is_abstract,
        "extends": extends,
        "implements": [i.strip() for i in impl_raw.split(",")] if impl_raw else [],
        "fields": fields,
        "methods": methods,
        "enum_vals": enum_vals,
    }
    packages[pkg].append(name)

known = set(classes.keys())
relationships = []

lines = [
    "@startuml",
    "",
    "skinparam classAttributeIconSize 0",
    "skinparam classFontSize 11",
    "skinparam classHeaderBackgroundColor #DDEEFF",
    "skinparam classBackgroundColor #F8F8FF",
    "skinparam classBorderColor #4477AA",
    "skinparam arrowColor #334466",
    "skinparam packageBackgroundColor #F0F4FF",
    "skinparam packageBorderColor #99AACC",
    "skinparam shadowing false",
    "skinparam linetype ortho",
    "",
]

for pkg in sorted(packages.keys()):
    lines.append(f'package "{pkg}" {{')
    for name in packages[pkg]:
        c = classes[name]

        if c["kind"] == "enum":
            lines.append(f"  enum {name} {{")
        elif c["kind"] == "interface":
            lines.append(f"  interface {name} {{")
        elif c["abstract"]:
            lines.append(f"  abstract class {name} {{")
        else:
            lines.append(f"  class {name} {{")

        for val in c["enum_vals"]:
            lines.append(f"    {val}")

        for f in c["fields"]:
            lines.append(f"    {f['vis']} {f['name']} : {f['type']}")
            ga = generic_arg(f["type"])
            bt = bare_type(f["type"])
            if ga and ga in known:
                relationships.append(f'{name} "1" o-- "*" {ga}')
            elif bt in known and bt != name:
                relationships.append(f"{name} *-- {bt}")

        for m in c["methods"]:
            abst = "{abstract} " if m["abstract"] else ""
            ret = f' : {m["ret"]}'
            lines.append(f"    {m['vis']} {abst}{m['name']}({m['params']}){ret}")

        lines.append("  }")
    lines.append("}")
    lines.append("")

lines.append("' Vererbung & Implementierung")
for name, c in classes.items():
    if c["extends"]:
        ext = bare_type(c["extends"])
        if ext in known:
            relationships.append(f"{name} --|> {ext}")
    for impl in c["implements"]:
        imp = bare_type(impl)
        if imp in known:
            relationships.append(f"{name} ..|> {imp}")

seen = set()
for r in relationships:
    if r not in seen:
        lines.append(r)
        seen.add(r)

lines += ["", "@enduml"]

OUT.write_text("\n".join(lines))
print(f"Generated {OUT} with {len(classes)} classes across {len(packages)} packages.")
