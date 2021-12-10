package net.mehvahdjukaar.supplementaries.client.gui;

import net.mehvahdjukaar.supplementaries.network.ClientBoundOpenScreenPacket;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public interface IScreenProvider {

    void openScreen(Level level, BlockPos pos, Player player);

    default void sendOpenGuiPacket(Level level, BlockPos pos, Player player) {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new ClientBoundOpenScreenPacket(pos));
    }
}
