package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.PickleData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public abstract class PicklePacket implements Message {

    protected UUID playerID;
    protected final boolean on;

    private PicklePacket(UUID appliesTo, boolean on) {
        this.playerID = appliesTo;
        this.on = on;
    }

    private PicklePacket(FriendlyByteBuf buf) {
        this.on = buf.readBoolean();
        if (buf.isReadable()) {
            this.playerID = buf.readUUID();
        }
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBoolean(this.on);
        if (this.playerID != null) {
            buf.writeUUID(this.playerID);
        }
    }

    public static class ServerBound extends PicklePacket {

        public ServerBound(UUID appliesTo, boolean on) {
            super(appliesTo, on);
        }

        public ServerBound(FriendlyByteBuf buf) {
            super(buf);
        }

        @Override
        public void handle(ChannelHandler.Context context) {
            //gets id from server just to be sure
            Player player = context.getSender();
            UUID id = player.getGameProfile().getId();
            if (PickleData.isDev(id)) { //validate if it is indeed a dev

                //stores value server side
                PickleData.set(id, this.on);
                this.playerID = id;
                //broadcast to all players
                for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
                    if (p != player) {
                        NetworkHandler.CHANNEL.sendToClientPlayer(p, new ClientBound(this.playerID, this.on));
                    }
                }
            }
        }
    }

    public static class ClientBound extends PicklePacket {

        public ClientBound(UUID appliesTo, boolean on) {
            super(appliesTo, on);
        }

        public ClientBound(FriendlyByteBuf buf) {
            super(buf);
        }

        @Override
        public void handle(ChannelHandler.Context context) {
            //receive broadcasted message
            PickleData.set(this.playerID, this.on);
        }
    }
}

