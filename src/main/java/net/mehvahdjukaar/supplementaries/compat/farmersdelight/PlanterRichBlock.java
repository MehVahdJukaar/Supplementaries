package net.mehvahdjukaar.supplementaries.compat.farmersdelight;

import net.mehvahdjukaar.supplementaries.block.blocks.PlanterBlock;
import net.mehvahdjukaar.supplementaries.compat.CompatObjects;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Lazy;

import java.util.Collections;
import java.util.List;
import java.util.Random;

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
    private static final Lazy<BlockState> RICH_SOIL_DELEGATE = Lazy.of(()->CompatObjects.RICH_SOIL.get().defaultBlockState());

    @Override
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        //hax
        RICH_SOIL_DELEGATE.get().randomTick(worldIn, pos, rand);
    }

}