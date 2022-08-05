package net.mehvahdjukaar.supplementaries.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.supplementaries.common.utils.SpecialPlayers;
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
        Executors.newSingleThreadExecutor().submit(()-> {
            String link = "https://raw.githubusercontent.com/MehVahdJukaar/WIPsuppMULTI/master/credits.json";
            try {
                INSTANCE = readFromURL(link, r -> GSON.fromJson(r, Credits.class));
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to fetch contributors data from url {}, {}", link, e);
            }
            SpecialPlayers
            int a = 1;
        });
    }

    public static Credits INSTANCE = null;

    private Map<String, Supporter> supporters = Map.of();
    private List<String> artists = List.of();
    private List<String> translations = List.of();


    public Credits(Map<String, Supporter> supporters, List<String> artists, List<String> translations) {
        this.supporters = supporters;
        this.artists = artists;
        this.translations = translations;
    }

    private static class Supporter {

        private static Codec<Supporter> CODEC = RecordCodecBuilder.<Supporter>create((i) -> i.group(
                        UUIDUtil.CODEC.optionalFieldOf("uuid").forGetter(p -> Optional.of(p.uuid)),
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
    }
}
