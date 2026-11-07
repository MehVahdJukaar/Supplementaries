package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CartographyTableScreen.class)
public abstract class CartographyTableScreenMixin extends AbstractContainerScreen<CartographyTableMenu> {

    protected CartographyTableScreenMixin(CartographyTableMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @ModifyVariable(method = "renderBg", at = @At(value = "STORE"), ordinal = 1)
    public ItemStack supp$setAntiqueInk(ItemStack stack) {
        if (this.menu.getSlot(1).getItem().is(ModRegistry.ANTIQUE_INK.get())) {
            ItemStack output = this.menu.getSlot(2).getItem();
            if (output.is(Items.FILLED_MAP)) {
                return output;
            }
        }
        return stack;
    }
}
