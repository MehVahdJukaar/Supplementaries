package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CartographyTableScreen.class)
public abstract class CartographyTableScreenMixin extends AbstractContainerScreen<CartographyTableMenu> {

    @Unique
    private final CyclingSlotBackground supplementaries$mapSlotIcon = new CyclingSlotBackground(0);
    @Unique
    private final CyclingSlotBackground supplementaries$ingredientsSlotIcon = new CyclingSlotBackground(1);

    protected CartographyTableScreenMixin(CartographyTableMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }


    @Inject(method = "renderBg", at = @At("TAIL"))
    public void supp$renderBetterSlots(GuiGraphics graphics, float ticks, int mouseX, int mouseY, CallbackInfo ci) {
        this.supplementaries$mapSlotIcon.render(this.menu, graphics, ticks, this.leftPos, this.topPos);
        this.supplementaries$ingredientsSlotIcon.render(this.menu, graphics, ticks, this.leftPos, this.topPos);
    }

    @ModifyVariable(method = "renderBg", at = @At(value = "STORE"), ordinal = 1)
    public ItemStack supp$antiqueInkHack(ItemStack stack) {
        if (this.menu.getSlot(1).getItem().is(ModRegistry.ANTIQUE_INK.get())) {
            ItemStack output = this.menu.getSlot(2).getItem();
            if (output.is(Items.FILLED_MAP)) {
                return output;
            }
        }
        return stack;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.supplementaries$mapSlotIcon.tick(ModTextures.MAP_ICONS);
        this.supplementaries$ingredientsSlotIcon.tick(ModTextures.CARTOGRAPHY_INGREDIENTS_ICONS);
    }
}
