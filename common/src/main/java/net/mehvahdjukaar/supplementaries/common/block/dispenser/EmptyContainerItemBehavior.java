package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

class EmptyContainerItemBehavior extends DispenserHelper.AdditionalDispenserBehavior {

    public EmptyContainerItemBehavior(SelectableContainerItem item) {
        super(item);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
        if (stack.getItem() instanceof SelectableContainerItem<?, ?> bi) {
            boolean success = bi.modify(stack, data -> {
                var extracted = data.tryRemovingOne();
                if (extracted != null) {
                    Direction direction = source.state().getValue(DispenserBlock.FACING);
                    Position position = DispenserBlock.getDispensePosition(source);

                    ItemStack toSpit = extracted.split(1);
                    DefaultDispenseItemBehavior.spawnItem(source.level(), toSpit, 6, direction, position);

                    if (!extracted.isEmpty()) {
                        data.tryAdding(extracted);
                    }
                    return true;
                }
                return false;
            });
            if (success) return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }


}