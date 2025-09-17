package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.client.hud.SelectableContainerItemHud;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.SelectableContainerTooltip;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SelectableContainerItem<D extends SelectableContainerItem.AbstractData> extends Item {

    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public SelectableContainerItem(Properties properties) {
        super(properties);
    }

    @NotNull
    public abstract D getData(ItemStack stack);

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack quiver, Slot pSlot, ClickAction pAction, Player pPlayer) {
        if (pAction != ClickAction.SECONDARY) {
            return false;
        } else {
            ItemStack itemstack = pSlot.getItem();
            //place into slot
            AtomicBoolean didStuff = new AtomicBoolean(false);
            if (itemstack.isEmpty()) {
                D data = this.getData(quiver);
                data.removeOneStack().ifPresent((stack) -> {
                    this.playRemoveOneSound(pPlayer);
                    data.tryAdding(pSlot.safeInsert(stack));
                    didStuff.set(true);
                });
            }
            //add
            else if (itemstack.getItem().canFitInsideContainerItems()) {
                ItemStack taken = pSlot.safeTake(itemstack.getCount(), itemstack.getMaxStackSize(), pPlayer);
                ItemStack remaining = mutable.tryAdding(taken);
                //
                old
                D data = this.getData(quiver);
                var taken = pSlot.safeTake(itemstack.getCount(), itemstack.getMaxStackSize(), pPlayer);
                ItemStack remaining = data.tryAdding(taken);
                //end old
                if (!remaining.equals(taken)) {
                    this.playInsertSound(pPlayer);
                    didStuff.set(true);
                }
                pSlot.safeInsert(remaining);
            }
            return didStuff.get();
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack quiver, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        if (pAction == ClickAction.SECONDARY && pSlot.allowModification(pPlayer)) {
            AbstractData data = this.getData(quiver);
            AtomicBoolean didStuff = new AtomicBoolean(false);
            if (pOther.isEmpty()) {
                data.removeOneStack().ifPresent((removed) -> {
                    this.playRemoveOneSound(pPlayer);
                    pAccess.set(removed);
                    didStuff.set(true);
                });
            } else {
                ItemStack i = data.tryAdding(pOther);
                if (!i.equals(pOther)) {
                    this.playInsertSound(pPlayer);
                    pAccess.set(i);
                    didStuff.set(true);
                }
            }
            return didStuff.get();
        }
        return false;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(this)) return super.use(pLevel, player, hand);

        D data = this.getData(stack);

        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack possibleArrowStack = player.getItemInHand(otherHand);

        //try inserting offhand
        if (data.canAcceptItem(possibleArrowStack)) {
            ItemStack remaining = data.tryAdding(possibleArrowStack);
            if (!remaining.equals(possibleArrowStack)) {
                this.playInsertSound(player);
                player.setItemInHand(otherHand, remaining);
                return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide);
            }
        }

        if (player.isSecondaryUseActive()) {
            if (data.cycle()) {
                this.playInsertSound(player);
            }
            return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide);
        } else {
            //same as startUsingItem but client only so it does not slow
            if (pLevel.isClientSide) {
                SelectableContainerItemHud.getInstance().setUsingItem(SlotReference.hand(hand), player);
            }
            this.playRemoveOneSound(player);
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (level.isClientSide) {
            SelectableContainerItemHud.getInstance().setUsingItem(SlotReference.EMPTY, livingEntity);
        }
        this.playInsertSound(livingEntity);
        livingEntity.swing(livingEntity.getUsedItemHand());
        super.releaseUsing(stack, level, livingEntity, timeCharged);
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        D data = this.getData(pStack);
        return data.getSelected().getCount() > 0;
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        D data = this.getData(pStack);
        return Math.min(1 + 12 * data.getSelectedItemCount() /
                (data.getSelected().getMaxStackSize() * data.getContentView().size()), 13);
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return BAR_COLOR;
    }


    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        D data = this.getData(pStack);
        NonNullList<ItemStack> list = NonNullList.create();
        boolean isEmpty = true;
        for (var v : data.getContentView()) {
            if (!v.isEmpty()) isEmpty = false;
            list.add(v);
        }
        if (!isEmpty) {
            return Optional.of(new SelectableContainerTooltip(data.getContentView(), data.getSelectedSlot()));
        }
        return Optional.empty();
    }


    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        D data = this.getData(pStack);
        int c = data.getSelectedItemCount();
        if (c != 0) {
            pTooltipComponents.add(Component.translatable("message.supplementaries.quiver.tooltip",
                    data.getSelected().getItem().getDescription(), c).withStyle(ChatFormatting.GRAY));
        }
    }


    @Override
    public void onDestroyed(ItemEntity pItemEntity) {
        D data = this.getData(pItemEntity.getItem());
        ItemUtils.onContainerDestroyed(pItemEntity, data.getContentView().stream());
    }

    protected void playRemoveOneSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }

    protected void playInsertSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }

    protected void playDropContentsSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }

    //used to reset the selected arrow. I wish I didn't have to do this but I dont have control over when the itemstack is decremented
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        D data = this.getData(stack);
        data.updateSelectedIfNeeded();
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    // BS instance fields

    public abstract int getMaxSlots();

    public interface AbstractData {

        int getSelectedSlot();

        void setSelectedSlot(int selectedSlot);

        /**
         * Do not modify this list directly if you are on fabric. On forge it can be modified
         */
        List<ItemStack> getContentView();

        boolean canAcceptItem(ItemStack toInsert);

        default ItemStack getSelected() {
            var content = this.getContentView();
            int selected = this.getSelectedSlot();
            return content.get(selected);
        }

        default boolean cycle() {
            return cycle(1);
        }

        default boolean cycle(boolean clockWise) {
            return cycle(clockWise ? 1 : -1);
        }

        default boolean cycle(int slotsMoved) {
            int originalSlot = this.getSelectedSlot();
            var content = this.getContentView();
            ItemStack selected;
            if (slotsMoved == 0) {
                //returns if it doesn't have to move
                selected = content.get(this.getSelectedSlot());
                if (!selected.isEmpty()) return false;
            }
            int maxSlots = content.size();
            slotsMoved = slotsMoved % maxSlots;
            this.setSelectedSlot((maxSlots + (this.getSelectedSlot() + slotsMoved)) % maxSlots);
            for (int i = 0; i < maxSlots; i++) {
                selected = content.get(this.getSelectedSlot());
                if (!selected.isEmpty()) break;
                this.setSelectedSlot((maxSlots + (this.getSelectedSlot() + (slotsMoved >= 0 ? 1 : -1))) % maxSlots);
            }
            return originalSlot != getSelectedSlot();
        }

        /**
         * Adds one item. returns the item that is remaining and has not been added. Same item if no change was made
         */
        ItemStack tryAdding(ItemStack pInsertedStack, boolean onlyOnExisting);

        default ItemStack tryAdding(ItemStack pInsertedStack) {
            return tryAdding(pInsertedStack, false);
        }


        Optional<ItemStack> removeOneStack();

        default int getSelectedItemCount() {
            ItemStack selected = this.getSelected();
            int amount = 0;
            for (var item : this.getContentView()) {

                if (ForgeHelper.canItemStack(selected, item)) {
                    amount += item.getCount();
                }
            }
            return amount;
        }

        default void updateSelectedIfNeeded() {
            this.cycle(0); //this works
        }

        //fabric and skeleton shoot goal. forge for player doesn't need this as stack decrement already affects the one in quiver
        void consumeSelected();

    }

}
