package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.items.FlagItem;
import net.minecraft.block.BannerBlock;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.LoomContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LoomContainer.class)
public abstract class LoomContainerMixin extends Container {

    protected LoomContainerMixin(@Nullable ContainerType<?> p_i50105_1_, int p_i50105_2_) {
        super(p_i50105_1_, p_i50105_2_);
    }
    /*
    @Redirect(method ="<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/util/IWorldPosCallable;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/inventory/container/LoomContainer;addSlot(Lnet/minecraft/inventory/container/Slot;)Lnet/minecraft/inventory/container/Slot;",
                    ordinal = 0))
    public Slot LoomContainer(LoomContainer loomContainer, Slot p_75146_1_) {
        return this.addSlot(new LoomSlot(this.inputContainer, 0, 13, 26));
    }*/

    @Redirect(method = "quickMoveStack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;",
                    ordinal = 0))
    public Item quickMoveStack(ItemStack stack) {
        Item i = stack.getItem();
        if (i instanceof FlagItem) {
            i = BannerBlock.byColor(((FlagItem) i).getColor()).asItem();
        }
        return i;
    }
}
