package net.mehvahdjukaar.supplementaries.client.screens.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;


public class MultiLineEditBoxWidget extends AbstractWidget {

    protected final int x1;
    protected final int y1;

    protected final Minecraft minecraft;
    protected final Font font;
    protected final TextFieldHelper pageEdit;

    @Nullable
    private Consumer<Boolean> onOutOfBounds = null;
    @Nonnull
    private String text = "";

    private int frameTick;
    private long lastClickTime;
    private int lastIndex = -1;

    @Nullable
    private DisplayCache displayCache = DisplayCache.EMPTY;

    public MultiLineEditBoxWidget(Minecraft mc, int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("hhhhh"));
        this.minecraft = mc;
        this.font = mc.font;
        this.x1 = x + width;
        this.y1 = y + height;

        this.pageEdit = new TextFieldHelper(this::getText, this::setText,
                this::getClipboard, this::setClipboard, this::isStringValid);

        this.clearDisplayCache();
    }

    public void setOutOfBoundResponder(Consumer<Boolean> onOutOfBounds) {
        this.onOutOfBounds = onOutOfBounds;
    }

    public void setState(boolean hasItem, boolean packed) {
        this.setFocused(false);
        if (packed) {
            this.active = false;
        } else {
            this.active = hasItem;
            if (!hasItem){
                this.setText("");
            }
        }
    }

    private boolean isStringValid(String s) {
        if (s != null && s.length() < 256) {
            if (s.endsWith("\n")) s = s + "-";
            return this.font.wordWrapHeight(s, this.width) <= this.height;
        }
        return false;
    }

    private void setClipboard(String p_98148_) {
        if (this.minecraft != null) {
            TextFieldHelper.setClipboardContents(this.minecraft, p_98148_);
        }
    }

    private String getClipboard() {
        return this.minecraft != null ? TextFieldHelper.getClipboardContents(this.minecraft) : "";
    }

    //call
    public void tick() {
        ++this.frameTick;
    }

    @Override
    public boolean charTyped(char c, int key) {
        if (this.canConsumeInput()) {
            if (SharedConstants.isAllowedChatCharacter(c)) {
                this.pageEdit.insertText(Character.toString(c));
                this.clearDisplayCache();
                return true;
            }
        }
        return false;
    }

    public boolean canConsumeInput() {
        return this.isFocused() && this.isActive();
    }

    @Override
    public boolean keyPressed(int key, int alt, int ctrl) {
        if (this.canConsumeInput()) {
            if (this.bookKeyPressed(key, alt, ctrl)) {
                this.clearDisplayCache();
                return true;
            }
        }
        return false;
    }

    private boolean bookKeyPressed(int key, int alt, int ctrl) {
        if (Screen.isSelectAll(key)) {
            this.pageEdit.selectAll();
            return true;
        } else if (Screen.isCopy(key)) {
            this.pageEdit.copy();
            return true;
        } else if (Screen.isPaste(key)) {
            this.pageEdit.paste();
            return true;
        } else if (Screen.isCut(key)) {
            this.pageEdit.cut();
            return true;
        } else {
            switch (key) {
                case 257:
                case 335:
                    var p = this.pageEdit.getCursorPos();
                    this.pageEdit.insertText("\n");
                    if (p == this.pageEdit.getCursorPos()) {
                        this.callOutOfBounds(false);
                    }
                    return true;
                case 259:
                    this.pageEdit.removeCharsFromCursor(-1);
                    return true;
                case 261:
                    this.pageEdit.removeCharsFromCursor(1);
                    return true;
                case 262:
                    this.pageEdit.moveByChars(1, Screen.hasShiftDown());
                    return true;
                case 263:
                    this.pageEdit.moveByChars(-1, Screen.hasShiftDown());
                    return true;
                case 264:
                    this.keyDown();
                    return true;
                case 265:
                    this.keyUp();
                    return true;
                case 268:
                    this.keyHome();
                    return true;
                case 269:
                    this.moveCursorToEnd();
                    return true;
                default:
                    return false;
            }
        }
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.canConsumeInput() && this.visible) {
            if (amount >= 1) {
                this.keyUp();
                this.clearDisplayCache();
                return true;
            } else if (amount <= -1) {
                this.keyDown();
                this.clearDisplayCache();
                return true;
            }
        }
        return false;
    }

    private void callOutOfBounds(boolean up) {
        if (this.onOutOfBounds != null) {
            this.onOutOfBounds.accept(up);
        }
    }

    private void keyUp() {
        this.changeLine(-1);
    }

    private void keyDown() {
        this.changeLine(1);
    }

    private void changeLine(int amount) {
        int i = this.pageEdit.getCursorPos();
        int j = this.getDisplayCache().changeLine(i, amount);
        this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
        if (i == j) {
            this.callOutOfBounds(amount < 0);
        }
    }

    private void keyHome() {
        int i = this.pageEdit.getCursorPos();
        int j = this.getDisplayCache().findLineStart(i);
        this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
    }

    public void moveCursorToEnd() {
        DisplayCache displayCache = this.getDisplayCache();
        int i = this.pageEdit.getCursorPos();
        int j = displayCache.findLineEnd(i);
        this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
        this.clearDisplayCache();
        this.onValueChanged();
    }

    public void onValueChanged() {
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int museY, float partialTicks) {
        if (this.visible) {
            DisplayCache displayCache = this.getDisplayCache();

            for (LineInfo lineInfo : displayCache.lines) {
                this.font.draw(poseStack, lineInfo.asComponent, (float) lineInfo.x, (float) lineInfo.y, -16777216);
            }

            if (this.isFocused()) {
                this.renderHighlight(displayCache.selection);
                this.renderCursor(poseStack, displayCache.cursor, displayCache.cursorAtEnd);
            }
        }
    }

    private void renderCursor(PoseStack poseStack, Pos2i pos2i, boolean p_98111_) {
        if (this.frameTick / 6 % 2 == 0) {
            pos2i = this.convertLocalToScreen(pos2i);
            if (!p_98111_) {
                GuiComponent.fill(poseStack, pos2i.x, pos2i.y - 1, pos2i.x + 1, pos2i.y + 9, -16777216);
            } else {
                this.font.draw(poseStack, "_", (float) pos2i.x, (float) pos2i.y, 0);
            }
        }
    }

    private void renderHighlight(Rect2i[] rect2is) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 255.0F, 255.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        for (Rect2i rect2i : rect2is) {
            int i = rect2i.getX();
            int j = rect2i.getY();
            int k = i + rect2i.getWidth();
            int l = j + rect2i.getHeight();
            bufferbuilder.vertex(i, l, 0.0D).endVertex();
            bufferbuilder.vertex(k, l, 0.0D).endVertex();
            bufferbuilder.vertex(k, j, 0.0D).endVertex();
            bufferbuilder.vertex(i, j, 0.0D).endVertex();
        }

        tesselator.end();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    private Pos2i convertScreenToLocal(Pos2i pos2i) {
        return new Pos2i(pos2i.x - this.getX(), pos2i.y - this.getY());
    }

    private Pos2i convertLocalToScreen(Pos2i pos2i) {
        return new Pos2i(pos2i.x + this.getX(), pos2i.y + this.getY());
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public void setFocused(boolean b) {
        super.setFocused(b);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setFocused(true);
        //this. parent. setfocus this
    }

    @Override
    public boolean clicked(double mouseX, double mouseY) {

        if (this.isMouseOver(mouseX, mouseY)) {
            long i = Util.getMillis();
            DisplayCache displayCache = this.getDisplayCache();
            int j = displayCache.getIndexAtPosition(this.font, this.convertScreenToLocal(new Pos2i((int) mouseX, (int) mouseY)));
            if (j >= 0) {
                if (j == this.lastIndex && i - this.lastClickTime < 250L) {
                    if (!this.pageEdit.isSelecting()) {
                        this.selectWord(j);
                    } else {
                        this.pageEdit.selectAll();
                    }
                } else {
                    this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
                }
                this.clearDisplayCache();
            }
            this.lastIndex = j;
            this.lastClickTime = i;

            return true;
        }
        return false;
    }

    private void selectWord(int p_98142_) {
        String s = this.getText();
        this.pageEdit.setSelectionRange(StringSplitter.getWordPosition(s, -1, p_98142_, false), StringSplitter.getWordPosition(s, 1, p_98142_, false));
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double dx, double dy) {
        DisplayCache displayCache = this.getDisplayCache();
        int i = displayCache.getIndexAtPosition(this.font, this.convertScreenToLocal(new Pos2i((int) mouseX, (int) mouseY)));
        this.pageEdit.setCursorPos(i, true);
        this.clearDisplayCache();
    }

    protected DisplayCache getDisplayCache() {
        if (this.displayCache == null) {
            this.displayCache = this.rebuildDisplayCache();
        }
        return this.displayCache;
    }

    protected void clearDisplayCache() {
        this.displayCache = null;
    }

    private DisplayCache rebuildDisplayCache() {
        String s = this.getText();
        if (s.isEmpty()) {
            return DisplayCache.EMPTY;
        } else {
            int i = this.pageEdit.getCursorPos();
            int j = this.pageEdit.getSelectionPos();
            IntList intlist = new IntArrayList();
            List<LineInfo> list = Lists.newArrayList();
            MutableInt mutableint = new MutableInt();
            MutableBoolean mutableboolean = new MutableBoolean();
            StringSplitter stringsplitter = this.font.getSplitter();
            stringsplitter.splitLines(s, this.width, Style.EMPTY, true, (lineInfo, p_98133_, p_98134_) -> {
                int k3 = mutableint.getAndIncrement();
                String s2 = s.substring(p_98133_, p_98134_);
                mutableboolean.setValue(s2.endsWith("\n"));
                String s3 = StringUtils.stripEnd(s2, " \n");
                int l3 = k3 * 9;
                Pos2i pos2i = this.convertLocalToScreen(new Pos2i(0, l3));
                intlist.add(p_98133_);
                list.add(new LineInfo(lineInfo, s3, pos2i.x, pos2i.y));
            });
            int[] toIntArray = intlist.toIntArray();
            boolean flag = i == s.length();
            Pos2i pos2i;
            if (flag && mutableboolean.isTrue()) {
                pos2i = new Pos2i(0, list.size() * 9);
            } else {
                int k = findLineFromPos(toIntArray, i);
                int l = this.font.width(s.substring(toIntArray[k], i));
                pos2i = new Pos2i(l, k * 9);
            }

            List<Rect2i> list1 = Lists.newArrayList();
            if (i != j) {
                int l2 = Math.min(i, j);
                int i1 = Math.max(i, j);
                int j1 = findLineFromPos(toIntArray, l2);
                int k1 = findLineFromPos(toIntArray, i1);
                if (j1 == k1) {
                    int l1 = j1 * 9;
                    int i2 = toIntArray[j1];
                    list1.add(this.createPartialLineSelection(s, stringsplitter, l2, i1, l1, i2));
                } else {
                    int i3 = j1 + 1 > toIntArray.length ? s.length() : toIntArray[j1 + 1];
                    list1.add(this.createPartialLineSelection(s, stringsplitter, l2, i3, j1 * 9, toIntArray[j1]));

                    for (int j3 = j1 + 1; j3 < k1; ++j3) {
                        int j2 = j3 * 9;
                        String s1 = s.substring(toIntArray[j3], toIntArray[j3 + 1]);
                        int k2 = (int) stringsplitter.stringWidth(s1);
                        list1.add(this.createSelection(new Pos2i(0, j2), new Pos2i(k2, j2 + 9)));
                    }

                    list1.add(this.createPartialLineSelection(s, stringsplitter, toIntArray[k1], i1, k1 * 9, toIntArray[k1]));
                }
            }

            return new DisplayCache(s, pos2i, flag, toIntArray, list.toArray(new LineInfo[0]), list1.toArray(new Rect2i[0]));
        }
    }

    static int findLineFromPos(int[] p_98150_, int p_98151_) {
        int i = Arrays.binarySearch(p_98150_, p_98151_);
        return i < 0 ? -(i + 2) : i;
    }

    private Rect2i createPartialLineSelection(String p_98120_, StringSplitter p_98121_, int p_98122_, int p_98123_, int p_98124_, int p_98125_) {
        String s = p_98120_.substring(p_98125_, p_98122_);
        String s1 = p_98120_.substring(p_98125_, p_98123_);
        Pos2i pos2i = new Pos2i((int) p_98121_.stringWidth(s), p_98124_);
        Pos2i pos2i1 = new Pos2i((int) p_98121_.stringWidth(s1), p_98124_ + 9);
        return this.createSelection(pos2i, pos2i1);
    }

    private Rect2i createSelection(Pos2i p_98117_, Pos2i p_98118_) {
        Pos2i pos2i = this.convertLocalToScreen(p_98117_);
        Pos2i pos2i1 = this.convertLocalToScreen(p_98118_);
        int i = Math.min(pos2i.x, pos2i1.x);
        int j = Math.max(pos2i.x, pos2i1.x);
        int k = Math.min(pos2i.y, pos2i1.y);
        int l = Math.max(pos2i.y, pos2i1.y);
        return new Rect2i(i, k, j - i, l - k);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {

    }

    static class DisplayCache {
        static final DisplayCache EMPTY = new DisplayCache("", new Pos2i(0, 0), true, new int[]{0}, new LineInfo[]{new LineInfo(Style.EMPTY, "", 0, 0)}, new Rect2i[0]);
        private final String fullText;
        final Pos2i cursor;
        final boolean cursorAtEnd;
        private final int[] lineStarts;
        final LineInfo[] lines;
        final Rect2i[] selection;

        public DisplayCache(String p_98201_, Pos2i p_98202_, boolean p_98203_, int[] p_98204_, LineInfo[] p_98205_, Rect2i[] p_98206_) {
            this.fullText = p_98201_;
            this.cursor = p_98202_;
            this.cursorAtEnd = p_98203_;
            this.lineStarts = p_98204_;
            this.lines = p_98205_;
            this.selection = p_98206_;
        }

        public int getIndexAtPosition(Font p_98214_, Pos2i p_98215_) {
            int i = p_98215_.y / 9;
            if (i < 0) {
                return 0;
            } else if (i >= this.lines.length) {
                return this.fullText.length();
            } else {
                LineInfo lineInfo = this.lines[i];
                return this.lineStarts[i] + p_98214_.getSplitter().plainIndexAtWidth(lineInfo.contents, p_98215_.x, lineInfo.style);
            }
        }

        public int changeLine(int p_98211_, int p_98212_) {
            int i = findLineFromPos(this.lineStarts, p_98211_);
            int j = i + p_98212_;
            int k;
            if (0 <= j && j < this.lineStarts.length) {
                int l = p_98211_ - this.lineStarts[i];
                int i1 = this.lines[j].contents.length();
                k = this.lineStarts[j] + Math.min(l, i1);
            } else {
                k = p_98211_;
            }

            return k;
        }

        public int findLineStart(int p_98209_) {
            int i = findLineFromPos(this.lineStarts, p_98209_);
            return this.lineStarts[i];
        }

        public int findLineEnd(int p_98219_) {
            int i = findLineFromPos(this.lineStarts, p_98219_);
            return this.lineStarts[i] + this.lines[i].contents.length();
        }
    }

    static class LineInfo {
        final Style style;
        final String contents;
        final Component asComponent;
        final int x;
        final int y;

        public LineInfo(Style style, String contents, int x, int y) {
            this.style = style;
            this.contents = contents;
            this.x = x;
            this.y = y;
            this.asComponent = (Component.literal(contents)).setStyle(style);
        }
    }

    record Pos2i(int x, int y) {
    }
}
