package net.mehvahdjukaar.supplementaries.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.utils.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FilteredPlayerListWidget implements Widget, NarratableEntry, GuiEventListener {

    private static final int ENTRY_PER_SCREEN = 3;
    private static final int ITEM_HEIGHT = 12;
    private static final int WIDTH = 101;
    private static final int HEIGHT = ITEM_HEIGHT * ENTRY_PER_SCREEN;

    private static final int SCROLLER_W = 6;
    private static final int SCROLLER_H = 17;
    private static final int SCROLLER_X = WIDTH + 1;


    private final List<SimplePlayerEntry> allPlayers = new ArrayList<>();
    private final List<SimplePlayerEntry> filtered = new ArrayList<>();
    private final Minecraft minecraft;
    protected final int x;
    protected final int y;
    protected final int x1;
    protected final int y1;
    private final Consumer<String> onClick;

    private String filter;
    private int scrollOff;
    private boolean isDragging;


    public FilteredPlayerListWidget(Minecraft minecraft, int x, int y, Consumer<String> onClick) {
        this.minecraft = minecraft;
        this.x = x;
        this.y = y;
        this.x1 = x + WIDTH;
        this.y1 = y + HEIGHT;

        this.onClick = onClick;


        Collection<UUID> collection = this.minecraft.player.connection.getOnlinePlayerIds();

        for(UUID uuid : collection) {
            PlayerInfo playerinfo = this.minecraft.player.connection.getPlayerInfo(uuid);
            if (playerinfo != null) {
                this.allPlayers.add(new SimplePlayerEntry(playerinfo, this.minecraft.font));
            }
        }

        this.filtered.addAll(allPlayers);
    }

    //set filter and return filtered values
    public List<String> setFilter(@Nullable String filter) {
        if (filter == null) filter = "";
        this.filter = filter.toLowerCase(Locale.ROOT);
        this.updateFilteredEntries();
        //if a player is added it wont update suggestion
        return this.filtered.stream().map(SimplePlayerEntry::getName).toList();
    }

    private void updateFilteredEntries(){
        this.filtered.clear();
        this.filtered.addAll(this.allPlayers.stream().filter(s -> s.getName().toLowerCase(Locale.ROOT).startsWith(this.filter)).toList());
    }

    public void addPlayer(PlayerInfo info) {
        this.allPlayers.add(new SimplePlayerEntry(info, this.minecraft.font));
        this.updateFilteredEntries();
    }

    public void removePlayer(UUID id) {
        for(SimplePlayerEntry simplePlayerEntry : this.allPlayers) {
            if (simplePlayerEntry.getId().equals(id)) {
                this.allPlayers.remove(simplePlayerEntry);
                this.updateFilteredEntries();
                return;
            }
        }
    }

    private boolean canScroll() {
        return this.filtered.size() > ENTRY_PER_SCREEN;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int key) {
        this.isDragging = this.canScroll() &&
                mouseX > (double) (x + SCROLLER_X) && mouseX < (double) (x + SCROLLER_X + SCROLLER_W) &&
                mouseY > (double) (y) && mouseY <= (double) (y + HEIGHT + 1);

        if (this.isMouseOver(mouseX, mouseY)) {
            SimplePlayerEntry e = this.getEntryAtPosition(mouseX, mouseY);
            if (e != null) {
                onClick.accept(e.playerName);
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double dx, double dy, int key, double mouseX, double mouseY) {
        if (this.isDragging) {

            int j = this.filtered.size() - ENTRY_PER_SCREEN;
            float f = ((float) dy - (float) y - 13.5F) / ((float) (y1 - y) - SCROLLER_H);
            f = f * (float) j + 0.5F;
            this.scrollOff = Mth.clamp((int) f, 0, j);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double a, double b, double c) {
        if (this.canScroll()) {
            int j = this.filtered.size() - ENTRY_PER_SCREEN;
            this.scrollOff = (int) ((double) this.scrollOff - c);
            this.scrollOff = Mth.clamp(this.scrollOff, 0, j);
        }

        return true;
    }

    @Override
    public boolean keyPressed(int key, int a, int b) {
        //up arrow
        if (key == 265) {
            this.scrollOff = Math.max(0, scrollOff - 1);
            return true;
        }
        // down arrow, enter
        else if (key == 264) {
            this.scrollOff = Mth.clamp(this.scrollOff + 1, 0, this.filtered.size() - ENTRY_PER_SCREEN);
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseY >= (double) this.y && mouseY <= (double) this.y1 &&
                mouseX >= (double) this.x && mouseX <= (double) this.x1;
    }

    //call is mouse over before this
    @Nullable
    protected final SimplePlayerEntry getEntryAtPosition(double mouseX, double mouseY) {
        if (mouseX > x1) return null;
        int rel = Mth.floor(mouseY - (double) this.y);
        int ind = this.scrollOff + rel / ITEM_HEIGHT;
        return rel >= 0 && ind < this.filtered.size() ? this.filtered.get(ind) : null;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        int size = this.filtered.size();
        this.renderScroller(poseStack, size);
        if (size != 0) {
            SimplePlayerEntry hovered = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;

            int currentY = y;
            for (int i = 0; this.scrollOff + i < size && i < ENTRY_PER_SCREEN; i++) {
                var e = this.filtered.get(this.scrollOff + i);
                e.render(poseStack, this.scrollOff + i, x, currentY, WIDTH, ITEM_HEIGHT, mouseX, mouseY,
                        Objects.equals(hovered, e), partialTicks);
                currentY += ITEM_HEIGHT;
            }
        }

    }

    private void renderScroller(PoseStack poseStack, int size) {
        RenderSystem.setShaderTexture(0, Textures.PRESENT_BLOCK_GUI_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int currentIndex = size + 1 - ENTRY_PER_SCREEN;
        if (currentIndex > 1) {
            int a = HEIGHT - (SCROLLER_H + (currentIndex - 1) * HEIGHT / currentIndex);
            int b = 1 + a / currentIndex + HEIGHT / currentIndex;
            int scroll = Math.min(HEIGHT - SCROLLER_H, this.scrollOff * b);
            if (this.scrollOff == currentIndex - 1) {
                scroll = HEIGHT - SCROLLER_H;
            }

            GuiComponent.blit(poseStack, x + SCROLLER_X, y + scroll, 0, 0.0F, 232, SCROLLER_W, SCROLLER_H, 256, 256);
        } else {
            GuiComponent.blit(poseStack, x + SCROLLER_X, y, 0, SCROLLER_W, 232, SCROLLER_W, SCROLLER_H, 256, 256);
        }

    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput elementOutput) {

    }

    public void setState(boolean hasItem, boolean packed) {
        this.filtered.clear();
        if (!packed && hasItem) {
            this.filtered.addAll(this.allPlayers);
        }
    }


    public static class SimplePlayerEntry {
        private static final int SKIN_SIZE = 8;

        private final Font font;
        private final UUID id;
        private final String playerName;
        private final Supplier<ResourceLocation> skinGetter;

        public SimplePlayerEntry(PlayerInfo playerInfo, Font font) {
            this.id = playerInfo.getProfile().getId();
            this.playerName = playerInfo.getProfile().getName();
            this.skinGetter = playerInfo::getSkinLocation;
            this.font = font;
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return playerName;
        }

        public void render(PoseStack poseStack, int pIndex, int pLeft, int pTop, int pWidth, int pHeight,
                           int pMouseX, int pMouseY, boolean hovered, float pPartialTicks) {

            int i = pLeft + 2;
            int j = pTop + (pHeight - SKIN_SIZE) / 2;

            int k = i + SKIN_SIZE + 2;

            RenderSystem.setShaderTexture(0, Textures.PRESENT_BLOCK_GUI_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(poseStack, pLeft, pTop, 0, 0, 220, pWidth, pHeight, 256, 256);


            RenderSystem.setShaderTexture(0, this.skinGetter.get());
            //face and overlay
            GuiComponent.blit(poseStack, i, j, SKIN_SIZE, SKIN_SIZE, 8.0F, 8.0F, 8, 8, 64, 64);
            RenderSystem.enableBlend();
            GuiComponent.blit(poseStack, i, j, SKIN_SIZE, SKIN_SIZE, 40.0F, 8.0F, 8, 8, 64, 64);
            RenderSystem.disableBlend();

            this.font.draw(poseStack, this.playerName, (float) k, (float) j, hovered ? -1 : 0);

        }

    }
}
