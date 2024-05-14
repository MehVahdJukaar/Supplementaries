package net.mehvahdjukaar.supplementaries.client.renderers.items;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.item.IItemDecoratorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ProjectileWeaponOverlayRenderer implements IItemDecoratorRenderer {

    private ItemStack clientCurrentAmmo = ItemStack.EMPTY;

    //unneded optimization
    public ItemStack getAmmoForPreview(ItemStack cannon, @Nullable Level world, Player player) {
        if (world != null) {
            if (world.getGameTime() % 10 == 0) {
                clientCurrentAmmo = ItemStack.EMPTY;

                ItemStack findAmmo = player.getProjectile(cannon);
                if (findAmmo.getItem() != net.minecraft.world.item.Items.ARROW) {
                    clientCurrentAmmo = findAmmo;
                }
            }
        }
        return clientCurrentAmmo;
    }

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {

            ItemStack ammo = getAmmoForPreview(stack, Minecraft.getInstance().level, player);

            renderAmmo(graphics, x, y, ammo);
        }
        return true;
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
