package net.mehvahdjukaar.supplementaries.integration;

import com.google.common.base.Suppliers;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class FarmersDelightCompat {


    @ExpectPlatform
    public static void init() {
    }

    public static PlanterBlock makePlanterRich() {
        return new PlanterRichBlock(BlockBehaviour.Properties.copy(Blocks.RED_TERRACOTTA)
                .strength(2f, 6f)
                .requiresCorrectToolForDrops()
                .randomTicks(), CompatObjects.RICH_SOIL);
    }

    @Nullable
    @ExpectPlatform
    public static Block getStickTomato() {
        throw new AssertionError();
    }

    public static boolean canAddStickToTomato(BlockState blockstate, BooleanProperty axis) {
        if (blockstate.getBlock() == getStickTomato()) {
            return !blockstate.getValue(axis);
        }
        return false;
    }

    @Contract
    @ExpectPlatform
    public static boolean tryTomatoLogging(ServerLevel level, BlockPos pos) {
        throw new AssertionError();
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
            richSoilDelegate.get().randomTick(worldIn, pos, rand);
        }
    }

    @ExpectPlatform
    public static void setupClient() {
        throw new AssertionError();
    }
}
