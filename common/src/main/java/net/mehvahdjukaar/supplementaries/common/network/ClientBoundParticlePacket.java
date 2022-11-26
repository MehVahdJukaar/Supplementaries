package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;


public class ClientBoundParticlePacket implements Message {

    public final EventType id;
    public final Vec3 pos;
    @Nullable
    public final Integer entityId;

    public ClientBoundParticlePacket(FriendlyByteBuf buffer) {
        this.id = buffer.readEnum(EventType.class);
        if (buffer.readBoolean()) {
            this.pos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            this.entityId = null;
        } else {
            this.entityId = buffer.readInt();
            this.pos = Vec3.ZERO;
        }
    }

    public ClientBoundParticlePacket(BlockPos pos, EventType id) {
        this(Vec3.atCenterOf(pos), id);
    }

    public ClientBoundParticlePacket(Vec3 pos, EventType id) {
        this.pos = pos;
        this.id = id;
        this.entityId = null;
    }

    public ClientBoundParticlePacket(Entity entity, EventType id) {
        this.entityId = entity.getId();
        this.id = id;
        this.pos = Vec3.ZERO;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {

        buffer.writeEnum(this.id);
        if (entityId != null) {
            buffer.writeBoolean(false);
            buffer.writeInt(entityId);
        } else {
            buffer.writeBoolean(true);
            buffer.writeDouble(this.pos.x);
            buffer.writeDouble(this.pos.y);
            buffer.writeDouble(this.pos.z);
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
        DISPENSER_MINECART
    }

}