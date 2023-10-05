package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.JarredHeadLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.PickleData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class PicklePacket implements Message {

    protected UUID playerID;
    protected final boolean on;
    private final boolean isJar;

    public PicklePacket(UUID appliesTo, boolean on, boolean isJar) {
        this.playerID = appliesTo;
        this.on = on;
        this.isJar = isJar;
    }

    public PicklePacket(FriendlyByteBuf buf) {
        this.on = buf.readBoolean();
        this.isJar = buf.readBoolean();
        if (buf.isReadable()) {
            this.playerID = buf.readUUID();
        }
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBoolean(this.on);
        buf.writeBoolean(this.isJar );
        if (this.playerID != null) {
            buf.writeUUID(this.playerID);
        }
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        if (context.getDirection() == NetworkDir.PLAY_TO_CLIENT) {
            PickleData.set(this.playerID, this.on, isJar);
        } else {
            //gets id from server just to be sure
            Player player = context.getSender();
            UUID id = player.getGameProfile().getId();
            if (PickleData.isDev(id, isJar)) { //validate if it is indeed a dev

                //stores value server side
                PickleData.set(id, this.on, this.isJar);
                this.playerID = id;
                //broadcast to all players
                for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
                    if (p != player) {
                        NetworkHandler.CHANNEL.sendToClientPlayer(p, new PicklePacket(this.playerID, this.on, this.isJar));
                    }
                }
            }
        }
    }

}

