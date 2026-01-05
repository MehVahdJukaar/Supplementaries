package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.InventoryTooltip;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkClientCompat;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.integration.ShulkerBoxTooltipCompat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
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
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack incoming, Slot slot, ClickAction action, Player player, SlotAccess accessor) {
        if (!CompatHandler.QUARK || !QuarkCompat.isShulkerDropInOn()) return false;
        return ItemsUtil.tryInteractingWithContainerItem(stack, incoming, slot, action, player, true);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (!CompatHandler.QUARK || !QuarkCompat.isShulkerDropInOn()) return false;
        return ItemsUtil.tryInteractingWithContainerItem(stack, slot.getItem(), slot, action, player, false);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        boolean quarkTooltip = CompatHandler.QUARK && QuarkClientCompat.canRenderBlackboardTooltip();
        boolean sbtTooltip = CompatHandler.SHULKER_BOX_TOOLTIP && ShulkerBoxTooltipCompat.hasPreviewProvider(pStack);
        if (quarkTooltip && !sbtTooltip) {
            CompoundTag cmp = pStack.getTagElement("BlockEntityTag");
            if (cmp != null && !cmp.contains("LootTable")) {
                return Optional.of(new InventoryTooltip(cmp, this, 27));
            }
        }
        return Optional.empty();
    }
}
