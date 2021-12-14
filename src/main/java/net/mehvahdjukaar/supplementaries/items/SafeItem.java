package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.quark.QuarkPlugin;
import net.mehvahdjukaar.supplementaries.compat.quark.QuarkTooltipPlugin;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class SafeItem extends BlockItem {
    public SafeItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

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

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack incoming, Slot slot, ClickAction action, Player player, SlotAccess accessor) {
        return ItemsUtil.tryAddingItemInContainerItem(stack, incoming, slot, action, player);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        return ItemsUtil.tryAddingItemInContainerItem(stack, slot.getItem(), slot, action, player);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        if (CompatHandler.quark && QuarkTooltipPlugin.canRenderTooltip()) {
            CompoundTag cmp = pStack.getTagElement("BlockEntityTag");
            if (cmp != null && !cmp.contains("LootTable")) {
                return Optional.of(new ItemsUtil.InventoryTooltip(cmp, this, 27));
            }
        }
        return Optional.empty();
    }
}
