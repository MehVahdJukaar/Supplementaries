package net.mehvahdjukaar.supplementaries.mixins.forge.self;

import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.mehvahdjukaar.supplementaries.common.items.forge.QuiverItemImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(QuiverItem.class)
public abstract class SelfQuiverItemMixin extends SelectableContainerItem<QuiverItem.Data> {

    protected SelfQuiverItemMixin(Properties arg) {
        super(arg);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new QuiverItemImpl.Cap(this.getMaxSlots());
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag baseTag = stack.getTag();
        var cap = QuiverItemImpl.getQuiverData(stack);
        if (cap instanceof QuiverItemImpl.Cap c) {
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
            var cap = QuiverItemImpl.getQuiverData(stack);
            if (cap instanceof QuiverItemImpl.Cap c) {
                c.deserializeNBT(capTag);
            }
        }
        stack.setTag(tag);
    }


}
