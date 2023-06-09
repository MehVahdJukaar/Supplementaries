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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

//mainly BookEditScreen code
public class MultiLineEditBoxWidget extends AbstractWidget {

    protected final Minecraft minecraft;
    protected final Font font;
    protected final TextFieldHelper pageEdit;

    @Nullable
    private Consumer<Boolean> onOutOfBounds = null;
    @NotNull
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

    private void setClipboard(String s) {
        if (this.minecraft != null) {
            TextFieldHelper.setClipboardContents(this.minecraft, s);
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
                case 257, 335 -> {
                    var p = this.pageEdit.getCursorPos();
                    this.pageEdit.insertText("\n");
                    if (p == this.pageEdit.getCursorPos()) {
                        this.callOutOfBounds(false);
                    }
                    return true;
                }
                case 259 -> {
                    this.pageEdit.removeCharsFromCursor(-1);
                    return true;
                }
                case 261 -> {
                    this.pageEdit.removeCharsFromCursor(1);
                    return true;
                }
                case 262 -> {
                    this.pageEdit.moveByChars(1, Screen.hasShiftDown());
                    return true;
                }
                case 263 -> {
                    this.pageEdit.moveByChars(-1, Screen.hasShiftDown());
                    return true;
                }
                case 264 -> {
                    this.keyDown();
                    return true;
                }
                case 265 -> {
                    this.keyUp();
                    return true;
                }
                case 268 -> {
                    this.keyHome();
                    return true;
                }
                case 269 -> {
                    this.moveCursorToEnd();
                    return true;
                }
                default -> {
                    return false;
                }
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
    public void renderWidget(GuiGraphics graphics, int i, int j, float f) {
        DisplayCache displayCache = this.getDisplayCache();

        for (LineInfo lineInfo : displayCache.lines) {
            this.font.draw(graphics, lineInfo.asComponent, lineInfo.x, lineInfo.y, -16777216);
        }

        if (this.isFocused()) {
            this.renderHighlight(graphics, displayCache.selection);
            this.renderCursor(graphics, displayCache.cursor, displayCache.cursorAtEnd);
        }
    }

    private void renderCursor(GuiGraphics graphics, Pos2i pos2i, boolean isEndOfText) {
        if (this.frameTick / 6 % 2 == 0) {
            pos2i = this.convertLocalToScreen(pos2i);
            if (!isEndOfText) {
                GuiComponent.fill(graphics, pos2i.x, pos2i.y - 1, pos2i.x + 1, pos2i.y + 9, -16777216);
            } else {
                this.font.draw(graphics, "_", pos2i.x, pos2i.y, 0);
            }
        }
    }

    private void renderHighlight(PoseStack poseStack, Rect2i[] rect2is) {
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        for (Rect2i rect2i : rect2is) {
            int i = rect2i.getX();
            int j = rect2i.getY();
            int k = i + rect2i.getWidth();
            int l = j + rect2i.getHeight();
            fill(poseStack, i, j, k, l, -16776961);
        }

        RenderSystem.disableColorLogicOp();
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

    private void selectWord(int i) {
        String s = this.getText();
        this.pageEdit.setSelectionRange(StringSplitter.getWordPosition(s, -1, i, false), StringSplitter.getWordPosition(s, 1, i, false));
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
            stringsplitter.splitLines(s, this.width, Style.EMPTY, true, (lineInfo, i1, i2) -> {
                int k3 = mutableint.getAndIncrement();
                String s2 = s.substring(i1, i2);
                mutableboolean.setValue(s2.endsWith("\n"));
                String s3 = StringUtils.stripEnd(s2, " \n");
                int l3 = k3 * 9;
                Pos2i pos2i = this.convertLocalToScreen(new Pos2i(0, l3));
                intlist.add(i1);
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

    static int findLineFromPos(int[] lineStarts, int find) {
        int i = Arrays.binarySearch(lineStarts, find);
        return i < 0 ? -(i + 2) : i;
    }

    private Rect2i createPartialLineSelection(String input, StringSplitter splitter, int i, int j, int k, int l) {
        String string = input.substring(l, i);
        String string2 = input.substring(l, j);
        Pos2i pos2i = new Pos2i((int)splitter.stringWidth(string), k);
        int var10002 = (int)splitter.stringWidth(string2);
        Objects.requireNonNull(this.font);
        Pos2i pos2i2 = new Pos2i(var10002, k + 9);
        return this.createSelection(pos2i, pos2i2);
    }

    private Rect2i createSelection(Pos2i corner1, Pos2i corner2) {
        Pos2i pos2i = this.convertLocalToScreen(corner1);
        Pos2i pos2i2 = this.convertLocalToScreen(corner2);
        int i = Math.min(pos2i.x, pos2i2.x);
        int j = Math.max(pos2i.x, pos2i2.x);
        int k = Math.min(pos2i.y, pos2i2.y);
        int l = Math.max(pos2i.y, pos2i2.y);
        return new Rect2i(i, k, j - i, l - k);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    static class DisplayCache {
        static final DisplayCache EMPTY = new DisplayCache("", new Pos2i(0, 0), true, new int[]{0}, new LineInfo[]{new LineInfo(Style.EMPTY, "", 0, 0)}, new Rect2i[0]);
        private final String fullText;
        final Pos2i cursor;
        final boolean cursorAtEnd;
        private final int[] lineStarts;
        final LineInfo[] lines;
        final Rect2i[] selection;

        public DisplayCache(String text, Pos2i cursorPos, boolean cursorAtEnd, int[] lineStart, LineInfo[] lines, Rect2i[] selection) {
            this.fullText = text;
            this.cursor = cursorPos;
            this.cursorAtEnd = cursorAtEnd;
            this.lineStarts = lineStart;
            this.lines = lines;
            this.selection = selection;
        }

        public int getIndexAtPosition(Font font, Pos2i pos2i) {
            int i = pos2i.y / 9;
            if (i < 0) {
                return 0;
            } else if (i >= this.lines.length) {
                return this.fullText.length();
            } else {
                LineInfo lineInfo = this.lines[i];
                return this.lineStarts[i] + font.getSplitter().plainIndexAtWidth(lineInfo.contents, pos2i.x, lineInfo.style);
            }
        }

        public int changeLine(int x, int y) {
            int i = findLineFromPos(this.lineStarts, x);
            int j = i + y;
            int k;
            if (0 <= j && j < this.lineStarts.length) {
                int l = x - this.lineStarts[i];
                int i1 = this.lines[j].contents.length();
                k = this.lineStarts[j] + Math.min(l, i1);
            } else {
                k = x;
            }

            return k;
        }

        public int findLineStart(int i1) {
            int i = findLineFromPos(this.lineStarts, i1);
            return this.lineStarts[i];
        }

        public int findLineEnd(int i1) {
            int i = findLineFromPos(this.lineStarts, i1);
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
