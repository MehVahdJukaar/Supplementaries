package net.mehvahdjukaar.supplementaries.common.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class LunchBaskedContent extends SelectableContainerContent<LunchBaskedContent.Mutable> {

    protected static final MutableComponent CLOSED_TOOLTIP = Component.translatable("message.supplementaries.lunch_box.tooltip.closed");
    protected static final MutableComponent OPEN_TOOLTIP = Component.translatable("message.supplementaries.lunch_box.tooltip.open");

    public static final Codec<LunchBaskedContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(LunchBaskedContent::getContentCopy),
            Codec.INT.fieldOf("selected_slot").forGetter(LunchBaskedContent::getSelectedSlot),
            Codec.BOOL.fieldOf("open").forGetter(LunchBaskedContent::canEatFrom)
    ).apply(instance, LunchBaskedContent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LunchBaskedContent> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, LunchBaskedContent::getContentCopy,
            ByteBufCodecs.INT, LunchBaskedContent::getSelectedSlot,
            ByteBufCodecs.BOOL, LunchBaskedContent::canEatFrom,
            LunchBaskedContent::new
    );

    private final boolean isOpen;

    LunchBaskedContent(List<ItemStack> stacks, int selectedSlot, boolean isOpen) {
        super(stacks, selectedSlot);
        this.isOpen = isOpen;
    }

    public static LunchBaskedContent empty(int count) {
        return new LunchBaskedContent(NonNullList.withSize(count, ItemStack.EMPTY), 0, false);
    }

    @Override
    public Mutable toMutable() {
        return new Mutable(this);
    }

    public boolean canEatFrom() {
        return isOpen;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.addToTooltip(context, tooltipAdder, tooltipFlag);
        tooltipAdder.accept(isOpen ? OPEN_TOOLTIP : CLOSED_TOOLTIP);
    }

    public static class Mutable extends Mut<LunchBaskedContent> {

        private boolean isOpen;

        public Mutable(LunchBaskedContent original) {
            super(original);
            this.isOpen = original.isOpen;
        }

        @Override
        public LunchBaskedContent toImmutable() {
            this.updateSelectedIfNeeded();
            return new LunchBaskedContent(this.stacks, this.selectedSlot, this.isOpen);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return LunchBoxItem.canAcceptItem(stack);
        }

        public void switchMode() {
            this.isOpen = !this.isOpen;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LunchBaskedContent that = (LunchBaskedContent) o;
        return selectedSlot == that.selectedSlot && selectedItemCount == that.selectedItemCount && Objects.equals(stacks, that.stacks)
                && isOpen == that.isOpen;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stacks, selectedSlot, isOpen);
    }
}
