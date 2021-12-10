package net.mehvahdjukaar.supplementaries.items;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.block.Block;

public class SafeItem extends BlockItem {
    public SafeItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    //TODO: use new tooltip thingie

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public void onDestroyed(ItemEntity pItemEntity) {
        CompoundTag compoundtag = pItemEntity.getItem().getTag();
        if (compoundtag != null) {
            ListTag listtag = compoundtag.getCompound("BlockEntityTag").getList("Items", 10);
            ItemUtils.onContainerDestroyed(pItemEntity, listtag.stream().map(CompoundTag.class::cast).map(ItemStack::of));
        }
    }
}
