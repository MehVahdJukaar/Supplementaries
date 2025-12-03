#!/usr/bin/env python3
"""
Generate advancement JSON files from recipe JSON files.

Behavior changes per your request:
 - The recipe ID is always formed as `namespace:path` where `namespace` is a plain string
   set in main() and `path` is the recipe file path relative to recipes_dir (no .json).
 - The script still copies top-level neoforge:conditions and fabric:load_conditions,
   tries to parse shaped/shapeless/ingredients-style recipes, and writes a failed list.
"""

import json
import os
import sys
from pathlib import Path
from typing import List, Dict, Tuple, Optional, Set, Any

def read_json(path: Path) -> Optional[dict]:
    try:
        with path.open("r", encoding="utf-8") as f:
            return json.load(f)
    except Exception as e:
        print(f"Warning: failed to read/parse JSON {path}: {e}", file=sys.stderr)
        return None

def ensure_parent(path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)

def gather_items_from_ingredient(ing: Any) -> List[Tuple[str,str]]:
    found: List[Tuple[str,str]] = []
    if ing is None:
        return found
    if isinstance(ing, str):
        # assume item string
        found.append(("item", ing))
        return found
    if isinstance(ing, dict):
        if "item" in ing and isinstance(ing["item"], str):
            found.append(("item", ing["item"]))
        if "tag" in ing and isinstance(ing["tag"], str):
            found.append(("tag", ing["tag"]))
        if "items" in ing:
            it = ing["items"]
            if isinstance(it, str):
                if it.startswith("#"):
                    found.append(("tag", it[1:]))
                else:
                    found.append(("item", it))
            elif isinstance(it, list):
                for sub in it:
                    found.extend(gather_items_from_ingredient(sub))
        if "ingredient" in ing:
            found.extend(gather_items_from_ingredient(ing["ingredient"]))
        # search nested dict/list values, best-effort
        for v in ing.values():
            if isinstance(v, (dict, list)):
                found.extend(gather_items_from_ingredient(v))
        return found
    if isinstance(ing, list):
        for e in ing:
            found.extend(gather_items_from_ingredient(e))
    return found

def extract_requirements(data: dict) -> Tuple[List[Tuple[str,str]], List[str]]:
    out: List[Tuple[str,str]] = []
    warnings: List[str] = []

    key = data.get("key")
    if isinstance(key, dict):
        for v in key.values():
            out.extend(gather_items_from_ingredient(v))

    if isinstance(data.get("ingredients"), list):
        for ing in data["ingredients"]:
            out.extend(gather_items_from_ingredient(ing))

    for name in ("ingredient", "components", "inputs"):
        val = data.get(name)
        if val is None:
            continue
        out.extend(gather_items_from_ingredient(val))

    seen: Set[Tuple[str,str]] = set()
    deduped: List[Tuple[str,str]] = []
    for pair in out:
        if not isinstance(pair, tuple) or len(pair) != 2:
            continue
        if pair not in seen:
            seen.add(pair)
            deduped.append(pair)
    if not deduped:
        warnings.append("no ingredients found")
    return deduped, warnings

def make_criterion_name(kind: str, identifier: str, idx: int) -> str:
    base = "has_tag" if kind == "tag" else "has_item"
    safe = "".join([c if c.isalnum() else "_" for c in identifier])
    return f"{base}_{safe}_{idx}"

def build_advancement(recipe_id: str,
                      data: dict,
                      ingredient_pairs: List[Tuple[str,str]]) -> dict:
    adv: Dict[str, Any] = {}

    for cond_field in ("neoforge:conditions", "fabric:load_conditions"):
        if cond_field in data:
            adv[cond_field] = data[cond_field]

    adv["parent"] = "minecraft:recipes/root"

    criteria: Dict[str, dict] = {}
    requirement_names: List[str] = []

    for i, (kind, identifier) in enumerate(ingredient_pairs, start=1):
        cname = make_criterion_name(kind, identifier, i)
        ident_val = f"#{identifier}" if kind == "tag" else identifier
        cond = {
            "conditions": {
                "items": [
                    {
                        "items": ident_val
                    }
                ]
            },
            "trigger": "minecraft:inventory_changed"
        }
        criteria[cname] = cond
        requirement_names.append(cname)

    recipe_crit_name = "has_the_recipe"
    criteria[recipe_crit_name] = {
        "conditions": {
            "recipe": recipe_id
        },
        "trigger": "minecraft:recipe_unlocked"
    }
    requirement_names.insert(0, recipe_crit_name)

    adv["criteria"] = criteria
    adv["requirements"] = [requirement_names]
    adv["rewards"] = {
        "recipes": [recipe_id]
    }
    return adv

def write_json(path: Path, data: dict) -> None:
    ensure_parent(path)
    with path.open("w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

def process_file(path: Path, recipes_dir: Path, target_dir: Path, namespace: str) -> Tuple[bool, Optional[str], List[str]]:
    rel = path.relative_to(recipes_dir)
    data = read_json(path)
    warnings: List[str] = []
    if data is None:
        warnings.append("invalid json")
        return False, None, warnings

    # Build recipe id as namespace:path (path is relative path without .json)
    # Use posix style for path (slashes), which is valid in recipe IDs
    rel_no_ext = rel.with_suffix("").as_posix()
    recipe_id = f"{namespace}:{rel_no_ext}"

    ingredients, ingr_warnings = extract_requirements(data)
    warnings.extend(ingr_warnings)

    if not ingredients:
        # Still allow advancement with only recipe unlock condition, but warn
        warnings.append("no parsed ingredients; advancement will only include recipe unlock")
    adv = build_advancement(recipe_id, data, ingredients)

    out_path = target_dir / rel
    write_json(out_path, adv)

    return True, recipe_id, warnings

def walk_and_process(recipes_dir: Path, target_dir: Path, namespace: str) -> List[str]:
    failed: List[str] = []
    total = 0
    for root, dirs, files in os.walk(recipes_dir):
        for fname in files:
            if not fname.lower().endswith(".json"):
                continue
            total += 1
            full = Path(root) / fname
            rel = full.relative_to(recipes_dir)
            ok, rid, warnings = process_file(full, recipes_dir, target_dir, namespace)
            if not ok:
                failed.append(rel.as_posix())
                print(f"[FAILED] {rel}  warnings: {warnings}", file=sys.stderr)
            else:
                if warnings:
                    print(f"[OK] {rel} -> {rid}  (warnings: {warnings})")
                else:
                    print(f"[OK] {rel} -> {rid}")
    print(f"Processed {total} recipe files.")
    return failed

def main():
    # <-- EDIT these plain strings:
    recipes_dir = Path("common/src/main/resources/data/supplementaries/recipe")   # folder to walk recursively containing recipe jsons
    target_dir = Path("common/src/main/resources/data/supplementaries/advancement/recipe")  # folder where advancement jsons will be written
    namespace = "supplementaries"  # <--- set the recipe namespace string here
    # -->

    recipes_dir = recipes_dir.resolve()
    target_dir = target_dir.resolve()
    if not recipes_dir.exists() or not recipes_dir.is_dir():
        print(f"Error: recipes_dir '{recipes_dir}' does not exist or is not a directory.", file=sys.stderr)
        sys.exit(1)

    target_dir.mkdir(parents=True, exist_ok=True)

    failed = walk_and_process(recipes_dir, target_dir, namespace)

    failed_file = target_dir / "failed_recipes.txt"
    if failed:
        with failed_file.open("w", encoding="utf-8") as f:
            for r in failed:
                f.write(r + "\n")
        print(f"Wrote failed recipes list to {failed_file}")
    else:
        if failed_file.exists():
            failed_file.unlink()
        print("All recipes processed successfully (no failures).")

if __name__ == "__main__":
    main()
