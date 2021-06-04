package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class FrameBraceBlock extends FrameBlock {
    public static final BooleanProperty FLIPPED = BlockProperties.FLIPPED;

    public FrameBraceBlock(Properties properties, Supplier<Block> daub) {
        super(properties,daub);
        this.registerDefaultState(this.stateDefinition.any().setValue(FLIPPED,false)
                .setValue(LIGHT_LEVEL, 0).setValue(HAS_BLOCK,false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FLIPPED);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos blockpos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        return this.defaultBlockState().setValue(FLIPPED, direction != Direction.DOWN && (direction == Direction.UP || !(context.getClickLocation().y - (double)blockpos.getY() > 0.5D)));
    }


}
