package net.mehvahdjukaar.supplementaries.client;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;

public class LumiseneFluidRenderer {

    protected static final ResourceLocation UNDERWATER_TEXTURE = Supplementaries.res("textures/block/lumisene_underwater.png");
    protected static final ResourceLocation SINGLE_TEXTURE = Supplementaries.res("textures/block/lumisene_underwater.png");
    protected static final ResourceLocation[][] STILL_TEXTURES =
            Util.make(new ResourceLocation[4][4], (textures) -> {
                for (int x = 0; x < 4; x++) {
                    for (int y = 0; y < 4; y++) {
                        textures[x][y] = Supplementaries.res("textures/block/lumisene_" + x + y + ".png");
                    }
                }
            });

    public ResourceLocation getStillTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        int x = Math.floorMod(pos.getX(), 4);
        int y = Math.floorMod(pos.getZ(), 4);
        return STILL_TEXTURES[x][y];
    }
}
