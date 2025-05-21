package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonAccess;
import net.mehvahdjukaar.supplementaries.common.entities.FallingUrnEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public record ServerBoundSyncCannonPacket(
        float yaw, float pitch, byte firePower, boolean fire, boolean stopControlling,
        TileOrEntityTarget target) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundSyncCannonPacket> CODEC = Message.makeType(
            Supplementaries.res("c2s_sync_cannon"), ServerBoundSyncCannonPacket::new);

    public ServerBoundSyncCannonPacket(FriendlyByteBuf buf) {
        this(buf.readFloat(), buf.readFloat(), buf.readByte(),
                buf.readBoolean(), buf.readBoolean(), TileOrEntityTarget.read(buf));
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeByte(this.firePower);
        buf.writeBoolean(this.fire);
        buf.writeBoolean(this.stopControlling);
        this.target.write(buf);
    }

    @Override
    public void handle(Context context) {

        // server world
        if (context.getPlayer() instanceof ServerPlayer player) {
            Level level = player.level();

            CannonAccess access = CannonAccess.find(player.level(), this.target);
            if (access != null) {
                var cannon = access.getCannon();
                if (cannon.isEditingPlayer(BlockPos.containing(access.getCannonPosition()), player)) {
                    cannon.setAttributes(this.yaw, this.pitch, this.firePower, this.fire, player, access);
                    cannon.setChanged();
                    if (stopControlling) {
                        cannon.setPlayerWhoMayEdit(null);
                    }
                    if (target.getPos() != null) {
                        //now update all clients
                        level.sendBlockUpdated(cannon.getBlockPos(), cannon.getBlockState(), cannon.getBlockState(), 3);
                    }//for boat?
                    return;
                }
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