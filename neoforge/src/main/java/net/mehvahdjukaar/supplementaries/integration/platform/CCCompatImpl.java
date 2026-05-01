package net.mehvahdjukaar.supplementaries.integration.platform;


import dan200.computercraft.api.ForgeComputerCraftAPI;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.media.items.PrintoutData;
import dan200.computercraft.shared.media.items.PrintoutItem;
import net.mehvahdjukaar.moonlight.api.util.math.EntityAngles;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.Objects;


public class CCCompatImpl {

    protected static BlockCapability<SpeakerPeripheral, Direction> SPEAKER_CAP =
            BlockCapability.createSided(Supplementaries.res("speaker_block"), SpeakerPeripheral.class);
    protected static BlockCapability<CannonPeripheral, Direction> CANNON_CAP =
            BlockCapability.createSided(Supplementaries.res("cannon"), CannonPeripheral.class);


    public static void init() {
        var currentBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        currentBus.addListener(CCCompatImpl::registerCap);


    }

    public static void setup() {
        ForgeComputerCraftAPI.registerGenericCapability(SPEAKER_CAP);
        ForgeComputerCraftAPI.registerGenericCapability(CANNON_CAP);
    }

    public static void registerCap(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(SPEAKER_CAP, ModRegistry.SPEAKER_BLOCK_TILE.get(),
                (tile, object2) -> new SpeakerPeripheral(tile));
        event.registerBlockEntity(CANNON_CAP, ModRegistry.CANNON_TILE.get(),
                (tile, object2) -> new CannonPeripheral(tile));
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

    /*
    public static boolean isPeripheralCap(Capability<?> cap) {
        return cap == Capabilities.CAPABILITY_PERIPHERAL;
    }

    public static LazyOptional<Object> getPeripheralSupplier(SpeakerBlockTile tile) {
        return LazyOptional.of(() -> new SpeakerPeripheral(tile));
    }*/


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
        public String getMessage() {
            return tile.getMessage(false).getString();
        }

        @LuaFunction
        public void setMessage(String message) {
            tile.setMessage(Component.literal(message));
            tile.setChanged();
        }

        @LuaFunction
        public String getName() {
            return tile.getName().getString();
        }

        @LuaFunction
        public void setName(String name) {
            tile.setCustomName(Component.literal(name));
            tile.setChanged();
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

        public CannonPeripheral(CannonBlockTile tile) {
            this.tile = tile;
        }

        @LuaFunction
        public float getYaw() {
            Quaternionf orientation = tile.getLocalOrientation(1);
            EntityAngles angles = EntityAngles.fromQuaternion(orientation);
            return angles.yaw();
        }

        @LuaFunction
        public void setYaw(double value) {
            Quaternionf orientation = tile.getLocalOrientation(1);
            EntityAngles angles = EntityAngles.fromQuaternion(orientation);
            angles = angles.withYaw((float) value);

            tile.setLocalOrientation(angles.toQuaternion());
            tile.syncToClients(false);
        }

        @LuaFunction
        public float getPitch() {
            Quaternionf orientation = tile.getLocalOrientation(1);
            EntityAngles angles = EntityAngles.fromQuaternion(orientation);
            return angles.pitch();
        }

        @LuaFunction
        public void setPitch(double value) {
            Quaternionf orientation = tile.getLocalOrientation(1);
            EntityAngles angles = EntityAngles.fromQuaternion(orientation);
            angles = angles.withPitch((float) value);

            tile.setLocalOrientation(angles.toQuaternion());

            tile.syncToClients(false);
        }

        @LuaFunction
        public byte getPower() {
            return tile.getPowerLevel();
        }

        @LuaFunction
        public void setPower(int inPower) {
            byte power = (byte) Math.min(Math.max(inPower, 1), CannonBlockTile.MAX_POWER_LEVEL);
            tile.setFirePower(power);
            tile.syncToClients(false);
        }

        @LuaFunction
        public void ignite() {
            tile.ignite(null);
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
