package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;

public record SyncCannonPacket(
        Quaternionf rotation, byte firePower, boolean fire, boolean stopControlling,
        TileOrEntityTarget target) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, SyncCannonPacket> CODEC = Message.makeType(
            Supplementaries.res("c2s_sync_cannon"), SyncCannonPacket::new);

    public SyncCannonPacket(FriendlyByteBuf buf) {
        this(ByteBufCodecs.QUATERNIONF.decode(buf), buf.readByte(),
                buf.readBoolean(), buf.readBoolean(), TileOrEntityTarget.read(buf));
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        ByteBufCodecs.QUATERNIONF.encode(buf, rotation);
        buf.writeByte(this.firePower);
        buf.writeBoolean(this.fire);
        buf.writeBoolean(this.stopControlling);
        this.target.write(buf);
    }

    @Override
    public void handle(Context context) {

        Player player = context.getPlayer();
        Level level = player.level();

        CannonAccess access = CannonAccess.find(level, this.target);
        if (access != null) {
            var cannon = access.getInternalCannon();
            if (cannon.canBeUsedBy(BlockPos.containing(access.getCannonGlobalPosition(1)), player)) {
                cannon.setAttributes(this.rotation, this.firePower, this.fire, player);
                cannon.setChanged();
                if (stopControlling) {
                    cannon.setCurrentUser(null);
                }
                if (!level.isClientSide) access.updateClients();
            } else {
                Supplementaries.LOGGER.warn("Player tried to control cannon {} without permission: {}", player.getName().getString(), this.target);
            }
        } else {
            Supplementaries.LOGGER.warn("Cannon not found for player {}: {}", player.getName().getString(), this.target);
        }
        // could happen if cannon is broken
        //Supplementaries.error(); //should not happen
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}