package net.mehvahdjukaar.supplementaries.common.network;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.MapLightHandler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class ClientBoundSyncAmbientLightPacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSyncAmbientLightPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_sync_ambient_light"), ClientBoundSyncAmbientLightPacket::new);

    private final Object2IntMap<ResourceKey<Level>> ambientLight = new Object2IntArrayMap<>();

    public ClientBoundSyncAmbientLightPacket(RegistryAccess registryAccess) {
        for (var d : registryAccess.registryOrThrow(Registries.DIMENSION).entrySet()) {
            Object obj = d.getValue();
            //mojank shit code doesnt even respect its own contracts
            DimensionType type = null;
            if (obj instanceof LevelStem stem) {
                type = stem.type().value();

            } else if (obj instanceof Level l) {
                type = l.dimensionType();
            }
            if (type != null) {
                // for dimensions with no skylight like nether we render fullbright
                float light = type.hasSkyLight() ?  type.ambientLight() : 1;
                ambientLight.put(d.getKey(), Mth.ceil(light * 15));
            }
        }
    }

    public ClientBoundSyncAmbientLightPacket(FriendlyByteBuf buf) {
        ambientLight.putAll(buf.readMap(buf1 -> buf1.readResourceKey(Registries.DIMENSION), FriendlyByteBuf::readVarInt));
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeMap(ambientLight, FriendlyByteBuf::writeResourceKey, FriendlyByteBuf::writeVarInt);
    }

    @Override
    public void handle(Context context) {
        MapLightHandler.setAmbientLight(ambientLight);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
