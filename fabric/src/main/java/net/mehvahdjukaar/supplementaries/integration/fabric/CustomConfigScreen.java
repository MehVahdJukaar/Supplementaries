package net.mehvahdjukaar.supplementaries.integration.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.integration.fabric.ClothConfigListScreen;
import net.mehvahdjukaar.supplementaries.client.gui.widgets.LinkButton;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CustomConfigScreen extends ClothConfigListScreen {

    public static final ItemStack MAIN_ICON = new ItemStack(ModRegistry.GLOBE_ITEM.get());

    public CustomConfigScreen(Screen parent) {
        super(parent, Component.literal("\u00A76Supplementaries Configs"), Textures.CONFIG_BACKGROUND,
                ClientConfigs.CLIENT_SPEC, CommonConfigs.SERVER_SPEC, RegistryConfigs.REGISTRY_SPEC);
    }

    @Override
    protected void addExtraButtons() {

        ResourceLocation icons = Textures.MISC_ICONS_TEXTURE;

        int y = this.height - 27;
        int centerX = this.width / 2;

        this.addRenderableWidget(new Button(centerX - 45, y, 90, 20, CommonComponents.GUI_BACK, (button) -> this.minecraft.setScreen(this.parent)));

        LinkButton patreon = LinkButton.create(icons, this, centerX - 45 - 22, y, 3, 1,
                "https://www.patreon.com/user?u=53696377", "Support me on Patreon :D");

        LinkButton kofi = LinkButton.create(icons, this, centerX - 45 - 22 * 2, y, 2, 2,
                "https://ko-fi.com/mehvahdjukaar", "Donate a Coffe");

        LinkButton curseforge = LinkButton.create(icons, this, centerX - 45 - 22 * 3, y, 1, 2,
                "https://www.curseforge.com/minecraft/mc-mods/supplementaries", "CurseForge Page");

        LinkButton github = LinkButton.create(icons, this, centerX - 45 - 22 * 4, y, 0, 2,
                "https://github.com/MehVahdJukaar/Supplementaries/wiki", "Mod Wiki");


        LinkButton discord = LinkButton.create(icons, this, centerX + 45 + 2, y, 1, 1,
                "https://discord.com/invite/qdKRTDf8Cv", "Mod Discord");

        LinkButton youtube = LinkButton.create(icons, this, centerX + 45 + 2 + 22, y, 0, 1,
                "https://www.youtube.com/watch?v=LSPNAtAEn28&t=1s", "Youtube Channel");

        LinkButton twitter = LinkButton.create(icons, this, centerX + 45 + 2 + 22 * 2, y, 2, 1,
                "https://twitter.com/Supplementariez?s=09", "Twitter Page");

        LinkButton akliz = LinkButton.create(icons, this, centerX + 45 + 2 + 22 * 3, y, 3, 2,
                "https://www.akliz.net/supplementaries", "Need a server? Get one with Akliz");


        this.addRenderableWidget(kofi);
        this.addRenderableWidget(akliz);
        this.addRenderableWidget(patreon);
        this.addRenderableWidget(curseforge);
        this.addRenderableWidget(discord);
        this.addRenderableWidget(youtube);
        this.addRenderableWidget(github);
        this.addRenderableWidget(twitter);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);

        if (isMouseWithin((this.width / 2) - 90, 2 + 6, 180, 16 + 2, mouseX, mouseY)) {
            this.renderTooltip(poseStack, this.font.split(Component.translatable("supplementaries.gui.info"), 200), mouseX, mouseY);
        }
        int titleWidth = this.font.width(this.title) + 35;
        this.itemRenderer.renderAndDecorateFakeItem(MAIN_ICON, (this.width / 2) + titleWidth / 2 - 17, 2 + 8);
        this.itemRenderer.renderAndDecorateFakeItem(MAIN_ICON, (this.width / 2) - titleWidth / 2, 2 + 8);
    }

    private boolean isMouseWithin(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(true)        return super.mouseClicked(mouseX, mouseY, button);
        if (isMouseWithin((this.width / 2) - 90, 2 + 6, 180, 16 + 2, (int) mouseX, (int) mouseY)) {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/supplementaries"));
            this.handleComponentClicked(style);
            return true;
        }
return false;
    }
}
