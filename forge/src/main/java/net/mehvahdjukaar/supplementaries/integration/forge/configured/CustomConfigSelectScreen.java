package net.mehvahdjukaar.supplementaries.integration.forge.configured;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.configured.client.screen.ConfigScreen;
import com.mrcrayfish.configured.client.screen.ModConfigSelectionScreen;
import com.mrcrayfish.configured.client.screen.widget.IconButton;
import com.mrcrayfish.configured.client.util.ScreenUtil;
import com.mrcrayfish.configured.util.ConfigHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.forge.ConfigSpecWrapper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.gui.widgets.LinkButton;
import net.mehvahdjukaar.supplementaries.common.ModTextures;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.*;

public class CustomConfigSelectScreen extends ModConfigSelectionScreen {

    public static final ResourceLocation ICONS_TEXTURES = ModTextures.MISC_ICONS_TEXTURE;

    static {
        Field temp = null;
        try {
            temp = ObfuscationReflectionHelper.findField(FileItem.class, "modifyButton");
        } catch (Exception ignored) {
        } finally {
            FILE_ITEM_BUTTON = temp;
        }
    }

    private static final Field FILE_ITEM_BUTTON;

    public CustomConfigSelectScreen(Screen parent, String displayName, ResourceLocation background, Map<ModConfig.Type, Set<ModConfig>> configMap) {
        super(parent, displayName, background, configMap);
    }

    public static void registerScreen() {

        ModContainer container = ModList.get().getModContainerById(Supplementaries.MOD_ID).get();
        Map<ModConfig.Type, Set<ModConfig>> modConfigMap = createConfigMap();

        container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory((mc, screen) ->
                        new CustomConfigSelectScreen(screen, "\u00A76Supplementaries Configured",
                                ModTextures.CONFIG_BACKGROUND, modConfigMap)));
    }

    private static Map<ModConfig.Type, Set<ModConfig>> createConfigMap() {
        Map<ModConfig.Type, Set<ModConfig>> modConfigMap = new HashMap<>();
        Set<ModConfig> s = new HashSet<>();
        s.add(((ConfigSpecWrapper) ClientConfigs.CLIENT_SPEC).getModConfig());
        modConfigMap.put(ModConfig.Type.CLIENT, s);
        Set<ModConfig> s1 = new HashSet<>();
        s1.add(((ConfigSpecWrapper) CommonConfigs.SERVER_SPEC).getModConfig());
        s1.add(((ConfigSpecWrapper) RegistryConfigs.REGISTRY_SPEC).getModConfig());
        modConfigMap.put(ModConfig.Type.COMMON, s1);
        return modConfigMap;
    }

    @Override
    protected void constructEntries(List<Item> entries) {
        super.constructEntries(entries);

        for (Item i : entries) {
            if (i instanceof FileItem item) {
                try {
                    FILE_ITEM_BUTTON.setAccessible(true);
                    FILE_ITEM_BUTTON.set(i, createModifyButton(getConfigFromLabel(item.getLabel())));
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    private ModConfig getConfigFromLabel(String label) {
        if (label.contains("Common")) return ((ConfigSpecWrapper) CommonConfigs.SERVER_SPEC).getModConfig();
        if (label.contains("Client")) return ((ConfigSpecWrapper) ClientConfigs.CLIENT_SPEC).getModConfig();
        return ((ConfigSpecWrapper) RegistryConfigs.REGISTRY_SPEC).getModConfig();
    }

    private Button createModifyButton(ModConfig config) {
        String langKey = "configured.gui.modify";
        return new IconButton(0, 0, 33, 0, 60, Component.translatable(langKey), (onPress) -> {
            Minecraft.getInstance().setScreen(new CustomConfigScreen(CustomConfigSelectScreen.this,
                    Component.literal("\u00A76Supplementaries Configured"),
                    config, CustomConfigSelectScreen.this.background));
        }, (button, matrixStack, mouseX, mouseY) -> {
            if (button.isHoveredOrFocused()) {
                if (ConfigScreen.isPlayingGame() && !ConfigHelper.isConfiguredInstalledOnServer()) {
                    CustomConfigSelectScreen.this.renderTooltip(matrixStack, this.font.split(Component.translatable("configured.gui.not_installed"),
                            Math.max(CustomConfigSelectScreen.this.width / 2 - 43, 170)), mouseX, mouseY);
                }
            }
        });
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);

        if (ScreenUtil.isMouseWithin((this.width / 2) - 90, 2, 180, 16, mouseX, mouseY)) {
            this.renderTooltip(poseStack, this.font.split(Component.translatable("supplementaries.gui.info"), 200), mouseX, mouseY);
        }
        int titleWidth = this.font.width(this.title) + 35;
        this.itemRenderer.renderAndDecorateFakeItem(CustomConfigScreen.MAIN_ICON, (this.width / 2) + titleWidth / 2 - 17, 2);
        this.itemRenderer.renderAndDecorateFakeItem(CustomConfigScreen.MAIN_ICON, (this.width / 2) - titleWidth / 2, 2);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ScreenUtil.isMouseWithin((this.width / 2) - 90, 2, 180, 16, (int) mouseX, (int) mouseY)) {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/supplementaries"));
            this.handleComponentClicked(style);
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
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

        this.addRenderableWidget(new Button(centerX - 45, y, 90, 20, CommonComponents.GUI_BACK, (button) -> this.minecraft.setScreen(this.parent)));

        LinkButton patreon = LinkButton.create(ICONS_TEXTURES, this, centerX - 45 - 22, y, 3, 1,
                "https://www.patreon.com/user?u=53696377", "Support me on Patreon :D");

        LinkButton kofi = LinkButton.create(ICONS_TEXTURES, this, centerX - 45 - 22 * 2, y, 2, 2,
                "https://ko-fi.com/mehvahdjukaar", "Donate a Coffe");

        LinkButton curseforge = LinkButton.create(ICONS_TEXTURES, this, centerX - 45 - 22 * 3, y, 1, 2,
                "https://www.curseforge.com/minecraft/mc-mods/supplementaries", "CurseForge Page");

        LinkButton github = LinkButton.create(ICONS_TEXTURES, this, centerX - 45 - 22 * 4, y, 0, 2,
                "https://github.com/MehVahdJukaar/Supplementaries/wiki", "Mod Wiki");


        LinkButton discord = LinkButton.create(ICONS_TEXTURES, this, centerX + 45 + 2, y, 1, 1,
                "https://discord.com/invite/qdKRTDf8Cv", "Mod Discord");

        LinkButton youtube = LinkButton.create(ICONS_TEXTURES, this, centerX + 45 + 2 + 22, y, 0, 1,
                "https://www.youtube.com/watch?v=LSPNAtAEn28&t=1s", "Youtube Channel");

        LinkButton twitter = LinkButton.create(ICONS_TEXTURES, this, centerX + 45 + 2 + 22 * 2, y, 2, 1,
                "https://twitter.com/Supplementariez?s=09", "Twitter Page");

        LinkButton akliz = LinkButton.create(ICONS_TEXTURES, this, centerX + 45 + 2 + 22 * 3, y, 3, 2,
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

}
