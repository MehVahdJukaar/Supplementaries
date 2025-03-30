package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.DataDefinedCatchableMob;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ClientBoundSendCapturedMobsPacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSendCapturedMobsPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_sync_captured_mobs"), ClientBoundSendCapturedMobsPacket::new);

    protected final Set<DataDefinedCatchableMob> mobSet;
    @Nullable
    protected final DataDefinedCatchableMob fish;

    public ClientBoundSendCapturedMobsPacket(final Set<DataDefinedCatchableMob> mobMap, @Nullable DataDefinedCatchableMob fish) {
        this.mobSet = mobMap;
        this.fish = fish;
    }

    public ClientBoundSendCapturedMobsPacket(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        this.mobSet = new HashSet<>();
        for (int i = 0; i < size; i++) {
            var r = DataDefinedCatchableMob.STREAM_CODEC.decode(buf);
            mobSet.add(r);
        }
        if (buf.readBoolean()) {
            this.fish = DataDefinedCatchableMob.STREAM_CODEC.decode(buf);
        } else fish = null;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.mobSet.size());
        for (var entry : this.mobSet) {
            DataDefinedCatchableMob.STREAM_CODEC.encode(buf, entry);
        }
        //47121:water, server
        //47081:water, client
        if (fish != null) {
            buf.writeBoolean(true);
            DataDefinedCatchableMob.STREAM_CODEC.encode(buf, fish);
        } else buf.writeBoolean(false);
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleSyncCapturedMobs(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
