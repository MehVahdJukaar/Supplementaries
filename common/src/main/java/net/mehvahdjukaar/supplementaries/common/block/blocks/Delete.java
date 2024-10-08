package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import static net.mehvahdjukaar.supplementaries.common.block.blocks.GunpowderBlock.TYPE;


public class Delete {


    public static void aaa(BlockState state, LevelAccessor world, BlockState downState, BlockPos mutable) {
        if (downState.is(ModRegistry.GUNPOWDER_BLOCK.get()) && state.getValue(TYPE) == GunpowderBlock.Type.TRIGGERED &&
                downState.getValue(TYPE) == GunpowderBlock.Type.HIDDEN) {
            world.setBlock(mutable, downState.setValue(TYPE, GunpowderBlock.Type.TRIGGERED), 3);
        }
       else if (downState.is(ModRegistry.GUNPOWDER_BLOCK.get()) && state.getValue(TYPE) == GunpowderBlock.Type.TRIGGERED &&
                downState.getValue(TYPE) != GunpowderBlock.Type.HIDDEN) {
            world.setBlock(mutable, downState.setValue(TYPE, GunpowderBlock.Type.DEFAULT), 3);
        }
    }

    public static void bbb(BlockState state, Level world, BlockPos pos) {
            BlockPos.withinManhattan(pos, 1, 1, 1).forEach((blockPos) -> {
                BlockState s = world.getBlockState(blockPos);
                if(s.is(state.getBlock()))return;
                if (s.getBlock() instanceof ILightable il) {
                    il.tryExtinguish(null, s, blockPos, world);
                }
                else if(s.hasProperty(BlockStateProperties.LIT) && s.is(ModTags.LIGHTABLE_BY_GUNPOWDER)) {
                    world.setBlock(blockPos, s.setValue(BlockStateProperties.LIT, Boolean.FALSE), 3);
                }
            });

    }
}
