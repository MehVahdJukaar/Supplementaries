package net.mehvahdjukaar.supplementaries.client.screens;


import net.mehvahdjukaar.supplementaries.common.block.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetTextHolderPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Arrays;
import java.util.stream.IntStream;

public abstract class TextHolderEditScreen<T extends BlockEntity & ITextHolderProvider> extends Screen {

    protected final T tile;
    protected final String[][] messages;
    protected final int totalLines;

    protected TextFieldHelper textInputUtil;
    // The index of the line that is being edited.
    protected int lineIndex = 0;
    protected int textHolderIndex = 0;
    //for ticking cursor
    protected int updateCounter;

    protected TextHolderEditScreen(T tile, Component title) {
        super(title);
        this.tile = tile;

        boolean filtering = Minecraft.getInstance().isTextFilteringEnabled();
        this.messages = IntStream.range(0, tile.textHoldersCount())
                .mapToObj(i -> IntStream.range(0, tile.getTextHolder(i).size())
                        .mapToObj(j -> tile.getTextHolder(i).getMessage(j, filtering))
                        .map(Component::getString)
                        .toArray(String[]::new))
                .toArray(String[][]::new);

        this.totalLines = Arrays.stream(messages)
                .mapToInt(innerArray -> innerArray.length)
                .sum();
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        this.textInputUtil.charTyped(codePoint);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.scrollText((int) delta);
        return true;
    }

    protected boolean canScroll() {
        return true;
    }

    protected void scrollText(int amount) {
        if (canScroll()) {
            lineIndex = lineIndex - amount;

            while (lineIndex<0)lineIndex+=totalLines;

            while(lineIndex>=messages[textHolderIndex].length){
                lineIndex-=messages[textHolderIndex].length;
                textHolderIndex+=1;
                textHolderIndex%=messages.length;
            }
            textInputUtil.setCursorToEnd();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // up arrow
        if (keyCode == 265) {
            this.scrollText(1);
            return true;
        }
        // !down arrow, !enter, !enter, handles special keys
        else if (keyCode != 264 && keyCode != 257 && keyCode != 335) {
            return this.textInputUtil.keyPressed(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
        }
        // down arrow, enter
        else {
            this.scrollText(-1);
            return true;
        }
    }

    @Override
    public void tick() {
        ++this.updateCounter;
        if (!isValid()) {
            this.onClose();
        }
    }

    private boolean isValid() {
        return this.minecraft != null && this.minecraft.player != null && !this.tile.isRemoved() &&
                !this.tile.playerIsTooFarAwayToEdit(tile.getLevel(), tile.getBlockPos(), this.minecraft.player.getUUID());
    }

    @Override
    public void onClose() {
        this.tile.setChanged();
        super.onClose();
    }

    @Override
    public void removed() {
        // send new text to the server
            NetworkHandler.CHANNEL.sendToServer(new ServerBoundSetTextHolderPacket(
                    this.tile.getBlockPos(), this.messages));
    }


    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height / 4 + 120, 200, 20).build());

        this.textInputUtil = new TextFieldHelper(
                () -> this.messages[textHolderIndex][lineIndex],
                (h) -> {
                    this.messages[textHolderIndex][lineIndex] = h;
                    this.tile.getTextHolder(textHolderIndex).setMessage(lineIndex, Component.literal(h));
                },
                TextFieldHelper.createClipboardGetter(this.minecraft),
                TextFieldHelper.createClipboardSetter(this.minecraft),
                (s) -> this.minecraft.font.width(s) <= tile.getTextHolder(textHolderIndex).getMaxLineVisualWidth()
        );
    }
}

