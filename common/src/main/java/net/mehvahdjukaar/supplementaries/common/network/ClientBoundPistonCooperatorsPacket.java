package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PistonAttemptsTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashSet;
import java.util.Set;

public class ClientBoundPistonCooperatorsPacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundPistonCooperatorsPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_piston_cooperators"), ClientBoundPistonCooperatorsPacket::new);

    public final BlockPos pos;
    public final Set<BlockPos> cooperators;

    public ClientBoundPistonCooperatorsPacket(BlockPos pos, Set<BlockPos> cooperators) {
        this.pos = pos;
        this.cooperators = cooperators;
    }

    public ClientBoundPistonCooperatorsPacket(RegistryFriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.cooperators = buffer.readCollection(HashSet::new, BlockPos.STREAM_CODEC);
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeCollection(this.cooperators, BlockPos.STREAM_CODEC);
    }

    @Override
    public void handle(Context context) {
        PistonAttemptsTracker.onClientCooperatorsReceived(context.getPlayer().level(), this.pos, this.cooperators);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
