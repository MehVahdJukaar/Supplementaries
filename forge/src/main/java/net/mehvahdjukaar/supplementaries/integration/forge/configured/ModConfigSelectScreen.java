package net.mehvahdjukaar.supplementaries.integration.forge.configured;


import net.mehvahdjukaar.moonlight.api.client.gui.LinkButton;
import net.mehvahdjukaar.moonlight.api.integration.configured.CustomConfigSelectScreen;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public class ModConfigSelectScreen extends CustomConfigSelectScreen {


    public ModConfigSelectScreen(Screen parent) {
        super(Supplementaries.MOD_ID, ModRegistry.GLOBE_ITEM.get().getDefaultInstance(),
                "§6Supplementaries Configured", ModTextures.CONFIG_BACKGROUND,
                parent, ModConfigScreen::new, ClientConfigs.SPEC, CommonConfigs.SPEC);
    }


    @Override
    protected void init() {
        super.init();
        Button found = null;
        for (var c : this.children()) {
            if (c instanceof Button button) {
                if (button.getWidth() == 150) found = button;
            }
        }
        if (found != null) this.removeWidget(found);

        int y = this.height - 29;
        int centerX = this.width / 2;

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> this.minecraft.setScreen(this.parent))
                .bounds(centerX - 45, y, 90, 20).build());

        this.addRenderableWidget(LinkButton.create(this, centerX - 45 - 22, y, 3, 1,
                "https://www.patreon.com/user?u=53696377", "Support me on Patreon :D"));

        this.addRenderableWidget(LinkButton.create(this, centerX - 45 - 22 * 2, y, 2, 2,
                "https://ko-fi.com/mehvahdjukaar", "Donate a Coffee"));

        this.addRenderableWidget(LinkButton.create(this, centerX - 45 - 22 * 3, y, 1, 2,
                "https://www.curseforge.com/minecraft/mc-mods/supplementaries", "CurseForge Page"));

        this.addRenderableWidget(LinkButton.create(this, centerX - 45 - 22 * 4, y, 0, 2,
                "https://github.com/MehVahdJukaar/Supplementaries/wiki", "Mod Wiki"));


        this.addRenderableWidget(LinkButton.create(this, centerX + 45 + 2, y, 1, 1,
                "https://discord.com/invite/qdKRTDf8Cv", "Mod Discord"));

        this.addRenderableWidget(LinkButton.create(this, centerX + 45 + 2 + 22, y, 0, 1,
                "https://www.youtube.com/watch?v=LSPNAtAEn28&t=1s", "Youtube Channel"));

        this.addRenderableWidget(LinkButton.create(this, centerX + 45 + 2 + 22 * 2, y, 2, 1,
                "https://twitter.com/Supplementariez?s=09", "Twitter Page"));

        this.addRenderableWidget(LinkButton.create(this, centerX + 45 + 2 + 22 * 3, y, 3, 2,
                "https://www.akliz.net/supplementaries", "Need a server? Get one with Akliz"));


    }

}
