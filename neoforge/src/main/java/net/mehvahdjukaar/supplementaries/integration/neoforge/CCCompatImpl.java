package net.mehvahdjukaar.supplementaries.integration.neoforge;


import dan200.computercraft.api.ForgeComputerCraftAPI;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import dan200.computercraft.shared.media.items.PrintoutItem;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CCCompatImpl {

    public static void setup() {
        ForgeComputerCraftAPI.registerPeripheralProvider((level, blockPos, direction) -> {
            var tile = level.getBlockEntity(blockPos);
            if (tile instanceof SpeakerBlockTile) {
                return tile.getCapability(Capabilities.CAPABILITY_PERIPHERAL, direction);
            }
            return LazyOptional.empty();
        });
    }

    public static int getPages(ItemStack itemstack) {
        return PrintoutItem.getPageCount(itemstack);
    }

    public static String[] getText(ItemStack itemstack) {
        return PrintoutItem.getText(itemstack);
    }

    public static boolean isPrintedBook(Item item) {
        return item instanceof PrintoutItem;
    }

    public static boolean isPeripheralCap(Capability<?> cap) {
        return cap == Capabilities.CAPABILITY_PERIPHERAL;
    }

    public static LazyOptional<Object> getPeripheralSupplier(SpeakerBlockTile tile) {
        return LazyOptional.of(() -> new SpeakerPeripheral(tile));
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
}
