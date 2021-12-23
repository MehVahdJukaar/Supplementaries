package net.mehvahdjukaar.supplementaries.integration.cctweaked;


import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SpeakerPeripheral implements IPeripheral {

    private final SpeakerBlockTile tile;

    public SpeakerPeripheral(SpeakerBlockTile tile) {
        this.tile = tile;
    }

    @LuaFunction
    public final void setNarrator(boolean narratorOn) {
        tile.narrator = narratorOn;
        tile.setChanged();
    }

    @LuaFunction
    public final boolean isNarratorEnabled() {
        return tile.narrator;
    }

    @LuaFunction
    public final void setMessage(String message) {
        tile.message = message;
        tile.setChanged();
    }

    @LuaFunction
    public final String getMessage() {
        return tile.message;
    }

    @LuaFunction
    public final void setName(String name) {
        tile.setCustomName(new TextComponent(name));
        tile.setChanged();
    }

    @LuaFunction
    public final String getName() {
        return tile.getCustomName().getString();
    }

    @LuaFunction
    public final double getVolume() {
        return tile.volume;
    }

    @LuaFunction
    public final void setVolume(double volume) {
        tile.volume = volume;
        tile.setChanged();
    }

    @LuaFunction
    public final void activate() {
        if (tile.getLevel().isClientSide) return;
        tile.sendMessage();
    }

    @NotNull
    @Override
    public String getType() {
        return "speaker_block";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return Objects.equals(this, other);
    }
}
