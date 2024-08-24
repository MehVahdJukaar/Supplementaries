package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.blocks.MovingSlidyBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.MovingSlidyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;


// why is this needed? no clue
public record ClientBoundSetSlidingBlockEntityPacket(BlockPos pos, BlockState state, BlockState movedState,
                                                     Direction direction) implements Message {

    public ClientBoundSetSlidingBlockEntityPacket(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readById(Block.BLOCK_STATE_REGISTRY),
                buffer.readById(Block.BLOCK_STATE_REGISTRY), buffer.readEnum(Direction.class));
    }

    public ClientBoundSetSlidingBlockEntityPacket(MovingSlidyBlockEntity be) {
        this(be.getBlockPos(), be.getBlockState(), be.getMovedState(), be.getDirection());
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeId(Block.BLOCK_STATE_REGISTRY, this.state);
        buf.writeId(Block.BLOCK_STATE_REGISTRY, this.movedState);
        buf.writeEnum(this.direction);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        ClientReceivers.handleSetSlidingBlockEntityPacket(this);
    }
}