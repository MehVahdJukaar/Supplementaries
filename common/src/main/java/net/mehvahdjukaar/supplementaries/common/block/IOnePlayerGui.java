package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IOnePlayerGui extends IScreenProvider {

    void setPlayerWhoMayEdit(@Nullable UUID uuid);

    UUID getPlayerWhoMayEdit();


    //call before access
    default void validatePlayerWhoMayEdit(Level level, BlockPos pos) {
        UUID uuid = this.getPlayerWhoMayEdit();
        if (uuid != null) {
            Player player = level.getPlayerByUUID(uuid);
            if(player == null || player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 64.0){
                this.setPlayerWhoMayEdit(null);
            }
        }
    }

    default boolean isOtherPlayerEditing(Player player) {
        UUID uuid = this.getPlayerWhoMayEdit();
        return uuid != null && !uuid.equals(player.getUUID());
    }

    default boolean tryOpeningEditGui(Player player, BlockPos pos){
        if (Utils.mayBuild(player, pos) && !this.isOtherPlayerEditing(player)) {
            // open gui (edit sign with empty hand)
            this.setPlayerWhoMayEdit(player.getUUID());

            this.sendOpenGuiPacket(player.level(), pos, player);
            return true;
        }
        return false;
    }
}
