package net.mehvahdjukaar.supplementaries.common.misc.songs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.InstrumentItem;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundPlaySongNotesPacket;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncSongsPacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
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

    //randomly selected currently playing songs
    private static final Map<UUID, Song> CURRENTLY_PAYING = new HashMap<>();


    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final SongsManager RELOAD_INSTANCE = new SongsManager();

    public SongsManager() {
        super(GSON, "flute_songs");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profile) {
        SONGS.clear();
        SONG_WEIGHTED_LIST.clear();
        List<Song> temp = new ArrayList<>();
        jsons.forEach((key, json) -> {
            try {
                var result = Song.CODEC.parse(JsonOps.INSTANCE, json);
                Song song = result.getOrThrow(false, e -> Supplementaries.LOGGER.error("Failed to parse flute song: {}", e));
                temp.add(song);
                SongsManager.addSong(song);
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to parse JSON object for song " + key);
            }
        });
        if (temp.size() != 0) Supplementaries.LOGGER.info("Loaded  " + temp.size() + " flute songs");
    }

    private static void addSong(Song song) {
        SONGS.put(song.getName(), song);
        SONG_WEIGHTED_LIST.add(WeightedEntry.wrap(song.getName(), song.getWeight()));
    }

    public static void acceptClientSongs(List<Song> songs) {
        SONGS.clear();
        SONG_WEIGHTED_LIST.clear();
        songs.forEach(SongsManager::addSong);
    }

    public static void sendDataToClient(ServerPlayer player) {
        NetworkHandler.CHANNEL.sendToClientPlayer(player,new ClientBoundSyncSongsPacket(SongsManager.SONGS.values()));
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
        Optional<WeightedEntry.Wrapper<String>> song = WeightedRandom.getRandomItem(random, SONG_WEIGHTED_LIST);
        return song.map(WeightedEntry.Wrapper::getData).orElseGet(() ->"");
    }

    //called on server only
    public static void playRandomSong(ItemStack stack, InstrumentItem instrument, LivingEntity entity,
                                      long timeSinceStarted) {
        UUID id = entity.getUUID();
        Song song;
        if (!CURRENTLY_PAYING.containsKey(id)) {

            String res = null;
            if (stack.hasCustomHoverName()) {
                String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
                for (var v : SONGS.keySet()) {
                    if (v.equals(name)) {
                        res = v;
                        break;
                    }
                }
            }
            if (res == null) res = selectRandomSong(entity.level.random);

            song = setCurrentlyPlaying(id, res);

        } else {
            song = CURRENTLY_PAYING.get(id);
        }

        playSong(instrument, entity, song, timeSinceStarted);
    }

    public static boolean playSong(InstrumentItem instrumentItem, LivingEntity entity, String sandstorm,
                                   long timeSinceStarted) {
        return playSong(instrumentItem, entity, SONGS.getOrDefault(sandstorm, Song.EMPTY), timeSinceStarted);
    }

    //server controls everything here
    public static boolean playSong(InstrumentItem instrument, LivingEntity entity, Song song,
                                   long timeSinceStarted) {
        boolean played = false;
        if (timeSinceStarted % song.getTempo() == 0) {
            IntList notes = song.getNoteToPlay(timeSinceStarted);
            if (!notes.isEmpty() && notes.getInt(0) > 0) {
                NetworkHandler.CHANNEL.sentToAllClientPlayersTrackingEntityAndSelf(entity,
                        new ClientBoundPlaySongNotesPacket(notes, entity));

                played = true;
            }
        }
        return played;
    }

    //util to make songs from a map
    private static final Map<Long, List<Integer>> RECORDING = new HashMap<>();
    private static final List<NoteBlockInstrument> WHITELIST = new ArrayList<>();

    private static boolean isRecording = false;

    public static void startRecording(NoteBlockInstrument[] whitelist) {
        RECORDING.clear();
        isRecording = true;
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
        TreeMap<Integer, Integer> treeMap = new TreeMap<>();

        for (var e : RECORDING.entrySet()) {
            int notes = 0;
            List<Integer> noteList = e.getValue();
            //can store max 4 notes in signed int
            for (int i = 0; i < Math.min(4, noteList.size()); i++) {
                notes += noteList.get(i) * Math.pow(100, i);
            }
            treeMap.put((int) (e.getKey() - start), notes);
        }

        int largestInterval = 1;
        Set<Integer> intervals = new HashSet<>();

        //put everything in one list
        List<Integer> arrayList = new ArrayList<>();
        int lastTime = 0;
        for (var entry : treeMap.entrySet()) {
            int note = entry.getValue();
            int key = entry.getKey();
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
            boolean match = intervals.stream().allMatch(j -> j % d == 0);
            if (match) {
                GCD = Math.abs(div);
                break;
            }
        }

        //simplify
        List<Integer> finalNotes = new ArrayList<>();

        for (int i : arrayList) {
            if (i < 0) finalNotes.add((i / GCD));
            else finalNotes.add(i);
        }

        if (name.isEmpty()) name = "recorded-" + start;

        Song song = new Song(name, GCD, finalNotes, "recorded in-game", 100);

        saveRecordedSong(song);

        //temporarily adds the song
        SONGS.clear();
        SONGS.put(name, song);

        if (!level.isClientSide) {
            NetworkHandler.CHANNEL.sendToAllClientPlayers(new ClientBoundSyncSongsPacket(SongsManager.SONGS.values()));
        }

        RECORDING.clear();
        return song.getTranslationKey();
    }

    public static void recordNote(LevelAccessor levelAccessor, BlockPos pos) {
        if (levelAccessor instanceof Level level && isRecording) {
            BlockState state = level.getBlockState(pos);
            recordNote(level, state.getValue(NoteBlock.NOTE) + 1, state.getValue(NoteBlock.INSTRUMENT));
        }
    }

    public static void recordNote(Level level, int note, NoteBlockInstrument instrument) {
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
            e.printStackTrace();
        }
    }
}
