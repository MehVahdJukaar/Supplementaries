package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.screens.BlackBoardScreen;
import net.mehvahdjukaar.supplementaries.common.block.IOnePlayerInteractable;
import net.mehvahdjukaar.supplementaries.common.block.IWaxable;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.common.items.components.BlackboardData;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public class BlackboardBlockTile extends BlockEntity implements
        IOnePlayerInteractable, IScreenProvider, IWaxable, IExtraModelDataProvider {

    public static final ModelDataKey<BlackboardData> BLACKBOARD_KEY = ModBlockProperties.BLACKBOARD;

    @Nullable
    private UUID playerWhoMayEdit = null;

    private BlackboardData data = null;

    public BlackboardBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BLACKBOARD_TILE.get(), pos, state);
        this.clearPixels();
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        builder.with(BLACKBOARD_KEY, getTextureKey());
    }

    public BlackboardData getTextureKey() {
        if (data == null) refreshTextureKey();
        return data;
    }

    public void refreshTextureKey() {
        this.data = BlackboardData.pack(this.pixels, this.getBlockState().getValue(BlackboardBlock.GLOWING), this.waxed);
    }

    @Override
    public void afterDataPacket(ExtraModelData oldData) {
        refreshTextureKey();
        IExtraModelDataProvider.super.afterDataPacket(oldData);
    }

    //I need this for when it's changed manually
    @Override
    public void setChanged() {
        if (this.level == null || this.level.isClientSide) return;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        super.setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.data =
                //TODO:use explicit component here instead
                this.waxed = tag.contains("Waxed") && tag.getBoolean("Waxed");
        this.pixels = new byte[16][16];
        if (tag.contains("Pixels")) {
            this.pixels = BlackboardData.unpackPixels(tag.getLongArray("Pixels"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.waxed) tag.putBoolean("Waxed", true);
        tag.putLongArray("Pixels", BlackboardData.packPixels(pixels));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!this.isEmpty()) {
            components.set(ModComponents.BLACKBOARD.get(), getTextureKey());
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        this.data = componentInput.getOrDefault(ModComponents.BLACKBOARD.get(), BlackboardData.EMPTY);
        this.components().
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("data");
    }

    public void clearPixels() {
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                this.pixels[x][y] = 0;
            }
        }
    }

    public boolean isEmpty() {
        boolean flag = false;
        for (byte[] pixel : pixels) {
            for (byte b : pixel) {
                if (b != 0) {
                    flag = true;
                    break;
                }
            }
        }
        return !flag;
    }

    public void setPixel(int x, int y, byte b) {
        this.pixels[x][y] = b;
    }

    public byte getPixel(int xx, int yy) {
        return this.pixels[xx][yy];
    }

    public void setPixels(byte[][] pixels) {
        this.pixels = pixels;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(NoticeBoardBlock.FACING);
    }

    @Override
    public void openScreen(Level level, BlockPos blockPos, Player player, Direction direction) {
        BlackBoardScreen.open(this);
    }

    @Override
    public void setWaxed(boolean b) {
        this.waxed = b;
    }

    @Override
    public boolean isWaxed() {
        return this.waxed;
    }

    @Override
    public UUID getPlayerWhoMayEdit() {
        return playerWhoMayEdit;
    }

    @Override
    public void setPlayerWhoMayEdit(UUID playerWhoMayEdit) {
        this.playerWhoMayEdit = playerWhoMayEdit;
    }

    public boolean tryAcceptingClientPixels(ServerPlayer player, byte[][] pixels) {
        if (!this.isEditingPlayer(player)) {
            Supplementaries.LOGGER.warn("Player {} just tried to change non-editable blackboard block",
                    player.getName().getString());
        }
        if (!Arrays.deepEquals(pixels, this.pixels)) {
            level.playSound(null, this.worldPosition, ModSounds.BLACKBOARD_DRAW.get(),
                    SoundSource.BLOCKS, 1, 1);

            this.setPlayerWhoMayEdit(null);
            this.setPixels(pixels);
            return true;
        }
        return false;

    }
}
