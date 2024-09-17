package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;


public record ClientBoundSyncAntiqueInk(BlockPos pos, boolean ink) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSyncAntiqueInk> CODEC = Message.makeType(
            Supplementaries.res("s2c_sync_antique_ink"), ClientBoundSyncAntiqueInk::new);

    public ClientBoundSyncAntiqueInk(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readBoolean());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.ink);
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleSyncAntiqueInkPacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}