package net.mehvahdjukaar.supplementaries.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.trades.AdventurerMapTrade;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class Credits implements Serializable {

    private static final Codec<Credits> CODEC = RecordCodecBuilder.create((i) -> i.group(
                    Codec.unboundedMap(Codec.STRING, Supporter.CODEC).fieldOf("supporters").forGetter(p->p.supporters),
                    Codec.STRING.listOf().fieldOf("additional_artists").forGetter(p->p.otherArtists),
                    Codec.STRING.listOf().fieldOf("translators").forGetter(p->p.translators),
                    Codec.STRING.listOf().fieldOf("mod_compatibility").forGetter(p->p.modCompatibility),
                    Codec.STRING.listOf().fieldOf("music_and_sounds").forGetter(p->p.soundArtists),
                    Codec.STRING.listOf().fieldOf("others").forGetter(p->p.others))
            .apply(i, Credits::new));

   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
            .registerTypeAdapter(Credits.class, (JsonDeserializer<Credits>)
                    (json, typeOfT, context) -> CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, e -> {})).create();

    //empty default one
    public static Credits INSTANCE = new Credits(Map.of(), List.of(), List.of(), List.of(), List.of(), List.of());

    private final transient List<UUID> devs = new ArrayList<>();
    private final transient Map<String, Pair<UUID, String>> statues = new HashMap<>();
    private final transient Map<String, ResourceLocation> globes = new HashMap<>();

    private final Map<String, Supporter> supporters;
    private final List<String> otherArtists;
    private final List<String> translators;
    private final List<String> modCompatibility;
    private final List<String> soundArtists;
    private final List<String> others;


    public Credits(Map<String, Supporter> supporters, List<String> artists, List<String> translators, List<String> mod_compat,
                   List<String> sounds, List<String> others) {
        this.supporters = supporters;
        this.otherArtists = artists;
        this.translators = translators;
        this.modCompatibility = mod_compat;
        this.soundArtists = sounds;
        this.others = others;

        addSpecialPlayer("Dev", true, false, false, "380df991-f603-344c-a090-369bad2a924a");
        addSpecialPlayer("Dev", true, false, true, "5084e6f3-8f54-43f1-8df5-1dca109e430f");
        addSpecialPlayer("MehVahdJukaar", true, false, true, "898b3a39-e486-405c-a873-d6b472dc3ba2", "TheEvilGolem");
        addSpecialPlayer("Capobianco", true, true, true, "90ceb598-9983-4da3-9cae-436d5afb9d81");
        addSpecialPlayer("Plantkillable", true, true, true, "720f165c-b066-4113-9622-63fc63c65696");
        addSpecialPlayer("Agrona", true, true, false, (UUID) null, "Pancake", "Pancakes");

        this.supporters.forEach((n, s) -> addSpecialPlayer(n, false, s.has_globe, s.has_statue, s.uuid));
    }

    private void addSpecialPlayer(String name, boolean isDev, boolean hasGlobe, boolean hasStatue, String id, String... alias) {
        UUID onlineId = id == null ? null : UUID.fromString(id);
        addSpecialPlayer(name, isDev, hasGlobe, hasStatue, onlineId);
    }

    private void addSpecialPlayer(String name, boolean isDev, boolean hasGlobe, boolean hasStatue, @Nullable UUID onlineId, String... alias) {
        name = name.toLowerCase(Locale.ROOT);

        if (isDev) {
            if (onlineId != null) devs.add(onlineId);
            devs.add(UUIDUtil.createOfflinePlayerUUID(name));

        }
        if (hasGlobe) {
            ResourceLocation texture = new ResourceLocation(Supplementaries.MOD_ID, "textures/entity/globes/globe_" + name + ".png");
            globes.put(name, texture);
            for (String n : alias) {
                globes.put(n.toLowerCase(Locale.ROOT), texture);
            }
        }
        Pair<UUID, String> p = Pair.of(onlineId, name);
        if (hasStatue) {
            statues.put(name, p);
            for (String n : alias) {
                statues.put(n.toLowerCase(Locale.ROOT), p);
            }
        }
    }

    public Map<String, ResourceLocation> globes() {
        return globes;
    }

    public Map<String, Pair<UUID, String>> statues() {
        return statues;
    }

    public List<UUID> getDevs() {
        return devs;
    }

    public Map<String, Supporter> supporters() {
        return supporters;
    }

    public String createCreditsText() {
        StringBuilder builder = new StringBuilder();
        builder.append("""
                \u00A76
                \u00A7lSupplementaries




                \u00A74Author:\u00A7r
                +
                \u00A70MehVahdJukaar

                \u00A74Artist:\u00A7r

                \u00A70Plantkillable
                                
                                
                """);

        builder.append("""
                \u00A74Supporters:\u00A7r
                \u00A70
                """);
        supporters.keySet().forEach(s -> builder.append(s).append("\n"));
        builder.append("\n\n");

        builder.append("""
                \u00A75Mod Compatibility:\u00A7r
                \u00A70
                """);
        modCompatibility.forEach(s -> builder.append(s).append("\n"));
        builder.append("\n\n");

        builder.append("""
                \u00A75Music and Sounds:\u00A7r
                \u00A70
                """);
        soundArtists.forEach(s -> builder.append(s).append("\n"));
        builder.append("\n\n");

        builder.append("""
                \u00A75Other Artists:\u00A7r
                \u00A70
                """);
        otherArtists.forEach(s -> builder.append(s).append("\n"));
        builder.append("\n\n");

        builder.append("""
                \u00A75Translators:\u00A7r
                \u00A70
                """);
        translators.forEach(s -> builder.append(s).append("\n"));
        builder.append("\n\n");

        builder.append("""
                \u00A75Others:\u00A7r
                \u00A70
                """);
        others.forEach(s -> builder.append(s).append("\n"));
        builder.append("\n\n\n\n\n");

        return builder.toString();
    }


    private static <T> T readFromURL(String link, Function<Reader, T> readerConsumer) throws IOException {
        URL url = new URL(link);

        URLConnection connection = url.openConnection();

        String encoding = connection.getContentEncoding();
        Charset charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
        try (Reader reader = new BufferedReader(new InputStreamReader(url.openStream(), charset))) {
            return readerConsumer.apply(reader);
        }
    }

    public static void fetchFromServer() {
        Thread creditsFetcher = new Thread(() -> {

            String link = "https://raw.githubusercontent.com/MehVahdJukaar/Supplementaries/master/credits.json";
            try {
                INSTANCE = readFromURL(link, r -> GSON.fromJson(r, Credits.class));
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to fetch contributors data from url {}, {}", link, e);
            }
        });
        creditsFetcher.start();
    }

    //don't convert to record, gson doesn't like them
    @SuppressWarnings("all")
    private static final class Supporter {

        private static final Codec<Supporter> CODEC = RecordCodecBuilder.create((i) -> i.group(
                        UUIDUtil.CODEC.optionalFieldOf("uuid").forGetter(p -> Optional.ofNullable(p.uuid)),
                        Codec.BOOL.optionalFieldOf("has_statue", false).forGetter(p -> p.has_statue),
                        Codec.BOOL.optionalFieldOf("has_globe", false).forGetter(p -> p.has_globe))
                .apply(i, Supporter::new));

        private final @Nullable UUID uuid;
        private final boolean has_statue;
        private final boolean has_globe;

        private Supporter(Optional<UUID> uuid, boolean has_statue, boolean has_globe) {
            this.uuid = uuid.orElse(null);
            this.has_statue = has_statue;
            this.has_globe = has_globe;
        }
    }
}
