package net.mehvahdjukaar.supplementaries.fabric;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.common.block.tiles.TrappedPresentBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ForgeHelperImpl {

    public static boolean canEntityDestroy(Level level, BlockPos pos, Animal animal) {
        if (!level.isLoaded(pos)) {
            return false;
        } else {
            return PlatformHelper.isMobGriefingOn(level, animal);
        }
    }

    public static void openContainerScreen(ServerPlayer player, MenuProvider menuProvider, BlockPos pos) {
        //TODO: check this
        player.openMenu(menuProvider);
    }

    public static boolean onExplosionStart(Level level, Explosion explosion) {
        return true;
    }
}
