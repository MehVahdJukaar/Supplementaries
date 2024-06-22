package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.IOwnerProtected;
import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.BlackboardManager.Key;
import net.mehvahdjukaar.supplementaries.client.screens.BlackBoardScreen;
import net.mehvahdjukaar.supplementaries.common.block.IOnePlayerInteractable;
import net.mehvahdjukaar.supplementaries.common.block.IWaxable;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BlackboardBlockTile extends BlockEntity implements IOwnerProtected,
        IOnePlayerInteractable, IScreenProvider, IWaxable, IExtraModelDataProvider {

    public static final ModelDataKey<Key> BLACKBOARD_KEY = ModBlockProperties.BLACKBOARD;

    private UUID owner = null;
    private boolean waxed = false;
    private byte[][] pixels = new byte[16][16];
    @Nullable
    private UUID playerWhoMayEdit = null;

    //client side
    private Key textureKey = null;

    public BlackboardBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BLACKBOARD_TILE.get(), pos, state);
        this.clear();
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        builder.with(BLACKBOARD_KEY, getTextureKey());
    }

    public Key getTextureKey() {
        if (textureKey == null) refreshTextureKey();
        return textureKey;
    }

    public void refreshTextureKey() {
        this.textureKey = Key.of(packPixels(this.pixels), this.getBlockState().getValue(BlackboardBlock.GLOWING));
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
    public void load(CompoundTag compound) {
        super.load(compound);
        loadFromTag(compound);
        this.loadOwner(compound);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        this.savePixels(compound);
        this.saveOwner(compound);
    }

    public CompoundTag savePixels(CompoundTag compound) {
        if (this.waxed) compound.putBoolean("Waxed", true);
        compound.putLongArray("Pixels", packPixels(pixels));
        return compound;
    }

    public void loadFromTag(CompoundTag compound) {
        this.waxed = compound.contains("Waxed") && compound.getBoolean("Waxed");
        this.pixels = new byte[16][16];
        if (compound.contains("Pixels")) {
            this.pixels = unpackPixels(compound.getLongArray("Pixels"));
        }
    }

    public static long[] packPixels(byte[][] pixels) {
        long[] packed = new long[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            long l = 0;
            for (int j = 0; j < pixels[i].length; j++) {
                l = l | (((long) (pixels[i][j] & 15)) << j * 4);
            }
            packed[i] = l;
        }
        return packed;
    }

    public static byte[][] unpackPixels(long[] packed) {
        byte[][] bytes = new byte[16][16];
        for (int i = 0; i < packed.length; i++) {
            for (int j = 0; j < 16; j++) {
                bytes[i][j] = (byte) ((packed[i] >> j * 4) & 15);
            }
        }
        return bytes;
    }

    //string length = 16*4 = 64
    public static String packPixelsToString(long[] packed) {
        StringBuilder builder = new StringBuilder();
        for (var l : packed) {
            char a = (char) (l & Character.MAX_VALUE);
            char b = (char) (l >> 16 & Character.MAX_VALUE);
            char c = (char) (l >> 32 & Character.MAX_VALUE);
            char d = (char) (l >> 48 & Character.MAX_VALUE);
            builder.append(a).append(b).append(c).append(d);
        }
        return builder.toString();
    }

    public static long[] unpackPixelsFromString(String packed) {
        long[] unpacked = new long[16];
        var chars = packed.toCharArray();
        int j = 0;
        for (int i = 0; i + 3 < chars.length; i += 4) {
            unpacked[j] = (long) chars[i + 3] << 48 | (long) chars[i + 2] << 32 | (long) chars[i + 1] << 16 | chars[i];
            j++;
        }
        return unpacked;
    }

    public static long[] unpackPixelsFromStringWhiteOnly(String packed) {
        long[] unpacked = new long[16];
        var chars = packed.toCharArray();
        int j = 0;
        for (int i = 0; i + 3 < chars.length; i += 4) {
            long l = 0;
            char c = chars[i];
            for (int k = 0; k < 4; k++) {
                l = l | (((c >> k) & 1) << 4 * k);
            }
            char c2 = chars[i + 1];
            for (int k = 0; k < 4; k++) {
                l = l | ((long) ((c2 >> k) & 1) << (16 + (4 * k)));
            }
            char c3 = chars[i + 2];
            for (int k = 0; k < 4; k++) {
                l = l | ((long) ((c3 >> k) & 1) << (32 + (4 * k)));
            }
            char c4 = chars[i + 3];
            for (int k = 0; k < 4; k++) {
                l = l | ((long) ((c4 >> k) & 1) << (48 + (4 * k)));
            }
            unpacked[j] = l;
            j++;
        }
        return unpacked;
    }

    public static String packPixelsToStringWhiteOnly(long[] packed) {
        StringBuilder builder = new StringBuilder();
        for (var l : packed) {
            char c = 0;
            for (int k = 0; k < 4; k++) {
                byte h = (byte) ((l >> 4 * k) & 1);
                c = (char) (c | (h << k));
            }
            char c1 = 0;
            for (int k = 0; k < 4; k++) {
                byte h = (byte) ((l >> (16 + (4 * k))) & 1);
                c1 = (char) (c1 | (h << k));
            }
            char c2 = 0;
            for (int k = 0; k < 4; k++) {
                byte h = (byte) ((l >> (32 + (4 * k))) & 1);
                c2 = (char) (c2 | (h << k));
            }
            char c3 = 0;
            for (int k = 0; k < 4; k++) {
                byte h = (byte) ((l >> (48 + (4 * k))) & 1);
                c3 = (char) (c3 | (h << k));
            }
            builder.append(c).append(c1).append(c2).append(c3);
        }
        return builder.toString();
    }

    public void clear() {
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
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(NoticeBoardBlock.FACING);
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

    @Override
    public void openScreen(Level level, BlockPos pos, Player player) {
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
        if (this.isEditingPlayer(player)) {
            level.playSound(null, this.worldPosition, ModSounds.BLACKBOARD_DRAW.get(),
                    SoundSource.BLOCKS, 1, 1);
            this.setPixels(pixels);
            this.setPlayerWhoMayEdit(null);
            return true;
        } else {
            Supplementaries.LOGGER.warn("Player {} just tried to change non-editable blackboard block",
                    player.getName().getString());
        }
        return false;
    }
}
