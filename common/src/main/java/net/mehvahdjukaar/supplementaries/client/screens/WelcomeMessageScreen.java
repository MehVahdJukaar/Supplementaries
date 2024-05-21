package net.mehvahdjukaar.supplementaries.client.screens;

import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

// Credits to Twilight Forest
public class WelcomeMessageScreen extends Screen {
    private final Screen lastScreen;
    private final Component text;
    private final Component url;
    private final Runnable onTurnOff;
    private int ticksUntilEnable;
    private MultiLineLabel message;
    private MultiLineLabel suggestions;

    private Button exitButton;
    private Button disaleButton;

    public WelcomeMessageScreen(Screen screen, int ticksUntilEnable,
                                Component title, Component text, Component url,
                                Runnable onTurnOff) {
        super(title);
        this.message = MultiLineLabel.EMPTY;
        this.suggestions = MultiLineLabel.EMPTY;
        this.lastScreen = screen;
        this.ticksUntilEnable = ticksUntilEnable;
        this.text = text;
        this.url = url;
        this.onTurnOff = onTurnOff;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), text);
    }

    @Override
    protected void init() {
        super.init();
        this.exitButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_PROCEED, (pressed) -> {
            Minecraft.getInstance().setScreen(this.lastScreen);
        }).bounds(this.width / 2 + 5, this.height * 5 / 6, 150, 20).build());
        this.exitButton.active = false;

        this.disaleButton = this.addRenderableWidget(Button.builder(
                Component.translatable("gui.supplementaries.welcome_screen.turn_off"), (pressed) -> {
                    Minecraft.getInstance().setScreen(this.lastScreen);
                    onTurnOff.run();
                }).bounds(this.width / 2 - 155, this.height * 5 / 6, 150, 20).build());
        this.disaleButton.active = false;

        this.message = MultiLineLabel.create(this.font, text, this.width - 50);
        this.suggestions = MultiLineLabel.create(this.font, url, this.width - 50);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 30, 16777215);
        this.message.renderCentered(graphics, this.width / 2, 55);
        this.suggestions.renderCentered(graphics, this.width / 2, 180);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.exitButton.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick() {
        super.tick();
        if (--this.ticksUntilEnable <= 0) {
            this.exitButton.active = true;
            this.disaleButton.active = true;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.ticksUntilEnable <= 0;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.lastScreen);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pMouseY > 180.0 && pMouseY < 190.0) {
            Style style = this.getClickedComponentStyleAt((int) pMouseX);
            if (style != null && style.getClickEvent() != null && style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
                this.handleComponentClicked(style);
                return false;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private @Nullable Style getClickedComponentStyleAt(int xPos) {
        int wid = Minecraft.getInstance().font.width(url);
        int left = this.width / 2 - wid / 2;
        int right = this.width / 2 + wid / 2;
        return xPos >= left && xPos <= right ? Minecraft.getInstance().font.getSplitter().componentStyleAtWidth(url, xPos - left) : null;
    }


    // static stuff

    private static final Component OF_TEXT = Component.translatable("gui.supplementaries.optifine.message");

    private static final Component OF_URL = Component.translatable("gui.supplementaries.optifine.suggestions")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).applyFormat(ChatFormatting.UNDERLINE)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://optifine.alternatives.lambdaurora.dev/")));

    private static final Component OF_TITLE = Component.translatable("gui.supplementaries.optifine.title")
            .withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD);

    public static WelcomeMessageScreen createOptifine(Screen screen) {
        return new WelcomeMessageScreen(screen, 200, OF_TITLE, OF_TEXT, OF_URL, () -> SuppPlatformStuff.disableOFWarn(true));
    }

    private static final Component AM_TEXT = Component.translatable("gui.supplementaries.amendments.message");

    private static final Component AM_URL = Component.translatable("gui.supplementaries.amendments.suggestions")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).applyFormat(ChatFormatting.UNDERLINE)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://legacy.curseforge.com/minecraft/mc-mods/amendments")));

    private static final Component AM_TITLE = Component.translatable("gui.supplementaries.amendments.title")
            .withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD);

    public static WelcomeMessageScreen createAmendments(Screen screen) {
        return new WelcomeMessageScreen(screen, 100, AM_TITLE, AM_TEXT, AM_URL, SuppPlatformStuff::disableAMWarn);
    }

}
