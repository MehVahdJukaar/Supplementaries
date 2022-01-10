package net.mehvahdjukaar.supplementaries.common.world.songs;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SafeBlock;
import net.mehvahdjukaar.supplementaries.common.items.InstrumentItem;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSetSongPacket;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncSongsPacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.*;

public class SongsManager {

    private static final Map<ResourceLocation, Song> SONGS = new LinkedHashMap<>();
    private static final List<WeightedEntry.Wrapper<ResourceLocation>> SONG_WEIGHTED_LIST = new ArrayList<>();

    //randomly selected currently playing songs
    private static final Map<UUID, Song> CURRENTLY_PAYING = new HashMap<>();

    public static void addSong(ResourceLocation res, Song song) {
        SONGS.put(res, song);
        SONG_WEIGHTED_LIST.add(WeightedEntry.wrap(res, song.getWeight()));
    }

    public static void clearSongs() {
        SONGS.clear();
        SONG_WEIGHTED_LIST.clear();
    }

    public static void sendSongsToClient() {
        NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientBoundSyncSongsPacket(SongsManager.SONGS));
    }

    public static Song setCurrentlyPlaying(UUID id, ResourceLocation songKey) {
        Song song = SONGS.getOrDefault(songKey, Song.EMPTY);
        CURRENTLY_PAYING.put(id, song);

        return song;
    }

    public static void clearCurrentlyPlaying(UUID id) {
        CURRENTLY_PAYING.remove(id);
    }

    @Nonnull
    private static ResourceLocation selectRandomSong(Random random) {
        Optional<WeightedEntry.Wrapper<ResourceLocation>> song = WeightedRandom.getRandomItem(random, SONG_WEIGHTED_LIST);
        return song.map(WeightedEntry.Wrapper::getData).orElseGet(() -> new ResourceLocation(""));
    }


    public static boolean playRandomSong(ItemStack stack, InstrumentItem instrument, LivingEntity entity,
                                         long timeSinceStarted) {
        UUID id = entity.getUUID();
        Song song;
        if (!CURRENTLY_PAYING.containsKey(id)) {

            if (entity.level.isClientSide) return false;

            ResourceLocation res = null;
            if(stack.hasCustomHoverName()){
                String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT).replace(" ","_");
                for(var v : SONGS.keySet()){
                    if(v.getPath().equals(name)){
                        res = v;
                        break;
                    }
                }
            }
            if(res == null) res = selectRandomSong(entity.level.random);

            song = setCurrentlyPlaying(id, res);

            //if (entity instanceof ServerPlayer player) {
                //player.displayClientMessage(new TextComponent("Playing: "+song.getTranslationKey()), true);
            //}

            //tells the client which song it will play
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new ClientBoundSetSongPacket(id, res));

        } else {
            song = CURRENTLY_PAYING.get(id);
        }
        return playSong(instrument, entity, song, timeSinceStarted);
    }

    public static boolean playSong(InstrumentItem instrumentItem, LivingEntity entity, ResourceLocation sandstorm,
                                   long timeSinceStarted) {
        return playSong(instrumentItem, entity, SONGS.getOrDefault(sandstorm, Song.EMPTY), timeSinceStarted);
    }

    public static boolean playSong(InstrumentItem instrument, LivingEntity entity, Song song,
                                   long timeSinceStarted) {
        boolean played = false;
        if (timeSinceStarted % song.getTempo() == 0) {
            List<Integer> notes = song.getNoteToPlay(timeSinceStarted);
            //0 are blank

            for (int note : notes) {
                if (note > 0) {
                    instrument.playNoteAtEntity(entity, note);
                    played = true;
                }
            }
        }
        return played;
    }

    //util to make songs from a map
    private static final Map<Long, List<Integer>> RECORDING = new HashMap<>();
    private static boolean IS_RECORDING = false;
    private static final List<NoteBlockInstrument> WHITELIST = new ArrayList<>();


    public static void startRecording(NoteBlockInstrument[] whitelist) {
        RECORDING.clear();
        IS_RECORDING = true;
        WHITELIST.clear();
        WHITELIST.addAll(List.of(whitelist));
    }

    public static String stopRecording(Level level, String name, int speedup) {
        IS_RECORDING = false;

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
        for (int key : treeMap.keySet()) {
            int note = treeMap.get(key);
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
            if (i < 0) finalNotes.add(i / GCD);
            else finalNotes.add(i);
        }

        if (name.isEmpty()) name = "recorded-" + start;

        Song song = new Song(name, GCD, finalNotes.toArray(new Integer[0]), "recorded in-game");

        FluteSongsReloadListener.saveRecordedSong(song);

        //temporairly adds the song
        //TODO: remove
        SONGS.clear();
        song.processForPlaying();
        SONGS.put(Supplementaries.res(name), song);
        if (!level.isClientSide) {
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientBoundSyncSongsPacket(SongsManager.SONGS));
        }

        RECORDING.clear();

        return song.getTranslationKey();
    }

    public static void recordNote(LevelAccessor levelAccessor, BlockPos pos) {
        if (levelAccessor instanceof Level level &&  IS_RECORDING) {
            BlockState state = level.getBlockState(pos);
            recordNote(level, state.getValue(NoteBlock.NOTE) + 1, state.getValue(NoteBlock.INSTRUMENT));
        }
    }

    public static void recordNote(Level level, int note, NoteBlockInstrument instrument) {
        if (WHITELIST.size() == 0 || WHITELIST.contains(instrument)) {
            List<Integer> notes = RECORDING.computeIfAbsent(level.getGameTime(), t -> new ArrayList<>());
            notes.add(note);
        }
    }
}
