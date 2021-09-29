package net.mehvahdjukaar.supplementaries.client.gui;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.color.HSLColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;

import java.util.Arrays;
import java.util.List;

public class ConfigButton extends Button {


    public ConfigButton(int x, int y) {
        super(x, y, 20, 20, new StringTextComponent("s"), ConfigButton::click);

    }

    @Override
    public int getFGColor() {
        return this.isHovered ? HSLColor.getRainbowColorPost(3) : 0xFFAA00;
    }

    public static void click(Button button) {
        Minecraft mc = Minecraft.getInstance();

        mc.setScreen(ModList.get().getModContainerById(Supplementaries.MOD_ID).get()
                .getCustomExtension(ExtensionPoint.CONFIGGUIFACTORY).get()
                .apply(mc, mc.screen));

        //ConfiguredCustomScreen.openScreen();
    }

    public static void setupConfigButton(GuiScreenEvent.InitGuiEvent event) {
        Screen gui = event.getGui();
        if (gui instanceof MainMenuScreen || gui instanceof IngameMenuScreen) {
            boolean isOnRight = true;//!CompatHandler.quark || !QuarkPlugin.hasQButtonOnRight();
            List<String> targets = isOnRight ?
                    Arrays.asList(new TranslationTextComponent("menu.online").getString(), new TranslationTextComponent("fml.menu.modoptions").getString(), new TranslationTextComponent("menu.shareToLan").getString())
                    : Arrays.asList(new TranslationTextComponent("menu.options").getString(), new TranslationTextComponent("fml.menu.mods").getString());

            List<Widget> widgets = event.getWidgetList();

            for (Widget b : widgets) {
                String name = b.getMessage().getString();
                if (targets.contains(name)) {
                    Button button = new ConfigButton(b.x + (isOnRight ? 102 : -24), b.y);
                    event.addWidget(button);
                    return;
                }
            }
        }
    }

}
