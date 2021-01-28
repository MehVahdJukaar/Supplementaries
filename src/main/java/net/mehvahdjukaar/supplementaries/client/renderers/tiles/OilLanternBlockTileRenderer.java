package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.blocks.OilLanternBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.EnhancedLanternBlockTile;
import net.mehvahdjukaar.supplementaries.block.tiles.OilLanternBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;


public class OilLanternBlockTileRenderer extends EnhancedLanternBlockTileRenderer<OilLanternBlockTile> {
    public OilLanternBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(OilLanternBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        AttachFace face = tile.getBlockState().get(OilLanternBlock.FACE);
        if(face==AttachFace.FLOOR)return;

        BlockState state = tile.getBlockState().getBlock().getDefaultState();
        this.renderLantern(tile,state,partialTicks,matrixStackIn,bufferIn,combinedLightIn,combinedOverlayIn,face==AttachFace.CEILING);




    }
}