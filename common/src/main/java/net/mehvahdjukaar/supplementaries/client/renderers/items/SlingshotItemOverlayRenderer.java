package net.mehvahdjukaar.supplementaries.client.renderers.items;

import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModEnchantments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class SlingshotItemOverlayRenderer extends ProjectileWeaponOverlayRenderer{

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        boolean overlay = ClientConfigs.Items.SLINGSHOT_OVERLAY.get();
        boolean outline = ClientConfigs.Items.SLINGSHOT_OUTLINE.get();
        if (overlay || outline) {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {

                if (overlay) {
                    ItemStack ammo = getAmmoForPreview(stack, Minecraft.getInstance().level, player);

                    renderAmmo(graphics, x, y, ammo);
                }
                if (outline) {
                    if (EnchantmentHelper.has( stack, ModEnchantments.PROJECTILE_NO_GRAVITY.get())) {
                        SlingshotRendererHelper.grabNewLookPos(player);
                    }
                }
            }
            return true;
        }
        return false;
    }

}
