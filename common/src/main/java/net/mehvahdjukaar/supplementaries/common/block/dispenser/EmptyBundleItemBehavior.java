package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

class EmptyBundleItemBehavior extends DispenserHelper.AdditionalDispenserBehavior {

    public EmptyBundleItemBehavior(Item item) {
        super(item);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
         var removed =  BundleItem.removeOne(stack);
         if(removed.isPresent()) {
             Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
             Position position = DispenserBlock.getDispensePosition(source);

             ItemStack extracted = removed.get();
             ItemStack toSpit = extracted.split(1);
             DefaultDispenseItemBehavior.spawnItem(source.getLevel(), toSpit, 6, direction, position);

             if(!extracted.isEmpty()){
                 BundleItem.add(stack, extracted);
             }
             return InteractionResultHolder.success(stack);
         }
        return InteractionResultHolder.pass(stack);
    }
}