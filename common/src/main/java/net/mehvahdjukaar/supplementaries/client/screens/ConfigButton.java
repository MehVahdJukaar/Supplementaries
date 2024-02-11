package net.mehvahdjukaar.supplementaries.client.screens;


import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ConfigUtils;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkClientCompat;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ConfigButton extends Button {

    public ConfigButton(int x, int y) {
        super(x, y, 20, 20, Component.literal("s"), ConfigButton::click, Button.DEFAULT_NARRATION);
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public int getFGColor() {
        return this.isHovered ? ColorHelper.getRainbowColorPost(3) : 0xFFAA00;
    }

    public static void click(Button button) {
        ConfigUtils.openModConfigs();
    }

    public static void setupConfigButton(Screen screen, List<? extends GuiEventListener> listeners, Consumer<GuiEventListener> adder) {
        if (screen instanceof TitleScreen || screen instanceof PauseScreen) {
            boolean isOnRight;
            if (CompatHandler.QUARK) {
                isOnRight = QuarkClientCompat.shouldHaveButtonOnRight();
            } else isOnRight = true;

            List<String> targets = isOnRight ?
                    Arrays.asList(Component.translatable("menu.online").getString(), Component.translatable("fml.menu.modoptions").getString(), Component.translatable("menu.shareToLan").getString())
                    : Arrays.asList(Component.translatable("menu.options").getString(), Component.translatable("fml.menu.mods").getString());

            for (GuiEventListener w : listeners) {
                if (w instanceof AbstractWidget b) {
                    String name = b.getMessage().getString();
                    if (targets.contains(name)) {
                        int spacing = 4;
                        GuiEventListener button = new ConfigButton(b.getX() + (isOnRight ? b.getWidth() + spacing : -20 - spacing),
                                b.getY() + ClientConfigs.General.CONFIG_BUTTON_Y_OFF.get());
                        adder.accept(button);
                        return;
                    }
                }
            }
        }
    }

}
