package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ConfigUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ServerBoundRequestConfigReloadPacket() implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundRequestConfigReloadPacket> CODEC = Message.makeType(
            Supplementaries.res("c2s_request_config_reload"), ServerBoundRequestConfigReloadPacket::new);

    public ServerBoundRequestConfigReloadPacket(RegistryFriendlyByteBuf buffer) {
        this();
    }

    @Override
    public void write(RegistryFriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void handle(Context context) {
        ConfigUtils.configScreenReload((ServerPlayer) context.getPlayer());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}