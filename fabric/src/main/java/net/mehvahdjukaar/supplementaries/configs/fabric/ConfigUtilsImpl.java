package net.mehvahdjukaar.supplementaries.configs.fabric;

import net.mehvahdjukaar.supplementaries.integration.fabric.ModConfigSelectScreen;
import net.minecraft.client.Minecraft;

public class ConfigUtilsImpl {

    public static void openModConfigs() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new ModConfigSelectScreen(Minecraft.getInstance().screen));
    }
}
