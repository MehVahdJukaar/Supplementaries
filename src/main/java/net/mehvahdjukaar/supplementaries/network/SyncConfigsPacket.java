package net.mehvahdjukaar.supplementaries.network;


import com.electronwill.nightconfig.toml.TomlFormat;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.ByteArrayInputStream;
import java.util.function.Supplier;

public class SyncConfigsPacket {

    private final byte[] configData;

    public SyncConfigsPacket(PacketBuffer buf) {
        this.configData = buf.readByteArray();
    }

    public SyncConfigsPacket(final byte[] configFileData) {
        this.configData = configFileData;
    }

    public static void buffer(SyncConfigsPacket message, PacketBuffer buf) {
        buf.writeByteArray(message.configData);
    }
    //client
    public static void handler(SyncConfigsPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
                return;
            }

            //CONFIG.setConfig(TomlFormat.instance().createParser().parse()

            ServerConfigs.SERVER_SPEC.setConfig(TomlFormat.instance().createParser().parse(new ByteArrayInputStream(message.configData)));
            ServerConfigs.cached.refresh();
            Supplementaries.LOGGER.info("Synced Common configs");
        });
        context.setPacketHandled(true);
    }
}
