package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.BlockHitResult;

public class BucketBehavior extends DispenserHelper.AdditionalDispenserBehavior {

    protected<I extends Item & DispensibleContainerItem> BucketBehavior(I item) {
        super(item);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
        DispensibleContainerItem dispensibleContainerItem = (DispensibleContainerItem) stack.getItem();
        BlockPos blockPos = source.getPos().relative( source.getBlockState().getValue(DispenserBlock.FACING));
        Level level = source.getLevel();
        if (dispensibleContainerItem.emptyContents( null, level, blockPos,  null)) {
            dispensibleContainerItem.checkExtraContent( null, level, stack, blockPos);
            return InteractionResultHolder.success(new ItemStack(Items.BUCKET));
        } else {
            return InteractionResultHolder.pass(stack);
        }
    }
}
