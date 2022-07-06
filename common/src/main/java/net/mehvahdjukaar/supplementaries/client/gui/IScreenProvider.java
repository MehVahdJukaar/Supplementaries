package net.mehvahdjukaar.supplementaries.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundOpenScreenPacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IScreenProvider {

    @Environment(EnvType.CLIENT)
    void openScreen(Level level, BlockPos pos, Player player);

    default void sendOpenGuiPacket(Level level, BlockPos pos, Player player) {
        NetworkHandler.CHANNEL.sendToPlayerClient((ServerPlayer) player,
                new ClientBoundOpenScreenPacket(pos));
    }
}
