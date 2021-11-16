package net.mehvahdjukaar.supplementaries.compat.configured;

import com.mrcrayfish.configured.client.screen.ConfigScreen;
import com.mrcrayfish.configured.client.screen.ModConfigSelectionScreen;
import com.mrcrayfish.configured.client.screen.widget.IconButton;
import com.mrcrayfish.configured.util.ConfigHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;

import java.lang.reflect.Field;
import java.util.*;

public class CustomConfigSelectionScreen extends ModConfigSelectionScreen {
    public CustomConfigSelectionScreen(Screen parent, String displayName, ResourceLocation background, Map<ModConfig.Type, Set<ModConfig>> configMap) {
        super(parent, displayName, background, configMap);
    }

    public static void registerScreen() {
        ModContainer container = ModList.get().getModContainerById(Supplementaries.MOD_ID).get();
        Map<ModConfig.Type, Set<ModConfig>> modConfigMap = createConfigMap();
        container.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, screen) ->
                new CustomConfigSelectionScreen(screen, "\u00A76Supplementaries Configured",
                        Textures.CONFIG_BACKGROUND, modConfigMap));
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
        Field f = ObfuscationReflectionHelper.findField(FileItem.class,"modifyButton");
        for(Item i : entries){
            if(i instanceof FileItem){
                FileItem item  = (FileItem) i;
                try {
                    f.setAccessible(true);
                    f.set(i, createModifyButton(getConfigFromLabel(item.getLabel())));
                }
                catch (IllegalAccessException ignored) {
                    int a = 1;
                }
            }
        }
    }

    private ModConfig getConfigFromLabel(String label){
        if(label.contains("Common")) return ConfigHandler.SERVER_CONFIG_OBJECT;
        if(label.contains("Client")) return ConfigHandler.CLIENT_CONFIG_OBJECT;
        return ConfigHandler.REGISTRY_CONFIG_OBJECT;
    }

    private Button createModifyButton(ModConfig config) {
        String langKey = "configured.gui.modify";
        return new IconButton(0, 0, 33, 0, 60, new TranslationTextComponent(langKey), (onPress) -> {
            Minecraft.getInstance().setScreen(new CustomConfigScreen(CustomConfigSelectionScreen.this,
                    new StringTextComponent("\u00A76Supplementaries Configured"),
                    config, CustomConfigSelectionScreen.this.background));
        }, (button, matrixStack, mouseX, mouseY) -> {
            if (button.isHovered()) {
                if (ConfigScreen.isPlayingGame() && !ConfigHelper.isConfiguredInstalledOnServer()) {
                    CustomConfigSelectionScreen.this.renderTooltip(matrixStack, Minecraft.getInstance().font.split(new TranslationTextComponent("configured.gui.not_installed"), Math.max(CustomConfigSelectionScreen.this.width / 2 - 43, 170)), mouseX, mouseY);
                }
            }
        });
    }

}
