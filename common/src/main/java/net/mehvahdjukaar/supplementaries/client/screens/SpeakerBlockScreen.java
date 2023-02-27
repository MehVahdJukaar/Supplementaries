package net.mehvahdjukaar.supplementaries.client.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.screens.widgets.ForgeSlider;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetSpeakerBlockPacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SpeakerBlockScreen extends Screen {
    private static final Component NARRATOR_TEXT = Component.translatable("gui.supplementaries.speaker_block.chat_message");
    private static final Component CHAT_TEXT = Component.translatable("gui.supplementaries.speaker_block.narrator_message");
    private static final Component ACTION_BAR_TEXT = Component.translatable("gui.supplementaries.speaker_block.action_bar_message");
    private static final Component TITLE_TEXT = Component.translatable("gui.supplementaries.speaker_block.title_message");
    private static final Component DISTANCE_BLOCKS = Component.translatable("gui.supplementaries.speaker_block.blocks");
    private static final Component VOLUME_TEXT = Component.translatable("gui.supplementaries.speaker_block.volume");
    private static final Component EDIT = Component.translatable("gui.supplementaries.speaker_block.edit");

    private EditBox editBox;
    private final SpeakerBlockTile tileSpeaker;
    private SpeakerBlockTile.Mode mode;
    private final String message;
    private Button modeBtn;
    private ForgeSlider volumeSlider;
    private final double initialVolume;

    public SpeakerBlockScreen(SpeakerBlockTile te) {
        super(EDIT);
        this.tileSpeaker = te;
        this.mode = tileSpeaker.getMode();
        this.message = tileSpeaker.getMessage();
        this.initialVolume = tileSpeaker.getVolume();
    }

    public static void open(SpeakerBlockTile te) {
        Minecraft.getInstance().setScreen(new SpeakerBlockScreen(te));
    }

    @Override
    public void tick() {
        this.editBox.tick();
    }

    private void updateMode() {
        switch (this.mode) {
            default -> this.modeBtn.setMessage(CHAT_TEXT);
            case NARRATOR -> this.modeBtn.setMessage(NARRATOR_TEXT);
            case STATUS_MESSAGE -> this.modeBtn.setMessage(ACTION_BAR_TEXT);
            case TITLE -> this.modeBtn.setMessage(TITLE_TEXT);
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
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);

        int range = CommonConfigs.Redstone.SPEAKER_RANGE.get();

        this.volumeSlider = new ForgeSlider(this.width / 2 - 75, this.height / 4 + 80, 150, 20,
                VOLUME_TEXT, DISTANCE_BLOCKS, 1, range,
                initialVolume, 1, 1, true);

        this.addRenderableWidget(this.volumeSlider);

        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, CommonComponents.GUI_DONE, (p_214266_1_) -> this.onDone()));
        this.modeBtn = this.addRenderableWidget(new Button(this.width / 2 - 75, this.height / 4 + 50, 150, 20, CHAT_TEXT, (p_214186_1_) -> {
            this.toggleMode();
            this.updateMode();
        }));
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
        this.editBox.setMaxLength(32);
        this.addRenderableWidget(this.editBox);
        this.setInitialFocus(this.editBox);
        this.editBox.setFocus(true);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.tileSpeaker.setMode(this.mode);
        this.tileSpeaker.setMessage(this.editBox.getValue());
        this.tileSpeaker.setVolume(this.volumeSlider.getValue());
        //refreshTextures server tile
        NetworkHandler.CHANNEL.sendToServer(new ServerBoundSetSpeakerBlockPacket(this.tileSpeaker.getBlockPos(),
                this.tileSpeaker.getMessage(), this.tileSpeaker.getMode(), this.tileSpeaker.getVolume()));
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
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
            return true;
        } else if (p_keyPressed_1_ != 257 && p_keyPressed_1_ != 335) {
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
                this.volumeSlider.onRelease(mouseX, mouseY);
                this.setFocused(this.editBox);
                this.editBox.setFocus(true);
            }
        }
        return true;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}