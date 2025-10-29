package net.mehvahdjukaar.supplementaries.configs.neoforge;

import net.mehvahdjukaar.supplementaries.common.block.blocks.LunchBoxBlock;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.neoforge.configured.ModConfigSelectScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.Tags;

public class ConfigUtilsImpl {

    public static void openModConfigs() {
        if (CompatHandler.CONFIGURED) {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new ModConfigSelectScreen(Minecraft.getInstance().screen));
        }
    }

}
