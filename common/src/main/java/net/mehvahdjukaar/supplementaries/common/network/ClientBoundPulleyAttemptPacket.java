package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PulleyCooperationData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Mirrors a pulley's "attempting a cooperative pull step" onto the client side-channel
 * ({@link PulleyCooperationData#markAttemptingClient}).
 * <p>
 * The client re-runs the pull move locally in {@code PulleyBlock.triggerEvent} (the
 * {@code MOVING_PULLEY} blocks are placed with a non-syncing flag, vanilla-piston style), so it
 * must resolve the SAME cooperative group the server did. Analog driving (turn tables) already
 * runs on the client, so it populates the client side-channel locally, but crank / manual pulls
 * happen server-only, so the client would otherwise see an empty cooperation table and resolve
 * each pulley solo (which fails for structures bridged across two ropes). This packet, sent from
 * {@code PulleyBlockTile.fireContinuousStep} just before the pull block-event, fills that gap for
 * every driver uniformly. It arrives ahead of the block-event (queued earlier in the tick), so by
 * the time {@code triggerEvent} runs on the client the cooperator is already registered.
 */
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
        // Runs on the client. markAttemptingClient is common static touching the client-only
        // side-channel tables; safe to call directly without a client-only receiver class.
        PulleyCooperationData.markAttemptingClient(this.pos, this.period, this.pushDir, this.tick);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
