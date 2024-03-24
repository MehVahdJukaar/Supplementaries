package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class ServerBoundSyncCannonPacket implements Message {
    private final float yaw;
    private final float pitch;
    private final byte firePower;
    private final boolean fire;
    private final BlockPos pos;

    public ServerBoundSyncCannonPacket(FriendlyByteBuf buf) {
        this.yaw = buf.readFloat();
        this.pitch = buf.readFloat();
        this.pos = buf.readBlockPos();
        this.firePower = buf.readByte();
        this.fire = buf.readBoolean();
    }

    public ServerBoundSyncCannonPacket(float yaw, float pitch, byte firePower, boolean fire, BlockPos pos) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.pos = pos;
        this.firePower = firePower;
        this.fire = fire;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeBlockPos(this.pos);
        buf.writeByte(this.firePower);
        buf.writeBoolean(this.fire);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // server world
        ServerPlayer player = (ServerPlayer) Objects.requireNonNull(context.getSender());
        Level level = player.level();
        float maxDist = 7;
        // validate position. Anti cheat
        if(pos.distToCenterSqr(player.position()) > maxDist*maxDist){
            return;
        }
        if (level.getBlockEntity(this.pos) instanceof CannonBlockTile cannon) {
            cannon.syncAttributes(this.yaw, this.pitch, this.firePower, this.fire);
        } else {
            Supplementaries.error(); //should not happen
        }
    }

}