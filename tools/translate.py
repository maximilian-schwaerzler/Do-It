#!/usr/bin/env python3
"""Translate app content into other languages using the DeepL API.

Setup:
    pip install deepl

Subcommands:

  strings   Translate app/src/main/res/values/strings.xml
  fastlane  Translate fastlane/metadata/android/en-US/ store listing files

Examples:
    python tools/translate.py strings --api-key KEY fr es it
    python tools/translate.py strings --api-key KEY de        # fill gaps only
    python tools/translate.py strings --api-key KEY --force de

    python tools/translate.py fastlane --api-key KEY de fr
    python tools/translate.py fastlane --api-key KEY --force de

By default, files/strings that already exist in the target are left untouched
so that hand-edited translations are never overwritten. Use --force to redo all.
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

# Maps locale codes (used as folder names) to DeepL target-language codes.
# The same map is used by both subcommands.
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
    "pt-BR": "PT-BR",
    "ro": "RO",
    "ru": "RU",
    "sk": "SK",
    "sl": "SL",
    "sv": "SV",
    "tr": "TR",
    "uk": "UK",
    "zh": "ZH",
    "zh-rTW": "ZH-HANT",
    "zh-TW": "ZH-HANT",
    "ta": "TA"
}

_XML_LICENSE_HEADER = """\
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
_PLACEHOLDER_RE = re.compile(r"%%|%\d+\$[sdf]|%[sdf]")


def _resolve_deepl_lang(locale: str) -> str | None:
    return DEEPL_LANG_MAP.get(locale)


def _protect_placeholders(text: str) -> str:
    """Wrap Android format placeholders so DeepL leaves them alone."""
    return _PLACEHOLDER_RE.sub(lambda m: f"<ph>{m.group()}</ph>", text)


def _restore_placeholders(text: str) -> str:
    return re.sub(r"<ph>(.*?)</ph>", lambda m: m.group(1), text)


def _android_escape(text: str) -> str:
    """Escape text for use inside an Android string resource element."""
    text = text.replace("&", "&amp;")
    text = text.replace("<", "&lt;")
    text = text.replace("'", "\\'")
    return text


def _android_unescape(text: str) -> str:
    """Reverse Android string-resource backslash escaping."""
    return text.replace("\\'", "'")


# Canonical ordering for Android plural quantity categories.
_PLURAL_QUANTITY_ORDER = ["zero", "one", "two", "few", "many", "other"]

# Representative sample numbers used to give DeepL grammatical context per quantity.
_QUANTITY_SAMPLE: dict[str, int] = {
    "zero": 0, "one": 1, "two": 2, "few": 3, "many": 10, "other": 2,
}


def _prepare_plural_item(text: str, quantity: str) -> tuple[str, list[str]]:
    """Replace placeholders with a representative number for the given quantity.

    Returns the substituted text and the original placeholder list for restoration.
    """
    sample = str(_QUANTITY_SAMPLE.get(quantity, 2))
    placeholders: list[str] = []

    def _replace(m: re.Match) -> str:
        placeholders.append(m.group())
        return sample

    return _PLACEHOLDER_RE.sub(_replace, text), placeholders


def _restore_plural_item(text: str, quantity: str, placeholders: list[str]) -> str:
    """Restore original placeholders after translation."""
    if not placeholders:
        return text
    sample = re.escape(str(_QUANTITY_SAMPLE.get(quantity, 2)))
    it = iter(placeholders)
    return re.sub(r"(?<!\d)" + sample + r"(?!\d)", lambda _: next(it), text, count=len(placeholders))

# ---------------------------------------------------------------------------
# strings subcommand
# ---------------------------------------------------------------------------

def _load_existing_strings(path: Path) -> dict[str, str]:
    if not path.exists():
        return {}
    try:
        root = ET.parse(path).getroot()
        return {
            el.get("name"): _android_unescape(el.text or "")
            for el in root.findall("string")
        }
    except ET.ParseError as exc:
        print(f"Warning: could not parse {path}: {exc}", file=sys.stderr)
        return {}


def _load_existing_plurals(path: Path) -> dict[str, dict[str, str]]:
    """Return {plurals_name: {quantity: unescaped_text}} for all <plurals> elements."""
    if not path.exists():
        return {}
    try:
        root = ET.parse(path).getroot()
        result: dict[str, dict[str, str]] = {}
        for el in root.findall("plurals"):
            name = el.get("name")
            result[name] = {
                item.get("quantity"): _android_unescape(item.text or "")
                for item in el.findall("item")
            }
        return result
    except ET.ParseError as exc:
        print(f"Warning: could not parse {path}: {exc}", file=sys.stderr)
        return {}


def _translate_strings_language(
    translator: deepl.Translator,
    source_root: ET.Element,
    res_dir: Path,
    locale: str,
    force: bool,
) -> None:
    deepl_lang = _resolve_deepl_lang(locale)
    if deepl_lang is None:
        print(f"[{locale}] Unknown locale — add it to DEEPL_LANG_MAP and retry.", file=sys.stderr)
        return

    target_dir = res_dir / f"values-{locale}"
    target_path = target_dir / "strings.xml"
    target_dir.mkdir(exist_ok=True)

    existing_strings = {} if force else _load_existing_strings(target_path)
    existing_plurals = {} if force else _load_existing_plurals(target_path)

    strings_to_translate: list[tuple[str, str]] = []
    for el in source_root.findall("string"):
        name = el.get("name")
        if el.get("translatable") == "false":
            continue
        if name not in existing_strings:
            strings_to_translate.append((name, el.text or ""))

    plurals_to_translate: list[tuple[str, str, str]] = []
    for el in source_root.findall("plurals"):
        name = el.get("name")
        if el.get("translatable") == "false":
            continue
        existing_items = existing_plurals.get(name, {})
        for item in el.findall("item"):
            quantity = item.get("quantity")
            if quantity not in existing_items:
                plurals_to_translate.append((name, quantity, item.text or ""))

    total = len(strings_to_translate) + len(plurals_to_translate)
    if total == 0:
        print(f"[{locale}] Nothing new to translate.")
    else:
        print(f"[{locale}] Translating {len(strings_to_translate)} string(s) and {len(plurals_to_translate)} plural item(s)…")

        if strings_to_translate:
            protected = [_protect_placeholders(t) for _, t in strings_to_translate]
            results = translator.translate_text(
                protected,
                source_lang="EN",
                target_lang=deepl_lang,
                tag_handling="xml",
                ignore_tags=["ph"],
            )
            for (name, _), result in zip(strings_to_translate, results):
                existing_strings[name] = _restore_placeholders(result.text)

        if plurals_to_translate:
            prepared: list[str] = []
            restore_data: list[tuple[str, list[str]]] = []
            for _, quantity, text in plurals_to_translate:
                subst, placeholders = _prepare_plural_item(text, quantity)
                prepared.append(subst)
                restore_data.append((quantity, placeholders))
            results = translator.translate_text(
                prepared,
                source_lang="EN",
                target_lang=deepl_lang,
            )
            for (name, quantity, _), result, (q, placeholders) in zip(plurals_to_translate, results, restore_data):
                if name not in existing_plurals:
                    existing_plurals[name] = {}
                existing_plurals[name][quantity] = _restore_plural_item(result.text, q, placeholders)

    lines: list[str] = [_XML_LICENSE_HEADER, "<resources>\n"]
    for el in source_root:
        name = el.get("name")
        if el.get("translatable") == "false":
            continue
        if el.tag == "string":
            value = _android_escape(existing_strings.get(name, ""))
            lines.append(f'    <string name="{name}">{value}</string>\n')
        elif el.tag == "plurals":
            items = existing_plurals.get(name, {})
            lines.append(f'    <plurals name="{name}">\n')
            for quantity in _PLURAL_QUANTITY_ORDER:
                if quantity in items:
                    value = _android_escape(items[quantity])
                    lines.append(f'        <item quantity="{quantity}">{value}</item>\n')
            lines.append(f'    </plurals>\n')
    lines.append("</resources>\n")

    target_path.write_text("".join(lines), encoding="utf-8")
    print(f"[{locale}] Written → {target_path}")


def cmd_strings(args: argparse.Namespace) -> None:
    source_path = Path(args.strings)
    if not source_path.exists():
        print(f"Source file not found: {source_path}", file=sys.stderr)
        sys.exit(1)

    source_root = ET.parse(source_path).getroot()
    res_dir = source_path.parent.parent
    translator = deepl.Translator(args.api_key)

    for locale in args.languages:
        _translate_strings_language(translator, source_root, res_dir, locale, args.force)


# ---------------------------------------------------------------------------
# fastlane subcommand
# ---------------------------------------------------------------------------

# Files whose content contains HTML markup — use DeepL's HTML mode for these.
_HTML_FILES = {"full_description.txt"}

# Files/directories inside the locale folder that should never be translated.
_SKIP_DIRS = {"images"}


def _collect_source_files(source_locale_dir: Path) -> list[Path]:
    """Return all translatable .txt files under the source locale directory."""
    return [
        p for p in source_locale_dir.rglob("*.txt")
        if not any(part in _SKIP_DIRS for part in p.relative_to(source_locale_dir).parts)
    ]


def _translate_fastlane_language(
    translator: deepl.Translator,
    source_locale_dir: Path,
    metadata_dir: Path,
    locale: str,
    force: bool,
) -> None:
    deepl_lang = _resolve_deepl_lang(locale)
    if deepl_lang is None:
        print(f"[{locale}] Unknown locale — add it to DEEPL_LANG_MAP and retry.", file=sys.stderr)
        return

    target_locale_dir = metadata_dir / locale
    source_files = _collect_source_files(source_locale_dir)

    to_translate: list[tuple[Path, str, bool]] = []  # (rel_path, text, is_html)
    for src in source_files:
        rel = src.relative_to(source_locale_dir)
        dst = target_locale_dir / rel
        if not force and dst.exists():
            continue
        text = src.read_text(encoding="utf-8").strip()
        is_html = src.name in _HTML_FILES
        to_translate.append((rel, text, is_html))

    if not to_translate:
        print(f"[{locale}] Nothing new to translate.")
        return

    print(f"[{locale}] Translating {len(to_translate)} file(s)…")

    # DeepL supports batch requests but requires uniform tag_handling per call,
    # so split into two batches: plain text and HTML.
    plain = [(rel, text) for rel, text, is_html in to_translate if not is_html]
    html = [(rel, text) for rel, text, is_html in to_translate if is_html]

    translated: dict[Path, str] = {}

    if plain:
        results = translator.translate_text(
            [t for _, t in plain],
            source_lang="EN",
            target_lang=deepl_lang,
        )
        for (rel, _), result in zip(plain, results):
            translated[rel] = result.text

    if html:
        results = translator.translate_text(
            [t for _, t in html],
            source_lang="EN",
            target_lang=deepl_lang,
            tag_handling="html",
        )
        for (rel, _), result in zip(html, results):
            translated[rel] = result.text

    for rel, text in translated.items():
        dst = target_locale_dir / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        dst.write_text(text + "\n", encoding="utf-8")
        print(f"[{locale}] Written → {dst}")


def cmd_fastlane(args: argparse.Namespace) -> None:
    metadata_dir = Path(args.metadata)
    source_locale_dir = metadata_dir / args.source_locale

    if not source_locale_dir.is_dir():
        print(f"Source locale directory not found: {source_locale_dir}", file=sys.stderr)
        sys.exit(1)

    translator = deepl.Translator(args.api_key)

    for locale in args.languages:
        _translate_fastlane_language(translator, source_locale_dir, metadata_dir, locale, args.force)


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def _add_common_args(p: argparse.ArgumentParser) -> None:
    p.add_argument("--api-key", required=True, help="DeepL API authentication key")
    p.add_argument(
        "--force",
        action="store_true",
        help="Re-translate everything, ignoring existing translations",
    )


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Translate app content via DeepL.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )

    subparsers = parser.add_subparsers(dest="command", required=True)

    # -- strings --
    p_strings = subparsers.add_parser("strings", help="Translate strings.xml resources")
    _add_common_args(p_strings)
    p_strings.add_argument(
        "languages",
        nargs="+",
        metavar="LANG",
        help="Android locale codes, e.g. fr es it de",
    )
    p_strings.add_argument(
        "--strings",
        default="app/src/main/res/values/strings.xml",
        metavar="PATH",
        help="Path to the source strings.xml (default: app/src/main/res/values/strings.xml)",
    )
    p_strings.set_defaults(func=cmd_strings)

    # -- fastlane --
    p_fastlane = subparsers.add_parser("fastlane", help="Translate fastlane store listing files")
    _add_common_args(p_fastlane)
    p_fastlane.add_argument(
        "languages",
        nargs="+",
        metavar="LANG",
        help="Fastlane locale codes, e.g. de fr es-ES pt-BR",
    )
    p_fastlane.add_argument(
        "--metadata",
        default="fastlane/metadata/android",
        metavar="PATH",
        help="Path to fastlane metadata directory (default: fastlane/metadata/android)",
    )
    p_fastlane.add_argument(
        "--source-locale",
        default="en-US",
        metavar="LOCALE",
        help="Source locale folder name (default: en-US)",
    )
    p_fastlane.set_defaults(func=cmd_fastlane)

    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()