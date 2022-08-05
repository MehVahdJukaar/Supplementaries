package net.mehvahdjukaar.supplementaries.common;

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
import java.util.Optional;
import java.util.UUID;
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
        String link = "https://raw.githubusercontent.com/MehVahdJukaar/supplementaries/main/credits.json";;
        try {
            INSTANCE = readFromURL(link, r -> GSON.fromJson(r, Credits.class));
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to fetch contributors data from url {}, {}", link, e);
        }
    }

    public static Credits INSTANCE = null;

    private final List<Supporter> supporters;
    private final List<String> artists;
    private final List<String> translations;


    public Credits(List<Supporter> supporters, List<String> artists, List<String> translations) {
        this.supporters = supporters;
        this.artists = artists;
        this.translations = translations;
    }


    private static class Supporter {

        public Codec<Supporter> CODEC2 = Codec.STRING.listOf().fieldOf("aa").forGetter(s -> s.aa);

        private static Codec<Supporter> CODEC = RecordCodecBuilder.<Supporter>create((i) -> i.group(
                        UUIDUtil.CODEC.optionalFieldOf("uuid").forGetter(p -> Optional.of(p.id)),
                        Codec.BOOL.optionalFieldOf("has_statue", false).forGetter(p -> p.hasStatue),
                        Codec.BOOL.optionalFieldOf("has_globe", false).forGetter(p -> p.hasGlobe))
                .apply(i, Supporter::new));

        @Nullable
        private final UUID id;
        private final boolean hasStatue;
        private final boolean hasGlobe;

        private Supporter(Optional<UUID> id, boolean hasStatue, boolean hasGlobe) {
            this.id = id.orElse(null);
            this.hasStatue = hasStatue;
            this.hasGlobe = hasGlobe;
        }
    }
}
