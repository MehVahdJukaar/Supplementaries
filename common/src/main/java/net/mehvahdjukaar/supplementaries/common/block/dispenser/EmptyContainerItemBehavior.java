package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.supplementaries.common.components.SelectableContainerContent;
import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponentType;
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
            return typed(source, stack, bi.getComponentType());
        }
        return InteractionResultHolder.pass(stack);
    }

    private static <T extends SelectableContainerContent<M>, M extends SelectableContainerContent.Mut<T>> InteractionResultHolder<ItemStack> typed(
            BlockSource source, ItemStack stack, DataComponentType<T> componentType) {
        T data = stack.get(componentType);
        if (data != null) {
            var mutable = data.toMutable();
            var extracted = mutable.tryRemovingOne();
            if (extracted != null) {
                Direction direction = source.state().getValue(DispenserBlock.FACING);
                Position position = DispenserBlock.getDispensePosition(source);

                ItemStack toSpit = extracted.split(1);
                DefaultDispenseItemBehavior.spawnItem(source.level(), toSpit, 6, direction, position);

                if (!extracted.isEmpty()) {
                    mutable.tryAdding(extracted);
                }
                stack.set(componentType, mutable.toImmutable());
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }
}