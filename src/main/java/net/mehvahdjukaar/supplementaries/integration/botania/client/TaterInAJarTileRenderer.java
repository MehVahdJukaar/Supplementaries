package net.mehvahdjukaar.supplementaries.integration.botania.client;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.client.render.tile.RenderTileTinyPotato;
import vazkii.botania.common.block.tile.TileTinyPotato;

public class TaterInAJarTileRenderer extends RenderTileTinyPotato {


    public TaterInAJarTileRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(@NotNull TileTinyPotato potato, float partialTicks, PoseStack ms, @NotNull MultiBufferSource buffers, int light, int overlay) {
        ms.pushPose();
        ms.translate(0, 1 / 16f, 0);

        super.render(potato, partialTicks, ms, buffers, light, overlay);
        ms.popPose();
    }
}
