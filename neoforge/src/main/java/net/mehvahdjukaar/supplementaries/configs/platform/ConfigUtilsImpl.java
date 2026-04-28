package net.mehvahdjukaar.supplementaries.configs.platform;

import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.platform.configured.ModConfigSelectScreen;
import net.minecraft.client.Minecraft;

public class ConfigUtilsImpl {

    public static void openModConfigs() {
        if (CompatHandler.CONFIGURED) {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new ModConfigSelectScreen(Minecraft.getInstance().screen));
        }
    }

}
