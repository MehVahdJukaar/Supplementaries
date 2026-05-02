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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.Optional;
import java.util.UUID;

public record SyncCannonPacket(
        Quaternionf rotation, byte firePower, boolean ignite, boolean stopControlling,
        TileOrEntityTarget target, @Nullable UUID userEntityId) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, SyncCannonPacket> CODEC = Message.makeType(
            Supplementaries.res("c2s_sync_cannon"), SyncCannonPacket::new);

    public SyncCannonPacket(FriendlyByteBuf buf) {
        this(ByteBufCodecs.QUATERNIONF.decode(buf), buf.readByte(),
                buf.readBoolean(), buf.readBoolean(), TileOrEntityTarget.read(buf),
                buf.readOptional(buffer -> buffer.readUUID()).orElse(null));
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        ByteBufCodecs.QUATERNIONF.encode(buf, rotation);
        buf.writeByte(this.firePower);
        buf.writeBoolean(this.ignite);
        buf.writeBoolean(this.stopControlling);
        this.target.write(buf);
        buf.writeOptional(Optional.ofNullable(this.userEntityId), (buffer, value) ->
                buffer.writeUUID(value));
    }

    @Override
    public void handle(Context context) {

        Level level = context.getPlayer().level();


        BlockEntity be = this.target.findTileOrContainedTile(level);
        if (!(be instanceof CannonBlockTile cannon)) {
            Supplementaries.LOGGER.warn("Cannon not found: {}", this.target);
            return;
        }
        //trusted
        if (level.isClientSide) {
            cannon.setTrustedInternalAttributes(this.rotation, this.firePower, this.ignite, null);
            if (stopControlling) {
                cannon.setCurrentUser(null);
            }
        } else if (level instanceof ServerLevel sl) {
            Entity entity = null;

            if (this.userEntityId != null) {
                entity = sl.getEntity(userEntityId);

                if (entity == null) {
                    Supplementaries.error("Failed to find entity with id {} for cannon controlling", userEntityId);
                    return;
                }
            }

            if (entity == null || cannon.canBeUsedBy(BlockPos.containing(cannon.getGlobalPosition(1)), entity)) {
                cannon.setTrustedInternalAttributes(this.rotation, this.firePower, this.ignite, entity);
                cannon.setChanged();
                if (stopControlling) {
                    cannon.setCurrentUser(null);
                }
                cannon.syncToClients(ignite);
            } else {
                Supplementaries.LOGGER.warn("Entity {} tried to control cannon {} without permission", entity, this.target);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}