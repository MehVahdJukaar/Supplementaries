package net.mehvahdjukaar.supplementaries.integration;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.misc.ModSoundType;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.Configuration;

import java.util.List;
import java.util.function.Supplier;

public class FarmersDelightCompat {

    public static final ModSoundType STICK_TOMATO_SOUND = new ModSoundType(1.0F, 1.0F,
            () -> SoundEvents.CROP_BREAK,
            () -> SoundEvents.GRASS_STEP,
            () -> SoundEvents.WOOD_PLACE,
            () -> SoundEvents.GRASS_HIT,
            () -> SoundEvents.GRASS_FALL);

    public static final Supplier<Block> ROPE_TOMATO = RegHelper.registerBlock(Supplementaries.res("rope_tomatoes"),
            () -> new TomatoRopeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
                    .forceSolidOff()));

    public static final Supplier<Block> STICK_TOMATOES = RegHelper.registerBlock(Supplementaries.res("stick_tomatoes"),
            () -> new TomatoStickBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
                    .forceSolidOff()
                    .sound(STICK_TOMATO_SOUND)));

    public static void init() {
    }


    // the state to place when an FD tomato vine climbs into one of our blocks (rope or stick), or null.
    // connections resolve on their own via neighbour updates after the block is placed.
    @Nullable
    public static BlockState getClimbReplacement(BlockState stateAbove) {
        if (isTomatoVineClimbingConfigOn()) {
            if (stateAbove.is(ModRegistry.ROPE.get())) {
                return ROPE_TOMATO.get().defaultBlockState();
            } else if (stateAbove.is(ModRegistry.STICK_BLOCK.get())) {
                return STICK_TOMATOES.get().defaultBlockState();
            }
        }
        return null;
    }

    public static boolean canClimbOurBlock(BlockState stateAbove) {
        return isTomatoVineClimbingConfigOn()
                && (stateAbove.is(ModRegistry.ROPE.get()) || stateAbove.is(ModRegistry.STICK_BLOCK.get()));
    }

    public static boolean isTomatoVineClimbingConfigOn() {
        return Configuration.ENABLE_TOMATO_VINE_CLIMBING_TAGGED_ROPES.get();
    }

    public static Block getStickTomato() {
        return STICK_TOMATOES.get();
    }

    public static void setupClient() {
        ClientHelper.registerRenderType(ROPE_TOMATO.get(), RenderType.cutout());
        ClientHelper.registerRenderType(STICK_TOMATOES.get(), RenderType.cutout());
    }

    public static boolean canAddStickToTomato(BlockState blockstate, BooleanProperty axis) {
        if (blockstate.getBlock() == getStickTomato()) {
            return !blockstate.getValue(axis);
        }
        return false;
    }

    public static PlanterBlock makePlanterRich() {
        return new PlanterRichBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RED_TERRACOTTA)
                .strength(2f, 6f)
                .requiresCorrectToolForDrops()
                .randomTicks(), CompatObjects.RICH_SOIL);
    }

    public static class PlanterRichBlock extends PlanterBlock {

        private final Supplier<BlockState> richSoilDelegate;

        public PlanterRichBlock(Properties properties, Supplier<Block> mimic) {
            super(properties);
            richSoilDelegate = Suppliers.memoize(() -> mimic.get().defaultBlockState());

            this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false)
                    .setValue(EXTENDED, false));
        }

        @Override
        public List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
            return super.getDrops(blockState, builder);
        }

        @Override
        public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
            //hax
            if (CommonConfigs.Building.FD_PLANTER.get()) {
                richSoilDelegate.get().randomTick(worldIn, pos, rand);
            }
        }
    }

}
