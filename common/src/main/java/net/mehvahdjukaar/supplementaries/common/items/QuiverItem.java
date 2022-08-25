package net.mehvahdjukaar.supplementaries.common.items;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class QuiverItem extends Item implements DyeableLeatherItem {

    public static final int SLOTS = 7;

    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public QuiverItem(Properties properties) {
        super(properties);
    }
    //TODO: quark arrow preview

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
            if (itemstack.isEmpty()) {
                this.playRemoveOneSound(pPlayer);
                removeOne(quiver).ifPresent((p_150740_) -> {
                    add(quiver, pSlot.safeInsert(p_150740_));
                });
            }
            //add
            else if (itemstack.getItem().canFitInsideContainerItems()) {
                ItemStack i = add(quiver, pSlot.safeTake(itemstack.getCount(), 64, pPlayer));
                if (!i.equals(itemstack)) {
                    this.playInsertSound(pPlayer);
                    pSlot.set(i);
                    return true;
                }
            }
            return true;
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack quiver, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        if (pAction == ClickAction.SECONDARY && pSlot.allowModification(pPlayer)) {
            if (pOther.isEmpty()) {
                removeOne(quiver).ifPresent((p_186347_) -> {
                    this.playRemoveOneSound(pPlayer);
                    pAccess.set(p_186347_);
                });
                return true;
            } else {
                ItemStack i = add(quiver, pOther);
                if (!i.equals(pOther)) {
                    this.playInsertSound(pPlayer);
                    pAccess.set(i);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if(pPlayer.isSecondaryUseActive()){
            ItemStack stack = pPlayer.getItemInHand(pUsedHand);
            QuiverItem.cycleArrow(stack);
            return InteractionResultHolder.sidedSuccess(stack,pLevel.isClientSide);
        }
        return InteractionResultHolder.pass(pPlayer.getItemInHand(pUsedHand));
    }


    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return getSelectedArrowCount(pStack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        return Math.min(1 + 12 * getSelectedArrowCount(pStack) / (64 * SLOTS), 13);
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return BAR_COLOR;
    }


    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        return Optional.ofNullable(QuiverItem.getQuiverTooltip(pStack));
    }


    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        var c = getSelectedArrowCount(pStack);
        if(c != 0) {
            pTooltipComponents.add(Component.translatable("message.supplementaries.quiver.tooltip",
                    getSelectedArrow(pStack, null).getItem().getDescription(), c).withStyle(ChatFormatting.GRAY));
        }
    }


    @Override
    public void onDestroyed(ItemEntity pItemEntity) {
        ItemUtils.onContainerDestroyed(pItemEntity, getContents(pItemEntity.getItem()));
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
    private static QuiverTooltip getQuiverTooltip(ItemStack pStack) {
        throw  new AssertionError();
    }

    @ExpectPlatform
    public static int getSelectedArrowCount(ItemStack pStack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ItemStack getSelectedArrow(ItemStack itemStack, @Nullable Predicate<ItemStack> supporterArrows) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ItemStack getQuiver(LivingEntity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Stream<ItemStack> getContents(ItemStack pStack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    private static void cycleArrow(ItemStack stack) {
        throw new AssertionError();
    }

    /**
     * Adds one item. returns the item that is remaining and has not been added
     */
    @ExpectPlatform
    private static ItemStack add(ItemStack pBundleStack, ItemStack pInsertedStack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    private static Optional<ItemStack> removeOne(ItemStack pStack) {
        throw new AssertionError();
    }



    public static class QuiverTooltip implements TooltipComponent {
        private final NonNullList<ItemStack> items;
        private final int selectedIndex;

        public QuiverTooltip(NonNullList<ItemStack> itemStacks, int selectedIndex) {
            this.items = itemStacks;
            this.selectedIndex = selectedIndex;
        }

        public NonNullList<ItemStack> getItems() {
            return this.items;
        }

        public int getSelectedIndex() {
            return this.selectedIndex;
        }
    }
}

