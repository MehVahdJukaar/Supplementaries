package net.mehvahdjukaar.supplementaries.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.gui.widgets.ForgeSlider;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetSpeakerBlockPacket;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SpeakerBlockGui extends Screen {
    private static final Component NARRATOR_TEXT = Component.translatable("gui.supplementaries.speaker_block.chat_message");
    private static final Component CHAT_TEXT = Component.translatable("gui.supplementaries.speaker_block.narrator_message");
    private static final Component DISTANCE_BLOCKS = Component.translatable("gui.supplementaries.speaker_block.blocks");
    private static final Component VOLUME_TEXT = Component.translatable("gui.supplementaries.speaker_block.volume");

    private EditBox commandTextField;
    private final SpeakerBlockTile tileSpeaker;
    private boolean narrator;
    private final String message;
    private Button modeBtn;
    private ForgeSlider volumeSlider;

    public SpeakerBlockGui(SpeakerBlockTile te) {
        super(Component.translatable("gui.supplementaries.speaker_block.edit"));
        this.tileSpeaker = te;
        this.narrator = tileSpeaker.narrator;
        this.message = tileSpeaker.message;
    }

    public static void open(SpeakerBlockTile te) {
        Minecraft.getInstance().setScreen(new SpeakerBlockGui(te));
    }

    @Override
    public void tick() {
        this.commandTextField.tick();
    }

    private void updateMode() {
        if (this.narrator) {
            this.modeBtn.setMessage(NARRATOR_TEXT);
        } else {
            this.modeBtn.setMessage(CHAT_TEXT);
        }
    }

    private void toggleMode() {
        this.narrator = !this.narrator;
    }

    @Override
    public void init() {
        assert this.minecraft != null;
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);

        int range = ServerConfigs.Blocks.SPEAKER_RANGE.get();

        double currentValue = this.tileSpeaker.volume * range;
        this.volumeSlider = new ForgeSlider(this.width / 2 - 75, this.height / 4 + 80, 150, 20, VOLUME_TEXT, DISTANCE_BLOCKS, 1, range,
                currentValue, 1, 1, true);

        this.addWidget(this.volumeSlider);

        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, CommonComponents.GUI_DONE, (p_214266_1_) -> this.onDone()));
        this.modeBtn = this.addRenderableWidget(new Button(this.width / 2 - 75, this.height / 4 + 50, 150, 20, CHAT_TEXT, (p_214186_1_) -> {
            this.toggleMode();
            this.updateMode();
        }));
        if (!ServerConfigs.Blocks.SPEAKER_NARRATOR.get()) {
            this.modeBtn.active = false;
        }

        this.updateMode();
        this.commandTextField = new EditBox(this.font, this.width / 2 - 100, this.height / 4 + 10, 200, 20, this.title) {
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage();
            }
        };
        this.commandTextField.setValue(message);
        this.commandTextField.setMaxLength(32);
        this.addWidget(this.commandTextField);
        this.setInitialFocus(this.commandTextField);
        this.commandTextField.setFocus(true);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.tileSpeaker.message = this.commandTextField.getValue();
        this.tileSpeaker.narrator = this.narrator;
        this.tileSpeaker.volume = this.volumeSlider.getValue();
        //refreshTextures server tile
        NetworkHandler.CHANNEL.sendToServer(new ServerBoundSetSpeakerBlockPacket(this.tileSpeaker.getBlockPos(), this.tileSpeaker.message, this.tileSpeaker.narrator, this.tileSpeaker.volume));
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
        if (button == 0) this.volumeSlider.onRelease(mouseX, mouseY);
        return false;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
        this.volumeSlider.render(matrixStack, mouseX, mouseY, partialTicks);
        this.commandTextField.render(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}