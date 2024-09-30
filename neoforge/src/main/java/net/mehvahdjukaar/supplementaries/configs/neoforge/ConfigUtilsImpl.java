package net.mehvahdjukaar.supplementaries.configs.neoforge;

import net.mehvahdjukaar.moonlight.api.platform.forge.ClientHelperImpl;
import net.mehvahdjukaar.moonlight.api.platform.forge.PlatHelperImpl;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.neoforge.configured.ModConfigSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.registries.ForgeRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class ConfigUtilsImpl {
    public static void openModConfigs() {
        if (CompatHandler.CONFIGURED) {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new ModConfigSelectScreen(Minecraft.getInstance().screen));
        }
    }

}
