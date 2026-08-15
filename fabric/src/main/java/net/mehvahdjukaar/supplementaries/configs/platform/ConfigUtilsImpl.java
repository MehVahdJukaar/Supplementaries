package net.mehvahdjukaar.supplementaries.configs.platform;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;

public class ConfigUtilsImpl {

    public static void openModConfigs() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(ClientHelper.getMoonlightConfigScreen(Supplementaries.MOD_ID, mc.screen, ModTextures.CONFIG_BACKGROUND));
    }
}
