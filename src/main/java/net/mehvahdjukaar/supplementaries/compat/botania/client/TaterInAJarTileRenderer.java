package net.mehvahdjukaar.supplementaries.compat.botania.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.client.render.tile.RenderTileTinyPotato;
import vazkii.botania.common.block.tile.TileTinyPotato;

public class TaterInAJarTileRenderer extends RenderTileTinyPotato {

    public TaterInAJarTileRenderer(TileEntityRendererDispatcher manager) {
        super(manager);
    }

    @Override
    public void render(@NotNull TileTinyPotato potato, float partialTicks, MatrixStack ms, @NotNull IRenderTypeBuffer buffers, int light, int overlay) {
        ms.pushPose();
        ms.translate(0, 1 / 16f, 0);

        super.render(potato, partialTicks, ms, buffers, light, overlay);
        ms.popPose();
    }
}
