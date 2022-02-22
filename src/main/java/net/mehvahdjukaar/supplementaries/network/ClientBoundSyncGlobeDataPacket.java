package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.world.data.GlobeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientBoundSyncGlobeDataPacket {
    public GlobeData data;

    public ClientBoundSyncGlobeDataPacket(FriendlyByteBuf buffer) {
        this.data = new GlobeData(buffer.readNbt());
    }

    public ClientBoundSyncGlobeDataPacket(GlobeData data) {
        this.data = data;
    }

    public static void buffer(ClientBoundSyncGlobeDataPacket message, FriendlyByteBuf buffer) {
        buffer.writeNbt(message.data.save(new CompoundTag()));
    }

    public static void handler(ClientBoundSyncGlobeDataPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                //assigns data to client
                GlobeData.setClientData(message.data);
                Supplementaries.LOGGER.info("Synced Globe data");
            }
        });
        context.setPacketHandled(true);
    }
}