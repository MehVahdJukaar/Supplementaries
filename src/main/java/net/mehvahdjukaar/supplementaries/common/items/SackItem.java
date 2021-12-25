package net.mehvahdjukaar.supplementaries.common.items;


import net.mehvahdjukaar.supplementaries.common.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.quark.QuarkPlugin;
import net.mehvahdjukaar.supplementaries.integration.quark.QuarkTooltipPlugin;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class SackItem extends BlockItem {
    public SackItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        if (!ServerConfigs.cached.SACK_PENALTY) return;
        if (worldIn.getGameTime() % 20L == 0L && entityIn instanceof ServerPlayer player && !player.isCreative() && !entityIn.isSpectator()) {
            Collection<MobEffectInstance> effects = player.getActiveEffects();
            for (MobEffectInstance effect : effects) {
                if (effect.getEffect() == MobEffects.MOVEMENT_SLOWDOWN)
                    return;
            }

            int i = 0;
            AtomicReference<IItemHandler> reference = new AtomicReference<>();
            entityIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(reference::set);
            boolean firstSack = false;
            if (reference.get() != null) {
                for (int _idx = 0; _idx < reference.get().getSlots(); _idx++) {
                    ItemStack slotItem = reference.get().getStackInSlot(_idx);
                    if (slotItem.getItem() instanceof SackItem) {
                        i++;
                    }
                }

                if (CompatHandler.quark) {
                    ItemStack backpack = player.getItemBySlot(EquipmentSlot.CHEST);
                    i += QuarkPlugin.getSacksInBackpack(backpack);
                }
            }
            int inc = ServerConfigs.cached.SACK_INCREMENT;
            if (i > inc) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, i / (inc + 1)));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (!CompatHandler.quark || !QuarkTooltipPlugin.canRenderTooltip()) {
            CompoundTag tag = stack.getTagElement("BlockEntityTag");
            if (tag != null) {
                if (tag.contains("LootTable", 8)) {
                    tooltip.add(new TextComponent("???????").withStyle(ChatFormatting.GRAY));
                }

                if (tag.contains("Items", 9)) {
                    NonNullList<ItemStack> nonnulllist = NonNullList.withSize(9, ItemStack.EMPTY);
                    ContainerHelper.loadAllItems(tag, nonnulllist);
                    int i = 0;
                    int j = 0;

                    for (ItemStack itemstack : nonnulllist) {
                        if (!itemstack.isEmpty()) {
                            ++j;
                            if (i <= 4) {
                                ++i;
                                MutableComponent component = itemstack.getHoverName().copy();
                                component.append(" x").append(String.valueOf(itemstack.getCount()));
                                tooltip.add(component.withStyle(ChatFormatting.GRAY));
                            }
                        }
                    }
                    if (j - i > 0) {
                        tooltip.add((new TranslatableComponent("container.shulkerBox.more", j - i))
                                .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
                    }
                }
            }
        }

        if (!ClientConfigs.cached.TOOLTIP_HINTS || !flagIn.isAdvanced()) return;
        tooltip.add(new TranslatableComponent("message.supplementaries.sack").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
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
        return ItemsUtil.tryAddingItemInContainerItem(stack, incoming, slot, action, player, true);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        return ItemsUtil.tryAddingItemInContainerItem(stack, slot.getItem(), slot, action, player, false);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        if (CompatHandler.quark && QuarkTooltipPlugin.canRenderTooltip()) {
            CompoundTag cmp = pStack.getTagElement("BlockEntityTag");
            if (cmp != null && !cmp.contains("LootTable")) {
                return Optional.of(new ItemsUtil.InventoryTooltip(cmp, this, ServerConfigs.cached.SACK_SLOTS));
            }
        }
        return Optional.empty();
    }


}
