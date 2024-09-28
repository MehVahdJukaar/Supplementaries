package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.EnderPearlBehavior;
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
    private final boolean stopControlling;

    public ServerBoundSyncCannonPacket(FriendlyByteBuf buf) {
        this.yaw = buf.readFloat();
        this.pitch = buf.readFloat();
        this.pos = buf.readBlockPos();
        this.firePower = buf.readByte();
        this.fire = buf.readBoolean();
        this.stopControlling = buf.readBoolean();
    }

    public ServerBoundSyncCannonPacket(float yaw, float pitch, byte firePower, boolean fire, BlockPos pos,
                                       boolean stopControlling) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.pos = pos;
        this.firePower = firePower;
        this.fire = fire;
        this.stopControlling = stopControlling;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeBlockPos(this.pos);
        buf.writeByte(this.firePower);
        buf.writeBoolean(this.fire);
        buf.writeBoolean(this.stopControlling);
    }

    @Override
    public void handle(ChannelHandler.Context context) {

        // server world
        if(context.getSender() instanceof ServerPlayer player) {
            Level level = player.level();

            if (level.getBlockEntity(this.pos) instanceof CannonBlockTile cannon && cannon.isEditingPlayer(player)) {
                cannon.setAttributes(this.yaw, this.pitch, this.firePower, this.fire, player);
                cannon.setChanged();
                if (stopControlling) {
                    cannon.setPlayerWhoMayEdit(null);
                }
                //now update all clients
                level.sendBlockUpdated(pos, cannon.getBlockState(), cannon.getBlockState(), 3);

                return;
            }
        }
        // could happen if cannon is broken
        //Supplementaries.error(); //should not happen
    }

}