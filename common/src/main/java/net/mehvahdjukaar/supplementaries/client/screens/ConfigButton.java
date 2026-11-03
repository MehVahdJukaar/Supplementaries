package net.mehvahdjukaar.supplementaries.client.screens;


import net.mehvahdjukaar.candlelight.api.VirtualOverride;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.util.math.Vec2i;
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
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.Nullable;

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
    private static final List<String> PREFERRED_ANCHORS = List.of(
            "fml.menu.mods", "fml.menu.mods.title", "menu.online", "fml.menu.modoptions", "menu.shareToLan",
            "menu.reportBugs", "menu.multiplayer", "menu.singleplayer");

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

    //TODO: allow manual placement too
    public static void setupConfigButton(Screen screen, List<? extends GuiEventListener> listeners,
                                         Consumer<AbstractWidget> adder, Consumer<AbstractWidget> remover) {
        if (!(screen instanceof TitleScreen) && !(screen instanceof PauseScreen)) return;

        int yOffset = ClientConfigs.General.CONFIG_BUTTON_Y_OFF.get();
        boolean openList = ClientConfigs.General.CONFIG_BUTTON_OPENS_MOD_LIST.get();
        List<AbstractWidget> widgets = new ArrayList<>();
        for (GuiEventListener l : listeners) {
            if ((l instanceof AbstractWidget w) && w.visible) widgets.add(w);
        }

        ScreenRectangle spot = null;
        Vec2i manualPos = ClientConfigs.General.CONFIG_BUTTON_POS.get();
        if (manualPos.x() != -1 && manualPos.y() != -1) {
            spot = new ScreenRectangle(manualPos.x(), manualPos.y(), BUTTON_SIZE, BUTTON_SIZE);
        } else {
            for (AbstractWidget anchor : sortAnchorsByPreference(widgets)) {
                spot = findFreeSpotNextTo(anchor, screen, widgets, yOffset);
                if (spot != null) break;
            }
        }

        ConfigButton button;
        if (spot != null) {
            button = new ConfigButton(spot.left(), spot.top(), openList);
        } else {
            int y = screen.height - BUTTON_SIZE - CORNER_MARGIN - BOTTOM_TEXT_CLEARANCE;
            button = new ConfigButton(CORNER_MARGIN, y + yOffset, openList);
        }
        addOnTop(button, listeners, adder, remover);
    }

    private static List<AbstractWidget> sortAnchorsByPreference(List<AbstractWidget> widgets) {
        List<AbstractWidget> anchors = new ArrayList<>();
        for (String key : PREFERRED_ANCHORS) {
            String text = Component.translatable(key).getString();
            for (AbstractWidget w : widgets) {
                if (!anchors.contains(w) && text.equals(w.getMessage().getString())) anchors.add(w);
            }
        }
        for (AbstractWidget w : widgets) {
            if (!anchors.contains(w)) anchors.add(w);
        }
        return anchors;
    }

    @Nullable
    private static ScreenRectangle findFreeSpotNextTo(AbstractWidget anchor, Screen screen,
                                                      List<AbstractWidget> widgets, int yOffset) {
        int y = anchor.getY() + (anchor.getHeight() - BUTTON_SIZE) / 2 + yOffset;
        int rightSide = anchor.getX() + anchor.getWidth() + ANCHOR_SPACING;
        int leftSide = anchor.getX() - ANCHOR_SPACING - BUTTON_SIZE;
        for (int x : new int[]{rightSide, leftSide}) {
            ScreenRectangle spot = new ScreenRectangle(x, y, BUTTON_SIZE, BUTTON_SIZE);
            if (isFree(spot, screen, widgets)) return spot;
        }
        return null;
    }

    private static boolean isFree(ScreenRectangle spot, Screen screen, List<AbstractWidget> widgets) {
        boolean onScreen = spot.left() >= 0 && spot.top() >= 0 && spot.right() <= screen.width && spot.bottom() <= screen.height;
        if (!onScreen) return false;
        for (AbstractWidget w : widgets) {
            if (w.getRectangle().overlaps(spot)) return false;
        }
        return true;
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
        return this.getRectangle().overlaps(other.getRectangle());
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
                    FastColor.ARGB32.opaque(ColorHelper.getRainbowColorPost(3)));
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
