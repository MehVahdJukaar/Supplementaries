package net.mehvahdjukaar.supplementaries.common.items;


import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.InventoryTooltip;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkClientCompat;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

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
                !player.isCreative() && !entityIn.isSpectator() && stack.getTagElement("BlockEntityTag") != null) {
            //var currentEffect = player.getEffect(ModRegistry.OVERENCUMBERED.get());
            //keep refreshing for better accuracy
            float amount;
            amount = ItemsUtil.getEncumbermentFromInventory(stack, player);
            int inc = CommonConfigs.Functional.SACK_INCREMENT.get();
            if (amount > inc) {
                player.addEffect(new MobEffectInstance(ModRegistry.OVERENCUMBERED.get(),
                        20 * 10, (((((int) amount) - 1) / inc) - 1), false, false, true));
            }
        }
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!CompatHandler.QUARK || !QuarkClientCompat.canRenderQuarkTooltip()) {
            CompoundTag tag = stack.getTagElement("BlockEntityTag");
            if (tag != null) {
                ItemsUtil.addShulkerLikeTooltips(tag, tooltip);
            }
        }
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
        if (CompatHandler.QUARK && QuarkClientCompat.canRenderQuarkTooltip()) {
            CompoundTag cmp = pStack.getTagElement("BlockEntityTag");
            if (cmp != null && !cmp.contains("LootTable")) {
                return Optional.of(new InventoryTooltip(cmp, this, CommonConfigs.Functional.SACK_SLOTS.get()));
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
        if (slotItem.getItem() instanceof SackItem) {
            CompoundTag tag = slotItem.getTag();
            if (tag != null) {
                var bet = tag.getCompound("BlockEntityTag");
                if (!bet.isEmpty()) {
                    var l = bet.getList("Items", 10);
                    if (!l.isEmpty()) return 1;
                }
            }
            return 0;
        } else if (slotItem.is(ModTags.OVERENCUMBERING)) {
            if (slotItem.hasTag()) return 1;
            return slotItem.getCount() / (float) slotItem.getMaxStackSize();
        }
        return 0;
    }

}
