package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.block.blocks.CopperLanternBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.OilLanternBlockTile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;


public class OilLanternBlockTileRenderer extends EnhancedLanternBlockTileRenderer<OilLanternBlockTile> {
    public OilLanternBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(OilLanternBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        AttachFace face = tile.getBlockState().getValue(CopperLanternBlock.FACE);
        if (face == AttachFace.FLOOR) return;

        BlockState state = tile.getBlockState().getBlock().defaultBlockState().setValue(CopperLanternBlock.LIT, tile.getBlockState().getValue(CopperLanternBlock.LIT));
        this.renderLantern(tile, state, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, face == AttachFace.CEILING);


    }
}