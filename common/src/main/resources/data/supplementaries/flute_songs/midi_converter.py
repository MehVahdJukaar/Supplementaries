import json

minecraft_to_midi = {
    0: 45,
    1: 47,
    2: 49,
    3: 50,
    4: 52,
    5: 54,
    6: 55,
    7: 57,
    8: 59,
    9: 60,
    10: 62,
    11: 64,
    12: 66,
    13: 67,
    14: 69,
    15: 71,
    16: 72,
    17: 74,
    18: 76,
    19: 77,
    20: 79,
    21: 81,
    22: 83,
    23: 84,
    24: 86
}


def find_closest(target_midi, precise: bool):
    closest_pitch = None
    min_distance = float('inf')

    for minecraft_pitch, midi_note in minecraft_to_midi.items():
        distance = abs(midi_note - target_midi)
        if distance < min_distance:
            min_distance = distance
            closest_pitch = minecraft_pitch

    return str('{:02d}'.format(closest_pitch))


print("Welcome to Supplementaries midi to flute format song converted."
      "Use this to convert midi json files generated with the website https://www.visipiano.com/midi-to-json-converter/")

while True:
    file_name = input("Enter the name of the JSON file: ")

    try:
        # Load the JSON data from the file
        with open(file_name, 'r') as file:
            data = json.load(file)
    except:
        print("No such file")
        continue

    tracks = data['tracks']
    selected_track = input("Found " + str(len(tracks)) + " tracks. Select one: ")
    # Extract the list of notes from the JSON data
    try:
        notes = data['tracks'][int(selected_track)]['notes']
    except:
        print("No such track")
        continue

    # Sort the notes by their 'time' attribute
    sorted_notes = sorted(notes, key=lambda note: note['time'])

    # Initialize a list to store the ordered notes with time steps
    ordered_notes_with_time_steps = []

    # Find the smallest time increment (time step)
    smallest_time_step = float('inf')

    # Initialize a list for the first group of notes
    current_group = [sorted_notes[0]["midi"]]

    # Iterate through the sorted notes to group them by time and add time steps
    current_time = 0
    for i in range(1, len(sorted_notes)):
        note = sorted_notes[i]
        if note["time"] == current_time:
            current_group.append(note["midi"])
        else:
            t = note["time"]
            delta = t - current_time
            smallest_time_step = min(delta, smallest_time_step)
            current_time = t
            ordered_notes_with_time_steps.append(current_group)
            ordered_notes_with_time_steps.append(delta)
            current_group = [note["midi"]]

    # Add the last group of notes and the corresponding time step
    ordered_notes_with_time_steps.append(current_group)

    skip_unknown = False # input("Skip unknown notes?").lower()=="yes"

    packed_notes = []

    for item in ordered_notes_with_time_steps:
        if isinstance(item, list):
            # Sum up and map the values in the sub-array
            sub_list = item[:5]
            concatenated_notes = "".join(str(find_closest(midi, skip_unknown)) for midi in sub_list)
            packed_notes.append(int(concatenated_notes))
        else:
            # Map the single value using the mapping dictionary
            mapped_value = -item / smallest_time_step
            packed_notes.append(mapped_value)

    tempo = input("Enter the tempo: ")
    name = input("Enter the output name: ")

    data = {
        "name": name,
        "tempo": int(tempo),
        "notes": packed_notes
    }

    # Ask the user for the file name to save the JSON data
    file_name = name+".json"

    # Save the JSON data to a file
    with open(file_name, 'w') as file:
        json.dump(data, file, indent=2)

    print("Successfully converted ", file_name)
