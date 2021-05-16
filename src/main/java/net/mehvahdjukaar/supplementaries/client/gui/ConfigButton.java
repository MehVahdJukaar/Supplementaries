package net.mehvahdjukaar.supplementaries.client.gui;


import net.mehvahdjukaar.supplementaries.client.renderers.HSLColor;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.configured.ConfiguredCustomScreen;
import net.mehvahdjukaar.supplementaries.compat.quark.QuarkTooltipPlugin;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;

import java.util.Arrays;
import java.util.List;

public class ConfigButton extends Button {


    public ConfigButton(int x, int y) {
        super(x, y, 20, 20, new StringTextComponent("s"), ConfigButton::click);

    }

    @Override
    public int getFGColor() {
        return this.isHovered? HSLColor.getRainbowColorPost(3) : 0xFFAA00;
    }

    public static void click(Button button) {
        ConfiguredCustomScreen.openScreen();
    }

    public static void setupConfigButton(GuiScreenEvent.InitGuiEvent event){
        Screen gui = event.getGui();
        if (gui instanceof MainMenuScreen || gui instanceof IngameMenuScreen) {
            boolean isOnRight = !CompatHandler.quark || !QuarkTooltipPlugin.hasQButtonOnRight();
            List<String> targets = isOnRight ?
                    Arrays.asList(new TranslationTextComponent("menu.online").getString(),new TranslationTextComponent("menu.shareToLan").getString())
                    :Arrays.asList(new TranslationTextComponent("menu.options").getString(),new TranslationTextComponent("fml.menu.mods").getString());

            List<Widget> widgets = event.getWidgetList();

            for (Widget b : widgets) {
                if (targets.contains(b.getMessage().getString())) {
                    Button button = new ConfigButton(b.x + (isOnRight ? 102 : -24), b.y);
                    event.addWidget(button);
                    return;
                }
            }
        }
    }

}
