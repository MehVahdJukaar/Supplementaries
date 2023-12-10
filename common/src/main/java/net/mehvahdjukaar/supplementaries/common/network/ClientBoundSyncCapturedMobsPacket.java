package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.CapturedMobHandler;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.DataDefinedCatchableMob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientBoundSyncCapturedMobsPacket implements Message {

    protected final Set<DataDefinedCatchableMob> mobSet;
    @Nullable
    protected final DataDefinedCatchableMob fish;

    public ClientBoundSyncCapturedMobsPacket(final Set<DataDefinedCatchableMob> mobMap, @Nullable DataDefinedCatchableMob fish) {
        this.mobSet = mobMap;
        this.fish = fish;
    }

    public ClientBoundSyncCapturedMobsPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.mobSet = new HashSet<>();
        for (int i = 0; i < size; i++) {
            CompoundTag tag = buf.readNbt();
            if (tag != null) {
                var r = DataDefinedCatchableMob.CODEC.parse(NbtOps.INSTANCE, tag);
                if (r.result().isPresent()) {
                    mobSet.add(r.result().get());
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
        for (var entry : this.mobSet) {
            if (entry == null) {
                Supplementaries.LOGGER.error("Found a null captured mob property. How??");
                continue; //satefy check
            }
            var r = DataDefinedCatchableMob.CODEC.encodeStart(NbtOps.INSTANCE, entry);
            if (r.result().isPresent()) {
                tags.add((CompoundTag) r.result().get());
            }
        }
        buf.writeInt(tags.size());
        tags.forEach(buf::writeNbt);
        if (fish != null) {
            var r = DataDefinedCatchableMob.CODEC.encodeStart(NbtOps.INSTANCE, fish);
            if (r.result().isPresent()) {
                buf.writeBoolean(true);
                buf.writeNbt((CompoundTag) r.result().get());
                return;
            }
        }
        buf.writeBoolean(false);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        //client world
        CapturedMobHandler.acceptClientData(mobSet, fish);
        Supplementaries.LOGGER.info("Synced Captured Mobs settings");
    }

}
