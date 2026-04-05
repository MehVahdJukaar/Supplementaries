package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Quaternionf;

public record SyncCannonPacket(
        Quaternionf rotation, byte firePower, boolean ignite, boolean stopControlling,
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
        buf.writeBoolean(this.ignite);
        buf.writeBoolean(this.stopControlling);
        this.target.write(buf);
    }

    @Override
    public void handle(Context context) {

        Player player = context.getPlayer();
        Level level = player.level();

        BlockEntity be = this.target.findTileOrContainedTile(level);
        if (be instanceof CannonBlockTile cannon) {
            if (cannon.canBeUsedBy(BlockPos.containing(cannon.getGlobalPosition(1)), player)) {
                cannon.setAttributes(this.rotation, this.firePower, this.ignite, player);
                cannon.setChanged();
                if (stopControlling) {
                    cannon.setCurrentUser(null);
                }
                if (!level.isClientSide) cannon.syncToClients(ignite);
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