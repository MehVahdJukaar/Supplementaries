package net.mehvahdjukaar.supplementaries.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class Credits {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static <T> T readFromURL(String link, Function<Reader, T> readerConsumer) throws IOException {
        URL url = new URL(link);
        URLConnection connection;
        connection = url.openConnection();

        String encoding = connection.getContentEncoding();
        Charset charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
        try (Reader reader = new BufferedReader(new InputStreamReader(url.openStream(), charset))) {
            return readerConsumer.apply(reader);
        }
    }

    public static void fetchFromServer() {
        Executors.newSingleThreadExecutor().submit(() -> {
            String link = "https://raw.githubusercontent.com/MehVahdJukaar/WIPsuppMULTI/master/credits.json";
            try {
                INSTANCE = readFromURL(link, r -> GSON.fromJson(r, Credits.class));
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to fetch contributors data from url {}, {}", link, e);
            }
            int a = 1;
        });
    }

    public static Credits INSTANCE = null;

    private final Map<String, Supporter> supporters;
    private final List<String> additional_artists;
    private final List<String> translators;
    private final List<String> mod_compatibility;
    private final List<String> music_and_sounds;
    private final List<String> others;


    public Credits(Map<String, Supporter> supporters, List<String> artists, List<String> translators, List<String> mod_compat,
                   List<String> sounds, List<String> others) {
        this.supporters = supporters;
        this.additional_artists = artists;
        this.translators = translators;
        this.mod_compatibility = mod_compat;
        this.music_and_sounds = sounds;
        this.others = others;
    }

    public Map<String, Supporter> getSupporters() {
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
        supporters.keySet().forEach(s-> builder.append(s).append("\n"));
        builder.append("\n\n");

        builder.append("""
                \u00A75Music and Sounds:\u00A7r
                \u00A70
                """);
        music_and_sounds.forEach(s-> builder.append(s).append("\n"));
        builder.append("\n\n");

        builder.append("""
                \u00A75Mod Compatibility:\u00A7r
                \u00A70
                """);
        mod_compatibility.forEach(s-> builder.append(s).append("\n"));
        builder.append("\n\n");

        builder.append("""
                \u00A75Other Artists:\u00A7r
                \u00A70
                """);
        additional_artists.forEach(s-> builder.append(s).append("\n"));
        builder.append("\n\n");

        builder.append("""
                \u00A75Translators:\u00A7r
                \u00A70
                """);
        translators.forEach(s-> builder.append(s).append("\n"));
        builder.append("\n\n");

        builder.append("""
                \u00A75Others:\u00A7r
                \u00A70
                """);
        others.forEach(s-> builder.append(s).append("\n"));
        builder.append("\n\n\n\n\n");

        return builder.toString();
    }

    public static class Supporter {

        private static final Codec<Supporter> CODEC = RecordCodecBuilder.create((i) -> i.group(
                        UUIDUtil.CODEC.optionalFieldOf("uuid").forGetter(p -> Optional.ofNullable(p.uuid)),
                        Codec.BOOL.optionalFieldOf("has_statue", false).forGetter(p -> p.has_statue),
                        Codec.BOOL.optionalFieldOf("has_globe", false).forGetter(p -> p.has_globe))
                .apply(i, Supporter::new));

        @Nullable
        private final UUID uuid;
        private final boolean has_statue;
        private final boolean has_globe;

        private Supporter(Optional<UUID> uuid, boolean hasStatue, boolean hasGlobe) {
            this.uuid = uuid.orElse(null);
            this.has_statue = hasStatue;
            this.has_globe = hasGlobe;
        }

        public boolean hasStatue() {
            return has_statue;
        }
        public boolean hasGlobe() {
            return has_globe;
        }
        public @Nullable UUID getUuid() {
            return uuid;
        }
    }
}
