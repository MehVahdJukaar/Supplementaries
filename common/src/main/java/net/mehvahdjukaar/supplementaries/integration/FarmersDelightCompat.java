package net.mehvahdjukaar.supplementaries.integration;

import com.google.common.base.Suppliers;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.RegUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Contract;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class FarmersDelightCompat {


    @ExpectPlatform
    public static void init() {
    }

    public static final String PLANTER_RICH_NAME = "planter_rich";
    public static final Supplier<Block> PLANTER_RICH = RegUtils.regWithItem(PLANTER_RICH_NAME, () ->
            new PlanterRichBlock(BlockBehaviour.Properties.copy(ModRegistry.PLANTER.get())
                    .randomTicks(), CompatObjects.RICH_SOIL), CreativeModeTab.TAB_DECORATIONS);

    public static final String PLANTER_RICH_SOUL_NAME = "planter_rich_soul";
    public static final Supplier<Block> PLANTER_RICH_SOUL = !CompatHandler.nethersdelight ? null :
            RegUtils.regWithItem(PLANTER_RICH_SOUL_NAME, () ->
                    new PlanterRichBlock(BlockBehaviour.Properties.copy(ModRegistry.PLANTER.get())
                            .randomTicks(), CompatObjects.RICH_SOUL_SOIL), CreativeModeTab.TAB_DECORATIONS);


    @ExpectPlatform
    public static InteractionResult onCakeInteract(BlockState state, BlockPos pos, Level level, ItemStack itemstack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void tryTomatoLogging(BlockState facingState, LevelAccessor worldIn, BlockPos facingPos, boolean isRope) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean canAddStickToTomato(BlockState blockstate, BooleanProperty axis) {
        throw new ArrayStoreException();
    }


    public static class PlanterRichBlock extends PlanterBlock {

        private final Supplier<BlockState> RICH_SOIL_DELEGATE;

        public PlanterRichBlock(Properties properties, Supplier<Block> mimic) {
            super(properties);
            RICH_SOIL_DELEGATE = Suppliers.memoize(() -> mimic.get().defaultBlockState());

            this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false)
                    .setValue(EXTENDED, false));
        }

        @Override
        public List<ItemStack> getDrops(BlockState p_220076_1_, LootContext.Builder p_220076_2_) {
            return Collections.singletonList(new ItemStack(this));
        }

        @Override
        public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
            //hax
            RICH_SOIL_DELEGATE.get().randomTick(worldIn, pos, rand);
        }
    }
}
