package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PancakeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

class PancakeBehavior extends DispenserHelper.AdditionalDispenserBehavior {

    protected PancakeBehavior(Item item) {
        super(item);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
        //this.setSuccessful(false);
        ServerLevel world = source.getLevel();
        BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
        BlockState state = world.getBlockState(blockpos);
        if (state.getBlock() instanceof PancakeBlock block) {
            if (block.tryAcceptingFluid(world, state, blockpos, new SoftFluidStack(BuiltInSoftFluids.HONEY.getHolder(), 1))) {
                return InteractionResultHolder.consume(new ItemStack(Items.GLASS_BOTTLE));
            }
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.pass(stack);
    }
}


