package net.mehvahdjukaar.supplementaries.client.renderers.items;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.supplementaries.client.renderers.CapturedMobCache;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.CageBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModEnchantments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;

import java.util.UUID;
import java.util.WeakHashMap;

public class SlingshotItemOverlayRenderer extends ProjectileWeaponOverlayRenderer {

    private final WeakHashMap<CompoundTag, Entity> cachedParrots = new WeakHashMap<>();

    private UUID lastUUID = null;

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        boolean overlay = ClientConfigs.Items.SLINGSHOT_OVERLAY.get();
        boolean outline = ClientConfigs.Items.SLINGSHOT_OUTLINE.get();
        if (overlay || outline) {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {

                if (overlay) {
                    if (!renderParrot(player, graphics, font, stack, x, y)) {
                        ItemStack ammo = getAmmoForPreview(stack, Minecraft.getInstance().level, player);
                        renderAmmo(graphics, x, y, ammo);
                    }
                }
                if (outline) {
                    if (EnchantmentHelper.has(stack, ModEnchantments.PROJECTILE_NO_GRAVITY.get())) {
                        SlingshotRendererHelper.grabNewLookPos(player);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean renderParrot(Player player, GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        CompoundTag firstParrot = player.getShoulderEntityLeft();
        if (firstParrot.isEmpty()) {
            firstParrot = player.getShoulderEntityRight();
        }
        if (firstParrot.isEmpty()) return false;
        if (!firstParrot.contains("UUID")) return false;
        UUID id = firstParrot.getUUID("UUID");

        Entity e = CapturedMobCache.getOrCreateCachedMob(player.level(), id, firstParrot);
        if (e == null) return false;
        e.setPos(0, 0, 0);
        e.setYRot(0);
        e.setYBodyRot(0);
        e.setYHeadRot(0);
        e.setXRot(0);

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        float xOff = 17;
        float yOff = +10;
        float a = 0f;
        AABB bb = e.getBoundingBox();
        float scale = (float) (0.7f * Math.max(bb.getXsize(), bb.getYsize()));
        poseStack.translate(16.0F * (-0.25D) + (xOff + x) * (1 - a),
                16.0F * (0.25D + 0.025) + (yOff + y) * (1 - a),
                16.0F + (200.0F) * (1 - a));
        poseStack.scale(scale, -scale, scale);


        RenderSystem.applyModelViewMatrix();
        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        poseStack.mulPose(Axis.XP.rotationDegrees(30));
        EntityRenderDispatcher entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
        CageBlockTileRenderer.renderMobStatic(e, 16, entityRenderer, graphics.pose(), 1, bufferSource,
                LightTexture.FULL_BRIGHT, 45);

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();


        return true;
    }

}
