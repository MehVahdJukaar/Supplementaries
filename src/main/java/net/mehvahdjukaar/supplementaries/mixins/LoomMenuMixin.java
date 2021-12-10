package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.items.FlagItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BannerBlock;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LoomMenu.class)
public abstract class LoomMenuMixin extends AbstractContainerMenu {

    @Shadow
    public Slot bannerSlot;

    @Final
    @Shadow
    private Container inputContainer;

    protected LoomMenuMixin(@Nullable MenuType<?> p_i50105_1_, int p_i50105_2_) {
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

    @Redirect(method ="quickMoveStack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",
                    ordinal = 0))
    public Item getItem(ItemStack stack) {
        Item i = stack.getItem();
        if(i instanceof FlagItem){
            //hax
            i = BannerBlock.byColor(((FlagItem) i).getColor()).asItem();
        }
        return i;
    }
}
