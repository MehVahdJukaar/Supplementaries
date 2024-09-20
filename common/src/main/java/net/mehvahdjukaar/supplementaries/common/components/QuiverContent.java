package net.mehvahdjukaar.supplementaries.common.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.function.Consumer;

public class QuiverContent extends SelectableContainerContent<QuiverContent.Mutable>{

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

    @Override
    public Mutable toMutable() {
        return new Mutable(this);
    }

    public static class Mutable extends Mut<QuiverContent>{

        protected Mutable(SelectableContainerContent<?> original) {
            super(original);
        }

        @Override
        public QuiverContent toImmutable() {
            return new QuiverContent(this.stacks, this.selectedSlot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    }


}
