package net.mehvahdjukaar.supplementaries.common.misc.songs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2IntRBTreeMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.HatStandEntity;
import net.mehvahdjukaar.supplementaries.common.items.SongInstrumentItem;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundPlaySongNotesPacket;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SongsManager extends SimpleJsonResourceReloadListener {

    private static final Map<String, Song> SONGS = new LinkedHashMap<>();
    private static final List<WeightedEntry.Wrapper<String>> SONG_WEIGHTED_LIST = new ArrayList<>();
    private static final List<String> CAROLS = List.of("carol of the bells", "merry xmas", "jingle bells");

    //randomly selected currently playing songs
    private static final Map<UUID, Song> CURRENTLY_PAYING = new HashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final HolderLookup.Provider registryAccess;

    public SongsManager(HolderLookup.Provider registryAccess) {
        super(GSON, "flute_songs");
        this.registryAccess = registryAccess;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profile) {
        SONGS.clear();
        SONG_WEIGHTED_LIST.clear();
        List<Song> temp = new ArrayList<>();
        var ops = ForgeHelper.conditionalOps(JsonOps.INSTANCE, registryAccess, this);
        var codec = ForgeHelper.conditionalCodec(Song.CODEC);
        jsons.forEach((key, json) -> {
            try {
                var result = codec.parse(ops, json);
                var song = result.getOrThrow();
                if(song.isPresent()) {
                    temp.add(song.get());
                    SongsManager.addSong(song.get());
                }
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to parse JSON object for song {}", key, e);
            }
        });
        if (!temp.isEmpty()) Supplementaries.LOGGER.info("Loaded {} flute songs", temp.size());
    }

    private static void addSong(Song song) {
        SONGS.put(song.getName(), song);
        int weight = song.getWeight();
        if (weight > 0) SONG_WEIGHTED_LIST.add(WeightedEntry.wrap(song.getName(), weight));
    }

    public static void acceptClientSongs(List<Song> songs) {
        SONGS.clear();
        SONG_WEIGHTED_LIST.clear();
        songs.forEach(SongsManager::addSong);
    }

    public static Song setCurrentlyPlaying(UUID id, String songKey) {
        Song song = SONGS.getOrDefault(songKey, Song.EMPTY);
        CURRENTLY_PAYING.put(id, song);
        song.validatePlayReady();
        return song;
    }

    public static void clearCurrentlyPlaying(UUID id) {
        CURRENTLY_PAYING.remove(id);
    }

    @NotNull
    private static String selectRandomSong(RandomSource random) {
        if (MiscUtils.FESTIVITY.isChristmas() && random.nextFloat() > 0.8) {
            return CAROLS.get(random.nextInt(CAROLS.size()));
        }
        Optional<WeightedEntry.Wrapper<String>> song = WeightedRandom.getRandomItem(random, SONG_WEIGHTED_LIST);
        return song.map(WeightedEntry.Wrapper::data).orElse("");
    }

    //called on server only
    public static void playRandomSong(ItemStack stack, SongInstrumentItem instrument, LivingEntity entity,
                                      long timeSinceStarted) {
        UUID id = entity.getUUID();
        Song song;
        if (!CURRENTLY_PAYING.containsKey(id)) {

            String res = null;
            if (stack.has(DataComponents.CUSTOM_NAME)) {
                String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
                for (var v : SONGS.keySet()) {
                    if (v.equals(name)) {
                        res = v;
                        break;
                    }
                }
            }
            if (res == null) res = selectRandomSong(entity.level().random);

            song = setCurrentlyPlaying(id, res);

        } else {
            song = CURRENTLY_PAYING.get(id);
        }

        playSong(instrument, entity, song, timeSinceStarted);
    }

    public static boolean playSong(SongInstrumentItem instrumentItem, LivingEntity entity, String sandstorm,
                                   long timeSinceStarted) {
        return playSong(instrumentItem, entity, SONGS.getOrDefault(sandstorm, Song.EMPTY), timeSinceStarted);
    }

    //server controls everything here
    public static boolean playSong(SongInstrumentItem instrument, LivingEntity entity, Song song, long timeSinceStarted) {
        boolean played = false;
        if (timeSinceStarted % song.getTempo() == 0) {
            IntList notes = song.getNoteToPlay(timeSinceStarted);
            if (!notes.isEmpty() && notes.getInt(0) > 0) {
                NetworkHelper.sendToAllClientPlayersTrackingEntityAndSelf(entity,
                        new ClientBoundPlaySongNotesPacket(notes, entity));

                played = true;
            }

            if (timeSinceStarted == 53 && song.getName().equals("skibidi")) {
                HatStandEntity.makeSkibidiInArea(entity);
            }
        }
        return played;
    }

    //util to make songs from a map
    private static final Map<Long, List<Integer>> RECORDING = new HashMap<>();
    private static final List<NoteBlockInstrument> WHITELIST = new ArrayList<>();

    private static boolean isRecording = false;
    private static Source soundSource = Source.NOTE_BLOCKS;

    public static void startRecording(Source source, NoteBlockInstrument[] whitelist) {
        RECORDING.clear();
        isRecording = true;
        soundSource = source;
        WHITELIST.clear();
        WHITELIST.addAll(List.of(whitelist));
    }

    public static String stopRecording(Level level, String name, int speedup) {
        isRecording = false;

        //this could be simplified alot
        long start = Long.MAX_VALUE;
        for (Long s : RECORDING.keySet()) {
            start = Math.min(start, s);
        }

        //sort and group notes and translate time
        Int2IntRBTreeMap treeMap = new Int2IntRBTreeMap();

        for (var e : RECORDING.entrySet()) {
            int notes = 0;
            var noteList = e.getValue();
            //can store max 4 notes in signed int
            for (int i = 0; i < Math.min(4, noteList.size()); i++) {
                notes += (int) (noteList.get(i) * Math.pow(100, i));
            }
            treeMap.put((int) (e.getKey() - start), notes);
        }

        int largestInterval = 1;
        IntArraySet intervals = new IntArraySet();

        //put everything in one list
        IntList arrayList = new IntArrayList();
        int lastTime = 0;
        for (var entry : treeMap.int2IntEntrySet()) {
            int note = entry.getIntValue();
            int key = entry.getIntKey();
            int interval = -(key - lastTime);
            lastTime = key;
            if (interval != 0) {
                intervals.add(-interval);
                if (-interval > largestInterval) largestInterval = -interval;
                //skips first 0 interval
                arrayList.add(interval);
            }

            arrayList.add(note);
        }

        //finds GCD
        int GCD = 1;
        for (int div = largestInterval; div > 0; div--) {
            int d = div;
            boolean match = intervals.intStream().allMatch(j -> j % d == 0);
            if (match) {
                GCD = Math.abs(div);
                break;
            }
        }

        //simplify
        IntList finalNotes = new IntArrayList();

        for (int i : arrayList) {
            if (i < 0) finalNotes.add((i / GCD));
            else finalNotes.add(i);
        }

        if (name.isEmpty()) name = "recorded-" + start;

        Song song = new Song(name, GCD, finalNotes, "recorded in-game", 100);

        saveRecordedSong(song);

        //temporarily adds the song
        //SONGS.clear();
        //SONGS.put(name, song);

        RECORDING.clear();
        return song.getTranslationKey();
    }

    public static void recordNoteFromNoteBlock(LevelAccessor levelAccessor, BlockPos pos) {
        if (isRecording && soundSource == Source.NOTE_BLOCKS && levelAccessor instanceof Level level) {
            BlockState state = level.getBlockState(pos);
            recordNote(level, state.getValue(NoteBlock.NOTE) + 1, state.getValue(NoteBlock.INSTRUMENT));
        }
    }


    public static void recordNoteFromSound(SoundInstance sound, String name) {
        if (isRecording && name.startsWith("block.note_block") && soundSource == Source.SOUND_EVENTS) {
            try {
                String[] parts = name.split("\\.");
                String result = parts[parts.length - 1];

                NoteBlockInstrument inst = NoteBlockInstrument.valueOf(result.toUpperCase(Locale.ROOT));
                float pitch = sound.getPitch();
                int note = (int) Math.round(12 * (Math.log(pitch) / Math.log(2)) + 12);
                if (sound.getSource() == SoundSource.RECORDS) {
                    recordNote(Minecraft.getInstance().getCameraEntity().level(), note, inst);
                }
            } catch (Exception ignored) {
                int aa = 1;
            }
        }
    }


    private static void recordNote(Level level, int note, NoteBlockInstrument instrument) {
        if (WHITELIST.isEmpty() || WHITELIST.contains(instrument)) {
            List<Integer> notes = RECORDING.computeIfAbsent(level.getGameTime(), t -> new ArrayList<>());
            notes.add(note);
        }
    }

    private static void saveRecordedSong(Song song) {
        File folder = PlatHelper.getGamePath().resolve("recorded_songs").toFile();

        if (!folder.exists()) {
            folder.mkdir();
        }

        File exportPath = new File(folder, song.getTranslationKey() + ".json");

        try {
            try (FileWriter writer = new FileWriter(exportPath)) {
                DataResult<JsonElement> r = Song.CODEC.encodeStart(JsonOps.INSTANCE, song);
                r.result().ifPresent(a -> GSON.toJson(a.getAsJsonObject(), writer));
            }
        } catch (IOException e) {
            Supplementaries.error("Failed to save recorded song: ", e);
        }
    }

    public enum Source {
        NOTE_BLOCKS, SOUND_EVENTS
    }
}
