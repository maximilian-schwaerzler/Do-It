#!/usr/bin/env python3
"""Translate Android strings.xml into other languages using the DeepL API.

Usage:
    pip install deepl
    python translate.py --api-key YOUR_KEY fr es it
    python translate.py --api-key YOUR_KEY de          # fill gaps in existing file
    python translate.py --api-key YOUR_KEY --force de  # re-translate everything

By default, strings already present in the target file are kept untouched so
that hand-edited translations are never overwritten.
"""

import argparse
import re
import sys
from pathlib import Path
from xml.etree import ElementTree as ET

try:
    import deepl
except ImportError:
    print("deepl package not found. Run: pip install deepl", file=sys.stderr)
    sys.exit(1)

# Maps Android resource locale codes to DeepL target-language codes.
# Add entries here for any language DeepL supports.
DEEPL_LANG_MAP: dict[str, str] = {
    "bg": "BG",
    "cs": "CS",
    "da": "DA",
    "de": "DE",
    "el": "EL",
    "es": "ES",
    "et": "ET",
    "fi": "FI",
    "fr": "FR",
    "hu": "HU",
    "id": "ID",
    "it": "IT",
    "ja": "JA",
    "ko": "KO",
    "lt": "LT",
    "lv": "LV",
    "nb": "NB",
    "nl": "NL",
    "pl": "PL",
    "pt": "PT-PT",
    "pt-rBR": "PT-BR",
    "ro": "RO",
    "ru": "RU",
    "sk": "SK",
    "sl": "SL",
    "sv": "SV",
    "tr": "TR",
    "uk": "UK",
    "zh": "ZH",
    "zh-rTW": "ZH-HANT",
    "ta": "TA"
}

LICENSE_HEADER = """\
<?xml version="1.0" encoding="utf-8"?>
<!--
  ~ Copyright 2026 Maximilian Schwärzler
  ~
  ~    Licensed under the Apache License, Version 2.0 (the "License");
  ~    you may not use this file except in compliance with the License.
  ~    You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~    Unless required by applicable law or agreed to in writing, software
  ~    distributed under the License is distributed on an "AS IS" BASIS,
  ~    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~    See the License for the specific language governing permissions and
  ~    limitations under the License.
  -->

"""

# Matches Android printf-style placeholders: %s, %d, %f, %1$s, %2$d, %%
_PLACEHOLDER_RE = re.compile(r'%%|%\d+\$[sdf]|%[sdf]')


def _protect(text: str) -> str:
    """Wrap placeholders so DeepL leaves them alone."""
    return _PLACEHOLDER_RE.sub(lambda m: f"<ph>{m.group()}</ph>", text)


def _restore(text: str) -> str:
    """Strip the wrapper tags added by _protect."""
    return re.sub(r"<ph>(.*?)</ph>", lambda m: m.group(1), text)


def _android_escape(text: str) -> str:
    """Apply the escaping rules required by Android string resources."""
    text = text.replace("&", "&amp;")
    text = text.replace("<", "&lt;")
    text = text.replace("'", "\\'")
    return text


def _load_existing(path: Path) -> dict[str, str]:
    if not path.exists():
        return {}
    try:
        root = ET.parse(path).getroot()
        return {el.get("name"): (el.text or "") for el in root.findall("string")}
    except ET.ParseError as exc:
        print(f"Warning: could not parse {path}: {exc}", file=sys.stderr)
        return {}


def translate_language(
    translator: deepl.Translator,
    source_root: ET.Element,
    res_dir: Path,
    android_lang: str,
    force: bool,
) -> None:
    deepl_lang = DEEPL_LANG_MAP.get(android_lang)
    if deepl_lang is None:
        print(
            f"[{android_lang}] Unknown locale — add it to DEEPL_LANG_MAP and retry.",
            file=sys.stderr,
        )
        return

    target_dir = res_dir / f"values-{android_lang}"
    target_path = target_dir / "strings.xml"
    target_dir.mkdir(exist_ok=True)

    existing = {} if force else _load_existing(target_path)

    # Collect strings that need translation (respects translatable="false")
    to_translate: list[tuple[str, str]] = []
    for el in source_root.findall("string"):
        name = el.get("name")
        if el.get("translatable") == "false":
            continue
        if name not in existing:
            to_translate.append((name, el.text or ""))

    if to_translate:
        print(f"[{android_lang}] Translating {len(to_translate)} string(s)…")
        protected_texts = [_protect(text) for _, text in to_translate]
        results = translator.translate_text(
            protected_texts,
            source_lang="EN",
            target_lang=deepl_lang,
            tag_handling="xml",
            ignore_tags=["ph"],
        )
        for (name, _), result in zip(to_translate, results):
            existing[name] = _restore(result.text)
    else:
        print(f"[{android_lang}] Nothing new to translate.")

    # Write output in the same order as the source file
    lines: list[str] = [LICENSE_HEADER, "<resources>\n"]
    for el in source_root.findall("string"):
        name = el.get("name")
        if el.get("translatable") == "false":
            continue
        value = _android_escape(existing.get(name, ""))
        lines.append(f'    <string name="{name}">{value}</string>\n')
    lines.append("</resources>\n")

    target_path.write_text("".join(lines), encoding="utf-8")
    print(f"[{android_lang}] Written → {target_path}")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Translate Android strings.xml via DeepL.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    parser.add_argument(
        "languages",
        nargs="+",
        metavar="LANG",
        help="Android locale codes to translate into, e.g. fr es it de",
    )
    parser.add_argument("--api-key", required=True, help="DeepL API authentication key")
    parser.add_argument(
        "--strings",
        default="app/src/main/res/values/strings.xml",
        metavar="PATH",
        help="Path to the source strings.xml (default: app/src/main/res/values/strings.xml)",
    )
    parser.add_argument(
        "--force",
        action="store_true",
        help="Re-translate all strings, ignoring existing translations",
    )
    args = parser.parse_args()

    source_path = Path(args.strings)
    if not source_path.exists():
        print(f"Source file not found: {source_path}", file=sys.stderr)
        sys.exit(1)

    source_root = ET.parse(source_path).getroot()
    res_dir = source_path.parent.parent
    translator = deepl.Translator(args.api_key)

    for lang in args.languages:
        translate_language(translator, source_root, res_dir, lang, args.force)


if __name__ == "__main__":
    main()
