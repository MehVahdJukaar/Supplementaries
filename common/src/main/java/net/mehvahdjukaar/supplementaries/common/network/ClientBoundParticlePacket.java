package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;


public class ClientBoundParticlePacket implements Message {

    public final Type id;
    @Nullable
    public final Vec3 pos;
    @Nullable
    public final Integer extraData;
    @Nullable
    public final Vec3 dir;

    public ClientBoundParticlePacket(FriendlyByteBuf buffer) {
        this.id = buffer.readEnum(Type.class);
        if (buffer.readBoolean()) {
            this.extraData = buffer.readInt();
        } else this.extraData = null;
        if (buffer.readBoolean()) {
            this.pos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        } else {
            this.pos = null;
        }if(buffer.readBoolean()) {
            this.dir = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }else{
            this.dir = null;
        }
    }

    public ClientBoundParticlePacket(BlockPos pos, Type id) {
        this(Vec3.atCenterOf(pos), id);
    }

    public ClientBoundParticlePacket(Vec3 pos, Type id) {
        this(pos, id, null);
    }

    public ClientBoundParticlePacket(Vec3 pos, Type id, Integer extraData) {
        this.pos = pos;
        this.id = id;
        this.extraData = extraData;
        this.dir = null;
    }

    public ClientBoundParticlePacket(Vec3 pos, Type id, Integer extraData, @Nullable Vec3 direction) {
        this.pos = pos;
        this.id = id;
        this.extraData = extraData;
        this.dir = direction;
    }

    public ClientBoundParticlePacket(Entity entity, Type id) {
        this.extraData = entity.getId();
        this.id = id;
        this.pos = null;
        this.dir = null;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.id);
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
        DISPENSER_MINECART,
        FLINT_BLOCK_IGNITE,
        WAX_ON,
        CONFETTI,
        CONFETTI_EXPLOSION,
        FEATHER
    }

}