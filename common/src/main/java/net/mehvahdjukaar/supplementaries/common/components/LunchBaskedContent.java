package net.mehvahdjukaar.supplementaries.common.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;

import java.util.List;
import java.util.function.Consumer;

public class LunchBaskedContent extends SelectableContainerContent<LunchBaskedContent.Mutable> {

    public static final Codec<LunchBaskedContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.listOf().fieldOf("Items").forGetter(LunchBaskedContent::getContentCopy),
            Codec.INT.fieldOf("SelectedSlot").forGetter(LunchBaskedContent::getSelectedSlot),
            Codec.BOOL.fieldOf("Open").forGetter(LunchBaskedContent::canEatFrom)
    ).apply(instance, LunchBaskedContent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LunchBaskedContent> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), LunchBaskedContent::getContentCopy,
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

    public static class Mutable extends Mut {

        private boolean isOpen;

        public Mutable(LunchBaskedContent original) {
            super(original);
            this.isOpen = original.isOpen;
        }

        @Override
        public LunchBaskedContent toImmutable() {
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

}
