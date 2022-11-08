package net.mehvahdjukaar.supplementaries.configs.fabric;

import com.nhoryzon.mc.farmersdelight.block.TomatoBushCropBlock;
import net.mehvahdjukaar.supplementaries.integration.fabric.ModConfigSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.BeetrootBlock;

public class ConfigUtilsImpl {
    public static void openModConfigs() {
        Minecraft mc = Minecraft.getInstance();

        mc.setScreen(new ModConfigSelectScreen(Minecraft.getInstance().screen));
    }
}
