package net.mehvahdjukaar.supplementaries.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetSpeakerBlockPacket;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.gui.widget.ForgeSlider;

public class SpeakerBlockGui extends Screen {
    private static final Component NARRATOR_TEXT = new TranslatableComponent("gui.supplementaries.speaker_block.chat_message");
    private static final Component CHAT_TEXT = new TranslatableComponent("gui.supplementaries.speaker_block.narrator_message");
    private static final Component DISTANCE_BLOCKS = new TranslatableComponent("gui.supplementaries.speaker_block.blocks");
    private static final Component VOLUME_TEXT = new TranslatableComponent("gui.supplementaries.speaker_block.volume");

    private EditBox commandTextField;
    private final SpeakerBlockTile tileSpeaker;
    private boolean narrator;
    private final String message;
    private Button modeBtn;
    private ForgeSlider volumeSlider;
    private final double initialVolume;

    public SpeakerBlockGui(SpeakerBlockTile te) {
        super(new TranslatableComponent("gui.supplementaries.speaker_block.edit"));
        this.tileSpeaker = te;
        this.narrator = tileSpeaker.isNarrator();
        this.message = tileSpeaker.getMessage();
        this.initialVolume = tileSpeaker.getVolume();
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

        int range = ServerConfigs.block.SPEAKER_RANGE.get();

        double a = this.tileSpeaker.getVolume(); //keep
        a++;
        this.volumeSlider = new ForgeSlider(this.width / 2 - 75, this.height / 4 + 80, 150, 20,
                VOLUME_TEXT, DISTANCE_BLOCKS, 1, range,
                initialVolume, 1, 1, true);

        this.addRenderableWidget(this.volumeSlider);

        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, CommonComponents.GUI_DONE, (p_214266_1_) -> this.onDone()));
        this.modeBtn = this.addRenderableWidget(new Button(this.width / 2 - 75, this.height / 4 + 50, 150, 20, CHAT_TEXT, (p_214186_1_) -> {
            this.toggleMode();
            this.updateMode();
        }));
        if (!ServerConfigs.block.SPEAKER_NARRATOR.get()) {
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
        this.addRenderableWidget(this.commandTextField);
        this.setInitialFocus(this.commandTextField);
        this.commandTextField.setFocus(true);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.tileSpeaker.setSettings(this.volumeSlider.getValue(), this.narrator, this.commandTextField.getValue());
        //refreshTextures server tile
        NetworkHandler.INSTANCE.sendToServer(new ServerBoundSetSpeakerBlockPacket(this.tileSpeaker.getBlockPos(),
                this.tileSpeaker.getMessage(), this.tileSpeaker.isNarrator(), this.tileSpeaker.getVolume()));
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
                this.setFocused(null);
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