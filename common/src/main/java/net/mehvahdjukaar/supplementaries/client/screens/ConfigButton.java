package net.mehvahdjukaar.supplementaries.client.screens;


import net.mehvahdjukaar.candlelight.api.VirtualOverride;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ConfigUtils;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ConfigButton extends Button {

    private static final int Z_OVER_OTHER_BUTTONS = 300;
    private static final int GEAR_SIZE = 16;
    private static final int ANCHOR_SPACING = 4;
    private static final int CORNER_MARGIN = 4;
    private static final int BUTTON_SIZE = 20;
    private static final int BOTTOM_TEXT_CLEARANCE = 12;

    private final boolean opensModList;

    public ConfigButton(int x, int y, boolean opensModList) {
        super(x, y, BUTTON_SIZE, BUTTON_SIZE, Component.literal("s"), b -> ((ConfigButton) b).open(), Button.DEFAULT_NARRATION);
        this.opensModList = opensModList;
    }

    private void open() {
        Minecraft mc = Minecraft.getInstance();
        if (this.opensModList) {
            Screen modList = ClientHelper.getModsListScreen(mc.screen, null);
            if (modList != null) {
                mc.setScreen(modList);
                return;
            }
        }
        ConfigUtils.openModConfigs();
    }

    public static void setupConfigButton(Screen screen, List<? extends GuiEventListener> listeners,
                                         Consumer<AbstractWidget> adder, Consumer<AbstractWidget> remover) {
        if (!(screen instanceof TitleScreen) && !(screen instanceof PauseScreen)) return;

        AbstractWidget anchor = findAnchorButton(listeners);
        int x;
        int y;
        if (anchor != null) {
            x = anchor.getX() + anchor.getWidth() + ANCHOR_SPACING;
            y = anchor.getY();
        } else {
            x = CORNER_MARGIN;
            y = screen.height - BUTTON_SIZE - CORNER_MARGIN - BOTTOM_TEXT_CLEARANCE;
        }

        ConfigButton button = new ConfigButton(x, y + ClientConfigs.General.CONFIG_BUTTON_Y_OFF.get(),
                ClientConfigs.General.CONFIG_BUTTON_OPENS_MOD_LIST.get());
        addOnTop(button, listeners, adder, remover);
    }

    private static AbstractWidget findAnchorButton(List<? extends GuiEventListener> listeners) {
        List<String> targets = Arrays.asList(
                Component.translatable("menu.online").getString(),
                Component.translatable("fml.menu.modoptions").getString(),
                Component.translatable("menu.shareToLan").getString());

        for (GuiEventListener w : listeners) {
            if (w instanceof AbstractWidget b && targets.contains(b.getMessage().getString())) return b;
        }
        return null;
    }

    private static void addOnTop(ConfigButton button, List<? extends GuiEventListener> listeners,
                                 Consumer<AbstractWidget> adder, Consumer<AbstractWidget> remover) {
        List<AbstractWidget> covered = new ArrayList<>();
        for (GuiEventListener w : listeners) {
            if (w instanceof AbstractWidget other && button.overlaps(other)) covered.add(other);
        }
        adder.accept(button);
        for (AbstractWidget w : covered) {
            remover.accept(w);
            if (!listeners.contains(w)) adder.accept(w);
        }
    }

    private boolean overlaps(AbstractWidget other) {
        return this.getX() < other.getX() + other.getWidth() && other.getX() < this.getX() + this.width
                && this.getY() < other.getY() + other.getHeight() && other.getY() < this.getY() + this.height;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.pose().pushPose();
        graphics.pose().translate(0f, 0f, (float) Z_OVER_OTHER_BUTTONS);
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (this.opensModList) {
            graphics.blitSprite(ModTextures.CONFIG_GEAR, this.getX() + (this.width - GEAR_SIZE) / 2,
                    this.getY() + (this.height - GEAR_SIZE) / 2, GEAR_SIZE, GEAR_SIZE);
        }
        if (this.isHovered && ClientConfigs.General.CONFIG_BUTTON_RAINBOW.get()) {
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height,
                    ColorHelper.getRainbowColorPost(3) | 0xFF000000);
        }
        graphics.pose().popPose();
    }

    @Override
    public void renderString(GuiGraphics graphics, Font font, int color) {
        if (!this.opensModList) super.renderString(graphics, font, color);
    }

    @VirtualOverride("neoforge")
    public int getFGColor() {
        boolean rainbow = this.isHovered && ClientConfigs.General.CONFIG_BUTTON_RAINBOW.get();
        return rainbow ? ColorHelper.getRainbowColorPost(3) : 0xFFAA00;
    }

}
