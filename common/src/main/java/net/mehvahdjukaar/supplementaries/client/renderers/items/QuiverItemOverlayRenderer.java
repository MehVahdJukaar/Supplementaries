package net.mehvahdjukaar.supplementaries.client.renderers.items;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.item.IItemDecoratorRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.SlingshotRendererHelper;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class QuiverItemOverlayRenderer implements IItemDecoratorRenderer {

    public boolean render(Font font, ItemStack stack, int x, int y, float blitOffset) {
        boolean overlay = ClientConfigs.Items.QUIVER_OVERLAY.get();
        if (overlay ) {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null) {
                ItemStack ammo = QuiverItem.getQuiverData(stack).getSelected();
                renderAmmo(x, y, blitOffset, ammo);
            }
            return true;
        }
        return false;
    }

    public static void renderAmmo(int x, int y, float blitOffset, ItemStack ammo) {
        if (!ammo.isEmpty()) {

            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            float xOff = 22;
            float yOff = 8;
            posestack.translate(16.0F * (-0.25D) + (xOff + x) * (1 - 0.4f),
                    16.0F * (0.25D + 0.025) + (yOff + y) * (1 - 0.4f),
                    16.0F + (200.0F + blitOffset) * (1 - 0.4f));
            posestack.scale(0.4f, 0.4f, 0.4f);

            //0.4 scale
            RenderSystem.applyModelViewMatrix();

            Minecraft.getInstance().getItemRenderer().renderGuiItem(ammo, x, y);

            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }
}
