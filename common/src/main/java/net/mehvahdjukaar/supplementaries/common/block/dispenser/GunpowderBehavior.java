package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper.AdditionalDispenserBehavior;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.DispenserBlock;

class GunpowderBehavior extends AdditionalDispenserBehavior {

    protected GunpowderBehavior(Item item) {
        super(item);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {

        Direction direction = source.state().getValue(DispenserBlock.FACING);
        BlockPos blockpos = source.pos().relative(direction);
        Direction direction1 = source.level().isEmptyBlock(blockpos.below()) ? direction : Direction.UP;
        InteractionResult result = ItemsUtil.place(new DirectionalPlaceContext(source.level(), blockpos, direction, stack, direction1),
                ModRegistry.GUNPOWDER_BLOCK.get());
        if (result.consumesAction()) return InteractionResultHolder.success(stack);

        return InteractionResultHolder.fail(stack);
    }
}
