package net.mehvahdjukaar.supplementaries.integration.fabric;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import dan200.computercraft.shared.media.items.PrintoutData;
import dan200.computercraft.shared.media.items.PrintoutItem;
import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonAccess;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CCCompatImpl {

    public static void setup() {
        PeripheralLookup.get().registerForBlockEntity((tile, direction) -> {
            if (tile.ccHack == null) tile.ccHack = new CannonPeripheral(tile);
            return (IPeripheral) tile.ccHack;
        }, ModRegistry.CANNON_TILE.get());

        PeripheralLookup.get().registerForBlockEntity((tile, direction) -> {
            if (tile.ccHack == null) tile.ccHack = new SpeakerPeripheral(tile);
            return (IPeripheral) tile.ccHack;
        }, ModRegistry.SPEAKER_BLOCK_TILE.get());
    }

    public static int getPages(ItemStack itemstack) {
        return PrintoutData.getOrEmpty(itemstack).pages();
    }

    public static String[] getText(ItemStack itemstack) {
        return PrintoutData.getOrEmpty(itemstack).lines()
                .stream().map(PrintoutData.Line::text).toArray(String[]::new);
    }

    public static boolean isPrintedBook(Item item) {
        return item instanceof PrintoutItem;
    }

    public static void init() {
    }

    @SuppressWarnings({"ClassCanBeRecord"})
    public static final class SpeakerPeripheral implements IPeripheral {
        private final SpeakerBlockTile tile;

        public SpeakerPeripheral(SpeakerBlockTile tile) {
            this.tile = tile;
        }

        @LuaFunction
        public void setNarrator(SpeakerBlockTile.Mode mode) {
            tile.setMode(mode);
            tile.setChanged();
        }

        @LuaFunction
        public SpeakerBlockTile.Mode getMode() {
            return tile.getMode();
        }

        @LuaFunction
        public void setMessage(String message) {
            tile.setMessage(Component.literal(message));
            tile.setChanged();
        }

        @LuaFunction
        public String getMessage() {
            return tile.getMessage(false).getString();
        }

        @LuaFunction
        public void setName(String name) {
            tile.setCustomName(Component.literal(name));
            tile.setChanged();
        }

        @LuaFunction
        public String getName() {
            return tile.getName().getString();
        }

        @LuaFunction
        public double getVolume() {
            return tile.getVolume();
        }

        @LuaFunction
        public void setVolume(double volume) {
            tile.setVolume(volume);
            tile.setChanged();
        }

        @LuaFunction
        public void activate() {
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

        public SpeakerBlockTile tile() {
            return tile;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (SpeakerPeripheral) obj;
            return Objects.equals(this.tile, that.tile);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tile);
        }

        @Override
        public String toString() {
            return "SpeakerPeripheral[" +
                    "tile=" + tile + ']';
        }

    }


    public static final class CannonPeripheral implements IPeripheral {
        private final CannonBlockTile tile;
        private final CannonAccess acc;

        public CannonPeripheral(CannonBlockTile tile) {
            this.tile = tile;
            this.acc = CannonAccess.find(tile.getLevel(), TileOrEntityTarget.of(tile));
        }

        @LuaFunction
        public void setYaw(double value) {
            tile.setYaw(acc, (float) value);
            acc.updateClients();
        }

        @LuaFunction
        public float getYaw() {
            return tile.getYaw();
        }

        @LuaFunction
        public void setPitch(double value) {
            tile.setPitch(acc, (float) value);
            acc.updateClients();
        }

        @LuaFunction
        public float getPitch() {
            return tile.getPitch();
        }

        @LuaFunction
        public void setPower(int inPower) {
            byte power = (byte) Math.min(Math.max(inPower, 1), CannonBlockTile.MAX_POWER_LEVEL);
            tile.setPowerLevel(power);
            acc.updateClients();
        }

        @LuaFunction
        public byte getPower() {
            return tile.getPowerLevel();
        }

        @LuaFunction
        public void ignite() {
            tile.ignite(null, acc);
        }

        @Override
        public String getType() {
            return "cannon";
        }

        @Override
        public boolean equals(@Nullable IPeripheral obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (CannonPeripheral) obj;
            return Objects.equals(this.tile, that.tile);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tile);
        }

        @Override
        public String toString() {
            return "CannonPeripheral[" +
                    "tile=" + tile + ']';
        }

    }
}
