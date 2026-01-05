#!/usr/bin/env python3
"""
apply_source_colors_two_clusters_mixed.py

Recolor the source image into the palettes of other images (in-place), using:
 - White cluster: map by palette index (sorted order)
 - Colored cluster: map by luminance
"""

from PIL import Image
import argparse
import os
import sys
from typing import Tuple, List, Dict

RGBA = Tuple[int, int, int, int]


def luminance(c: RGBA) -> float:
    r, g, b, a = c
    return 0.2126*r + 0.7152*g + 0.0722*b


def rgb_dist_sq_to_white(c: RGBA) -> int:
    r, g, b, a = c
    dr = 255 - r
    dg = 255 - g
    db = 255 - b
    return dr*dr + dg*dg + db*db


def get_nontransparent_colors_sorted(img: Image.Image) -> List[RGBA]:
    rgba = img.convert("RGBA")
    s = {px for px in rgba.getdata() if px[3] != 0}
    return sorted(s, key=lambda c: (luminance(c), c[3], c[0], c[1], c[2]))


def split_palette_white_colored(palette: List[RGBA], white_threshold: int) -> Tuple[List[RGBA], List[RGBA]]:
    thr_sq = white_threshold*white_threshold
    white = []
    colored = []
    for c in palette:
        if rgb_dist_sq_to_white(c) <= thr_sq:
            white.append(c)
        else:
            colored.append(c)
    return white, colored


def map_by_index(source: List[RGBA], target: List[RGBA]) -> Dict[RGBA, RGBA]:
    mapping = {}
    if not target:
        # fallback to source itself (no mapping possible)
        for c in source:
            mapping[c] = c
        return mapping
    min_len = min(len(source), len(target))
    for i in range(min_len):
        mapping[source[i]] = target[i]
    # if source has extra colors, map them all to last target color
    for i in range(min_len, len(source)):
        mapping[source[i]] = target[-1]
    return mapping


def map_by_luminance(source: List[RGBA], target: List[RGBA]) -> Dict[RGBA, RGBA]:
    target_lums = [luminance(c) for c in target]
    mapping = {}
    for sc in source:
        s_l = luminance(sc)
        best_idx = 0
        best_diff = abs(s_l - target_lums[0])
        for i in range(1, len(target)):
            d = abs(s_l - target_lums[i])
            if d < best_diff:
                best_diff = d
                best_idx = i
        mapping[sc] = target[best_idx]
    return mapping


def build_combined_mapping(source_palette: List[RGBA], other_palette: List[RGBA], white_threshold: int) -> Dict[RGBA, RGBA]:
    src_white, src_colored = split_palette_white_colored(source_palette, white_threshold)
    oth_white, oth_colored = split_palette_white_colored(other_palette, white_threshold)

    mapping: Dict[RGBA, RGBA] = {}

    # White cluster: map by index (sorted order)
    if src_white:
        if not oth_white:
            # fallback to colored cluster
            mapping.update(map_by_index(src_white, oth_colored))
        else:
            mapping.update(map_by_index(src_white, oth_white))

    # Colored cluster: map by luminance
    if src_colored:
        if not oth_colored:
            # fallback to white cluster
            mapping.update(map_by_luminance(src_colored, oth_white))
        else:
            mapping.update(map_by_luminance(src_colored, oth_colored))

    return mapping


def recolor_source_using_mapping(source_img: Image.Image, mapping: Dict[RGBA, RGBA]) -> Image.Image:
    rgba = source_img.convert("RGBA")
    out = []
    for px in rgba.getdata():
        if px[3] == 0:
            out.append(px)
        else:
            out.append(mapping.get(px, px))
    new_img = Image.new("RGBA", rgba.size)
    new_img.putdata(out)
    return new_img


def main(folder: str, source_filename: str, white_threshold: int):
    folder = os.path.abspath(folder)
    if not os.path.isdir(folder):
        sys.exit(f"Folder not found: {folder}")

    source_path = os.path.join(folder, source_filename)
    if not os.path.isfile(source_path):
        sys.exit(f"Source file not found: {source_path}")

    source_img = Image.open(source_path).convert("RGBA")
    source_palette = get_nontransparent_colors_sorted(source_img)
    if not source_palette:
        sys.exit("Source image has no non-transparent colors.")
    print(f"Source '{source_filename}': size={source_img.size} non-transparent colors={len(source_palette)}")

    for fname in sorted(os.listdir(folder)):
        if not fname.lower().endswith(".png") or fname == source_filename:
            continue

        other_path = os.path.join(folder, fname)
        try:
            other_img = Image.open(other_path).convert("RGBA")
        except:
            print(f"Skipping '{fname}': cannot open as image")
            continue

        # Skip images with same resolution as source
        if other_img.size == source_img.size:
            print(f"Skipping '{fname}': same resolution as source {other_img.size}")
            continue

        other_palette = get_nontransparent_colors_sorted(other_img)
        if not other_palette:
            print(f"Skipping '{fname}': all transparent")
            continue

        print(f"Processing '{fname}': size={other_img.size} non-transparent colors={len(other_palette)}")

        mapping = build_combined_mapping(source_palette, other_palette, white_threshold)
        recolored = recolor_source_using_mapping(source_img, mapping)

        try:
            recolored.save(other_path, format="PNG")
            print(f"Overwrote '{fname}' with recolored source image.")
        except Exception as e:
            print(f"Failed to save '{fname}': {e}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Recolor source image into other images using index/luminance for clusters")
    parser.add_argument("folder", help="Folder containing images")
    parser.add_argument("source", help="Source PNG filename in the folder")
    parser.add_argument("--white-threshold", type=int, default=100,
                        help="RGB distance-to-white threshold for white cluster (default 30)")
    args = parser.parse_args()
    main(args.folder, args.source, args.white_threshold)
