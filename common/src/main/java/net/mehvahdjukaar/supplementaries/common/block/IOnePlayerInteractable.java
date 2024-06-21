package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// Interface for blocks that can be edited by one player at a time
public interface IOnePlayerInteractable {

    void setPlayerWhoMayEdit(@Nullable UUID uuid);

    UUID getPlayerWhoMayEdit();

    //call before access
    default boolean isEditingPlayer(Player player) {
        Level level = ((BlockEntity) this).getLevel();
        BlockPos pos = ((BlockEntity) this).getBlockPos();
        if (level == null) return false;
        UUID uuid = this.getPlayerWhoMayEdit();
        if (uuid != null && playerIsTooFarAwayToEdit(level, pos, uuid)) {
            this.setPlayerWhoMayEdit(null);
            return false;
        }

        return uuid != null && uuid.equals(player.getUUID());
    }


    default boolean playerIsTooFarAwayToEdit(Level level, BlockPos pos, UUID uUID) {
        Player player = level.getPlayerByUUID(uUID);
        return player == null || player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 64.0;
    }

    default boolean isOtherPlayerEditing(Player player) {
        UUID uuid = this.getPlayerWhoMayEdit();
        return uuid != null && !uuid.equals(player.getUUID());
    }

    default boolean tryOpeningEditGui(ServerPlayer player, BlockPos pos) {
        if (Utils.mayBuild(player, pos) && !this.isOtherPlayerEditing(player)) {
            // open gui (edit sign with empty hand)
            this.setPlayerWhoMayEdit(player.getUUID());

            if (this instanceof IScreenProvider sp) {
                sp.sendOpenGuiPacket(player.level(), pos, player);
                return false;
            }
            if (this instanceof MenuProvider mp) {
                PlatHelper.openCustomMenu(player, mp, pos);
                return true;
            }
        }
        return false;
    }
}
