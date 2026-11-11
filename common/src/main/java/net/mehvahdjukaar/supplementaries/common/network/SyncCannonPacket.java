package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.BallisticData;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Quaternionf;

import java.util.Optional;

public record SyncCannonPacket(
        Quaternionf localRot, byte firePower, boolean ignite, boolean stopControlling,
        Optional<BallisticData> ballisticData,
        TileOrEntityTarget target) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, SyncCannonPacket> CODEC = Message.makeType(
            Supplementaries.res("sync_cannon"), SyncCannonPacket::new);

    public SyncCannonPacket(FriendlyByteBuf buf) {
        this(ByteBufCodecs.QUATERNIONF.decode(buf), buf.readByte(),
                buf.readBoolean(), buf.readBoolean(),
                buf.readOptional(BallisticData.STREAM_CODEC),
                TileOrEntityTarget.read(buf));
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        ByteBufCodecs.QUATERNIONF.encode(buf, localRot);
        buf.writeByte(this.firePower);
        buf.writeBoolean(this.ignite);
        buf.writeBoolean(this.stopControlling);
        buf.writeOptional(this.ballisticData, BallisticData.STREAM_CODEC);
        this.target.write(buf);
    }

    @Override
    public void handle(Context context) {

        Level level = context.getPlayer().level();

        BlockPos targetPos = this.target.getPos();
        if (targetPos != null && !level.isLoaded(targetPos)) return;
        BlockEntity be = this.target.findTileOrContainedTile(level);
        if (!(be instanceof CannonBlockTile cannon)) {
            Supplementaries.LOGGER.warn("Cannon not found: {}", this.target);
            return;
        }
        //trusted
        if (level.isClientSide) {
            cannon.setTrustedInternalAttributes(this.localRot, this.firePower, this.ignite, null,
                    this.ballisticData.orElse(null));
            if (stopControlling) {
                cannon.setCurrentUser(null);
            }
        } else if (context.getPlayer() instanceof ServerPlayer player) {
            boolean badRotation = !this.localRot.isFinite() || this.localRot.lengthSquared() < 1.0E-6f;
            BlockPos cannonPos = BlockPos.containing(cannon.getGlobalPosition(1));

            if (!badRotation && cannon.isCloseEnoughToUse(player, cannonPos) && cannon.canBeUsedBy(cannonPos, player)) {
                cannon.setTrustedInternalAttributes(this.localRot, this.firePower, this.ignite, player, null);
                cannon.setChanged();
                if (stopControlling) {
                    cannon.setCurrentUser(null);
                    if (cannon.isRider(player) && !cannon.isFiring()) cannon.dismount();
                }
                cannon.syncToClients(ignite);
            } else {
                Supplementaries.LOGGER.warn("Player {} tried to control cannon {} without permission", player.getName().getString(), this.target);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}