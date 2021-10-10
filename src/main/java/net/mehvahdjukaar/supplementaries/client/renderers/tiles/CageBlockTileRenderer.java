package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.common.mobholder.IMobContainerProvider;
import net.mehvahdjukaar.supplementaries.common.mobholder.MobContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;


public class CageBlockTileRenderer<T extends TileEntity & IMobContainerProvider> extends TileEntityRenderer<T> {
    private final EntityRendererManager entityRenderer;

    public CageBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    public void renderMob(MobContainer mobHolder, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, Direction dir) {
        Entity mob = mobHolder.getDisplayedMob();
        if (mob != null) {
            matrixStack.pushPose();

            float s = mobHolder.getData().getScale();

            renderMobStatic(mob, s, entityRenderer, matrixStack, partialTicks, bufferIn, combinedLightIn, dir.toYRot());

            matrixStack.popPose();
        }
    }

    public static void renderMobStatic(Entity mob, float scale, EntityRendererManager renderer, MatrixStack matrixStack, float partialTicks,  IRenderTypeBuffer bufferIn, int combinedLightIn, float rot){

        double y = MathHelper.lerp(partialTicks, mob.yOld, mob.getY());//0;//mobHolder.getYOffset(partialTicks);
        double x = mob.getX();
        double z = mob.getZ();

        y = relativeOffset(y);
        x = relativeOffset(x);
        z = relativeOffset(z);

        matrixStack.translate(x, y, z);

        matrixStack.mulPose(Const.rot(-(int) rot));

        matrixStack.scale(scale, scale, scale);

        renderer.setRenderShadow(false);
        renderer.render(mob, 0, 0, 0, 0.0F, partialTicks, matrixStack, bufferIn, combinedLightIn);
        renderer.setRenderShadow(true);
    }


    public static double relativeOffset(double pos) {
        if (pos < 0) return 1 + pos % 1;
        return pos % 1;
    }

    @Override
    public void render(T tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        this.renderMob(tile.getMobContainer(), partialTicks, matrixStackIn, bufferIn, combinedLightIn, tile.getDirection());

   }
}

