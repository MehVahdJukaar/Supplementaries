package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.selene.client.asset_generators.textures.Palette;
import net.mehvahdjukaar.selene.client.asset_generators.textures.SpriteUtils;
import net.mehvahdjukaar.selene.client.asset_generators.textures.TextureImage;
import net.mehvahdjukaar.selene.client.texture_renderer.FrameBufferBackedDynamicTexture;
import net.mehvahdjukaar.selene.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.selene.math.colors.HCLColor;
import net.mehvahdjukaar.selene.math.colors.RGBColor;
import net.mehvahdjukaar.supplementaries.common.entities.LabelEntity;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Function;


public class LabelEntityRenderer extends EntityRenderer<LabelEntity> {

    private final ModelBlockRenderer modelRenderer;
    private final ModelManager modelManager;

    public LabelEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        Minecraft minecraft = Minecraft.getInstance();
        this.modelRenderer = minecraft.getBlockRenderer().getModelRenderer();
        this.modelManager = minecraft.getBlockRenderer().getBlockModelShaper().getModelManager();
    }

    @Override
    public void render(LabelEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, light);

        if (entity.tickCount % 500 == 0) {
            // FlatItemTextureManager.clearCache();
            //  return;
        }


        poseStack.pushPose();
        Direction direction = entity.getDirection();

        //poseStack.translate((double) direction.getStepX() * d0, (double) direction.getStepY() * d0, (double) direction.getStepZ() * d0);
        Vector3f v = direction.step();
        float w = entity.getBbWidth() / 2f;
        v.mul(w, 0, w);

        //poseStack.mulPose(Vector3f.XP.rotationDegrees(entity.getXRot()));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entity.getYRot()));
        //poseStack.translate(0.5D+v.x(), 0.5D+entity.getEyeHeight(), 0.5D+v.z());
        poseStack.translate(-0.5D, -0.5D + entity.getBbHeight() / 2f, -0.5D - 6 / 32f);

        modelRenderer.renderModel(poseStack.last(), buffer.getBuffer(Sheets.cutoutBlockSheet()),
                null, modelManager.getModel(this.getModel(entity)), 1.0F, 1.0F, 1.0F,
                light, OverlayTexture.NO_OVERLAY);

        Item item = entity.getItem().getItem();

        if (item != Items.AIR) {


            FrameBufferBackedDynamicTexture tex = RenderedTexturesManager.getFlatItemTexture(item, 16,
                    "lb", LabelEntityRenderer::pp);

            ResourceLocation loc = tex.getTextureLocation();


            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(loc));


            Matrix4f tr = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();
            int overlay = OverlayTexture.NO_OVERLAY;

            float z = 15.5f / 16f;
            float s = 0.25f;
            poseStack.translate(0.5, 0.5, 0);
            vertexConsumer.vertex(tr, -s, -s, z).color(1f, 1f, 1f, 1f).uv(1f, 0f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();
            vertexConsumer.vertex(tr, -s, s, z).color(1f, 1f, 1f, 1f).uv(1f, 1f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();

            vertexConsumer.vertex(tr, s, s, z).color(1f, 1f, 1f, 1f).uv(0f, 1f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();
            vertexConsumer.vertex(tr, s, -s, z).color(1f, 1f, 1f, 1f).uv(0f, 0f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();

        }

        poseStack.popPose();

    }

    public static void pp(NativeImage image) {
        HCLColor dark = new RGBColor(41 / 255f, 22 / 255f, 1 / 255f, 1).asHCL();
        HCLColor light = new RGBColor(196 / 255f, 155 / 255f, 88 / 255f, 1).asHCL();

        //tex.getPixels().flipY();
        SpriteUtils.grayscaleImage(image);
        int cutoff = 13;
        Function<Integer, Integer> fn = i -> {
            if (i < cutoff) return i;
            else return (int) (Math.pow(i - cutoff + 1, 1 / 3f) + cutoff - 1);
        };

        SpriteUtils.reduceColors(image, fn);


        var t = TextureImage.of(image, null);
        Palette old = Palette.fromImage(t, null, 0);
        int s = old.size();
        Palette newPalette;
        if (s < 3) {
            newPalette = Palette.ofColors(List.of(light, dark));
        } else {
            newPalette = Palette.fromArc(light, dark, s);
        }
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int c = image.getPixelRGBA(x, y);
                for (int i = 0; i < old.size(); i++) {
                    if (old.getValues().get(i).value() == c) {
                        c = newPalette.getValues().get(i).value();
                        image.setPixelRGBA(x, y, c);

                        break;
                    }
                }
                if (new RGBColor(c).alpha() != 0) {
                    if ((x == 0 || new RGBColor(image.getPixelRGBA(x - 1, y)).alpha() == 0) ||
                            (x == image.getWidth() - 1 || new RGBColor(image.getPixelRGBA(x + 1, y)).alpha() == 0) ||
                            (y == 0 || new RGBColor(image.getPixelRGBA(x, y - 1)).alpha() == 0) ||
                            (y == image.getHeight() - 1 || new RGBColor(image.getPixelRGBA(x, y + 1)).alpha() == 0)) {
                        image.setPixelRGBA(x, y, dark.asRGB().mixWith(new RGBColor(c), 0.2f).toInt());
                    }


                }
            }
        }
        //r.recolor(p);


    }

    private ResourceLocation getModel(LabelEntity entity) {
        return ClientRegistry.LABEL_MODELS.get(entity.getAttachmentType());
    }

    //TODO USE this
    public Vec3 getRenderOffset(LabelEntity entity, float partialTicks) {
        return new Vec3((float) entity.getDirection().getStepX() * 0.3F, -0.25D, (float) entity.getDirection().getStepZ() * 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(LabelEntity p_110775_1_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    protected boolean shouldShowName(LabelEntity p_177070_1_) {
        return false;
    }

}

