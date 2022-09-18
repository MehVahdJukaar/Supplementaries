package net.mehvahdjukaar.supplementaries.configs.forge;

import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.forge.configured.ModConfigSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition;

public class ConfigUtilsImpl {
    public static void openModConfigs() {

        if (CompatHandler.configured) {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new ModConfigSelectScreen(Minecraft.getInstance().screen));
        }
    }
}
