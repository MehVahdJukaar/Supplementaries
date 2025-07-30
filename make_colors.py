import os
import argparse
import re
import json

COLORS = [
    "black", "gray", "light_gray", "red", "brown", "white", "orange", "yellow", "pink"
    "lime", "green", "cyan", "light_blue", "blue", "magenta", "purple"
]

def replace_color_in_data(data, base_color, new_color):
    """
    Recursively replace base_color with new_color in all string values and keys,
    except in values of keys named "parent".
    """
    if isinstance(data, dict):
        new_dict = {}
        for k, v in data.items():
            new_key = k.replace(base_color, new_color)
            if k == "parent" and isinstance(v, str):
                new_dict[new_key] = v  # do NOT replace inside parent
            else:
                new_dict[new_key] = replace_color_in_data(v, base_color, new_color)
        return new_dict
    elif isinstance(data, list):
        return [replace_color_in_data(item, base_color, new_color) for item in data]
    elif isinstance(data, str):
        # Replace only exact matches (not substrings in other words)
        pattern = re.compile(rf'(?<![a-zA-Z0-9]){re.escape(base_color)}(?![a-zA-Z0-9])')
        return pattern.sub(new_color, data)
    else:
        return data

def generate_all_colors_from_file(file_path):
    if not os.path.isfile(file_path) or not file_path.endswith(".json"):
        print("Provided path is not a valid .json file." + file_path)
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

    # Read JSON content
    with open(file_path, "r", encoding="utf-8") as f:
        try:
            content = json.load(f)
        except json.JSONDecodeError as e:
            print(f"Invalid JSON in {file_path}: {e}")
            return

    for color in COLORS:
        if color == base_color:
            continue

        new_filename = filename.replace(base_color, color)
        new_filepath = os.path.join(folder, new_filename)

        if os.path.exists(new_filepath):
            print(f"Skipped (already exists): {new_filepath}")
            continue

        new_content = replace_color_in_data(content, base_color, color)

        with open(new_filepath, "w", encoding="utf-8") as f:
            json.dump(new_content, f, indent=2)
        print(f"Created: {new_filepath}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate color variants of a JSON file based on its name and contents.")
    parser.add_argument("file_path", type=str, help="Path to the .json file containing a base color name.")
    args = parser.parse_args()

    generate_all_colors_from_file("./common/src/main/resources/"+ args.file_path+".json")
