package net.mehvahdjukaar.supplementaries.client.screens;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.util.CommonColors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// Credits to Twilight Forest, used as inspirtaion for this class
public class WelcomeMessageScreen extends Screen {
    private static final Component OF_TEXT = Component.translatable("gui.supplementaries.optifine.message");
    private static final Component OF_URL = Component.translatable("gui.supplementaries.optifine.suggestions")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).applyFormat(ChatFormatting.UNDERLINE)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://optifine.alternatives.lambdaurora.dev/")));
    private static final Component OF_TITLE = Component.translatable("gui.supplementaries.optifine.title")
            .withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD);
    private static final Component AM_TEXT = Component.translatable("gui.supplementaries.amendments.message");
    private static final Component AM_URL = Component.translatable("gui.supplementaries.amendments.suggestions")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).applyFormat(ChatFormatting.UNDERLINE)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://legacy.curseforge.com/minecraft/mc-mods/amendments")));
    private static final Component IM_TITLE = Component.translatable("gui.supplementaries.incompatible_mods.title")
            .withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD);

    //leave accessible
    public static final ArrayList<String> MODS_WITH_KNOWN_ISSUES =
            new ArrayList<>(PlatHelper.getPlatform().isFabric() ?
                    List.of("particular") : List.of());

    private static final String IM_LIST_TEXT = MODS_WITH_KNOWN_ISSUES.stream()
            .filter(PlatHelper::isModLoaded)
            .collect(Collectors.joining(", "));

    private final Screen lastScreen;
    private final Component text;
    @Nullable
    private final Component url;
    private final Runnable onTurnOff;
    private int ticksUntilEnable;
    private MultiLineLabel message;
    private MultiLineLabel suggestions;
    private Button exitButton;
    private Button disaleButton;

    public WelcomeMessageScreen(Screen screen, int ticksUntilEnable,
                                Component title, Component text, @Nullable Component url,
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


    // static stuff

    public static WelcomeMessageScreen createOptifine(Screen screen) {
        return new WelcomeMessageScreen(screen, 200, OF_TITLE, OF_TEXT,
                OF_URL, ClientConfigs::disableOfWarn);
    }

    public static WelcomeMessageScreen createIncompatibleMods(Screen screen) {
        return new WelcomeMessageScreen(screen, 60, IM_TITLE,
                Component.translatable("gui.supplementaries.incompatible_mods.message",
                        Component.literal(IM_LIST_TEXT).withStyle(ChatFormatting.RED)),
                null, ClientConfigs::disableIncompatWarn);
    }

    public static boolean hasIncompat() {
        for (String s : MODS_WITH_KNOWN_ISSUES) {
            if (PlatHelper.isModLoaded(s)) return true;
        }
        return false;
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
        this.suggestions = url == null ? MultiLineLabel.EMPTY :
                MultiLineLabel.create(this.font, url, this.width - 50);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 30, CommonColors.WHITE);
        this.message.renderCentered(graphics, this.width / 2, 55);
        this.suggestions.renderCentered(graphics, this.width / 2, 180);
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
        if (pMouseY > 180.0 && pMouseY < 190.0 && this.url != null) {
            Style style = this.getClickedComponentStyleAt((int) pMouseX);
            if (url != null && style != null && style.getClickEvent() != null && style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
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


    public static Screen setup(Screen newScreen) {
        boolean unfunny = ClientConfigs.General.UNFUNNY.get();
        if (MiscUtils.Festivity.compute().isAprilsFool()) {
            newScreen = new HiScreen(newScreen, unfunny);
        }
        return newScreen;
    }


    private static class HiScreen extends Screen {
        private final Screen parent;
        private final boolean isEvenFunnier;
        private Component errorHeader;

        public HiScreen(Screen parent, boolean isEvenFunnier) {
            super(Component.literal("Loading Error"));
            this.parent = parent;
            this.isEvenFunnier = isEvenFunnier;
        }

        @Override
        public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.fillGradient(0, 0, this.width, this.height, -12574688, -11530224);
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }

        @Override
        public void init() {
            super.init();
            this.clearWidgets();
            List<Component> modLoadErrors = List.of(

                    Component.literal(
                            "Supplementaries (suslementaries) has done messed up :(\n" +
                                    ChatFormatting.GRAY +
                                    (isEvenFunnier ?
                                            "net.mehvahdjukaar.supplementaries.code_that_makes_the_game_crash.GameIsKilException" :
                                            "net.mehvahdjukaar.supplementaries.code_that_purposefully_crashes_modpack.GameIsKillException")
                                    + ": Something went wrong! Too bad!"
                    )
            );
            this.errorHeader = Component.literal(ChatFormatting.RED +
                    "Error loading mods\n" +
                    0 +
                    " errors have occurred during mod loading!" +
                    ChatFormatting.RESET);

            int yOffset = 46;
            this.addRenderableWidget(new FakeButton(50, this.height - yOffset, this.width / 2 - 55, 20,
                    Component.literal("Open Mods Folder")
            ));
            this.addRenderableWidget(new FakeButton(this.width / 2 + 5, this.height - yOffset, this.width / 2 - 55, 20,
                    Component.literal("Open log file")
            ));
            this.addRenderableWidget(new FakeButton(50, this.height - 24, this.width / 2 - 55, 20,
                    Component.literal("Open crash report")
            ));
            this.addRenderableWidget(new FakeButton(this.width / 2 + 5, this.height - 24, this.width / 2 - 55, 20,
                    Component.literal("Quit Game")));

            LoadingEntryList entryList = new LoadingEntryList(modLoadErrors);
            this.addRenderableWidget(entryList);
            this.setFocused(entryList);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            drawMultiLineCenteredString(guiGraphics, font, errorHeader, this.width / 2, 10);
        }

        private void drawMultiLineCenteredString(GuiGraphics guiGraphics, Font fr, Component str, int x, int y) {
            for (FormattedCharSequence s : fr.split(str, this.width)) {
                guiGraphics.drawString(fr, s, (int) (x - fr.width(s) / 2.0), y, CommonColors.WHITE, true);
                y += fr.lineHeight;
            }
        }

        private class FakeButton extends Button {

            public FakeButton(int i, int i1, int i2, int i3, MutableComponent translatable) {
                super(i, i1, i2, i3, translatable, b -> {
                    Minecraft.getInstance().setScreen(parent);
                }, DEFAULT_NARRATION);
            }
        }

        public class LoadingEntryList extends ObjectSelectionList<LoadingEntryList.LoadingMessageEntry> {
            LoadingEntryList(final List<Component> errors) {
                super(HiScreen.this.minecraft, parent.width, parent.height - 85, 35,
                        errors.stream().mapToInt(error -> HiScreen.this.font
                                .split(error, parent.width - 20).size()).max().orElse(0)
                                * HiScreen.this.minecraft.font.lineHeight + 8);
                errors.forEach(e -> addEntry(new LoadingMessageEntry(e)));
            }

            @Override
            protected int getScrollbarPosition() {
                return this.getRight() - 6;
            }

            @Override
            public int getRowWidth() {
                return this.width - 15;
            }

            public class LoadingMessageEntry extends ObjectSelectionList.Entry<LoadingMessageEntry> {
                private final Component message;
                private final boolean center;

                LoadingMessageEntry(final Component message) {
                    this(message, false);
                }

                LoadingMessageEntry(final Component message, final boolean center) {
                    this.message = Objects.requireNonNull(message);
                    this.center = center;
                }

                @Override
                public Component getNarration() {
                    return Component.translatable("narrator.select", message);
                }

                @Override
                public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, final int entryWidth, final int entryHeight, final int mouseX, final int mouseY, final boolean p_194999_5_, final float partialTick) {
                    Font font = Minecraft.getInstance().font;
                    final List<FormattedCharSequence> strings = font.split(message, LoadingEntryList.this.width - 20);
                    int y = top + 2;
                    for (FormattedCharSequence string : strings) {
                        if (center)
                            guiGraphics.drawString(font, string, (int) (left + (width - font.width(string)) / 2F), y, CommonColors.WHITE, false);
                        else
                            guiGraphics.drawString(font, string, left + 5, y, CommonColors.WHITE, false);
                        y += font.lineHeight;
                    }
                }
            }
        }

    }

}
