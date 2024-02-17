package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.block_models.JarBakedModel;
import net.mehvahdjukaar.supplementaries.client.renderers.VertexUtils;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


public class JarBlockTileRenderer extends CageBlockTileRenderer<JarBlockTile> {
    private final ItemRenderer itemRenderer;

    public JarBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        Minecraft minecraft = Minecraft.getInstance();
        itemRenderer = minecraft.getItemRenderer();
    }


    public static void renderFluid(float percentageFill, int color, int luminosity, ResourceLocation texture, PoseStack poseStack, MultiBufferSource bufferIn, int light, int combinedOverlayIn) {
        poseStack.pushPose();
        if (luminosity != 0) light = light & 15728640 | luminosity << 4;
        VertexConsumer builder = ModMaterials.get(texture).buffer(bufferIn, RenderType::entityTranslucentCull);
        Vector3f dimensions = JarBakedModel.getJarLiquidDimensions();
        poseStack.translate(0.5, dimensions.z(), 0.5);
        VertexUtil.addCube(builder, poseStack,
                dimensions.x(),
                percentageFill * dimensions.y(),
                light, color);
        poseStack.popPose();
    }

    @Override
    public void render(JarBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        long r = tile.getBlockPos().asLong();
        RandomSource rand = RandomSource.create(r);
        //render cookies
        AtomicInteger i = new AtomicInteger();
        renderCookies(poseStack, bufferIn, rand, combinedLightIn, combinedOverlayIn,
                () -> {
                    int j = i.getAndIncrement();
                    return j < tile.getContainerSize() ? tile.getItem(j) : ItemStack.EMPTY;
                });
        //render fish
        var data = tile.mobContainer.getData();
        if (data != null) {
            if (data.is2DFish()) {
                poseStack.pushPose();

                long time = System.currentTimeMillis() + r;
                float angle = (time % (360 * 80)) / 80f;
                float angle2 = (time % (360 * 3)) / 3f;
                float angle3 = (time % (360 * 350)) / 350f;
                float wo = 0.015f * Mth.sin((float) (2 * Math.PI * angle2 / 360));
                float ho = 0.1f * Mth.sin((float) (2 * Math.PI * angle3 / 360));
                VertexConsumer builder = bufferIn.getBuffer(RenderType.cutout());
                poseStack.translate(0.5, 0.5, 0.5);
                Quaternionf rotation = Axis.YP.rotationDegrees(-angle);
                poseStack.mulPose(rotation);
                poseStack.scale(0.625f, 0.625f, 0.625f);
                Vector3f dimensions = JarBakedModel.getJarLiquidDimensions();

                poseStack.translate(0, -0.2, -0.335 * (dimensions.x() / 0.5f));
                int fishType = data.getFishTexture();

                //overlay
                VertexUtils.renderFish(builder, poseStack, wo, ho, fishType, combinedLightIn);
                poseStack.popPose();

            } else {
                super.render(tile, partialTicks, poseStack, bufferIn, combinedLightIn, combinedOverlayIn);
            }
            var fluid = tile.mobContainer.shouldRenderWithFluid();
            if (fluid != null && fluid.isPresent()) {
                if (fluid.get() == BuiltInSoftFluids.WATER.get()) {
                    //sand
                    poseStack.pushPose();
                    Vector3f dimensions = JarBakedModel.getJarLiquidDimensions();

                    poseStack.translate(0.5, 0.0015 + dimensions.z(), 0.5);

                    VertexConsumer builder = ModMaterials.SAND_MATERIAL.buffer(bufferIn, RenderType::entityCutout);
                    VertexUtil.addCube(builder, poseStack, 0.99f * dimensions.x(), dimensions.y() / 12,
                            combinedLightIn, -1);
                    poseStack.popPose();
                }
                poseStack.pushPose();
                SoftFluid s = fluid.get();
                renderFluid(9 / 12f, s.getTintColor(), 0, s.getStillTexture(),
                        poseStack, bufferIn, combinedLightIn, combinedOverlayIn);
                poseStack.popPose();
            }
        }
        //render fluid
        SoftFluidTank tank = tile.fluidHolder;
        if (!USE_MODEL && !tank.isEmpty()) {
            SoftFluid fluid = tank.getFluidValue();
            renderFluid(tank.getHeight(1), tank.getTintColor(tile.getLevel(), tile.getBlockPos()),
                    fluid.getLuminosity(), fluid.getStillTexture(),
                    poseStack, bufferIn, combinedLightIn, combinedOverlayIn);
        }
    }

    public static void renderCookies(PoseStack poseStack, MultiBufferSource buffer, RandomSource rand,
                                     int light, int overlay, Supplier<ItemStack> itemIterator) {

        ItemStack cookieStack = itemIterator.get();
        if (!cookieStack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(RotHlpr.XN90);
            poseStack.translate(0, 0, -0.5);
            float scale = 8f / 14f;
            poseStack.scale(scale, scale, scale);
            do {
                poseStack.mulPose(Axis.ZP.rotationDegrees(rand.nextInt(360)));
                poseStack.translate(0, 0, 1 / (16f * scale));
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                BakedModel model = itemRenderer.getModel(cookieStack, null, null, 0);
                itemRenderer.render(cookieStack, ItemDisplayContext.FIXED, true, poseStack, buffer, light,
                        overlay, model);
                cookieStack = itemIterator.get();
            } while (!cookieStack.isEmpty());
            poseStack.popPose();
        }
    }

    private static final boolean USE_MODEL = false;// PlatHelper.getPlatform().isForge();
}

