package net.mehvahdjukaar.supplementaries.client.renderers.items;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.item.IItemDecoratorRenderer;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

public class QuiverItemOverlayRenderer implements IItemDecoratorRenderer {

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        boolean overlay = ClientConfigs.Items.QUIVER_OVERLAY.get();
        if (overlay) {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null) {
                ItemStack ammo = QuiverItem.getQuiverContent(stack).getSelected();
                renderAmmo(graphics, x, y, ammo);
            }
            return true;
        }
        return false;
    }

    public static void renderAmmo(GuiGraphics graphics, int x, int y, ItemStack ammo) {
        if (!ammo.isEmpty()) {
            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();
            float xOff = 22;
            float yOff = 8;
            poseStack.translate(16.0F * (-0.25D) + (xOff + x) * (1 - 0.4f),
                    16.0F * (0.25D + 0.025) + (yOff + y) * (1 - 0.4f),
                    16.0F + (200.0F) * (1 - 0.4f));
            poseStack.scale(0.4f, 0.4f, 0.4f);

            //0.4 scale
            RenderSystem.applyModelViewMatrix();

            graphics.renderFakeItem(ammo, x, y);

            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }
}
