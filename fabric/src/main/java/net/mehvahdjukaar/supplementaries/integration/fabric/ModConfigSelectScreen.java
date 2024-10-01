package net.mehvahdjukaar.supplementaries.integration.fabric;

import net.mehvahdjukaar.moonlight.api.client.gui.MediaButton;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigListScreen;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ModConfigSelectScreen extends FabricConfigListScreen {

    public ModConfigSelectScreen(Screen parent) {
        super(Supplementaries.MOD_ID, ModRegistry.GLOBE_ITEM.get().getDefaultInstance(),
                Component.literal("§6Supplementaries Configs"), ModTextures.CONFIG_BACKGROUND,
                parent, ClientConfigs.CONFIG_HOLDER, CommonConfigs.CONFIG_HOLDER);
    }

    @Override
    protected void addExtraButtons() {

        int y = this.height - 27;
        int centerX = this.width / 2;

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> this.minecraft.setScreen(this.parent))
                .bounds(centerX - 45, y, 90, 20).build());

        this.addRenderableWidget(MediaButton.patreon(this, centerX - 45 - 22, y,
                "https://www.patreon.com/user?u=53696377"));

        this.addRenderableWidget(MediaButton.koFi(this, centerX - 45 - 22 * 2, y,
                "https://ko-fi.com/mehvahdjukaar"));

        this.addRenderableWidget(MediaButton.curseForge(this, centerX - 45 - 22 * 3, y,
                "https://www.curseforge.com/minecraft/mc-mods/supplementaries"));

        this.addRenderableWidget(MediaButton.github(this, centerX - 45 - 22 * 4, y,
                "https://github.com/MehVahdJukaar/Supplementaries/wiki"));


        this.addRenderableWidget(MediaButton.discord(this, centerX + 45 + 2, y,
                "https://discord.com/invite/qdKRTDf8Cv"));

        this.addRenderableWidget(MediaButton.youtube(this, centerX + 45 + 2 + 22, y,
                "https://www.youtube.com/watch?v=LSPNAtAEn28&t=1s"));

        this.addRenderableWidget(MediaButton.twitter(this, centerX + 45 + 2 + 22 * 2, y,
                "https://twitter.com/Supplementariez?s=09"));

        this.addRenderableWidget(MediaButton.akliz(this, centerX + 45 + 2 + 22 * 3, y,
                "https://www.akliz.net/supplementaries"));

    }

}
