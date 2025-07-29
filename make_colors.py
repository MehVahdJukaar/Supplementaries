import os
import argparse
import re

COLORS = [
    "black", "gray", "light_gray", "red", "brown", "white", "orange", "yellow",
    "lime", "green", "cyan", "light_blue", "blue", "magenta", "purple"
]

def generate_all_colors_from_file(file_path):
    if not os.path.isfile(file_path) or not file_path.endswith(".json"):
        print("Provided path is not a valid .json file. " + file_path)
        return

    filename = os.path.basename(file_path)
    folder = os.path.dirname(file_path)

    # Detect base color in filename
    base_color = None
    for color in COLORS:
        if color in filename:
            base_color = color
            break

    if base_color is None:
        print("No known color found in filename.")
        return

    # Read original file content
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    for color in COLORS:
        if color == base_color:
            continue

        # Create new filename by replacing color
        new_filename = filename.replace(base_color, color)
        new_filepath = os.path.join(folder, new_filename)

        if os.path.exists(new_filepath):
            print(f"Skipped (already exists): {new_filepath}")
            continue

        # Replace ALL occurrences of the base color in content
        # This includes paths like "block/white_lamp" → "block/red_lamp"
        # It replaces color names when they're:
        # - surrounded by word boundaries (\b)
        # - preceded by / or _
        pattern = re.compile(rf'(?<![a-zA-Z0-9]){re.escape(base_color)}(?![a-zA-Z0-9])')
        new_content = pattern.sub(color, content)

        with open(new_filepath, "w", encoding="utf-8") as f:
            f.write(new_content)

        print(f"Created: {new_filepath}")
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate color variants of a JSON file based on its name and contents.")
    parser.add_argument("file_path", type=str, help="Path to the .json file containing a base color name.")
    args = parser.parse_args()

    generate_all_colors_from_file("./common/src/main/resources/"+ args.file_path+".json")
