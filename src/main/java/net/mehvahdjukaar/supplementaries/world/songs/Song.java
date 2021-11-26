package net.mehvahdjukaar.supplementaries.world.songs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

//needed for Gson conversion
@SuppressWarnings("FieldMayBeFinal")
public class Song {

    private String name;
    private int tempo;
    private Integer[] notes;

    private String credits;

    public static final Song EMPTY = new Song("Error", 1, new Integer[0]);

    public Song(String name, int tempo, Integer[] notes) {
        this(name, tempo, notes, "");
    }

    public Song(String name, int tempo, Integer[] notes, String credits) {
        this.name = name;
        this.tempo = Math.max(1, tempo);
        this.notes = notes;
        this.credits = credits;
    }

    //makes it usable to be played
    public void processForPlaying() {
        List<Integer> newNotes = new ArrayList<>();
        for (int i : notes) {
            if (i <= 0) {
                int j = -Math.min(-1, i);
                //-1 and 0 are the same
                int blanks = j - 1;

                for (int k = 0; k < blanks; k++) {
                    newNotes.add(0);
                }

            } else newNotes.add(i);
        }
        this.notes = newNotes.toArray(new Integer[0]);
    }

    public String getTranslationKey() {
        return name;
    }

    public TranslatableComponent getName() {
        return new TranslatableComponent(getTranslationKey());
    }

    public int getTempo() {
        if(tempo<=1){
            int a =1;
        }
        return  Math.max(1,tempo);
    }

    public Integer[] getNotes() {
        return notes;
    }

    public static CompoundTag saveToTag(Song song) {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", song.name);
        tag.putInt("tempo", song.tempo);
        tag.putIntArray("notes", List.of(song.notes));
        tag.putString("credits", song.credits);
        return tag;
    }

    public static Song loadFromTag(CompoundTag tag) {
        String name = tag.getString("name");
        int tempo = tag.getInt("tempo");
        int[] notes = tag.getIntArray("notes");

        Integer[] n = new Integer[notes.length];
        for (int i = 0; i < notes.length; i++) {
            n[i] = notes[i];
        }
        String credits = tag.getString("credits");
        return new Song(name, tempo, n, credits);
    }

    public List<Integer> getNoteToPlay(long timeSinceStarted) {
        List<Integer> toPlay = new ArrayList<>();

        try {
            int currentIndex = (int) (timeSinceStarted / this.getTempo()) % this.notes.length;
            int n = notes[currentIndex];
            while (n > 1) {
                toPlay.add(Mth.clamp(n % 100, 0, 25));
                n = n / 100;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toPlay;
    }
    //TODO: fix 0 ength songs

    @Override
    public String toString() {
        return "Song{" +
                "name='" + name + '\'' +
                '}';
    }
}