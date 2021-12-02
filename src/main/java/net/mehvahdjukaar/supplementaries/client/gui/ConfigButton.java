package net.mehvahdjukaar.supplementaries.client.gui;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.quark.QuarkPlugin;
import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fmlclient.ConfigGuiHandler;

import java.util.Arrays;
import java.util.List;

public class ConfigButton extends Button {

    public ConfigButton(int x, int y) {
        super(x, y, 20, 20, new TextComponent("s"), ConfigButton::click);
    }

    @Override
    public int getFGColor() {
        return this.isHovered ? ColorHelper.getRainbowColorPost(3) : 0xFFAA00;
    }

    public static void click(Button button) {
        ConfigHandler.openModConfigs();
        //ConfiguredCustomScreen.openScreen();
    }

    public static void setupConfigButton(GuiScreenEvent.InitGuiEvent event) {
        Screen gui = event.getGui();
        if (gui instanceof TitleScreen || gui instanceof PauseScreen) {
            boolean isOnRight = CompatHandler.quark || !QuarkPlugin.hasQButtonOnRight();
            List<String> targets = isOnRight ?
                    Arrays.asList(new TranslatableComponent("menu.online").getString(), new TranslatableComponent("fml.menu.modoptions").getString(), new TranslatableComponent("menu.shareToLan").getString())
                    : Arrays.asList(new TranslatableComponent("menu.options").getString(), new TranslatableComponent("fml.menu.mods").getString());

            List<GuiEventListener> widgets = event.getWidgetList();

            for (GuiEventListener w : widgets) {
                if(w instanceof AbstractWidget b) {
                    String name = b.getMessage().getString();
                    if (targets.contains(name)) {
                        int spacing = 4;
                        Button button = new ConfigButton(b.x + (isOnRight ? b.getWidth() + spacing : -20 - spacing), b.y);
                        event.addWidget(button);
                        return;
                    }
                }
            }
        }
    }

}
