import os
import argparse

# List of Minecraft colors
COLORS = [
    "black", "gray", "light_gray", "red", "brown", "white", "orange", "yellow",
    "lime", "green", "cyan", "light_blue", "blue", "magenta", "purple", "white"
]

def generate_colowhite_files(folder_path):
    # List all JSON files that contain "white" in the name
    folder_path = "common/src/main/resources/assets/supplementaries/"+ folder_path
    white_files = [f for f in os.listdir(folder_path) if "white" in f and f.endswith(".json")]

    if not white_files:
        print("No JSON files containing 'white' found in the specified folder.")
        return

    for white_file_name in white_files:
        white_file_path = os.path.join(folder_path, white_file_name)

        # Read the content of the white file
        with open(white_file_path, "r") as white_file:
            white_content = white_file.read()

        for color in COLORS:
            if color == "white":
                continue  # Skip generating a file for white itself

            # Replace "white" with the current color in both filename and content
            colowhite_file_name = white_file_name.replace("white", color)
            colowhite_content = white_content.replace("white", color)

            colowhite_file_path = os.path.join(folder_path, colowhite_file_name)
            with open(colowhite_file_path, "w") as colowhite_file:
                colowhite_file.write(colowhite_content)
            print(f"Created: {colowhite_file_path}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate colowhite JSON files from all white*.json files.")
    parser.add_argument("folder_path", type=str, help="Path to the folder containing JSON files with 'white' in the name")
    args = parser.parse_args()

    generate_colowhite_files(args.folder_path)
