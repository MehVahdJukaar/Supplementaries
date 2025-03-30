package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.IWaxable;
import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.screens.BlackBoardScreen;
import net.mehvahdjukaar.supplementaries.common.block.IGlowable;
import net.mehvahdjukaar.supplementaries.common.block.IOnePlayerInteractable;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.common.items.components.BlackboardData;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock.colorToByte;

public class BlackboardBlockTile extends BlockEntity implements
        IOnePlayerInteractable, IScreenProvider, IWaxable, IGlowable, IExtraModelDataProvider {

    public static final ModelDataKey<BlackboardData> BLACKBOARD_KEY = ModBlockProperties.BLACKBOARD;

    @Nullable
    private UUID playerWhoMayEdit = null;

    private BlackboardData data = BlackboardData.EMPTY;

    public BlackboardBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BLACKBOARD_TILE.get(), pos, state);
        this.clearPixels();
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        builder.with(BLACKBOARD_KEY, data);
    }

    //I need this for when it's changed manually
    @Override
    public void setChanged() {
        if (this.level == null || this.level.isClientSide) return;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        super.setChanged();
    }

    @Override
    public boolean tryOpeningEditGui(ServerPlayer player, BlockPos pos, ItemStack stack) {
        if (isWaxed()) return false;
        return IOnePlayerInteractable.super.tryOpeningEditGui(player, pos, stack);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        //old logic
        if (tag.contains("Waxed") || tag.contains("Pixels")) {
            boolean waxed = tag.contains("Waxed") && tag.getBoolean("Waxed");
            byte[][] pixels = new byte[16][16];
            if (tag.contains("Pixels")) {
                pixels = BlackboardData.unpackPixels(tag.getLongArray("Pixels"));
            }
            this.data = new BlackboardData(pixels, false, waxed);
        } else if (tag.contains("values")) {
            this.data = BlackboardData.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
        }
        this.requestModelReload();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.data.equals(BlackboardData.EMPTY)) {
            tag.merge((CompoundTag) BlackboardData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow());
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!this.isEmpty()) {
            components.set(ModComponents.BLACKBOARD.get(), data);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        var data = componentInput.get(ModComponents.BLACKBOARD.get());
        if (data != null) {
            this.data = data;
        } else {
            this.clearPixels();
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        //same as in the components itself
        tag.remove("values");
        tag.remove("glow");
        tag.remove("waxed");
    }

    public void clearPixels() {
        this.data = this.data.makeCleared();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public void setPixel(int x, int y, byte b) {
        this.data = this.data.withPixel(x, y, b);
    }

    public byte getPixel(int xx, int yy) {
        return this.data.getPixel(xx, yy);
    }

    public void setPixels(byte[][] pixels) {
        this.data = this.data.withPixels(pixels);
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
        this.data = this.data.withWaxed(b);
    }

    @Override
    public boolean isWaxed() {
        return this.data.isWaxed();
    }

    @Override
    public void setGlowing(boolean b) {
        this.data = this.data.withGlow(b);
    }

    @Override
    public boolean isGlowing() {
        return this.data.isGlow();
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
        if (!this.isEditingPlayer(player) || this.isWaxed()) {
            Supplementaries.LOGGER.warn("Player {} just tried to change non-editable blackboard block",
                    player.getName().getString());
        }
        //check if all pixels are non colored
        if (!CommonConfigs.Building.BLACKBOARD_COLOR.get()) {
            byte black = colorToByte(DyeColor.BLACK);
            byte white = colorToByte(DyeColor.WHITE);
            for (byte[] pixel : pixels) {
                for (byte b : pixel) {
                    if (b != black && b != white) {
                        Supplementaries.LOGGER.warn("Player {} just tried to change blackboard block with colored pixels",
                                player.getName().getString());
                        return false;
                    }
                }
            }
        }
        if (!data.hasSamePixels(pixels)) {
            level.playSound(null, this.worldPosition, ModSounds.BLACKBOARD_DRAW.get(),
                    SoundSource.BLOCKS, 1, 1);

            this.setPlayerWhoMayEdit(null);
            this.setPixels(pixels);
            return true;
        }
        return false;

    }

}
