package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;


public class ClientBoundParticlePacket implements Message {

    public final EventType id;
    @Nullable
    public final Vec3 pos;
    @Nullable
    public final Integer extraData;

    public ClientBoundParticlePacket(FriendlyByteBuf buffer) {
        this.id = buffer.readEnum(EventType.class);
        if (buffer.readBoolean()) {
            this.extraData = buffer.readInt();
        } else this.extraData = null;
        if (buffer.readBoolean()) {
            this.pos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        } else {
            this.pos = null;
        }
    }

    public ClientBoundParticlePacket(BlockPos pos, EventType id) {
        this(Vec3.atCenterOf(pos), id);
    }

    public ClientBoundParticlePacket(Vec3 pos, EventType id) {
        this(pos, id, null);
    }

    public ClientBoundParticlePacket(Vec3 pos, EventType id, Integer extraData) {
        this.pos = pos;
        this.id = id;
        this.extraData = extraData;
    }

    public ClientBoundParticlePacket(Entity entity, EventType id) {
        this.extraData = entity.getId();
        this.id = id;
        this.pos = null;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.id);
        if (extraData != null) {
            buffer.writeBoolean(true);
            buffer.writeInt(extraData);
        } else {
            buffer.writeBoolean(true);
        }
        if (pos != null) {
            buffer.writeBoolean(true);
            buffer.writeDouble(this.pos.x);
            buffer.writeDouble(this.pos.y);
            buffer.writeDouble(this.pos.z);
        } else {
            buffer.writeBoolean(true);
        }
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        ClientReceivers.handleSpawnBlockParticlePacket(this);
    }

    public enum EventType {
        BUBBLE_BLOW,
        BUBBLE_CLEAN,
        BUBBLE_CLEAN_ENTITY,
        DISPENSER_MINECART,
        FLINT_BLOCK_IGNITE
    }

}