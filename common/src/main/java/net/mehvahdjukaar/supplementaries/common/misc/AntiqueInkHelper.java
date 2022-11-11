package net.mehvahdjukaar.supplementaries.common.misc;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncAntiqueInk;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AntiqueInkHelper {

    private AntiqueInkHelper() {
    }

    public static boolean isEnabled() {
        return PlatformHelper.getPlatform().isForge() && RegistryConfigs.ANTIQUE_INK_ENABLED.get();
    }

    public static boolean toggleAntiqueInkOnSigns(Level world, Player player, ItemStack stack,
                                                  boolean newState, BlockPos pos, BlockEntity tile) {
        var cap = SuppPlatformStuff.getForgeCap(tile, IAntiqueTextProvider.class);

        boolean success = false;
        if(cap != null){
            if (cap.hasAntiqueInk() != newState) {
                cap.setAntiqueInk(newState);
                tile.setChanged();
                if (world instanceof ServerLevel serverLevel) {
                    NetworkHandler.CHANNEL.sendToAllClientPlayersInRange(serverLevel, pos,256,
                            new ClientBoundSyncAntiqueInk(pos, newState));
                }
                success = true;
            }
        }
        if (success) {
            if (newState) {
                world.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                world.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            if (!player.isCreative()) stack.shrink(1);
            return true;
        }
        return false;
    }

    public static void setAntiqueInk(BlockEntity tile, boolean ink) {
        var cap = SuppPlatformStuff.getForgeCap(tile, IAntiqueTextProvider.class);
        if (cap != null) {
            cap.setAntiqueInk(ink);
        }
    }
}
