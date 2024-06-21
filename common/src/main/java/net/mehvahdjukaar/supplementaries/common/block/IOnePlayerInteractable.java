package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
        validateEditingPlayer();
        UUID uuid = this.getPlayerWhoMayEdit();
        return uuid != null && uuid.equals(player.getUUID());
    }

    default boolean isOtherPlayerEditing(Player player) {
        validateEditingPlayer();
        UUID uuid = this.getPlayerWhoMayEdit();
        return uuid != null && !uuid.equals(player.getUUID());
    }


    private void validateEditingPlayer() {
        Level level = ((BlockEntity) this).getLevel();
        BlockPos pos = ((BlockEntity) this).getBlockPos();
        if (level == null) {
            this.setPlayerWhoMayEdit(null);
            return;
        }
        UUID uuid = this.getPlayerWhoMayEdit();
        if (uuid == null) return;

        Player player = level.getPlayerByUUID(uuid);
        if (player == null || player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 64.0) {
            this.setPlayerWhoMayEdit(null);
        }
    }

    default boolean tryOpeningEditGui(ServerPlayer player, BlockPos pos, ItemStack stack) {
        if (Utils.mayPerformBlockAction(player, pos, stack) && !this.isOtherPlayerEditing(player)) {
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
