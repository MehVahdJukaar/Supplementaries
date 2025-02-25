import os
import json

def convert_conditions(fabric_conditions):
    """Convert fabric:load_conditions to neoforge:load_conditions, keeping all keys except 'condition', which is renamed to 'type'."""
    return [{"type": cond.pop("condition", None), **cond} for cond in fabric_conditions]

def process_json_file(file_path):
    """Process a JSON file to add neoforge:load_conditions if needed."""
    with open(file_path, 'r', encoding='utf-8') as file:
        try:
            data = json.load(file)
        except json.JSONDecodeError:
            print(f"Skipping invalid JSON file: {file_path}")
            return

    if "fabric:load_conditions" in data and "neoforge:load_conditions" not in data:
        data["neoforge:load_conditions"] = convert_conditions(data["fabric:load_conditions"])
        with open(file_path, 'w', encoding='utf-8') as file:
            json.dump(data, file, indent=2)
        print(f"Updated: {file_path}")

def process_folder(directory):
    """Recursively process all JSON files in a directory."""
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".json"):
                process_json_file(os.path.join(root, file))

if __name__ == "__main__":
    folder_path = "src/main/resources"
    if os.path.isdir(folder_path):
        process_folder(folder_path)
        print("Processing complete.")
    else:
        print("Invalid folder path.")
