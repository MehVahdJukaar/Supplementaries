package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PancakeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

class PancakeBehavior extends DispenserHelper.AdditionalDispenserBehavior {

    protected PancakeBehavior(Item item) {
        super(item);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
        //this.setSuccessful(false);
        ServerLevel level = source.level();
        BlockPos blockpos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
        BlockState state = level.getBlockState(blockpos);
        if (state.getBlock() instanceof PancakeBlock) {
            var t = ModBlockProperties.Topping.fromItem(stack, level.registryAccess());
            ModBlockProperties.Topping topping = t.getFirst();
            if (topping != ModBlockProperties.Topping.NONE) {
                if (PancakeBlock.setTopping(state, level, blockpos, topping)) {
                    return InteractionResultHolder.consume(t.getSecond().getDefaultInstance());
                }
            }
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.pass(stack);
    }
}


