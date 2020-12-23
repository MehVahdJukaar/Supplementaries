package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.world.data.GlobeData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;


public class SyncGlobeDataPacket {
    public GlobeData data;
    public SyncGlobeDataPacket(PacketBuffer buffer) {
        this.data = new GlobeData() ;
        this.data.read(buffer.readCompoundTag());
    }

    public SyncGlobeDataPacket(GlobeData data) {
        this.data = data;
    }

    public static void buffer(SyncGlobeDataPacket message, PacketBuffer buffer) {
        buffer.writeCompoundTag(message.data.write(new CompoundNBT()));
    }

    public static void handler(SyncGlobeDataPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (!context.getDirection().getReceptionSide().isServer()) {
                //assigns data to client
                GlobeData.clientSide = message.data;
            }
        });
        context.setPacketHandled(true);
    }
}