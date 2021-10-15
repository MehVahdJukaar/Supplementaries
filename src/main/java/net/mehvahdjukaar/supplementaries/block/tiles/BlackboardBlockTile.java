package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager.BlackboardKey;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public class BlackboardBlockTile extends BlockEntity implements IOwnerProtected {

    private UUID owner = null;

    public byte[][] pixels = new byte[16][16];

    //client side
    public BlackboardKey textureKey = null;
    //public static final ModelProperty<BlackboardKey> TEXTURE = new ModelProperty<>();
    //private final IModelData data;

    public BlackboardBlockTile() {
        super(ModRegistry.BLACKBOARD_TILE.get());
        //Arrays.fill(pixels, Arrays.fill(new boolean[], false));
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                this.pixels[x][y] = 0;
            }
        }
        //this.data = (new ModelDataMap.Builder()).withInitial(TEXTURE, null).build();
    }

    //I need this for when it's changed manually
    @Override
    public void setChanged() {
        if (this.level == null || this.level.isClientSide) return;
        this.setCorrectBlockState(this.getBlockState(), this.worldPosition, this.level);
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        super.setChanged();
    }

    public void setCorrectBlockState(BlockState state, BlockPos pos, Level world) {
        if (!world.isClientSide) {
            boolean written = !this.isEmpty();
            if (state.getValue(BlackboardBlock.WRITTEN) != written) {
                world.setBlock(pos, state.setValue(BlackboardBlock.WRITTEN, written), 2);
            }
        }
    }


    //public IModelData getModelData() return this.data;

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

    //client
    public void updateModelData() {
        this.textureKey = null;
        //this.textureKey = BlackboardTextureManager.INSTANCE.getUpdatedKey(this);
        //this.data.setData(TEXTURE, textureKey);
        //ModelDataManager.requestModelDataRefresh(this);
        //this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
    }

    @Override
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);
        loadFromTag(compound);
        this.loadOwner(compound);
        if (this.level != null && !this.level.isClientSide) {
            this.setCorrectBlockState(this.getBlockState(), this.worldPosition, this.level);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        this.saveToTag(compound);
        this.saveOwner(compound);
        return compound;
    }

    public CompoundTag saveToTag(CompoundTag compound) {
        compound.putLongArray("Pixels", packPixels(pixels));
        return compound;
    }

    public void loadFromTag(CompoundTag compound) {
        this.pixels = new byte[16][16];
        if (compound.contains("Pixels")) {
            this.pixels = unpackPixels(compound.getLongArray("Pixels"));
        }
        //TODO: backwards compat. remove
        if (compound.contains("pixels_0")) {
            for (int i = 0; i < 16; i++) {
                byte[] b = compound.getByteArray("pixels_" + i);
                if (b.length == 16) this.pixels[i] = b;
            }
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
        this.load(this.getBlockState(), pkt.getTag());
        this.updateModelData();
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(NoticeBoardBlock.FACING);
    }

    public float getYaw() {
        return -this.getDirection().toYRot();
    }

    @Override
    public double getViewDistance() {
        return 96;
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
}
