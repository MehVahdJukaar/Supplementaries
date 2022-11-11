package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.CapturedMobHandler;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.DataDefinedCatchableMob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientBoundSyncCapturedMobsPacket implements Message {

    protected final Set<DataDefinedCatchableMob> mobMap;
    @Nullable
    protected final DataDefinedCatchableMob fish;

    public ClientBoundSyncCapturedMobsPacket(final Set<DataDefinedCatchableMob> mobMap, @Nullable DataDefinedCatchableMob fish) {
        this.mobMap = mobMap;
        this.fish = fish;
    }

    public ClientBoundSyncCapturedMobsPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.mobMap = new HashSet<>();
        for (int i = 0; i < size; i++) {
            CompoundTag tag = buf.readNbt();
            if (tag != null) {
                var r = DataDefinedCatchableMob.CODEC.parse(NbtOps.INSTANCE, tag);
                if (r.result().isPresent()) {
                    mobMap.add(r.result().get());
                }
            }
        }
        if (buf.readBoolean()) {
            CompoundTag tag = buf.readNbt();
            var r = DataDefinedCatchableMob.CODEC.parse(NbtOps.INSTANCE, tag);
            if (r.result().isPresent()) {
                this.fish = r.result().get();
            } else fish = null;
        } else fish = null;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        List<CompoundTag> tags = new ArrayList<>();
        for (var entry : this.mobMap) {
            var r = DataDefinedCatchableMob.CODEC.encodeStart(NbtOps.INSTANCE, entry);
            if (r.result().isPresent()) {
                tags.add((CompoundTag) r.result().get());
            }
        }
        buf.writeInt(tags.size());
        tags.forEach(buf::writeNbt);
        var r = DataDefinedCatchableMob.CODEC.encodeStart(NbtOps.INSTANCE, fish);
        if (r.result().isPresent()) {
            buf.writeBoolean(true);
            buf.writeNbt((CompoundTag) r.result().get());
        } else buf.writeBoolean(false);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        //client world
        CapturedMobHandler.acceptClientData(mobMap, fish);
        Supplementaries.LOGGER.info("Synced Captured Mobs settings");
    }

}
