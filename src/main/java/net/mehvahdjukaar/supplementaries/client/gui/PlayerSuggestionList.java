package net.mehvahdjukaar.supplementaries.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;
import java.util.function.Supplier;

public class PlayerSuggestionList extends AbstractSelectionList<PlayerSuggestionList.SimplePlayerEntry> {


    public PlayerSuggestionList(Minecraft minecraft, int p_93405_, int p_93406_, int p_93407_, int p_93408_, int p_93409_) {
        super(minecraft, p_93405_, p_93406_, p_93407_, p_93408_, p_93409_);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    public static class SimplePlayerEntry {
        private final UUID id;
        private final String playerName;
        private final Supplier<ResourceLocation> skinGetter;

        public SimplePlayerEntry(PlayerInfo playerInfo){
            this.id = playerInfo.getProfile().getId();
            this.playerName = playerInfo.getProfile().getName();
            this.skinGetter = playerInfo::getSkinLocation;
        }

    }
}
