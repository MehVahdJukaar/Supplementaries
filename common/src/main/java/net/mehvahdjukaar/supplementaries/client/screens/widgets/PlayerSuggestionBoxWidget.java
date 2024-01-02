package net.mehvahdjukaar.supplementaries.client.screens.widgets;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.StatueBlockTileRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class PlayerSuggestionBoxWidget extends MultiLineEditBoxWidget {

    private static final Map<UUID, String> USERNAME_CACHE = new HashMap<>();

    public static void setUsernameCache(Map<UUID, String> usernameCache) {
        USERNAME_CACHE.clear();
        USERNAME_CACHE.putAll(usernameCache);
    }

    private static final Component EMPTY_SEARCH = (Component.translatable("gui.supplementaries.present.send"))
            .withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC);


    private final List<SimplePlayerEntry> allPlayers = new ArrayList<>();
    private final List<SimplePlayerEntry> filtered = new ArrayList<>();

    private SimplePlayerEntry selectedPlayer = null;
    //missing lines
    @Nullable
    private String suggestion;
    //basically value + suggestion but formatted
    private String fullSuggestion = "";


    public PlayerSuggestionBoxWidget(Minecraft mc, int x, int y, int width, int height) {
        super(mc, x, y, width, height);

        Collection<UUID> onlinePlayers = mc.player.connection.getOnlinePlayerIds();

        for (UUID uuid : onlinePlayers) {
            PlayerInfo playerinfo = mc.player.connection.getPlayerInfo(uuid);
            if (playerinfo != null) {
                this.allPlayers.add(new SimplePlayerEntry(playerinfo));
            }
        }

        //offline players
        for (var entry : USERNAME_CACHE.entrySet()) {
            if (!onlinePlayers.contains(entry.getKey())) {
                this.allPlayers.add(new SimplePlayerEntry(entry.getKey(), entry.getValue()));
            }
        }

        this.filtered.addAll(allPlayers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (this.visible) {
            if (this.canConsumeInput() && this.suggestion != null) {
                int x = this.getX();
                var cache = this.getDisplayCache();
                if (cache.lines.length > 0) {
                    x += this.font.width(cache.lines[0].contents);
                }

                graphics.drawString(font, this.suggestion, x, this.getY(), -8355712, false);
            }

            if (this.getText().isEmpty()) {
                graphics.drawString(font, EMPTY_SEARCH, this.getX(), this.getY(), 0, false);
            } else {
                if (this.selectedPlayer != null) {
                    this.selectedPlayer.render(graphics, this.getX(), this.getY(), this.width, this.height, partialTicks);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int key, int alt, int ctrl) {
        //fill in suggestion
        if (key == 258 && this.canConsumeInput()) {
            if (!this.fullSuggestion.isEmpty()) {
                this.setText(this.fullSuggestion);
                this.moveCursorToEnd();
                this.clearDisplayCache();
            }
            return true;
        }
        return super.keyPressed(key, alt, ctrl);
    }

    @Override
    public void onValueChanged() {
        this.updateFilteredEntries();
        String newValue = this.getText();
        this.selectedPlayer = null;
        String suggestion = "";
        this.fullSuggestion = "";
        if (!newValue.isEmpty()) {
            for (var entry : this.filtered) {
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
        this.filtered.addAll(this.allPlayers.stream().filter(s -> s.getName().toLowerCase(Locale.ROOT).startsWith(finalFilter)).toList());
    }

    public void addPlayer(PlayerInfo info) {
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

        public SimplePlayerEntry(PlayerInfo playerInfo) {
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
                SkullBlockEntity.updateGameprofile(this.profile, (gameProfile) -> this.profile = gameProfile);
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

        public void render(GuiGraphics graphics, int x, int y, int width, int height, float pPartialTicks) {

            int i = x + width - SKIN_SIZE / 2;

            float c = this.isOnline ? 1 : 0.5f;
            RenderSystem.setShaderColor(1, c, c, 1);
            ResourceLocation resourceLocation = this.skinGetter.get();
            //face and overlay
            graphics.blit(resourceLocation, i, y, SKIN_SIZE, SKIN_SIZE, 8.0F, 8.0F, 8, 8, 64, 64);
            RenderSystem.enableBlend();
            graphics.blit(resourceLocation, i, y, SKIN_SIZE, SKIN_SIZE, 40.0F, 8.0F, 8, 8, 64, 64);
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1,1,1,1);
        }
    }
}

