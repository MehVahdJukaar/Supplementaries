package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBallEntity;
import net.mehvahdjukaar.supplementaries.common.misc.explosion.BombExplosion;
import net.mehvahdjukaar.supplementaries.common.misc.explosion.CannonBallExplosion;
import net.mehvahdjukaar.supplementaries.common.misc.explosion.GunpowderExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ClientBoundExplosionPacket(Type type, Vec3 pos, float power, List<BlockPos> toBlow,
                                         @Nullable Vec3 knockback, int getId) implements Message {

    public static ClientBoundExplosionPacket bomb(BombExplosion expl, @Nullable Player player) {
        Vec3 pos = new Vec3(expl.x, expl.y, expl.z);
        return new ClientBoundExplosionPacket(Type.BOMB, pos, expl.radius, expl.getToBlow(),
                expl.getHitPlayers().get(player), expl.bombType().ordinal());
    }

    public static ClientBoundExplosionPacket cannonball(CannonBallExplosion expl, CannonBallEntity source) {
        Vec3 pos = new Vec3(expl.x, expl.y, expl.z);
        return new ClientBoundExplosionPacket(Type.CANNONBALL, pos, expl.radius, expl.getToBlow(), source.getDeltaMovement(), source.getId());
    }

    public static ClientBoundExplosionPacket gunpowder(GunpowderExplosion expl) {
        Vec3 pos = new Vec3(expl.x, expl.y, expl.z);
        return new ClientBoundExplosionPacket(Type.GUNPOWDER, pos, expl.radius, expl.getToBlow(), null, -1);
    }


    public static ClientBoundExplosionPacket fromBuffer(FriendlyByteBuf buffer) {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();

        var power = buffer.readFloat();
        int i = Mth.floor(x);
        int j = Mth.floor(y);
        int k = Mth.floor(z);
        var toBlow = buffer.readList((friendlyByteBuf) -> {
            int l = friendlyByteBuf.readByte() + i;
            int m = friendlyByteBuf.readByte() + j;
            int n = friendlyByteBuf.readByte() + k;
            return new BlockPos(l, m, n);
        });
        Vec3 knockback;
        if (buffer.readBoolean()) {
            knockback = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        } else {
            knockback = null;
        }
        Type type = buffer.readEnum(Type.class);
        int id = buffer.readVarInt();
        return new ClientBoundExplosionPacket(type, new Vec3(x, y, z), power, toBlow, knockback, id);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeDouble(pos.x);
        buffer.writeDouble(pos.y);
        buffer.writeDouble(pos.z);
        buffer.writeFloat(this.power);
        int i = Mth.floor(pos.x);
        int j = Mth.floor(pos.y);
        int k = Mth.floor(pos.z);
        buffer.writeCollection(this.toBlow, (friendlyByteBuf, blockPos) -> {
            int l = blockPos.getX() - i;
            int m = blockPos.getY() - j;
            int n = blockPos.getZ() - k;
            friendlyByteBuf.writeByte(l);
            friendlyByteBuf.writeByte(m);
            friendlyByteBuf.writeByte(n);
        });
        buffer.writeBoolean(knockback != null);
        if (knockback != null) {
            buffer.writeDouble(knockback.x);
            buffer.writeDouble(knockback.y);
            buffer.writeDouble(knockback.z);
        }
        buffer.writeEnum(type);
        buffer.writeVarInt(getId);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        ClientReceivers.handleExplosionPacket(this);
    }

    public enum Type {
        BOMB,
        CANNONBALL,
        GUNPOWDER
    }
}
