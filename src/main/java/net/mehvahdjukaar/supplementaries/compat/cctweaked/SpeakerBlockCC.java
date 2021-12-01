package net.mehvahdjukaar.supplementaries.compat.cctweaked;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.mehvahdjukaar.supplementaries.block.blocks.SpeakerBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SpeakerBlockCC extends SpeakerBlock implements IPeripheralProvider {

    public static void initialize() {
        ComputerCraftAPI.registerPeripheralProvider((IPeripheralProvider) ModRegistry.SPEAKER_BLOCK.get());
    }

    public SpeakerBlockCC() {
        super();
    }

    @NotNull
    @Override
    public LazyOptional<IPeripheral> getPeripheral(@NotNull World world, @NotNull BlockPos pos, @NotNull Direction side) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof SpeakerBlockTile) {
            return ((SpeakerBlockTile) tile).getPeripheral(world, pos, side).cast();
        }
        return LazyOptional.empty();
    }

    public static LazyOptional<Object> getPeripheralSupplier(SpeakerBlockTile tile) {
        return LazyOptional.of(() -> new SpeakerPeripheral(tile));
    }

    public static class SpeakerPeripheral implements IPeripheral {

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
            tile.setCustomName(new StringTextComponent(name));
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


}
