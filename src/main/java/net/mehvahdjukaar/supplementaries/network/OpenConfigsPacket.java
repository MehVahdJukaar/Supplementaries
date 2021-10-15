package net.mehvahdjukaar.supplementaries.network;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenConfigsPacket {
    public OpenConfigsPacket(FriendlyByteBuf buffer) {}
    public OpenConfigsPacket() {}

    public static void buffer(OpenConfigsPacket message, FriendlyByteBuf buf) {}

    public static void handler(OpenConfigsPacket message, Supplier<NetworkEvent.Context> ctx) {
        // client world
        ctx.get().enqueueWork(() -> {

            Minecraft mc = Minecraft.getInstance();

            //FileConfig f = FileConfig.of(ConfigHandler.getServerConfigPath());
            //ServerConfigs.SERVER_CONFIG.getSpec().apply(ConfigHandler.getServerConfigPath().toString());
            //ServerConfigs.SERVER_CONFIG.getSpec().apply(ConfigHandler.getServerConfigPath().toString());
            //ServerConfigs.SERVER_CONFIG.save();

            ServerConfigs.loadLocal();

            //if(configured)ConfiguredCustomScreen.openScreen();

            mc.setScreen(ModList.get().getModContainerById(Supplementaries.MOD_ID).get()
                    .getCustomExtension(ExtensionPoint.CONFIGGUIFACTORY).get()
                    .apply(mc,mc.screen));


        });
        ctx.get().setPacketHandled(true);
    }
}