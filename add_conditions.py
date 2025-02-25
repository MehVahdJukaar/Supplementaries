import os
import json

def process_neoforge_condition(condition):
    """
    Recursively convert a condition object for neoforge:
    - If the dict has a "condition" key, use its value (with fabric:→neoforge: replacement) for the "type" key.
    - Remove the "condition" key.
    - Process nested dicts/lists similarly.
    - Finally, reorder keys so that "type" comes first.
    """
    new_cond = {}
    # If a "condition" key exists, use it to create "type"
    if "condition" in condition:
        val = condition["condition"]
        if isinstance(val, str):
            new_cond["type"] = val.replace("fabric:", "neoforge:")
        else:
            new_cond["type"] = val
    # Process remaining keys
    for key, value in condition.items():
        if key == "condition":
            continue  # skip original
        # Process recursively if needed
        if isinstance(value, dict):
            new_value = process_neoforge_condition(value)
        elif isinstance(value, list):
            new_list = []
            for item in value:
                if isinstance(item, dict):
                    new_list.append(process_neoforge_condition(item))
                elif isinstance(item, str):
                    new_list.append(item.replace("fabric:", "neoforge:"))
                else:
                    new_list.append(item)
            new_value = new_list
        elif isinstance(value, str):
            new_value = value.replace("fabric:", "neoforge:")
        else:
            new_value = value
        new_cond[key] = new_value
    # Reorder keys: ensure "type" is first if present
    if "type" in new_cond:
        ordered = {"type": new_cond["type"]}
        for k, v in new_cond.items():
            if k != "type":
                ordered[k] = v
        return ordered
    return new_cond

def reorder_top_level_keys(data):
    """
    Reorder top-level keys so that if present, "fabric:load_conditions" comes first,
    then "neoforge:conditions", then all other keys.
    """
    new_data = {}
    for key in ["fabric:load_conditions", "neoforge:conditions"]:
        if key in data:
            new_data[key] = data[key]
    for key, value in data.items():
        if key not in new_data:
            new_data[key] = value
    return new_data

def process_json_file(file_path):
    """Process a JSON file only if it contains fabric:load_conditions or neoforge:conditions."""
    with open(file_path, 'r', encoding='utf-8') as file:
        try:
            data = json.load(file)
        except json.JSONDecodeError:
            print(f"Skipping invalid JSON file: {file_path}")
            return

    modified = False
    # Only process if at least one of the keys exists.
    if "fabric:load_conditions" in data or "neoforge:conditions" in data:
        # Leave fabric:load_conditions untouched.
        # For neoforge:conditions, generate/update it based on fabric if needed.
        if "fabric:load_conditions" in data and "neoforge:conditions" not in data:
            data["neoforge:conditions"] = [
                process_neoforge_condition(cond)
                for cond in data["fabric:load_conditions"]
            ]
            modified = True
        if "neoforge:conditions" in data:
            data["neoforge:conditions"] = [
                process_neoforge_condition(cond)
                for cond in data["neoforge:conditions"]
            ]
            modified = True

    if modified:
        # Reorder top-level keys before writing back.
        data = reorder_top_level_keys(data)
        with open(file_path, 'w', encoding='utf-8') as file:
            json.dump(data, file, ensure_ascii=False, indent=2)
        print(f"Updated: {file_path}")
    else:
        print(f"No changes needed: {file_path}")

def process_folder(directory):
    """Recursively process all JSON files in a directory."""
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".json"):
                process_json_file(os.path.join(root, file))

if __name__ == "__main__":
    folder_path = "common/src/main/resources"
    if os.path.isdir(folder_path):
        process_folder(folder_path)
        print("Processing complete.")
    else:
        print("Invalid folder path.")
