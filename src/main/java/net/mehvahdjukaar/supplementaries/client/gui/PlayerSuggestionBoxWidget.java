package net.mehvahdjukaar.supplementaries.client.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.StatueBlockTileRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.UsernameCache;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PlayerSuggestionBoxWidget extends MultiLineEditBoxWidget {

    private static final ITextComponent EMPTY_SEARCH = (new TranslationTextComponent("gui.supplementaries.present.send"))
            .withStyle(TextFormatting.GRAY).withStyle(TextFormatting.ITALIC);


    private final List<SimplePlayerEntry> allPlayers = new ArrayList<>();
    private final List<SimplePlayerEntry> filtered = new ArrayList<>();

    private SimplePlayerEntry selectedPlayer = null;
    //missing lines
    @Nullable
    private String suggestion;
    //basically value + suggestion but formatted
    private String fullSuggestion = "";
    private Consumer<PlayerSuggestionBoxWidget> onClick;


    public PlayerSuggestionBoxWidget(Minecraft mc, int x, int y, int width, int height) {
        super(mc, x, y, width, height);

        Collection<UUID> onlinePlayers = mc.player.connection.getOnlinePlayerIds();

        for (UUID uuid : onlinePlayers) {
            NetworkPlayerInfo playerinfo = mc.player.connection.getPlayerInfo(uuid);
            if (playerinfo != null) {
                this.allPlayers.add(new SimplePlayerEntry(playerinfo));
            }
        }
        Map<UUID,String> offlinePlayers = UsernameCache.getMap();
        for (Map.Entry<UUID,String> entry : offlinePlayers.entrySet()) {
            if (!onlinePlayers.contains(entry.getKey())) {
                this.allPlayers.add(new SimplePlayerEntry(entry.getKey(), entry.getValue()));
            }
        }

        this.filtered.addAll(allPlayers);
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        if (this.visible) {
            if (this.canConsumeInput() && this.suggestion != null) {
                int x = this.x;
                DisplayCache cache = this.getDisplayCache();
                if (cache.lines.length > 0) {
                    x += this.font.width(cache.lines[0].contents);
                }

                this.font.draw(poseStack, this.suggestion, x, this.y, -8355712);
            }

            if (this.getText().isEmpty()) {
                this.font.draw(poseStack, EMPTY_SEARCH, (float) this.x, (float) this.y, 0);
            } else {
                if (this.selectedPlayer != null) {
                    this.selectedPlayer.render(poseStack, this.x, this.y, this.width, this.height, partialTicks);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int key, int p_94133_, int p_94134_) {
        //fill in suggestion
        if (key == 258 && this.canConsumeInput()) {
            if (!this.fullSuggestion.isEmpty()) {
                this.setText(this.fullSuggestion);
                this.moveCursorToEnd();
                this.clearDisplayCache();
            }
            return true;
        }
        return super.keyPressed(key, p_94133_, p_94134_);
    }

    @Override
    public void onValueChanged() {
        this.updateFilteredEntries();
        String newValue = this.getText();
        this.selectedPlayer = null;
        String suggestion = "";
        this.fullSuggestion = "";
        if (!newValue.isEmpty()) {
            for (SimplePlayerEntry entry : this.filtered) {
                String name = entry.getName();
                if (fullSuggestion.isEmpty()) {
                    //sets new suggestion
                    this.fullSuggestion = name;
                    suggestion = name.substring(newValue.length());
                }
                if (name.equalsIgnoreCase(newValue)) {
                    this.selectedPlayer = entry;
                    break;
                }
            }
        }
        this.setSuggestion(suggestion);
    }

    private void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    //editble/ non editable
    @Override
    public void setState(boolean hasItem, boolean packed) {
        super.setState(hasItem, packed);

        this.filtered.clear();
        if (!packed && hasItem) {
            this.setFocused(true);
            this.filtered.addAll(this.allPlayers);
        }
    }

    private void updateFilteredEntries() {
        String filter = this.getText();
        if (filter == null) filter = "";
        else filter = filter.toLowerCase(Locale.ROOT);
        this.filtered.clear();
        String finalFilter = filter;
        this.filtered.addAll(this.allPlayers.stream().filter(s -> s.getName().toLowerCase(Locale.ROOT).startsWith(finalFilter)).collect(Collectors.toList()));
    }

    public void addPlayer(NetworkPlayerInfo info) {
        this.allPlayers.removeIf(simplePlayerEntry -> simplePlayerEntry.getId().equals(info.getProfile().getId()));
        this.allPlayers.add(new SimplePlayerEntry(info));
        this.updateFilteredEntries();
        this.onValueChanged();
    }

    public void removePlayer(UUID id) {
        for (SimplePlayerEntry simplePlayerEntry : this.allPlayers) {
            if (simplePlayerEntry.getId().equals(id)) {
                simplePlayerEntry.setOnline(false);
                //this.allPlayers.remove(simplePlayerEntry);
                //this.updateFilteredEntries();
                //this.onValueChanged();
                return;
            }
        }

    }

    public static class SimplePlayerEntry {
        private static final int SKIN_SIZE = 8;

        private final Supplier<ResourceLocation> skinGetter;
        private GameProfile profile;
        private boolean isOnline;

        public SimplePlayerEntry(NetworkPlayerInfo playerInfo) {
            this.profile = playerInfo.getProfile();
            //uses online player info
            this.skinGetter = playerInfo::getSkinLocation;
            this.isOnline = true;
        }

        public SimplePlayerEntry(UUID id, String lastName) {
            GameProfile profile = new GameProfile(id, lastName);
            this.skinGetter = () -> StatueBlockTileRenderer.getPlayerSkin(this.profile);

            if (!(profile.isComplete() && profile.getProperties().containsKey("textures"))) {
                synchronized (this) {
                    this.profile = profile;
                }
                SkullTileEntity.updateGameprofile(this.profile);
            }
            this.isOnline = false;
        }

        public void setOnline(boolean online) {
            this.isOnline = online;
        }

        public UUID getId() {
            return this.profile.getId();
        }

        public String getName() {
            return this.profile.getName();
        }

        public void render(MatrixStack poseStack, int x, int y, int width, int height, float pPartialTicks) {

            int i = x + width - SKIN_SIZE / 2;

            float c = this.isOnline ? 1 : 0.5f;

            RenderSystem.color4f(1.0F, c, c, 1.0F);
            Minecraft.getInstance().getTextureManager().bind(this.skinGetter.get());

            //face and overlay
            AbstractGui.blit(poseStack, i, y, SKIN_SIZE, SKIN_SIZE, 8.0F, 8.0F, 8, 8, 64, 64);
            RenderSystem.enableBlend();
            AbstractGui.blit(poseStack, i, y, SKIN_SIZE, SKIN_SIZE, 40.0F, 8.0F, 8, 8, 64, 64);
            RenderSystem.disableBlend();
        }
    }
}

