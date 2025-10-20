package net.mehvahdjukaar.supplementaries.common.misc.globe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedData;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedDataType;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.nio.ByteBuffer;


public class GlobeData extends WorldSavedData {

    public static final Codec<GlobeData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BYTE_BUFFER.xmap(ByteBuffer::array, ByteBuffer::wrap).fieldOf("pixels").forGetter(g -> g.globePixels),
            Codec.LONG.fieldOf("seed").forGetter(g -> g.seed)
    ).apply(i, GlobeData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GlobeData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE_ARRAY, g -> g.globePixels,
            ByteBufCodecs.VAR_LONG, g -> g.seed,
            GlobeData::new
    );

    private static final int TEXTURE_H = 16;
    private static final int TEXTURE_W = 32;

    private final byte[] globePixels;
    private final long seed;

    private GlobeData(byte[] flattenedPixels, long seed) {
        this.globePixels = flattenedPixels;
        this.seed = seed;
    }
    public static GlobeData fromLevel(ServerLevel level) {
        return GlobeData.fromSeed(level.getSeed());
    }

    //generate new from seed
    public static GlobeData fromSeed(long seed) {
        byte[][] generate = GlobeTextureGenerator.generate(seed);
        byte[] flattened = new byte[TEXTURE_W * TEXTURE_H];
        for (int x = 0; x < TEXTURE_W; x++) {
            System.arraycopy(generate[x], 0, flattened, x * TEXTURE_H, TEXTURE_H);
        }
        return new GlobeData(flattened, seed);
    }

    public byte getPixel(int x, int y) {
        if (x < 0 || x >= TEXTURE_W || y < 0 || y >= TEXTURE_H) return 0;
        //get pixels into flattened array
        return this.globePixels[x * TEXTURE_H + y];
    }

    @Override
    public WorldSavedDataType<GlobeData> getType() {
        return ModRegistry.GLOBE_DATA;
    }

    public void onReassigned(Level level) {
        if (level.isClientSide) GlobeManager.refreshTextures();
    }


    public static void changeDataWithSeed(ServerLevel level, long seed) {
        GlobeData generate = GlobeData.fromSeed(seed);
        ModRegistry.GLOBE_DATA.setData(level, generate);
        generate.sync();
    }
}



