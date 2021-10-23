package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.StructureTempBlockTile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class StructureTempBlock extends Block {

    public StructureTempBlock(Properties properties) {
        super(properties);

    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new StructureTempBlockTile();
    }

    @Override
    public boolean canBeReplacedByLeaves(BlockState state, LevelReader world, BlockPos pos) {
        return false;
    }

    //Todo: make so grass and flowers can replace
    @Override
    public boolean canBeReplaced(BlockState p_196253_1_, BlockPlaceContext p_196253_2_) {
        return super.canBeReplaced(p_196253_1_, p_196253_2_);
    }
}
