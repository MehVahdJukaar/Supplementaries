package net.mehvahdjukaar.supplementaries.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.Nullable;

// Credits to Twilight Forest
public class OptifrickScreen extends Screen {
    private final Screen lastScreen;
    private int ticksUntilEnable = 200;
    private MultiLineLabel message;
    private MultiLineLabel suggestions;
    private static final Component text = Component.translatable("gui.supplementaries.optifine.message");

    private static final MutableComponent url = Component.translatable("gui.supplementaries.optifine.suggestions")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).applyFormat(ChatFormatting.UNDERLINE)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/TheUsefulLists/UsefulMods")));
    private Button exitButton;
    private Button disaleButton;

    public OptifrickScreen(Screen screen) {
        super(Component.translatable("gui.supplementaries.optifine.title")
                .withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
        this.message = MultiLineLabel.EMPTY;
        this.suggestions = MultiLineLabel.EMPTY;
        this.lastScreen = screen;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), text);
    }

    @Override
    protected void init() {
        super.init();
        this.exitButton = this.addRenderableWidget(new Button(
                this.width / 2 + 5, this.height * 5 / 6, 150, 20,
                CommonComponents.GUI_PROCEED, (pressed) -> {
            Minecraft.getInstance().setScreen(this.lastScreen);
        }));
        this.exitButton.active = false;

        this.disaleButton = this.addRenderableWidget(new Button(
                this.width / 2 - 155, this.height * 5 / 6, 150, 20,
                Component.translatable("gui.supplementaries.optifine.turn_off"), (pressed) -> {
                    Minecraft.getInstance().setScreen(this.lastScreen);
                    ((ForgeConfigSpec.BooleanValue) ClientConfigs.General.NO_OPTIFINE_WARN).set(true);
                }));
        this.disaleButton.active = false;

        this.message = MultiLineLabel.create(this.font, text, this.width - 50);
        this.suggestions = MultiLineLabel.create(this.font, url, this.width - 50);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        Gui.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 30, 16777215);
        this.message.renderCentered(poseStack, this.width / 2, 55);
        this.suggestions.renderCentered(poseStack, this.width / 2, 180);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.exitButton.render(poseStack, mouseX, mouseY, partialTick);
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
}
