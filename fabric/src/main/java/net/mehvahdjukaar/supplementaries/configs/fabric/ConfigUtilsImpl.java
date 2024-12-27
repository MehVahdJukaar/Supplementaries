package net.mehvahdjukaar.supplementaries.configs.fabric;

import mezz.jei.api.constants.Tags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.mehvahdjukaar.supplementaries.integration.fabric.ModConfigSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;

public class ConfigUtilsImpl {

    public static void openModConfigs() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new ModConfigSelectScreen(Minecraft.getInstance().screen));
    }
}
