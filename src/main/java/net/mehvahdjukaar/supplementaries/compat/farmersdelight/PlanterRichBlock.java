package net.mehvahdjukaar.supplementaries.compat.farmersdelight;

import net.mehvahdjukaar.supplementaries.block.blocks.PlanterBlock;
import net.minecraft.block.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ForgeHooks;
import vectorwing.farmersdelight.blocks.MushroomColonyBlock;
import vectorwing.farmersdelight.registry.ModBlocks;
import vectorwing.farmersdelight.setup.Configuration;
import vectorwing.farmersdelight.utils.MathUtils;
import vectorwing.farmersdelight.utils.tags.ModTags;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

public class PlanterRichBlock extends PlanterBlock {

    public PlanterRichBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false)
                .setValue(EXTENDED, false));
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_220076_1_, LootContext.Builder p_220076_2_) {
        return Collections.singletonList(new ItemStack(this));
    }

    //fd stuff
    public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
        if (!worldIn.isClientSide) {
            BlockPos abovePos = pos.above();
            BlockState aboveState = worldIn.getBlockState(abovePos);
            Block aboveBlock = aboveState.getBlock();

            //manually grows mushroom colony
            if (aboveBlock instanceof MushroomColonyBlock) {
                int age = aboveState.getValue(MushroomColonyBlock.COLONY_AGE);
                if (age < ((MushroomColonyBlock) aboveBlock).getMaxAge() && worldIn.getRawBrightness(pos.above(), 0) <= 12 &&
                        ForgeHooks.onCropsGrowPre(worldIn, abovePos, aboveState, rand.nextInt(5) == 0)) {
                    worldIn.setBlock(abovePos, aboveState.setValue(MushroomColonyBlock.COLONY_AGE, age + 1), 2);
                    ForgeHooks.onCropsGrowPost(worldIn, abovePos, aboveState);
                }
                return;
            }

            if (ModTags.UNAFFECTED_BY_RICH_SOIL.contains(aboveBlock) || aboveBlock instanceof TallFlowerBlock) {
                return;
            }

            if (aboveBlock == Blocks.BROWN_MUSHROOM) {
                if (worldIn.getRawBrightness(pos.above(), 0) <= 12) {
                    worldIn.setBlockAndUpdate(pos.above(), ModBlocks.BROWN_MUSHROOM_COLONY.get().defaultBlockState());
                }
                return;
            }

            if (aboveBlock == Blocks.RED_MUSHROOM) {
                if (worldIn.getRawBrightness(pos.above(), 0) <= 12) {
                    worldIn.setBlockAndUpdate(pos.above(), ((Block)ModBlocks.RED_MUSHROOM_COLONY.get()).defaultBlockState());
                }
                return;
            }

            if (Configuration.RICH_SOIL_BOOST_CHANCE.get() == 0.0D) {
                return;
            }

            if (aboveBlock instanceof IGrowable && (double) MathUtils.RAND.nextFloat() <= Configuration.RICH_SOIL_BOOST_CHANCE.get()) {
                IGrowable growable = (IGrowable)aboveBlock;
                if (growable.isValidBonemealTarget(worldIn, pos.above(), aboveState, false) && ForgeHooks.onCropsGrowPre(worldIn, pos.above(), aboveState, true)) {
                    growable.performBonemeal(worldIn, worldIn.random, pos.above(), aboveState);
                    worldIn.levelEvent(2005, pos.above(), 0);
                    ForgeHooks.onCropsGrowPost(worldIn, pos.above(), aboveState);
                }
            }
        }

    }

}