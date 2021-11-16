package net.mehvahdjukaar.supplementaries.compat.farmersdelight;

import net.mehvahdjukaar.supplementaries.block.blocks.PlanterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Lazy;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class PlanterRichBlock extends PlanterBlock {

    private final Lazy<BlockState> RICH_SOIL_DELEGATE;

    public PlanterRichBlock(Properties properties, Supplier<Block> mimic) {
        super(properties);
        RICH_SOIL_DELEGATE = Lazy.of(() -> mimic.get().defaultBlockState());

        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false)
                .setValue(EXTENDED, false));
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_220076_1_, LootContext.Builder p_220076_2_) {
        return Collections.singletonList(new ItemStack(this));
    }


    @Override
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        //hax
        RICH_SOIL_DELEGATE.get().randomTick(worldIn, pos, rand);
    }

}