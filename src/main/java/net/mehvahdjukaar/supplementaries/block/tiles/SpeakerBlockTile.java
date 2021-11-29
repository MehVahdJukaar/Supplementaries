package net.mehvahdjukaar.supplementaries.block.tiles;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.block.blocks.SpeakerBlock;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.network.ClientBoundPlaySpeakerMessagePacket;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class SpeakerBlockTile extends BlockEntity implements Nameable, IOwnerProtected, IPeripheralProvider {
    private UUID owner = null;

    public String message = "";
    public boolean narrator = false;
    public double volume = 1;
    private Component customName;

    public SpeakerBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.SPEAKER_BLOCK_TILE.get(), pos, state);
    }

    public void setCustomName(Component name) {
        this.customName = name;
    }

    public Component getName() {
        return this.customName != null ? this.customName : this.getDefaultName();
    }

    public Component getCustomName() {
        return this.customName;
    }

    public Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.speaker_block");
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains("CustomName", 8)) {
            this.customName = Component.Serializer.fromJson(compound.getString("CustomName"));
        }

        this.message = compound.getString("Message");
        if (!ServerConfigs.cached.SPEAKER_NARRATOR) this.narrator = false;
        else this.narrator = compound.getBoolean("Narrator");
        this.volume = compound.getDouble("Volume");
        this.loadOwner(compound);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        if (this.customName != null) {
            compound.putString("CustomName", Component.Serializer.toJson(this.customName));
        }
        compound.putString("Message", this.message);
        compound.putBoolean("Narrator", this.narrator);
        compound.putDouble("Volume", this.volume);
        this.saveOwner(compound);
        return compound;
    }

    public void sendMessage() {
        BlockState state = this.getBlockState();
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        ResourceKey<Level> dimension = level.dimension();
        if (currentServer != null && !this.message.equals("")) {
            // particle
            BlockPos pos = this.getBlockPos();
            level.blockEvent(pos, this.getBlockState().getBlock(), 0, 0);
            PlayerList players = currentServer.getPlayerList();

            Style style = !state.getValue(SpeakerBlock.ANTIQUE) ? Style.EMPTY.applyFormats(ChatFormatting.ITALIC) :
                    Style.EMPTY.withFont(Textures.ANTIQUABLE_FONT).applyFormats(ChatFormatting.ITALIC);

            Component message = new TextComponent(this.getName().getString() + ": " + this.message)
                    .withStyle(style);

            players.broadcast(null, pos.getX(), pos.getY(), pos.getZ(),
                    ServerConfigs.cached.SPEAKER_RANGE * this.volume,
                    dimension, NetworkHandler.INSTANCE.toVanillaPacket(
                            new ClientBoundPlaySpeakerMessagePacket(message, this.narrator),
                            NetworkDirection.PLAY_TO_CLIENT));
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @NotNull
    @Override
    public LazyOptional<IPeripheral> getPeripheral(@NotNull Level world, @NotNull BlockPos pos, @NotNull Direction side) {
        return peripheral;
    }

    private final LazyOptional<IPeripheral> peripheral = LazyOptional.of(() -> new SpeakerPeripheral(this));

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
            tile.customName = new TextComponent(name);
            tile.setChanged();
        }

        @LuaFunction
        public final String getName() {
            return tile.customName.getString();
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
            if (tile.level.isClientSide) return;
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