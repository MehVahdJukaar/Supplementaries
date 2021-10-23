package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.world.data.GlobeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;


public class SyncGlobeDataPacket {
    public GlobeData data;

    public SyncGlobeDataPacket(FriendlyByteBuf buffer) {
        this.data = new GlobeData();
        this.data.load(buffer.readNbt());
    }

    public SyncGlobeDataPacket(GlobeData data) {
        this.data = data;
    }

    public static void buffer(SyncGlobeDataPacket message, FriendlyByteBuf buffer) {
        buffer.writeNbt(message.data.save(new CompoundTag()));
    }

    public static void handler(SyncGlobeDataPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (!context.getDirection().getReceptionSide().isServer()) {
                //assigns data to client

                GlobeData.setClientData(message.data);
                Supplementaries.LOGGER.info("Synced Globe data");
            }
        });
        context.setPacketHandled(true);
    }
}