package net.mehvahdjukaar.supplementaries.common.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class QuiverContent extends SelectableContainerContent<QuiverContent.Mutable> {

    public static final Codec<QuiverContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.listOf().fieldOf("Items").forGetter(QuiverContent::getContentCopy),
            Codec.INT.fieldOf("SelectedSlot").forGetter(QuiverContent::getSelectedSlot)
    ).apply(instance, QuiverContent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuiverContent> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), QuiverContent::getContentCopy,
            ByteBufCodecs.INT, QuiverContent::getSelectedSlot,
            QuiverContent::new
    );

    QuiverContent(List<ItemStack> stacks, int selected) {
        super(stacks, selected);
    }

    public ItemStack getSelected() {
        return getSelected(null);
    }

    public ItemStack getSelected(@Nullable Predicate<ItemStack> supporterArrows) {
        if (supporterArrows == null) return super.getSelected();

        var content = this.getContentUnsafe();
        int size = content.size();
        for (int i = 0; i < size; i++) {
            ItemStack s = content.get((i + this.selectedSlot) % size);
            if (supporterArrows.test(s)) return s.copy();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Mutable toMutable() {
        return new Mutable(this);
    }


    public static class Mutable extends Mut<QuiverContent> {

        protected Mutable(SelectableContainerContent<?> original) {
            super(original);
        }

        @Override
        public QuiverContent toImmutable() {
            this.updateSelectedIfNeeded();
            return new QuiverContent(this.stacks, this.selectedSlot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return QuiverItem.canAcceptItem(stack);
        }


    }


}
