package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.blocks.CopperLanternBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.OilLanternBlockTile;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.AttachFace;


public class OilLanternBlockTileRenderer extends EnhancedLanternBlockTileRenderer<OilLanternBlockTile> {
    public OilLanternBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(OilLanternBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        AttachFace face = tile.getBlockState().getValue(CopperLanternBlock.FACE);
        if(face==AttachFace.FLOOR)return;

        BlockState state = tile.getBlockState().getBlock().defaultBlockState().setValue(CopperLanternBlock.LIT,tile.getBlockState().getValue(CopperLanternBlock.LIT));
        this.renderLantern(tile,state,partialTicks,matrixStackIn,bufferIn,combinedLightIn,combinedOverlayIn,face==AttachFace.CEILING);


    }
}