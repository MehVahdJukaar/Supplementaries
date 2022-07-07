package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;


public class ClientBoundParticlePacket implements Message {
    public final Vec3 pos;
    public final EventType id;

    public ClientBoundParticlePacket(FriendlyByteBuf buffer) {
        this.pos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        this.id = buffer.readEnum(EventType.class);
    }

    public ClientBoundParticlePacket(BlockPos pos, EventType id) {
        this(Vec3.atCenterOf(pos), id);
    }

    public ClientBoundParticlePacket(Vec3 pos, EventType id) {
        this.pos = pos;
        this.id = id;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeDouble(this.pos.x);
        buffer.writeDouble(this.pos.y);
        buffer.writeDouble(this.pos.z);
        buffer.writeEnum(this.id);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        ClientReceivers.handleSpawnBlockParticlePacket(this);
    }

    public enum EventType {
        BUBBLE_BLOW,
        BUBBLE_CLEAN,
        DISPENSER_MINECART
    }

}