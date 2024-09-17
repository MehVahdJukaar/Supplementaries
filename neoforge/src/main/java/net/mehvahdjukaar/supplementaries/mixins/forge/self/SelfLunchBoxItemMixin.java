package net.mehvahdjukaar.supplementaries.mixins.forge.self;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.mehvahdjukaar.supplementaries.common.items.forge.LunchBoxItemImpl;
import net.mehvahdjukaar.supplementaries.common.items.forge.QuiverItemImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LunchBoxItem.class)
public abstract class SelfLunchBoxItemMixin extends SelectableContainerItem<QuiverItem.Data> {

    protected SelfLunchBoxItemMixin(Properties arg) {
        super(arg);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new LunchBoxItemImpl.Cap(this.getMaxSlots());
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag baseTag = stack.getTag();
        var cap = LunchBoxItemImpl.getLunchBoxData(stack);
        if (cap instanceof LunchBoxItemImpl.Cap c) {
            if (baseTag == null) baseTag = new CompoundTag();
            baseTag = baseTag.copy();
            baseTag.put("LunchBoxCap", c.serializeNBT());
        }
        return baseTag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag tag) {
        if (tag != null && tag.contains("LunchBoxCap")) {
            CompoundTag capTag = tag.getCompound("LunchBoxCap");
            tag.remove("LunchBoxCap");
            var cap = LunchBoxItemImpl.getLunchBoxData(stack);
            if (cap instanceof LunchBoxItemImpl.Cap c) {
                c.deserializeNBT(capTag);
            }
        }
        stack.setTag(tag);
    }


}
