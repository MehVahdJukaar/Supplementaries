package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.util.IMobHolder;
import net.mehvahdjukaar.supplementaries.block.util.MobHolder;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;


public class CageBlockTileRenderer<T extends TileEntity & IMobHolder> extends TileEntityRenderer<T> {
    public CageBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    public void renderMob(MobHolder mobHolder, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, Direction dir){
        Entity mob = mobHolder.mob;
        if(mob!=null) {
            matrixStackIn.pushPose();
            float y = mobHolder.yOffset + MathHelper.lerp(partialTicks,mobHolder.prevJumpY,mobHolder.jumpY);
            float s = mobHolder.scale;

            matrixStackIn.translate(0.5, y,0.5);
            matrixStackIn.mulPose(dir.getRotation());
            matrixStackIn.mulPose(Const.XN90);
            matrixStackIn.scale(s,s,s);
            Minecraft.getInstance().getEntityRenderDispatcher().render(mob, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, matrixStackIn, bufferIn, combinedLightIn);
            matrixStackIn.popPose();
        }
    }

    @Override
    public void render(T tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        this.renderMob(tile.getMobHolder(), partialTicks, matrixStackIn, bufferIn, combinedLightIn, tile.getDirection());
    }
}

