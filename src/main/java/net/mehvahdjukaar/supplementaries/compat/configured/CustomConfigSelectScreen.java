package net.mehvahdjukaar.supplementaries.compat.configured;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.configured.client.screen.ConfigScreen;
import com.mrcrayfish.configured.client.screen.ModConfigSelectionScreen;
import com.mrcrayfish.configured.client.screen.widget.IconButton;
import com.mrcrayfish.configured.client.util.ScreenUtil;
import com.mrcrayfish.configured.util.ConfigHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.*;

public class CustomConfigSelectScreen extends ModConfigSelectionScreen {

    public static final ResourceLocation ICONS_TEXTURES = Textures.MISC_ICONS_TEXTURE;

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

        container.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () ->
                new ConfigGuiHandler.ConfigGuiFactory((mc, screen) ->
                        new CustomConfigSelectScreen(screen, "\u00A76Supplementaries Configured",
                                Textures.CONFIG_BACKGROUND, modConfigMap)));
    }

    private static Map<ModConfig.Type, Set<ModConfig>> createConfigMap() {
        Map<ModConfig.Type, Set<ModConfig>> modConfigMap = new HashMap<>();
        Set<ModConfig> s = new HashSet<>();
        s.add(ConfigHandler.CLIENT_CONFIG_OBJECT);
        modConfigMap.put(ModConfig.Type.CLIENT, s);
        Set<ModConfig> s1 = new HashSet<>();
        s1.add(ConfigHandler.REGISTRY_CONFIG_OBJECT);
        s1.add(ConfigHandler.SERVER_CONFIG_OBJECT);
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
        if (label.contains("Common")) return ConfigHandler.SERVER_CONFIG_OBJECT;
        if (label.contains("Client")) return ConfigHandler.CLIENT_CONFIG_OBJECT;
        return ConfigHandler.REGISTRY_CONFIG_OBJECT;
    }

    private Button createModifyButton(ModConfig config) {
        String langKey = "configured.gui.modify";
        return new IconButton(0, 0, 33, 0, 60, new TranslatableComponent(langKey), (onPress) -> {
            Minecraft.getInstance().setScreen(new CustomConfigScreen(CustomConfigSelectScreen.this,
                    new TextComponent("\u00A76Supplementaries Configured"),
                    config, CustomConfigSelectScreen.this.background));
        }, (button, matrixStack, mouseX, mouseY) -> {
            if (button.isHoveredOrFocused()) {
                if (ConfigScreen.isPlayingGame() && !ConfigHelper.isConfiguredInstalledOnServer()) {
                    CustomConfigSelectScreen.this.renderTooltip(matrixStack, this.font.split(new TranslatableComponent("configured.gui.not_installed"),
                            Math.max(CustomConfigSelectScreen.this.width / 2 - 43, 170)), mouseX, mouseY);
                }
            }
        });
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);

        if (ScreenUtil.isMouseWithin((this.width / 2) - 90, 2, 180, 16, mouseX, mouseY)) {
            this.renderTooltip(poseStack, this.font.split(new TranslatableComponent("supplementaries.gui.info"), 200), mouseX, mouseY);
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

        ButtonIcon patreon = ButtonIcon.linkButton(this, centerX - 45 - 22, y, 3, 1,
                getLink("https://www.patreon.com/user?u=53696377"), "Support me on Patreon :D");

        ButtonIcon kofi = ButtonIcon.linkButton(this, centerX - 45 - 22 * 2, y, 2, 2,
                getLink("https://ko-fi.com/mehvahdjukaar"), "Donate a Coffe");

        ButtonIcon curseforge = ButtonIcon.linkButton(this, centerX - 45 - 22 * 3, y, 1, 2,
                getLink("https://www.curseforge.com/minecraft/mc-mods/supplementaries"), "CurseForge Page");

        ButtonIcon github = ButtonIcon.linkButton(this, centerX - 45 - 22 * 4, y, 0, 2,
                getLink("https://github.com/MehVahdJukaar/Supplementaries/wiki"), "Mod Wiki");


        ButtonIcon discord = ButtonIcon.linkButton(this, centerX + 45 + 2, y, 1, 1,
                getLink("https://discord.com/invite/qdKRTDf8Cv"), "Mod Discord");

        ButtonIcon youtube = ButtonIcon.linkButton(this, centerX + 45 + 2 + 22, y, 0, 1,
                getLink("https://www.youtube.com/watch?v=LSPNAtAEn28&t=1s"), "Youtube Channel");

        ButtonIcon twitter = ButtonIcon.linkButton(this, centerX + 45 + 2 + 22 * 2, y, 2, 1,
                getLink("https://twitter.com/Supplementariez?s=09"), "Twitter Page");

        ButtonIcon akliz = ButtonIcon.linkButton(this, centerX + 45 + 2 + 22 * 3, y, 3, 2,
                getLink("https://www.akliz.net/supplementaries"), "Need a server? Get one with Akliz");


        this.addRenderableWidget(kofi);
        this.addRenderableWidget(akliz);
        this.addRenderableWidget(patreon);
        this.addRenderableWidget(curseforge);
        this.addRenderableWidget(discord);
        this.addRenderableWidget(youtube);
        this.addRenderableWidget(github);
        this.addRenderableWidget(twitter);

    }

    public String getLink(String original) {
        return CommonUtil.FESTIVITY.isAprilsFool() ? "https://www.youtube.com/watch?v=dQw4w9WgXcQ" : original;
    }

    public static class ButtonIcon extends Button {
        private static final int ICON_WIDTH = 14;

        private final Component label;
        private final int u;
        private final int v;

        public static ButtonIcon linkButton(Screen parent, int x, int y, int uInd, int vInd, String url, String tooltip) {
            OnPress onPress = (op) -> {
                Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                parent.handleComponentClicked(style);
            };
            OnTooltip onTooltip = (button, poseStack, mouseX, mouseY) -> {
                if (button.isHoveredOrFocused()) {
                    parent.renderTooltip(poseStack, parent.getMinecraft().font.split(
                            new TextComponent(tooltip), Math.max(parent.width / 2 - 43, 170)), mouseX, mouseY);
                }
            };
            return new ButtonIcon(x, y, uInd * ICON_WIDTH, vInd * ICON_WIDTH, 20, TextComponent.EMPTY, onPress, onTooltip);
        }

        public ButtonIcon(int x, int y, int u, int v, int width, Component label, OnPress onPress, OnTooltip onTooltip) {
            super(x, y, width, 20, TextComponent.EMPTY, onPress, onTooltip);
            this.label = label;
            this.u = u;
            this.v = v;
        }

        @Override
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            super.renderButton(poseStack, mouseX, mouseY, partialTicks);
            Minecraft mc = Minecraft.getInstance();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, ICONS_TEXTURES);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            int contentWidth = ICON_WIDTH + mc.font.width(this.label);
            int iconX = (int) (this.x + Math.ceil((this.width - contentWidth) / 2f));
            int iconY = (int) (this.y + Math.ceil((this.width - ICON_WIDTH) / 2f));
            float brightness = this.active ? 1.0F : 0.5F;
            RenderSystem.setShaderColor(brightness, brightness, brightness, this.alpha);
            blit(poseStack, iconX, iconY, this.getBlitOffset(), (float) this.u, (float) this.v, ICON_WIDTH, ICON_WIDTH, 64, 64);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            int textColor = this.getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24;
            drawString(poseStack, mc.font, this.label, iconX + 14, iconY + 1, textColor);
        }
    }
}
