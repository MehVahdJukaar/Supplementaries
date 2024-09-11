package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

class EmptyContainerItemBehavior extends DispenserHelper.AdditionalDispenserBehavior {

    public EmptyContainerItemBehavior(Item item) {
        super(item);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
        Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);

        if (stack.getItem() instanceof BundleItem bi) {
            Position position = DispenserBlock.getDispensePosition(source);
            ItemStack itemStack = stack.split(1);
            DefaultDispenseItemBehavior.spawnItem(source.getLevel(), itemStack, 6, direction, position);
            return InteractionResultHolder.pass(stack);
        }

        return InteractionResultHolder.pass(stack);
    }
}