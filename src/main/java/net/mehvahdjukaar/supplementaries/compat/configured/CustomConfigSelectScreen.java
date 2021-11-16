package net.mehvahdjukaar.supplementaries.compat.configured;

import com.mrcrayfish.configured.client.screen.ConfigScreen;
import com.mrcrayfish.configured.client.screen.ModConfigSelectionScreen;
import com.mrcrayfish.configured.client.screen.widget.IconButton;
import com.mrcrayfish.configured.util.ConfigHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.fmlclient.ConfigGuiHandler;

import java.lang.reflect.Field;
import java.util.*;

public class CustomConfigSelectScreen extends ModConfigSelectionScreen {

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
            if (button.isHovered()) {
                if (ConfigScreen.isPlayingGame() && !ConfigHelper.isConfiguredInstalledOnServer()) {
                    CustomConfigSelectScreen.this.renderTooltip(matrixStack, this.font.split(new TranslatableComponent("configured.gui.not_installed"),
                            Math.max(CustomConfigSelectScreen.this.width / 2 - 43, 170)), mouseX, mouseY);
                }
            }
        });
    }
}
