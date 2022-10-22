package net.mehvahdjukaar.supplementaries.common.capabilities.antique_ink;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.common.items.PancakeItem;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AntiqueInkProvider {

    private AntiqueInkProvider(){
    }

    public static boolean isEnabled() {
        return PlatformHelper.getPlatform().isForge() && RegistryConfigs.ANTIQUE_INK_ENABLED.get();
    }

    @ExpectPlatform
    public static boolean toggleAntiqueInkOnSigns(Level world, Player player, ItemStack stack, boolean newState, BlockPos pos, BlockEntity tile) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setAntiqueInk(BlockEntity tile, boolean ink) {
        throw new AssertionError();
    }
}
