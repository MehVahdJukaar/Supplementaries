package net.mehvahdjukaar.supplementaries.common.items;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.client.QuiverArrowSelectGui;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.QuiverTooltip;
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
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class QuiverItem extends Item implements DyeableLeatherItem {

    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public QuiverItem(Properties properties) {
        super(properties);
    }

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
                Data data = getQuiverData(quiver);
                if (data != null) {
                    data.removeOneStack().ifPresent((stack) -> {
                        this.playRemoveOneSound(pPlayer);
                        data.tryAdding(pSlot.safeInsert(stack));
                        didStuff.set(true);
                    });
                }
            }
            //add
            else if (itemstack.getItem().canFitInsideContainerItems()) {
                Data data = getQuiverData(quiver);
                if (data != null) {
                    var taken = pSlot.safeTake(itemstack.getCount(), itemstack.getMaxStackSize(), pPlayer);
                    ItemStack remaining = data.tryAdding(taken);
                    if (!remaining.equals(taken)) {
                        this.playInsertSound(pPlayer);
                        didStuff.set(true);
                    }
                    pSlot.set(remaining);
                }
            }
            return didStuff.get();
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack quiver, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        if (pAction == ClickAction.SECONDARY && pSlot.allowModification(pPlayer)) {
            Data data = getQuiverData(quiver);
            if (data != null) {
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
        }
        return false;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand pUsedHand) {
        ItemStack stack = player.getItemInHand(pUsedHand);
        if (player.isSecondaryUseActive()) {
            Data data = getQuiverData(stack);
            if (data != null) {
                if (data.cycle()) {
                    this.playInsertSound(player);
                }
            }
        } else {
            //same as startUsingItem but client only so it does not slow
            if (pLevel.isClientSide) {
                QuiverArrowSelectGui.setUsingItem(true);
            }
            this.playRemoveOneSound(player);
            player.startUsingItem(pUsedHand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        return super.finishUsingItem(stack, level, livingEntity);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (level.isClientSide) {
            QuiverArrowSelectGui.setUsingItem(false);
        }
        this.playInsertSound(livingEntity);
        livingEntity.swing(livingEntity.getUsedItemHand());
        super.releaseUsing(stack, level, livingEntity, timeCharged);
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        Data data = getQuiverData(pStack);
        if (data != null) {
            return data.getSelected().getCount() > 0;
        }
        return false;
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        Data data = getQuiverData(pStack);
        if (data != null) {
            return Math.min(1 + 12 * data.getSelectedArrowCount() /
                    (data.getSelected().getMaxStackSize() * data.getContentView().size()), 13);
        }
        return 0;
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return BAR_COLOR;
    }


    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        Data data = getQuiverData(pStack);
        if (data != null) {
            NonNullList<ItemStack> list = NonNullList.create();
            boolean isEmpty = true;
            for (var v : data.getContentView()) {
                if (!v.isEmpty()) isEmpty = false;
                list.add(v);
            }
            if (!isEmpty) {
                return Optional.of(new QuiverTooltip(new ArrayList<>(data.getContentView()), data.getSelectedSlot()));
            }
        }
        return Optional.empty();
    }


    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        Data data = getQuiverData(pStack);
        if (data != null) {
            int c = data.getSelectedArrowCount();
            if (c != 0) {
                pTooltipComponents.add(Component.translatable("message.supplementaries.quiver.tooltip",
                        data.getSelected(null).getItem().getDescription(), c).withStyle(ChatFormatting.GRAY));
            }
        }
    }


    @Override
    public void onDestroyed(ItemEntity pItemEntity) {
        Data data = getQuiverData(pItemEntity.getItem());
        if (data != null) {
            ItemUtils.onContainerDestroyed(pItemEntity, data.getContentView().stream());
        }
    }

    private void playRemoveOneSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + pEntity.getLevel().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + pEntity.getLevel().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + pEntity.getLevel().getRandom().nextFloat() * 0.4F);
    }

    @Nullable
    @ExpectPlatform
    public static QuiverItem.Data getQuiverData(ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ItemStack getQuiver(LivingEntity entity) {
        throw new AssertionError();
    }

    //used to reset the selected arrow. I wish I didn't have to do this but I dont have control over when the itemstack is decremented
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        Data data = getQuiverData(stack);
        if (data != null) data.updateSelectedIfNeeded();
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }


    //this is cap, cap provider
    public interface Data {

        int getSelectedSlot();

        void setSelectedSlot(int selectedSlot);

        /**
         * Do not modify this list directly
         */
        List<ItemStack> getContentView();

        default boolean canAcceptItem(ItemStack toInsert) {
            return toInsert.getItem() instanceof ArrowItem;
        }

        default ItemStack getSelected() {
            return getSelected(null);
        }

        default ItemStack getSelected(@Nullable Predicate<ItemStack> supporterArrows) {
            var content = this.getContentView();
            int selected = this.getSelectedSlot();
            if (supporterArrows == null) return content.get(selected);
            int size = content.size();
            for (int i = 0; i < size; i++) {
                ItemStack s = content.get((i + selected) % size);
                if (supporterArrows.test(s)) return s;
            }
            return ItemStack.EMPTY;
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

        default int getSelectedArrowCount() {
            ItemStack selected = this.getSelected(null);
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
        void consumeArrow();

    }


}

