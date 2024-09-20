package net.mehvahdjukaar.supplementaries.common.components;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.function.Consumer;

public class QuiverContent extends SelectableContainerContent<QuiverContent.Mutable>{

    QuiverContent(List<ItemStack> stacks, int selected) {
        super(stacks, selected);
    }

    @Override
    public Mutable toMutable() {
        return new Mutable(this);
    }

    public static class Mutable extends Mut{

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
