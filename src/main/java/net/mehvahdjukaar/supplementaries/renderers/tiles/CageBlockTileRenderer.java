package net.mehvahdjukaar.supplementaries.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.blocks.tiles.CageBlockTile;
import net.mehvahdjukaar.supplementaries.common.MobHolder;
import net.mehvahdjukaar.supplementaries.renderers.Const;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CageBlockTileRenderer extends TileEntityRenderer<CageBlockTile> {
    public CageBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(CageBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        MobHolder mobHolder = tile.mobHolder;
        Entity mob = mobHolder.mob;
        if(mob!=null) {
            matrixStackIn.push();
            float y = mobHolder.yOffset + MathHelper.lerp(partialTicks,mobHolder.prevJumpY,mobHolder.jumpY);
            float s = mobHolder.scale;

            matrixStackIn.translate(0.5, y,0.5);
            matrixStackIn.rotate(tile.getDirection().getRotation());
            matrixStackIn.rotate(Const.XN90);
            matrixStackIn.scale(s,s,s);
            Minecraft.getInstance().getRenderManager().renderEntityStatic(mob, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, matrixStackIn, bufferIn, combinedLightIn);
            matrixStackIn.pop();
        }
    }
}

