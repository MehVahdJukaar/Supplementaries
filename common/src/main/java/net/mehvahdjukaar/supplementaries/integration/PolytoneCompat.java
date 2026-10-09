package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.polytone.api.ResolvedPointLight;
import net.mehvahdjukaar.polytone.api.VoxelCell;
import net.mehvahdjukaar.polytone.api.VoxelVolumeApi;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GobletBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Function;

public class PolytoneCompat {

    public static void setupClient() {
        VoxelVolumeApi.registerCallback(VoxelVolumeApi.Activation.ONLY_WITH_COLORED_LIGHT_PACKS, event -> {
            event.addBlockLight(ModFluids.LUMISENE_BLOCK.get(),
                    blockState -> new ResolvedPointLight(0xfff9bfff, blockState.getLightEmission()));
            event.addBlockEntityCallback(ModRegistry.JAR_TILE.get(), (tile, cell) -> colorFromFluid(tile, JarBlockTile::getSoftFluidTank, cell));
            event.addBlockEntityCallback(ModRegistry.GOBLET_TILE.get(), (tile, cell) -> colorFromFluid(tile, GobletBlockTile::getSoftFluidTank, cell));
            event.addBlockEntityCallback(ModRegistry.FAUCET_TILE.get(), (tile, cell) -> colorFromFluid(tile, t -> t.tempFluidHolder, cell));
        });
    }

    //force refresh lights
    public static void markDirty(BlockPos pos) {
        VoxelVolumeApi.markDirty(pos);
    }

    private static <T extends BlockEntity> void colorFromFluid(T tile, Function<T, SoftFluidTank> tankGetter, VoxelCell cell) {
        SoftFluidTank tank = tankGetter.apply(tile);
        if (cell.lightLevel() == 0 || tank == null || tank.isEmpty()) return;
        cell.setLight(tank.getCachedParticleColor(tile.getLevel(), tile.getBlockPos()), cell.lightLevel());
    }
}
