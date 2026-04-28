package net.mehvahdjukaar.supplementaries.configs.platform;

import net.mehvahdjukaar.supplementaries.integration.platform.ModConfigSelectScreen;
import net.minecraft.client.Minecraft;

public class ConfigUtilsImpl {

    public static void openModConfigs() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new ModConfigSelectScreen(Minecraft.getInstance().screen));
    }
}
