package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.block.DispenserBlock;

class EmptyBundleItemBehavior extends DispenserHelper.AdditionalDispenserBehavior {

    public EmptyBundleItemBehavior(Item item) {
        super(item);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
        BundleContents content  = stack.get(DataComponents.BUNDLE_CONTENTS);
        if(content != null) {
            var mutable = new BundleContents.Mutable(content);
            ItemStack extracted = mutable.removeOne();
            if (extracted != null) {
                Direction direction = source.state().getValue(DispenserBlock.FACING);
                Position position = DispenserBlock.getDispensePosition(source);

                ItemStack toSpit = extracted.split(1);
                DefaultDispenseItemBehavior.spawnItem(source.level(), toSpit, 6, direction, position);

                if (!extracted.isEmpty()) {
                    mutable.tryInsert(extracted);
                }
                stack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }
}