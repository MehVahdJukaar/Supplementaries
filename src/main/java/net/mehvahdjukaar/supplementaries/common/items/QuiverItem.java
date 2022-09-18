package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.client.QuiverArrowSelectGui;
import net.mehvahdjukaar.supplementaries.common.entities.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
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
            if (itemstack.is(ModRegistry.QUIVER_ITEM.get())) return false;
            //place into slot
            AtomicBoolean didStuff = new AtomicBoolean(false);
            if (itemstack.isEmpty()) {
                IQuiverData data = getQuiverData(quiver);
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
                IQuiverData data = getQuiverData(quiver);
                if (data != null) {
                    var taken = pSlot.safeTake(itemstack.getCount(), 64, pPlayer);
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
        if (pAction == ClickAction.SECONDARY && pSlot.allowModification(pPlayer) && !pOther.is(ModRegistry.QUIVER_ITEM.get())) {
            IQuiverData data = getQuiverData(quiver);
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
            IQuiverData data = getQuiverData(stack);
            if (data != null) {
                if (data.cycle()) {
                    this.playInsertSound(player);
                }
            }
        } else {
            //same as startUsingItem but client only so it does not slow
            if (pLevel.isClientSide) {
                QuiverArrowSelectGui.setActive(true);
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
            QuiverArrowSelectGui.setActive(false);
        }
        this.playInsertSound(livingEntity);
        livingEntity.swing(livingEntity.getUsedItemHand());
        super.releaseUsing(stack, level, livingEntity, timeCharged);
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        IQuiverData data = getQuiverData(pStack);
        if (data != null) {
            return data.getSelected().getCount() > 0;
        }
        return false;
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        IQuiverData data = getQuiverData(pStack);
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
        IQuiverData data = getQuiverData(pStack);
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
        IQuiverData data = getQuiverData(pStack);
        if (data != null) {
            int c = data.getSelectedArrowCount();
            if (c != 0) {
                pTooltipComponents.add(new TranslatableComponent("message.supplementaries.quiver.tooltip",
                        data.getSelected(null).getItem().getDescription(), c).withStyle(ChatFormatting.GRAY));
            }
        }
    }


    @Override
    public void onDestroyed(ItemEntity pItemEntity) {
        IQuiverData data = getQuiverData(pItemEntity.getItem());
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

    //used to reset the selected arrow. I wish I didn't have to do this but I dont have control over when the itemstack is decremented
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        IQuiverData data = getQuiverData(stack);
        if (data != null) data.updateSelectedIfNeeded();
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }


    public record QuiverTooltip(List<ItemStack> stacks, int selected) implements TooltipComponent {
    }

    //this is cap, cap provider
    public interface IQuiverData {

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
         * Adds one item. returns the item that is remaining and has not been added
         */
        ItemStack tryAdding(ItemStack pInsertedStack);

        Optional<ItemStack> removeOneStack();

        default int getSelectedArrowCount() {
            ItemStack selected = this.getSelected(null);
            int amount = 0;
            for (var item : this.getContentView()) {
                if (ItemHandlerHelper.canItemStacksStack(selected, item)) {
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


    public static ItemStack getQuiver(LivingEntity entity) {
        if (!(entity instanceof Player) && entity instanceof IQuiverEntity e) return e.getQuiver();
        var cap = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
        if (cap != null) {
            for (int i = 0; i < cap.getSlots(); i++) {
                ItemStack quiver = cap.getStackInSlot(i);
                if (quiver.getItem() == ModRegistry.QUIVER_ITEM.get()) return quiver;
            }
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    public static QuiverItem.IQuiverData getQuiverData(ItemStack stack) {
        return (QuiverCapability) stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
    }

    public static class QuiverCapability extends ItemStackHandler implements ICapabilitySerializable<CompoundTag>, QuiverItem.IQuiverData {

        private final LazyOptional<IItemHandler> lazyOptional = LazyOptional.of(() -> this);

        //Provider
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, lazyOptional);
        }

        @Override
        public CompoundTag serializeNBT() {
            var c = super.serializeNBT();
            c.putInt("SelectedSlot", this.selectedSlot);
            return c;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            super.deserializeNBT(nbt);
            this.selectedSlot = nbt.getByte("SelectedSlot");
        }

        //actual cap

        private int selectedSlot = 0;

        public QuiverCapability() {
            super(ServerConfigs.item.QUIVER_SLOTS.get());
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return this.canAcceptItem(stack);
        }

        public List<ItemStack> getContentView() {
            return this.stacks;
        }

        @Override
        public int getSelectedSlot() {
            return selectedSlot;
        }

        public void setSelectedSlot(int selectedSlot) {
            if (!stacks.get(selectedSlot).isEmpty()) {
                this.selectedSlot = selectedSlot;
            }
        }

        public boolean cycle(int slotsMoved) {
            int originalSlot = this.selectedSlot;
            int maxSlots = this.stacks.size();
            slotsMoved = slotsMoved % maxSlots;
            this.selectedSlot = (maxSlots + (this.selectedSlot + slotsMoved)) % maxSlots;
            for (int i = 0; i < maxSlots; i++) {
                var stack = this.getStackInSlot(selectedSlot);
                if (!stack.isEmpty()) break;
                this.selectedSlot = (maxSlots + (this.selectedSlot + (slotsMoved >= 0 ? 1 : -1))) % maxSlots;
            }
            return originalSlot != selectedSlot;
        }

        public ItemStack tryAdding(ItemStack toInsert) {
            if (!toInsert.isEmpty() && toInsert.getItem().canFitInsideContainerItems()) {
                return ItemHandlerHelper.insertItem(this, toInsert, false);
            }
            return ItemStack.EMPTY;
        }

        public Optional<ItemStack> removeOneStack() {
            int i = 0;
            for (var s : this.getContentView()) {
                if (!s.isEmpty()) {
                    var extracted = this.extractItem(i, s.getCount(), false);
                    this.updateSelectedIfNeeded();
                    return Optional.of(extracted);
                }
                i++;
            }
            return Optional.empty();
        }

        @Override
        public void consumeArrow() {
            var s = this.getSelected();
            s.shrink(1);
            if (s.isEmpty()) this.stacks.set(this.selectedSlot, ItemStack.EMPTY);
            this.updateSelectedIfNeeded();
            //not implemented because it isn't needed
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new QuiverCapability();
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag baseTag = stack.getTag();
        var cap = getQuiverData(stack);
        if (cap instanceof QuiverCapability c) {
            if (baseTag == null) baseTag = new CompoundTag();
            baseTag = baseTag.copy();
            baseTag.put("QuiverCap", c.serializeNBT());
        }
        return baseTag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag tag) {
        if (tag != null && tag.contains("QuiverCap")) {
            CompoundTag capTag = tag.getCompound("QuiverCap");
            tag.remove("QuiverCap");
            var cap = getQuiverData(stack);
            if (cap instanceof QuiverCapability c) {
                c.deserializeNBT(capTag);
            }
        }
        stack.setTag(tag);
    }

}

