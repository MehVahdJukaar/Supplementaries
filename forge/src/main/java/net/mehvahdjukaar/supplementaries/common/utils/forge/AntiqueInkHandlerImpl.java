package net.mehvahdjukaar.supplementaries.common.utils.forge;

import net.mehvahdjukaar.supplementaries.common.capabilities.forge.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncAntiqueInk;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.concurrent.atomic.AtomicBoolean;

public class AntiqueInkHandlerImpl {
    public static boolean toggleAntiqueInkOnSigns(Level world, Player player, ItemStack stack, boolean newState, BlockPos pos, BlockEntity tile) {
        var cap = tile.getCapability(CapabilityHandler.ANTIQUE_TEXT_CAP);
        AtomicBoolean success = new AtomicBoolean(false);
        cap.ifPresent(c -> {
            if (c.hasAntiqueInk() != newState) {
                c.setAntiqueInk(newState);
                tile.setChanged();
                if (world instanceof ServerLevel serverLevel) {
                    NetworkHandler.CHANNEL.sendToAllClientPlayersInRange(serverLevel, pos,256,
                            new ClientBoundSyncAntiqueInk(pos, newState));
                }
                success.set(true);
            }
        });
        if (success.get()) {
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
        tile.getCapability(CapabilityHandler.ANTIQUE_TEXT_CAP).ifPresent(c -> c.setAntiqueInk(ink));
    }
}
