package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class ClientBoundParticlePacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundParticlePacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_particle"), ClientBoundParticlePacket::new);


    public final Kind type;
    @Nullable
    public final Vec3 pos;
    private final int @NotNull [] extraData;
    @Nullable
    public final Vec3 dir;

    public ClientBoundParticlePacket(RegistryFriendlyByteBuf buffer) {
        this.type = buffer.readEnum(Kind.class);
        if (buffer.readBoolean()) {
            this.extraData = new int[]{buffer.readInt()};
        } else this.extraData = new int[0];
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

    public ClientBoundParticlePacket(BlockPos pos, Kind type) {
        this(Vec3.atCenterOf(pos), type);
    }

    public ClientBoundParticlePacket(Vec3 pos, Kind type) {
        this(pos, type, null);
    }

    public ClientBoundParticlePacket(Vec3 pos, Kind type, Integer extraData) {
        this(pos, type, null, extraData);
    }

    public ClientBoundParticlePacket(Vec3 pos, Kind type, @Nullable Vec3 direction,  int ...extraData) {
        this.pos = pos;
        this.type = type;
        this.dir = direction;
        this.extraData = extraData;
    }

    public ClientBoundParticlePacket(Entity entity, Kind type) {
        this(entity, type, null);
    }

    public ClientBoundParticlePacket(Entity entity, Kind type, Vec3 dir) {
        this.extraData = new int[]{entity.getId()};
        this.type = type;
        this.pos = null;
        this.dir = dir;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(this.type);
            buffer.writeVarIntArray(this.extraData);
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
    public void handle(Context context) {
        ClientReceivers.handleSpawnBlockParticlePacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }

    public List<Integer> getExtraData() {
        return Arrays.stream(extraData).boxed().collect(Collectors.toList());
    }

    public Integer getFirstExtraData() {
        if (extraData != null && extraData.length > 0) {
            return extraData[0];
        }
        return null;
    }

    public enum Kind {
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
        PEARL_TELEPORT,
        BOMB_EXPLOSION
    }

}