package net.mehvahdjukaar.supplementaries.integration.create;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.BallisticData;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CreateCompat;
import net.minecraft.core.BlockPos;
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

/**
 * Aim sync for a cannon riding inside a Create contraption. A contraption has no server-side block entity, so
 * {@link net.mehvahdjukaar.supplementaries.common.network.SyncCannonPacket} (which addresses the cannon by block pos)
 * can't be used; this packet carries the contraption entity id + local pos instead.
 */
public record SyncContraptionCannonPacket(
        UUID contraptionId, BlockPos localPos, Quaternionf localRot, byte firePower,
        boolean ignite, boolean stopControlling, Optional<BallisticData> ballisticData,
        @Nullable UUID userEntityId) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, SyncContraptionCannonPacket> CODEC =
            Message.makeType(Supplementaries.res("sync_contraption_cannon"), SyncContraptionCannonPacket::new);

    public SyncContraptionCannonPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readBlockPos(), ByteBufCodecs.QUATERNIONF.decode(buf), buf.readByte(),
                buf.readBoolean(), buf.readBoolean(), buf.readOptional(BallisticData.STREAM_CODEC),
                buf.readOptional(b -> b.readUUID()).orElse(null));
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(contraptionId);
        buf.writeBlockPos(localPos);
        ByteBufCodecs.QUATERNIONF.encode(buf, localRot);
        buf.writeByte(firePower);
        buf.writeBoolean(ignite);
        buf.writeBoolean(stopControlling);
        buf.writeOptional(ballisticData, BallisticData.STREAM_CODEC);
        buf.writeOptional(Optional.ofNullable(userEntityId), (b, value) -> b.writeUUID(value));
    }

    @Override
    public void handle(Context context) {
        Level level = context.getPlayer().level();
        if (level.isClientSide) {
            applyToRenderBlockEntity(level);
        } else if (level instanceof ServerLevel sl) {
            relayAndPersist(sl);
        }
    }

    private void applyToRenderBlockEntity(Level level) {
        Entity contraption = CreateCompat.findContraption(level, contraptionId);
        if (contraption == null) return;
        BlockEntity be = CreateCompat.getClientBlockEntity(contraption, localPos);
        if (be instanceof CannonBlockTile cannon) {
            cannon.setTrustedInternalAttributes(localRot, firePower, ignite, null, ballisticData.orElse(null));
            if (stopControlling) cannon.setCurrentUser(null);
        }
    }

    private void relayAndPersist(ServerLevel level) {
        Entity contraption = CreateCompat.findContraption(level, contraptionId);
        if (contraption == null) return;
        CreateCompat.persistCannonAim(contraption, localPos, localRot, firePower);
        NetworkHelper.sendToAllClientPlayersInDefaultRange(level, contraption.blockPosition(),
                new SyncContraptionCannonPacket(contraptionId, localPos, localRot, firePower, ignite,
                        stopControlling, ballisticData, userEntityId));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
