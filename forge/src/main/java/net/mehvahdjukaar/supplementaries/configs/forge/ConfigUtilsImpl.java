package net.mehvahdjukaar.supplementaries.configs.forge;

import net.mehvahdjukaar.supplementaries.common.block.blocks.UrnBlock;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.forge.configured.ModConfigSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.FallingBlock;

public class ConfigUtilsImpl {
    public static void openModConfigs() {
        if (CompatHandler.CONFIGURED) {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new ModConfigSelectScreen(Minecraft.getInstance().screen));
        }
    }
}
