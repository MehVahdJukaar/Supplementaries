package net.mehvahdjukaar.supplementaries.block.tiles;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.SendSpeakerBlockMessagePacket;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.INameable;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class SpeakerBlockTile extends TileEntity implements INameable, IOwnerProtected, IPeripheralProvider {
    private UUID owner = null;

    public String message = "";
    public boolean narrator = false;
    public double volume = 1;
    private ITextComponent customName;
    public SpeakerBlockTile() {
        super(ModRegistry.SPEAKER_BLOCK_TILE.get());
    }

    public void setCustomName(ITextComponent name) {
        this.customName = name;
    }

    @Override
    public ITextComponent getName() {
        return this.customName != null ? this.customName : this.getDefaultName();
    }

    @Override
    public ITextComponent getCustomName() {
        return this.customName;
    }

    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.speaker_block");
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        if (compound.contains("CustomName", 8)) {
            this.customName = ITextComponent.Serializer.fromJson(compound.getString("CustomName"));
        }

        this.message = compound.getString("Message");
        this.narrator = compound.getBoolean("Narrator");
        this.volume = compound.getDouble("Volume");
        this.loadOwner(compound);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        if (this.customName != null) {
            compound.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
        }
        compound.putString("Message", this.message);
        compound.putBoolean("Narrator", this.narrator);
        compound.putDouble("Volume", this.volume);
        this.saveOwner(compound);
        return compound;
    }

    public void sendMessage(){
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        RegistryKey<World> dimension = level.dimension();
        if (currentServer != null && !this.message.equals("")) {
            // particle
            BlockPos pos = this.getBlockPos();
            level.blockEvent(pos, this.getBlockState().getBlock(), 0, 0);
            PlayerList players = currentServer.getPlayerList();

            ITextComponent message = new StringTextComponent(this.getName().getString() + ": " + this.message).withStyle(TextFormatting.ITALIC);

            players.broadcast(null, pos.getX(), pos.getY(), pos.getZ(), ServerConfigs.cached.SPEAKER_RANGE * this.volume,
                    dimension, NetworkHandler.INSTANCE.toVanillaPacket(
                            new SendSpeakerBlockMessagePacket(message, this.narrator),
                            NetworkDirection.PLAY_TO_CLIENT));
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
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
    public LazyOptional<IPeripheral> getPeripheral(@NotNull World world, @NotNull BlockPos pos, @NotNull Direction side) {
        return peripheral;
    }

    private final LazyOptional<IPeripheral> peripheral = LazyOptional.of(() -> new SpeakerPeripheral(this));

    public static class SpeakerPeripheral implements IPeripheral{

        private final SpeakerBlockTile tile;

        public SpeakerPeripheral(SpeakerBlockTile tile) {
            this.tile = tile;
        }

        @LuaFunction
        public final void setNarrator(boolean narratorOn){
            tile.narrator = narratorOn;
            tile.setChanged();
        }
        @LuaFunction
        public final boolean isNarratorEnabled(){
            return tile.narrator;
        }
        @LuaFunction
        public final void setMessage(String message){
            tile.message = message;
            tile.setChanged();
        }
        @LuaFunction
        public final String getMessage(){
            return tile.message;
        }
        @LuaFunction
        public final void setName(String name){
            tile.customName = new StringTextComponent(name);
            tile.setChanged();
        }
        @LuaFunction
        public final String getName(){
            return tile.customName.getString();
        }
        @LuaFunction
        public final double getVolume(){
            return tile.volume;
        }
        @LuaFunction
        public final void setVolume(double volume){
            tile.volume = volume;
            tile.setChanged();
        }
        @LuaFunction
        public final void activate(){
            if(tile.level.isClientSide) return;
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