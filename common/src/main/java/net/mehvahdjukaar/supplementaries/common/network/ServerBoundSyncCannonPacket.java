package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Objects;

public record ServerBoundSyncCannonPacket(
        float yaw, float pitch, byte firePower, boolean fire, BlockPos pos, boolean stopControlling
) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundSyncCannonPacket> CODEC = Message.makeType(
            Supplementaries.res("c2s_sync_cannon"), ServerBoundSyncCannonPacket::new);

    public ServerBoundSyncCannonPacket(FriendlyByteBuf buf) {
        this(buf.readFloat(), buf.readFloat(), buf.readByte(),
                buf.readBoolean(), buf.readBlockPos(), buf.readBoolean());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeByte(this.firePower);
        buf.writeBoolean(this.fire);
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.stopControlling);
    }

    @Override
    public void handle(Context context) {

        // server world
        if(context.getPlayer() instanceof ServerPlayer player) {
            Level level = player.level();

            if (level.getBlockEntity(this.pos) instanceof CannonBlockTile cannon &&
                    cannon.isEditingPlayer(player)) {
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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}