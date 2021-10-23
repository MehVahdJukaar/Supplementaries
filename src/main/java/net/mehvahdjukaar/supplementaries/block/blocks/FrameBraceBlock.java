package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import vazkii.quark.api.IRotationLockable;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class FrameBraceBlock extends FrameBlock implements IRotationLockable {
    public static final BooleanProperty FLIPPED = BlockProperties.FLIPPED;

    public FrameBraceBlock(Properties properties, Supplier<Block> daub) {
        super(properties, daub);
        this.registerDefaultState(this.stateDefinition.any().setValue(FLIPPED, false)
                .setValue(LIGHT_LEVEL, 0).setValue(HAS_BLOCK, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FLIPPED);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos blockpos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        return this.defaultBlockState().setValue(FLIPPED, direction != Direction.DOWN && (direction == Direction.UP || !(context.getClickLocation().y - (double) blockpos.getY() > 0.5D)));
    }

    //quark rot lock
    @Override
    public BlockState applyRotationLock(Level world, BlockPos blockPos, BlockState state, Direction direction, int half) {
        return state.setValue(FLIPPED, half == 1);
    }
}
