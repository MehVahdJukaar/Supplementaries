package net.mehvahdjukaar.supplementaries.common.items;


import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.InventoryViewTooltip;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkClientCompat;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.integration.ShulkerBoxTooltipCompat;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;

public class SackItem extends BlockItem {

    public SackItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        if (!CommonConfigs.Functional.SACK_PENALTY.get()) return;
        if (worldIn.getGameTime() % 27L == 0L && entityIn instanceof ServerPlayer player &&
                !player.isCreative() && !entityIn.isSpectator()) {
            //var currentEffect = player.getEffect(ModRegistry.OVERENCUMBERED.get());
            //keep refreshing for better accuracy
            float amount = ItemsUtil.getEncumbermentFromInventory(stack, player);
            int inc = CommonConfigs.Functional.SACK_INCREMENT.get();
            if (amount > inc) {
                player.addEffect(new MobEffectInstance(ModRegistry.OVERENCUMBERED.getHolder(),
                        20 * 10, (((((int) amount) - 1) / inc) - 1), false, false, true));
            }
        }
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        boolean quarkTooltips = CompatHandler.QUARK && QuarkClientCompat.canRenderQuarkTooltip();
        boolean sbtTooltips = CompatHandler.SHULKER_BOX_TOOLTIP && ShulkerBoxTooltipCompat.hasPreviewProvider(stack);
        if (!quarkTooltips && !sbtTooltips) {
            ItemsUtil.addShulkerLikeTooltips(stack, tooltipComponents);
        }
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @ForgeOverride
    public boolean canFitInsideContainerItems(ItemStack stack) {
        return !stack.has(DataComponents.CONTAINER);
    }

    @Override
    public void onDestroyed(ItemEntity pItemEntity) {
        ItemStack stack = pItemEntity.getItem();
        var contents = stack.get(DataComponents.CONTAINER);
        if (contents != null) {
            stack.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            ItemUtils.onContainerDestroyed(pItemEntity, contents.nonEmptyItemsCopy());
        }
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
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        boolean quarkTooltips = CompatHandler.QUARK && QuarkClientCompat.canRenderQuarkTooltip();
        boolean sbtTooltips = CompatHandler.SHULKER_BOX_TOOLTIP && ShulkerBoxTooltipCompat.hasPreviewProvider(pStack);
        if (quarkTooltips && !sbtTooltips) {
            if (!pStack.has(DataComponents.CONTAINER_LOOT)) {
                var container = pStack.get(DataComponents.CONTAINER);
                return Optional.of(new InventoryViewTooltip(container, CommonConfigs.Functional.SACK_SLOTS.get()));
            }
        }
        return Optional.empty();
    }

    //TODO: add these
    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    //0 nothing, 1 non empty sack. In between for custom non full stacks
    public static float getEncumber(ItemStack slotItem) {
        if (slotItem.is(ModTags.OVERENCUMBERING)) {
            ItemContainerContents contents = slotItem.get(DataComponents.CONTAINER);
            if (contents != null) {
                return contents != ItemContainerContents.EMPTY ? 1 : 0;
            } else {
                return slotItem.getCount() / (float) slotItem.getMaxStackSize();
            }
        }
        return 0;
    }

}
