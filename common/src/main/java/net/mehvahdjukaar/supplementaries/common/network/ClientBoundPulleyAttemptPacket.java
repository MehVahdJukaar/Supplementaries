package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PulleyCooperationData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// the client runs the pull move itself so it needs the same cooperation table the server had.
// turn tables tick clientside anyway, cranks and manual pulls don't, hence this
public class ClientBoundPulleyAttemptPacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundPulleyAttemptPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_pulley_attempt"), ClientBoundPulleyAttemptPacket::new);

    public final BlockPos pos;
    public final int period;
    public final Direction pushDir;
    public final long tick;

    public ClientBoundPulleyAttemptPacket(BlockPos pos, int period, Direction pushDir, long tick) {
        this.pos = pos;
        this.period = period;
        this.pushDir = pushDir;
        this.tick = tick;
    }

    public ClientBoundPulleyAttemptPacket(RegistryFriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.period = buffer.readVarInt();
        this.pushDir = buffer.readEnum(Direction.class);
        this.tick = buffer.readVarLong();
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeVarInt(this.period);
        buffer.writeEnum(this.pushDir);
        buffer.writeVarLong(this.tick);
    }

    @Override
    public void handle(Context context) {
        PulleyCooperationData.markAttemptingClient(this.pos, this.period, this.pushDir, this.tick);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
