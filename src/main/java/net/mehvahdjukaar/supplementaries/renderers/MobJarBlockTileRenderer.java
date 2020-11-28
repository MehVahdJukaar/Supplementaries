package net.mehvahdjukaar.supplementaries.renderers;


import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.blocks.MobJarBlockTile;
import net.mehvahdjukaar.supplementaries.blocks.WindVaneBlock;
import net.mehvahdjukaar.supplementaries.blocks.WindVaneBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.VexEntity;
import net.minecraft.entity.passive.AmbientEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

@OnlyIn(Dist.CLIENT)
public class MobJarBlockTileRenderer extends TileEntityRenderer<MobJarBlockTile> {
    public MobJarBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(MobJarBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        matrixStackIn.push();

        Entity mob = tile.mob;
        if(mob!=null) {

            float y = tile.yOffset + MathHelper.lerp(partialTicks,tile.prevJumpY,tile.jumpY);
            float s = tile.scale;

            matrixStackIn.translate(0.5, y,0.5);
            matrixStackIn.rotate(tile.getDirection().getRotation());
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90));
            matrixStackIn.scale(s,s,s);
            Minecraft.getInstance().getRenderManager().renderEntityStatic(mob, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, matrixStackIn, bufferIn, combinedLightIn);
        }
        matrixStackIn.pop();
    }
}