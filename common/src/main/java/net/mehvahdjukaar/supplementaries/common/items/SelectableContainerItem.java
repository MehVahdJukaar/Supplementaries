package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.client.hud.SelectableContainerItemHud;
import net.mehvahdjukaar.supplementaries.common.items.components.SelectableContainerContent;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.minecraft.core.component.DataComponentType;
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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class SelectableContainerItem<C extends SelectableContainerContent<M>,
        M extends SelectableContainerContent.Mut<C>> extends Item {

    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public SelectableContainerItem(Properties properties) {
        super(properties);
    }

    public abstract DataComponentType<C> getComponentType();

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack myStack, Slot pSlot, ClickAction pAction, Player pPlayer) {
        if (pAction != ClickAction.SECONDARY) {
            return false;
        } else {
            ItemStack itemstack = pSlot.getItem();
            //place into invSlot
            boolean didStuff = false;
            C c = myStack.get(this.getComponentType());
            if (c == null) return false;
            M mutable = c.toMutable();

            if (itemstack.isEmpty()) {
                ItemStack removed = mutable.tryRemovingOne();
                if (removed != null) {
                    this.playRemoveOneSound(pPlayer);
                    ItemStack remainder = pSlot.safeInsert(removed);
                    mutable.tryAdding(remainder);
                    didStuff = true;
                }
            }
            //add
            else if (itemstack.getItem().canFitInsideContainerItems()) {
                ItemStack taken = pSlot.safeTake(itemstack.getCount(), itemstack.getMaxStackSize(), pPlayer);
                ItemStack remaining = mutable.tryAdding(taken);
                if (!remaining.equals(taken)) {
                    this.playInsertSound(pPlayer);
                    didStuff = true;
                }
                pSlot.safeInsert(remaining);
            }
            if (didStuff) {
                myStack.set(this.getComponentType(), mutable.toImmutable());
            }

            return didStuff;
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack myStack, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        if (pAction == ClickAction.SECONDARY && pSlot.allowModification(pPlayer)) {
            var c = myStack.get(this.getComponentType());
            if (c == null) return false;
            var data = c.toMutable();
            boolean didStuff = false;
            if (pOther.isEmpty()) {
                ItemStack removed = data.tryRemovingOne();
                if (removed != null) {
                    this.playRemoveOneSound(pPlayer);
                    pAccess.set(removed);
                    didStuff = true;
                }
            } else {
                ItemStack i = data.tryAdding(pOther);
                if (!i.equals(pOther)) {
                    this.playInsertSound(pPlayer);
                    pAccess.set(i);
                    didStuff = true;
                }
            }
            if (didStuff) {
                myStack.set(this.getComponentType(), data.toImmutable());
            }
            return didStuff;
        }
        return false;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand hand) {
        ItemStack myStack = player.getItemInHand(hand);
        var data = myStack.get(this.getComponentType());
        if (data == null) return InteractionResultHolder.fail(myStack);
        var mutable = data.toMutable();

        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack possibleArrowStack = player.getItemInHand(otherHand);

        //try inserting offhand
        if (mutable.isItemValid(possibleArrowStack)) {
            ItemStack remaining = mutable.tryAdding(possibleArrowStack);
            if (!remaining.equals(possibleArrowStack)) {
                this.playInsertSound(player);
                player.setItemInHand(otherHand, remaining);

                myStack.set(this.getComponentType(), mutable.toImmutable());
                return InteractionResultHolder.sidedSuccess(myStack, pLevel.isClientSide);
            }
        }

        if (player.isSecondaryUseActive()) {
            if (mutable.cycle()) {
                this.playInsertSound(player);
            }
            myStack.set(this.getComponentType(), mutable.toImmutable());
            return InteractionResultHolder.sidedSuccess(myStack, pLevel.isClientSide);
        } else {
            //same as startUsingItem but client only so it does not slow
            if (pLevel.isClientSide) {
                SelectableContainerItemHud.getInstance().setUsingItem(SlotReference.hand(hand), player);
            }
            this.playRemoveOneSound(player);
            myStack.set(this.getComponentType(), mutable.toImmutable());

            player.startUsingItem(hand);
            return InteractionResultHolder.consume(myStack);
        }
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
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
        var data = pStack.get(this.getComponentType());
        if (data != null) {
            return data.getSelectedCount() > 0;
        }
        return false;
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        var data = pStack.get(this.getComponentType());
        if (data != null) {
            return data.getBarSize();
        }
        return 0;
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return BAR_COLOR;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        var data = pStack.get(this.getComponentType());
        if (data != null && !data.isEmpty()) {
            return Optional.of(data);
        }
        return Optional.empty();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        var data = stack.get(this.getComponentType());
        if (data != null) {
            data.addToTooltip(context, tooltipComponents::add, tooltipFlag);
        }
    }


    @Override
    public void onDestroyed(ItemEntity pItemEntity) {
        var data = pItemEntity.getItem().get(this.getComponentType());
        if (data != null) {
            ItemUtils.onContainerDestroyed(pItemEntity, data.getContentCopy());
        }
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

    public abstract int getMaxSlots();

    public boolean modify(ItemStack stack, Function<M, Boolean> consumer) {
        C data = stack.get(this.getComponentType());
        if (data != null) {
            M mutable = data.toMutable();
            if (consumer.apply(mutable)) {
                stack.set(this.getComponentType(), mutable.toImmutable());
                return true;
            }
        }
        return false;
    }
}
