package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;


public class ClientBoundParticlePacket implements Message {

    public final Type type;
    @Nullable
    public final Vec3 pos;
    @Nullable
    public final Integer extraData;
    @Nullable
    public final Vec3 dir;

    public ClientBoundParticlePacket(FriendlyByteBuf buffer) {
        this.type = buffer.readEnum(Type.class);
        if (buffer.readBoolean()) {
            this.extraData = buffer.readInt();
        } else this.extraData = null;
        if (buffer.readBoolean()) {
            this.pos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        } else {
            this.pos = null;
        }
        if (buffer.readBoolean()) {
            this.dir = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        } else {
            this.dir = null;
        }
    }

    public ClientBoundParticlePacket(BlockPos pos, Type type) {
        this(Vec3.atCenterOf(pos), type);
    }

    public ClientBoundParticlePacket(Vec3 pos, Type type) {
        this(pos, type, null);
    }

    public ClientBoundParticlePacket(Vec3 pos, Type type, Integer extraData) {
        this(pos, type, extraData, null);
    }

    public ClientBoundParticlePacket(Vec3 pos, Type type, Integer extraData, @Nullable Vec3 direction) {
        this.pos = pos;
        this.type = type;
        this.extraData = extraData;
        this.dir = direction;
    }

    public ClientBoundParticlePacket(Entity entity, Type type) {
        this(entity, type, null);
    }

    public ClientBoundParticlePacket(Entity entity, Type type, Vec3 dir) {
        this.extraData = entity.getId();
        this.type = type;
        this.pos = null;
        this.dir = dir;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.type);
        if (extraData != null) {
            buffer.writeBoolean(true);
            buffer.writeInt(extraData);
        } else {
            buffer.writeBoolean(false);
        }
        if (pos != null) {
            buffer.writeBoolean(true);
            buffer.writeDouble(this.pos.x);
            buffer.writeDouble(this.pos.y);
            buffer.writeDouble(this.pos.z);
        } else {
            buffer.writeBoolean(false);
        }
        if (dir != null) {
            buffer.writeBoolean(true);
            buffer.writeDouble(this.dir.x);
            buffer.writeDouble(this.dir.y);
            buffer.writeDouble(this.dir.z);
        } else {
            buffer.writeBoolean(false);
        }
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        ClientReceivers.handleSpawnBlockParticlePacket(this);
    }

    public enum Type {
        BUBBLE_BLOW,
        BUBBLE_CLEAN,
        BUBBLE_CLEAN_ENTITY,
        BUBBLE_EAT,
        DISPENSER_MINECART,
        FLINT_BLOCK_IGNITE,
        WAX_ON,
        GLOW_ON,
        CONFETTI,
        CONFETTI_EXPLOSION,
        FEATHER,
        WRENCH_ROTATION,
        PEARL_TELEPORT
    }

}