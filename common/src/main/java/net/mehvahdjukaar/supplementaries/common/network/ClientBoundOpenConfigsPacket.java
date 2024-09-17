package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ConfigUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientBoundOpenConfigsPacket() implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundOpenConfigsPacket> CODEC = Message.makeType(
            Supplementaries.res("s2c_open_configs"), ClientBoundOpenConfigsPacket::new);

    public ClientBoundOpenConfigsPacket(RegistryFriendlyByteBuf buffer) {
        this();
    }

    @Override
    public void write(RegistryFriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void handle(Context context) {
        ConfigUtils.openModConfigs();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}