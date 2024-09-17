package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.MovingSlidyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


// why is this needed? no clue
public record ClientBoundSetSlidingBlockEntityPacket(BlockPos pos, BlockState state, BlockState movedState,
                                                     Direction direction) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSetSlidingBlockEntityPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_set_sliding_block_entity"), ClientBoundSetSlidingBlockEntityPacket::new);

    public ClientBoundSetSlidingBlockEntityPacket(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readById(Block.BLOCK_STATE_REGISTRY::byIdOrThrow),
                buffer.readById(Block.BLOCK_STATE_REGISTRY::byIdOrThrow), buffer.readEnum(Direction.class));
    }

    public ClientBoundSetSlidingBlockEntityPacket(MovingSlidyBlockEntity be) {
        this(be.getBlockPos(), be.getBlockState(), be.getMovedState(), be.getDirection());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeById(Block.BLOCK_STATE_REGISTRY::getId, this.state);
        buf.writeById(Block.BLOCK_STATE_REGISTRY::getId, this.movedState);
        buf.writeEnum(this.direction);
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleSetSlidingBlockEntityPacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}