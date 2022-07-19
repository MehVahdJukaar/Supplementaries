package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.items.SlingshotRendererHelper;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class SlingshotItemOverlayRenderer {

    public static void render(ItemStack stack, int x, int y, float blitOffset) {
        boolean overlay = ClientConfigs.Items.SLINGSHOT_OVERLAY.get();
        boolean outline = ClientConfigs.Items.SLINGSHOT_OUTLINE.get();
        if (overlay || outline) {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {

                if (overlay) {
                    ItemStack ammo = SlingshotRendererHelper.getAmmoForPreview(stack, Minecraft.getInstance().level, player);

                    if (!ammo.isEmpty()) {

                        PoseStack posestack = RenderSystem.getModelViewStack();
                        posestack.pushPose();


                        posestack.translate(16.0F * (-0.25D) + (8.0F + x) * (1 - 0.4f),
                                16.0F * (0.25D + 0.025) + (8.0F + y) * (1 - 0.4f),
                                16.0F + (100.0F + blitOffset) * (1 - 0.4f));
                        posestack.scale(0.4f, 0.4f, 0.4f);

                        //0.4 scale
                        RenderSystem.applyModelViewMatrix();

                        Minecraft.getInstance().getItemRenderer().renderGuiItem(ammo, x, y);

                        posestack.popPose();
                        RenderSystem.applyModelViewMatrix();
                    }
                }
                if (outline) {
                    if (EnchantmentHelper.getItemEnchantmentLevel(ModRegistry.STASIS_ENCHANTMENT.get(), stack) != 0) {
                        SlingshotRendererHelper.grabNewLookPos(player);
                    }
                }
            }
        }
    }
}
