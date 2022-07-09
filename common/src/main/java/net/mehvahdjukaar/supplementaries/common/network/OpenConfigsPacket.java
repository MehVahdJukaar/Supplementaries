package net.mehvahdjukaar.supplementaries.common.network;


import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.configs.ConfigUtils;
import net.minecraft.network.FriendlyByteBuf;

public class OpenConfigsPacket implements Message {
    public OpenConfigsPacket(FriendlyByteBuf buffer) {
    }

    public OpenConfigsPacket() {
    }


    @Override
    public void writeToBuffer(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        ConfigUtils.openModConfigs();
    }

}