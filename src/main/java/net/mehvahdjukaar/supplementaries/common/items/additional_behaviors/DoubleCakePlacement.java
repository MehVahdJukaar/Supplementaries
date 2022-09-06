package net.mehvahdjukaar.supplementaries.common.items.additional_behaviors;

import net.mehvahdjukaar.supplementaries.api.IAdditionalPlacement;
import net.mehvahdjukaar.supplementaries.common.block.blocks.DoubleCakeBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DoubleCakePlacement implements IAdditionalPlacement {

    @Override
    public BlockPlaceContext overrideUpdatePlacementContext(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos().relative(pContext.getClickedFace());
        Level level = pContext.getLevel();
        BlockState state = level.getBlockState(pos);
        if (isValidCake(state)) {
            return BlockPlaceContext.at(pContext, pos, pContext.getClickedFace());
        }
        return null;
    }

    private boolean isValidCake(BlockState state) {
        if (!ServerConfigs.cached.DOUBLE_CAKE_PLACEMENT) return false;
        Block block = state.getBlock();
        return (block == Blocks.CAKE || block == ModRegistry.DIRECTIONAL_CAKE.get()) && state.getValue(CakeBlock.BITES) == 0;
    }

    @Override
    public BlockState overrideGetPlacementState(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        BlockState state = level.getBlockState(pos);
        if (isValidCake(state)) {
            return ModRegistry.DOUBLE_CAKE.get().withPropertiesOf(state).setValue(DoubleCakeBlock.FACING,pContext.getHorizontalDirection());
        }
        return null;
    }

}
