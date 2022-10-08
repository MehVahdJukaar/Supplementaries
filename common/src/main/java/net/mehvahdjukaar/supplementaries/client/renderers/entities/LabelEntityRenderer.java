package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.FrameBufferBackedDynamicTexture;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.moonlight.api.resources.textures.Palette;
import net.mehvahdjukaar.moonlight.api.resources.textures.SpriteUtils;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HCLColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.TextUtil;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.NoticeBoardBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.entities.LabelEntity;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
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

        //debug. refresh
        if (entity.tickCount % 100 == 0) {
            //  RenderedTexturesManager.clearCache();
            //  return;
        }
        poseStack.pushPose();

        poseStack.mulPose(Vector3f.YP.rotationDegrees(180 - entity.getYRot()));
        poseStack.translate(0, -0, -0.5 + 1 / 32f);
        poseStack.translate(-0.5, -0.5, -0.5);

        modelRenderer.renderModel(poseStack.last(), buffer.getBuffer(Sheets.cutoutBlockSheet()), //
                null, ClientPlatformHelper.getModel(modelManager, this.getModel(entity)), 1.0F, 1.0F, 1.0F,
                light, OverlayTexture.NO_OVERLAY);

        Item item = entity.getItem().getItem();

        if (item != Items.AIR) {

            FrameBufferBackedDynamicTexture tex = getLabelTexture(item, 16);

            ResourceLocation loc = tex.getTextureLocation();


            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(loc));


            Matrix4f tr = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();
            int overlay = OverlayTexture.NO_OVERLAY;

            float z = 15.8f / 16f;
            float s = 0.25f;
            poseStack.translate(0.5, 0.5, z);
            poseStack.pushPose();

            poseStack.scale(0.75f, 0.75f, 1);
            vertexConsumer.vertex(tr, -s, -s, 0).color(1f, 1f, 1f, 1f).uv(1f, 0f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();
            vertexConsumer.vertex(tr, -s, s, 0).color(1f, 1f, 1f, 1f).uv(1f, 1f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();

            vertexConsumer.vertex(tr, s, s, 0).color(1f, 1f, 1f, 1f).uv(0f, 1f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();
            vertexConsumer.vertex(tr, s, -s, 0).color(1f, 1f, 1f, 1f).uv(0f, 0f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();

            poseStack.popPose();

            drawStatueNameplate(poseStack, buffer, Utils.getID(item).getPath());
        }

        poseStack.popPose();

    }


    private static FrameBufferBackedDynamicTexture getLabelTexture(Item item, int size) {
        //texture id for item size pair
        ResourceLocation res = Supplementaries.res(Utils.getID(item).toString().replace(":", "/"));
        return RenderedTexturesManager.getRenderedTexture(res, size, t -> {
            drawLabel(item, t);
        }, false);
    }

    private static void drawLabel(Item item, FrameBufferBackedDynamicTexture t) {
        RenderedTexturesManager.drawItem(t, item.getDefaultInstance());
        t.download();
        NativeImage img = t.getPixels();
        postProcess(img);
        t.upload();
    }


    //post process image
    public static void postProcess(NativeImage image) {
        HCLColor dark = new RGBColor(64 / 255f, 34 / 255f, 0 / 255f, 1).asHCL();
        //HCLColor light = new RGBColor(196 / 255f, 155 / 255f, 88 / 255f, 1).asHCL();
        HCLColor light = new RGBColor(235 / 255f, 213 / 255f, 178 / 255f, 1).asHCL();

        //tex.getPixels().flipY();
        SpriteUtils.grayscaleImage(image);
        int cutoff = 13;
        Function<Integer, Integer> fn = i -> {
            if (i < cutoff) return i;
            else return (int) (Math.pow(i - cutoff + 1, 1 / 3f) + cutoff - 1);
        };

        SpriteUtils.reduceColors(image, fn);
        //if (true) return;

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

    private void drawStatueNameplate(PoseStack matrixStack, MultiBufferSource buffer, String name) {
        var text = Component.literal(name).withStyle(ChatFormatting.BLACK);
        Font font = Minecraft.getInstance().font;
        int width = font.width(text);

        matrixStack.pushPose();
        matrixStack.translate(0, -0.25, 0);


        float borderY = 0.125f;
        float borderX = 0.1875f;
        float paperWidth = 1 - (2 * borderX);
        float paperHeight = 1 - (2 * borderY);
        float maxLines;
        int scalingFactor;
        List<FormattedCharSequence> tempPageLines;
        do {
           scalingFactor = Mth.floor(Mth.sqrt((width * 8f) / (paperWidth * paperHeight)));

            tempPageLines = font.split(text, Mth.floor(paperWidth * scalingFactor));
            //tempPageLines = RenderComponentsUtil.splitText(txt, MathHelper.floor(lx * scalingfactor), font, true, true);

            maxLines = paperHeight * scalingFactor / 8f;
            width += 1;
            // when lines fully filled @scaling factor > actual lines -> no overflow lines
            // rendered
        } while (maxLines < tempPageLines.size());

        matrixStack.scale(0.01F, -0.01F, 0.01F);

        font.drawInBatch(text, -width / 2F, -font.lineHeight / 2f, 0xFF000000, false,
                matrixStack.last().pose(), buffer, false, 0, LightTexture.FULL_BRIGHT);

        matrixStack.popPose();
    }

    private ResourceLocation getModel(LabelEntity entity) {
        return ClientRegistry.LABEL_MODEL;
    }

    @Override
    public Vec3 getRenderOffset(LabelEntity entity, float partialTicks) {
        return Vec3.ZERO;
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