package net.mehvahdjukaar.supplementaries.plugins.configured;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.configured.client.screen.ConfigScreen;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;

public class CustomConfiguredScreen extends ConfigScreen {

    public static void openScreen(Minecraft mc){
        mc.displayGuiScreen(new CustomConfiguredScreen(mc.currentScreen));
    }

    public CustomConfiguredScreen(Screen parent) {
        super(parent, "Supplementaries Configs", ClientConfigs.CLIENT_CONFIG, ServerConfigs.SERVER_CONFIG);
    }

    //I hope they'll add custmization here
    @Override
    public void renderBackground(MatrixStack matrixStack) {
    }

    @Override
    public void onClose() {
        super.onClose();
        PlayerEntity player = Minecraft.getInstance().player;


        ClientConfigs.cached.refresh();
        ServerConfigs.cached.refresh();
        //reload server values and get new ones with packet
        //this isn't working...
        //NetworkHandler.INSTANCE.sendToServer(new RequestConfigReloadPacket());

    }
}
