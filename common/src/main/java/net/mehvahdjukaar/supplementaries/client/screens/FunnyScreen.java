package net.mehvahdjukaar.supplementaries.client.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Objects;

public class FunnyScreen extends Screen {
    private final Screen parent;
    private final boolean isEvenFunnier;
    private Component errorHeader;

    public FunnyScreen(Screen parent, boolean isEvenFunnier) {
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
                        "Supplementaries (suslementaries) has dome messed up :(\n" +
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
            guiGraphics.drawString(fr, s, (int) (x - fr.width(s) / 2.0), y, 0xFFFFFF, true);
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
            super(FunnyScreen.this.minecraft, parent.width, parent.height - 85, 35,
                    errors.stream().mapToInt(error -> FunnyScreen.this.font
                            .split(error, parent.width - 20).size()).max().orElse(0)
                            * FunnyScreen.this.minecraft.font.lineHeight + 8);
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
                        guiGraphics.drawString(font, string, (int) (left + (width - font.width(string)) / 2F), y, 0xFFFFFF, false);
                    else
                        guiGraphics.drawString(font, string, left + 5, y, 0xFFFFFF, false);
                    y += font.lineHeight;
                }
            }
        }
    }


}
