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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.function.Function;

public class PolytoneCompat {

    public static void setupClient() {
        VoxelVolumeApi.registerCallback(VoxelVolumeApi.Activation.ONLY_WITH_COLORED_LIGHT_PACKS, event -> {
            event.addBlockEntityCallback(ModRegistry.JAR_TILE.get(), (tile, cell) -> colorFromFluid(tile, JarBlockTile::getSoftFluidTank, cell));
            event.addBlockEntityCallback(ModRegistry.GOBLET_TILE.get(), (tile, cell) -> colorFromFluid(tile, GobletBlockTile::getSoftFluidTank, cell));
            event.addBlockEntityCallback(ModRegistry.FAUCET_TILE.get(), (tile, cell) -> colorFromFluid(tile, t -> t.tempFluidHolder, cell));
            event.addBlockLight(ModRegistry.CRYSTAL_DISPLAY.get(), 0xB25CFF);
            event.addBlockAlias(ModRegistry.SCONCE.get(), litAs(Blocks.TORCH));
            event.addBlockAlias(ModRegistry.SCONCE_WALL.get(), litAs(Blocks.WALL_TORCH));
            event.addBlockAlias(ModRegistry.SCONCE_LEVER.get(), litAs(Blocks.WALL_TORCH));
            event.addBlockAlias(ModRegistry.SCONCE_SOUL.get(), litAs(Blocks.SOUL_TORCH));
            event.addBlockAlias(ModRegistry.SCONCE_WALL_SOUL.get(), litAs(Blocks.SOUL_WALL_TORCH));
            event.addBlockLight(ModRegistry.SCONCE_GREEN.get(), 0x93F37C);
            event.addBlockLight(ModRegistry.SCONCE_WALL_GREEN.get(), 0x93F37C);
            event.addBlockAlias(ModRegistry.BLACKSTONE_LAMP.get(), Blocks.SOUL_TORCH);
            event.addBlockLight(ModRegistry.BLAZE_ROD_BLOCK.get(), 0xF3C34A);
            event.addBlockLight(ModRegistry.GROUPER.get(), 0x4FE0D0);
            event.addBlockLight(ModRegistry.END_STONE_LAMP.get(), 0xE8DCF5);
        });
    }

    private static Function<BlockState, BlockState> litAs(Block torch) {
        return s -> s.getValue(BlockStateProperties.LIT) ? torch.withPropertiesOf(s) : s;
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
