package net.mehvahdjukaar.supplementaries.client.screens;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.SuppClientPlatformStuff;
import net.mehvahdjukaar.supplementaries.client.screens.widgets.ISlider;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetSpeakerBlockPacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SpeakerBlockScreen extends Screen {
    private static final Component CHAT_TEXT = Component.translatable("gui.supplementaries.speaker_block.chat_message");
    private static final Component NARRATOR_TEXT = Component.translatable("gui.supplementaries.speaker_block.narrator_message");
    private static final Component ACTION_BAR_TEXT = Component.translatable("gui.supplementaries.speaker_block.action_bar_message");
    private static final Component TITLE_TEXT = Component.translatable("gui.supplementaries.speaker_block.title_message");
    private static final Component DISTANCE_BLOCKS = Component.translatable("gui.supplementaries.speaker_block.blocks");
    private static final Component VOLUME_TEXT = Component.translatable("gui.supplementaries.speaker_block.volume");
    private static final Component EDIT = Component.translatable("gui.supplementaries.speaker_block.edit");

    private EditBox editBox;
    private final SpeakerBlockTile tileSpeaker;
    private SpeakerBlockTile.Mode mode;
    private Button modeBtn;
    private ISlider volumeSlider;

    public SpeakerBlockScreen(SpeakerBlockTile te) {
        super(EDIT);
        this.tileSpeaker = te;
    }

    public static void open(SpeakerBlockTile te) {
        Minecraft.getInstance().setScreen(new SpeakerBlockScreen(te));
    }

    private void updateMode() {
        switch (this.mode) {
            case NARRATOR -> this.modeBtn.setMessage(NARRATOR_TEXT);
            case STATUS_MESSAGE -> this.modeBtn.setMessage(ACTION_BAR_TEXT);
            case TITLE -> this.modeBtn.setMessage(TITLE_TEXT);
            default -> this.modeBtn.setMessage(CHAT_TEXT);
        }
    }

    private void toggleMode() {
        this.mode = SpeakerBlockTile.Mode.values()[(this.mode.ordinal() + 1) % SpeakerBlockTile.Mode.values().length];
        if (!CommonConfigs.Redstone.SPEAKER_NARRATOR.get() && mode == SpeakerBlockTile.Mode.NARRATOR) {
            this.mode = SpeakerBlockTile.Mode.CHAT;
        }
    }

    @Override
    public void init() {
        assert this.minecraft != null;

        int range = CommonConfigs.Redstone.SPEAKER_RANGE.get();

        this.mode = tileSpeaker.getMode();
        String message = tileSpeaker.getMessage(Minecraft.getInstance().isTextFilteringEnabled()).getString();
        double initialVolume = tileSpeaker.getVolume();

        this.volumeSlider = SuppClientPlatformStuff.createSlider(this.width / 2 - 75, this.height / 4 + 80, 150, 20,
                VOLUME_TEXT, DISTANCE_BLOCKS, 1, range,
                initialVolume, 1, 1, true);

        this.addRenderableWidget(this.volumeSlider);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone())
                .bounds(this.width / 2 - 100, this.height / 4 + 120, 200, 20).build());
        this.modeBtn = this.addRenderableWidget(Button.builder(CHAT_TEXT, button -> {
            this.toggleMode();
            this.updateMode();
        }).bounds(this.width / 2 - 75, this.height / 4 + 50, 150, 20).build());
        
        if (!CommonConfigs.Redstone.SPEAKER_NARRATOR.get()) {
            this.modeBtn.active = false;
        }

        this.updateMode();
        this.editBox = new EditBox(this.font, this.width / 2 - 100, this.height / 4 + 10, 200, 20, this.title) {
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage();
            }
        };
        this.editBox.setValue(message);
        this.editBox.setMaxLength(CommonConfigs.Redstone.MAX_TEXT.get());
        this.addRenderableWidget(this.editBox);
        this.setInitialFocus(this.editBox);
        this.editBox.setFocused(true);
    }

    @Override
    public void removed() {
        NetworkHelper.sendToServer(new ServerBoundSetSpeakerBlockPacket(this.tileSpeaker.getBlockPos(),
                this.editBox.getValue(), this.mode, this.volumeSlider.getValue()));
    }

    private void onDone() {
        this.tileSpeaker.setChanged();
        this.minecraft.setScreen(null);
    }

    @Override
    public void onClose() {
        this.onDone();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode != 257 && keyCode != 335) {
            return false;
        } else {
            this.onDone();
            return true;
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.volumeSlider == this.getFocused()) {
            if (button == 0) {
                this.volumeSlider.onReleased(mouseX, mouseY);
                this.setFocused(this.editBox);
            }
        }
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);
    }
}