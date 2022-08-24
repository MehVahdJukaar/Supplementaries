package net.mehvahdjukaar.supplementaries.common.items;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class QuiverItem extends Item {

    public static final int SLOTS = 7;
    public static final String TAG_ITEMS = "Items";

    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public QuiverItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack pStack, Slot pSlot, ClickAction pAction, Player pPlayer) {
        if (pAction != ClickAction.SECONDARY) {
            return false;
        } else {
            ItemStack itemstack = pSlot.getItem();
            //place into slot
            if (itemstack.isEmpty()) {
                this.playRemoveOneSound(pPlayer);
                removeOne(pStack).ifPresent((p_150740_) -> {
                    add(pStack, pSlot.safeInsert(p_150740_));
                });
            }
            //add
            else if (itemstack.getItem().canFitInsideContainerItems()) {
               // int i = (64 - getArrowFullness(pStack)) / getWeight(itemstack);
              //  int j = add(pStack, pSlot.safeTake(itemstack.getCount(), i, pPlayer));
               // if (j > 0) {
               //     this.playInsertSound(pPlayer);
              //  }
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
            } else {
                ItemStack i = add(quiver, pOther);
                if (i.equals(pOther)) {
                    //ifk if this will work
                    this.playInsertSound(pPlayer);
                    pOther.setCount(i.getCount());
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
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

    /**
     * Adds one item. returns the item that is remaining and has not been added
     */
    @ExpectPlatform
    private static ItemStack add(ItemStack pBundleStack, ItemStack pInsertedStack) {
        throw new UnsupportedOperationException();
    }

    //expect platform
    @ExpectPlatform
    private static Optional<ItemStack> removeOne(ItemStack pStack) {
        throw new UnsupportedOperationException();
    }

    @ExpectPlatform
    public static Stream<ItemStack> getContents(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTag();
        if (compoundtag == null) {
            return Stream.empty();
        } else {
            ListTag listtag = compoundtag.getList("Items", 10);
            return listtag.stream().map(CompoundTag.class::cast).map(ItemStack::of);
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        getContents(pStack).forEach(stacks::add);
        return Optional.of(new QuiverTooltip(stacks,1));
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("item.supplementaries.quiver.tooltip", getSelectedArrow(pStack), getSelectedArrowCount(pStack)));
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

    public static ItemStack getSelectedArrow(ItemStack stack) {
return stack;
    }


    private int getSelectedArrowCount(ItemStack pStack) {
        return 0;
    }




    //capability and provider in one

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

