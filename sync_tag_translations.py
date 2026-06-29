#!/usr/bin/env python3
"""
Scan Minecraft mod projects for datapack tags and add missing tag translation keys to lang files.

Only processes tags owned by the mod (namespace from gradle.properties mod_id), unless
--namespace is used to include extra namespaces (e.g. supplementaries compat tags in amendments).

Tag JSON:  <module>/src/main/resources/data/<namespace>/tags/<registry...>/<tag_name>.json
Lang key:  tag.<registry>.<namespace>.<path...>.<tag_name>
           (registry sub-path uses dots; slashes in tag paths become dots)

Examples:
  data/supplementaries/tags/block/column_shape_4x4.json
    -> tag.block.supplementaries.column_shape_4x4
  data/supplementaries/tags/item/enchantable/stasis.json
    -> tag.item.supplementaries.enchantable.stasis
  data/supplementaries/tags/worldgen/biome/has_basalt_ash.json
    -> tag.worldgen.supplementaries.biome.has_basalt_ash
  data/supplementaries/tags/banner_pattern/pattern_item/dragon.json
    -> tag.banner_pattern.supplementaries.pattern_item.dragon

Usage:
  python sync_tag_translations.py                     # scan cwd
  python sync_tag_translations.py ../amendments       # scan another mod
  python sync_tag_translations.py --dry-run .         # preview only
  python sync_tag_translations.py --lang fr_fr .      # override locale (default: en_us)
  python sync_tag_translations.py --namespace supplementaries ../amendments
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path


MODULE_RESOURCE_DIRS = (
    "common/src/main/resources",
    "fabric/src/main/resources",
    "forge/src/main/resources",
    "neoforge/src/main/resources",
    "src/main/resources",
)


def tag_path_to_lang_key(namespace: str, registry_parts: tuple[str, ...], tag_name: str) -> str:
    if not registry_parts:
        raise ValueError("empty registry path")
    registry = registry_parts[0]
    suffix = ".".join(registry_parts[1:] + (tag_name,))
    return f"tag.{registry}.{namespace}.{suffix}" if suffix else f"tag.{registry}.{namespace}.{tag_name}"


def tag_name_to_label(tag_name: str) -> str:
    words: list[str] = []
    for part in tag_name.replace("\\", "/").split("/"):
        words.extend(part.split("_"))

    labeled: list[str] = []
    for word in words:
        if re.fullmatch(r"\d+x\d+", word):
            labeled.append(word)
        else:
            labeled.append(word.capitalize())
    return " ".join(labeled)


def read_mod_id(mod_root: Path) -> str | None:
    props = mod_root / "gradle.properties"
    if not props.is_file():
        return None
    for line in props.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        if line.startswith("mod_id"):
            return line.split("=", 1)[1].strip()
    return None


def discover_resource_roots(mod_root: Path) -> list[Path]:
    roots: list[Path] = []
    for rel in MODULE_RESOURCE_DIRS:
        path = (mod_root / rel).resolve()
        if path.is_dir():
            roots.append(path)
    return roots


def discover_tags(
    resource_roots: list[Path],
    allowed_namespaces: set[str],
) -> dict[str, tuple[str, str]]:
    """
    Returns {lang_key: (namespace, label)} for owned tags.
    Deduplicates across loader source sets (common / fabric / neoforge).
    """
    tags: dict[str, tuple[str, str]] = {}

    for root in resource_roots:
        for tag_file in root.glob("data/*/tags/**/*.json"):
            rel = tag_file.relative_to(root)
            parts = rel.parts
            if len(parts) < 4 or parts[0] != "data" or parts[2] != "tags":
                continue

            namespace = parts[1]
            if namespace not in allowed_namespaces:
                continue

            registry_parts = tuple(parts[3:-1])
            tag_name = tag_file.stem
            if not registry_parts:
                continue

            lang_key = tag_path_to_lang_key(namespace, registry_parts, tag_name)
            tags.setdefault(lang_key, (namespace, tag_name_to_label(tag_name)))

    return tags


def discover_lang_files(resource_roots: list[Path]) -> dict[str, list[Path]]:
    by_namespace: dict[str, set[Path]] = {}

    for root in resource_roots:
        for lang_file in root.glob("assets/*/lang/*.json"):
            rel = lang_file.relative_to(root)
            parts = rel.parts
            if len(parts) != 4 or parts[0] != "assets" or parts[2] != "lang":
                continue
            namespace = parts[1]
            by_namespace.setdefault(namespace, set()).add(lang_file.resolve())

    return {ns: sorted(paths) for ns, paths in by_namespace.items()}


def load_lang(path: Path) -> dict[str, str] | None:
    try:
        with path.open(encoding="utf-8") as f:
            return json.load(f)
    except json.JSONDecodeError as exc:
        print(f"[error] {path}: invalid JSON ({exc})", file=sys.stderr)
        return None


def save_lang(path: Path, data: dict[str, str]) -> None:
    with path.open("w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def sync_mod(
    mod_root: Path,
    *,
    dry_run: bool,
    lang_filter: str | None,
    extra_namespaces: list[str],
) -> int:
    mod_root = mod_root.resolve()
    mod_id = read_mod_id(mod_root)
    if not mod_id:
        print(f"[error] {mod_root}: could not read mod_id from gradle.properties", file=sys.stderr)
        return 0

    allowed_namespaces = {mod_id, *extra_namespaces}
    resource_roots = discover_resource_roots(mod_root)
    if not resource_roots:
        print(f"[skip] {mod_root.name}: no module resource directories found", file=sys.stderr)
        return 0

    all_tags = discover_tags(resource_roots, allowed_namespaces)
    lang_by_namespace = discover_lang_files(resource_roots)

    added_total = 0
    tags_by_namespace: dict[str, dict[str, str]] = {}
    for lang_key, (namespace, label) in all_tags.items():
        tags_by_namespace.setdefault(namespace, {})[lang_key] = label

    for namespace in sorted(tags_by_namespace):
        tag_map = tags_by_namespace[namespace]
        lang_files = lang_by_namespace.get(namespace, [])
        if not lang_files:
            print(
                f"[warn] {mod_root.name}: namespace '{namespace}' has {len(tag_map)} tag(s) "
                f"but no assets/{namespace}/lang/"
            )
            continue

        if lang_filter:
            lang_files = [p for p in lang_files if p.stem == lang_filter]
            if not lang_files:
                continue

        for lang_file in lang_files:
            lang_data = load_lang(lang_file)
            if lang_data is None:
                continue
            missing = {k: v for k, v in sorted(tag_map.items()) if k not in lang_data}
            if not missing:
                continue

            try:
                rel_lang = lang_file.relative_to(mod_root)
            except ValueError:
                rel_lang = lang_file

            print(f"[{mod_root.name}] {rel_lang}: adding {len(missing)} key(s)")
            for key, label in missing.items():
                print(f"  + {key} = {label!r}")

            if not dry_run:
                lang_data.update(missing)
                save_lang(lang_file, lang_data)

            added_total += len(missing)

    skipped = sorted(set(extra_namespaces) - {mod_id})
    if skipped:
        print(f"[info] {mod_root.name}: also scanned extra namespace(s): {', '.join(skipped)}")

    return added_total


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument(
        "mod_roots",
        nargs="*",
        default=["."],
        help="Mod project directories to scan (default: current directory)",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print missing keys without writing lang files",
    )
    parser.add_argument(
        "--lang",
        metavar="LOCALE",
        default="en_us",
        help="Locale to update (default: en_us)",
    )
    parser.add_argument(
        "--namespace",
        action="append",
        default=[],
        metavar="NS",
        help="Also scan tags for this namespace (repeatable). mod_id is always included.",
    )
    args = parser.parse_args()

    total = 0
    for root_arg in args.mod_roots:
        mod_root = Path(root_arg)
        if not mod_root.is_dir():
            print(f"[error] not a directory: {mod_root}", file=sys.stderr)
            return 1
        total += sync_mod(
            mod_root,
            dry_run=args.dry_run,
            lang_filter=args.lang,
            extra_namespaces=args.namespace,
        )

    action = "Would add" if args.dry_run else "Added"
    print(f"\n{action} {total} translation key(s) total.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
