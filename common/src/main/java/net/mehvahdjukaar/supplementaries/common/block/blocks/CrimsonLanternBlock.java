package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrimsonLanternBlock extends LanternBlock {

    private static final VoxelShape SHAPE = Shapes.or(Block.box(4.0D, 1.0D, 4.0D, 12.0D, 8.0D, 12.0D),
            Block.box(6.0D, 0.0D, 6.0D, 10.0D, 9.0D, 10.0D));
    public CrimsonLanternBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
