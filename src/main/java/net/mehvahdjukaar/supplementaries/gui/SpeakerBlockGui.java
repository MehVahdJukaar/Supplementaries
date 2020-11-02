package net.mehvahdjukaar.supplementaries.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.blocks.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.network.Networking;
import net.mehvahdjukaar.supplementaries.network.UpdateServerSpeakerBlockPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.client.CUpdateSignPacket;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class SpeakerBlockGui extends Screen {
    private TextFieldWidget commandTextField;
    private final SpeakerBlockTile tileSpeaker;
    private boolean narrator;
    private final String message;
    private Button modeBtn;
    public SpeakerBlockGui(SpeakerBlockTile te) {
        super(new StringTextComponent("Set Speaker Block Message"));
        this.tileSpeaker = te;
        this.narrator = tileSpeaker.narrator;
        this.message = tileSpeaker.message;
    }

    public static void open(SpeakerBlockTile te) {
        Minecraft.getInstance().displayGuiScreen(new SpeakerBlockGui(te));
    }

    public void tick() {
        this.commandTextField.tick();
    }

    private static final ITextComponent NARRATOR_TEXT = new StringTextComponent("Narrator message");
    private static final ITextComponent CHAT_TEXT = new StringTextComponent("Chat message");
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
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, DialogTexts.GUI_DONE, (p_214266_1_) -> this.close()));
        this.modeBtn = this.addButton(new Button(this.width / 2 - 75, this.height / 4 + 50, 150, 20, CHAT_TEXT, (p_214186_1_) -> {
            this.toggleMode();
            this.updateMode();
        }));
        this.updateMode();
        this.commandTextField = new TextFieldWidget(this.font, this.width / 2 - 100, this.height / 4 + 10, 200, 20, this.title) {
            protected IFormattableTextComponent getNarrationMessage() {
                return super.getNarrationMessage();
            }
        };
        this.commandTextField.setText(message);
        this.commandTextField.setMaxStringLength(32);
        this.children.add(this.commandTextField);
        this.setFocusedDefault(this.commandTextField);
        this.commandTextField.setFocused2(true);
    }

    @Override
    public void onClose() {
        this.minecraft.keyboardListener.enableRepeatEvents(false);
        //update client tile
        this.tileSpeaker.message = this.commandTextField.getText();
        this.tileSpeaker.narrator = this.narrator;
        //update server tile

        ClientPlayNetHandler clientplaynethandler = this.minecraft.getConnection();
        Networking.INSTANCE.sendToServer(new UpdateServerSpeakerBlockPacket(this.tileSpeaker.getPos(), this.tileSpeaker.message, this.tileSpeaker.narrator));

    }

    private void close() {
        this.tileSpeaker.markDirty();
        this.minecraft.displayGuiScreen((Screen)null);
    }
    @Override
    public void closeScreen() {
        this.close();
    }

    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
            return true;
        } else if (p_keyPressed_1_ != 257 && p_keyPressed_1_ != 335) {
            return false;
        } else {
            this.close();
            return true;
        }
    }

    @Override
    public void render(MatrixStack matrixStack,  int p_render_1_, int p_render_2_, float p_render_3_) {
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
        //this.drawString(this.font, I18n.format("advMode.command"), this.width / 2 - 150, 40, 10526880);
        this.commandTextField.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);
        super.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);
    }
}