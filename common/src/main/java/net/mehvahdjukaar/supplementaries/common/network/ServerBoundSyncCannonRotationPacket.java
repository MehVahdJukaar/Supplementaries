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

public class ServerBoundSyncCannonRotationPacket implements Message {
    private final float yaw;
    private final float pitch;
    private final BlockPos pos;

    public ServerBoundSyncCannonRotationPacket(FriendlyByteBuf buf) {
        this.yaw = buf.readFloat();
        this.pitch = buf.readFloat();
        this.pos = buf.readBlockPos();
    }

    public ServerBoundSyncCannonRotationPacket(float yaw, float pitch, BlockPos pos) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.pos = pos;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
          buf.writeFloat(this.yaw);
            buf.writeFloat(this.pitch);
            buf.writeBlockPos(this.pos);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // server world
        ServerPlayer player = (ServerPlayer) Objects.requireNonNull(context.getSender());
        Level level = player.level();
        if (level.getBlockEntity(this.pos) instanceof CannonBlockTile cannon) {
            cannon.setYaw(this.yaw);
            cannon.setPitch(this.pitch);
        } else {
            Supplementaries.error(); //should not happen
        }
    }

    public enum Slot {
        MAIN_HAND,
        OFF_HAND,
        INVENTORY
    }
}