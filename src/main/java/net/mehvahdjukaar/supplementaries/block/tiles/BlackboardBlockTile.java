package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager.BlackboardKey;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;


public class BlackboardBlockTile extends TileEntity {

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

    public void setCorrectBlockState(BlockState state, BlockPos pos, World world){
        if (!world.isClientSide) {
            world.setBlock(pos, state.setValue(BlackboardBlock.WRITTEN, !this.isEmpty()), 2);
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
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        loadFromTag(compound);
        if(this.level != null  && !this.level.isClientSide){
            this.setCorrectBlockState(this.getBlockState(), this.worldPosition, this.level);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        this.saveToTag(compound);
        return compound;
    }

    public CompoundNBT saveToTag(CompoundNBT compound) {
        compound.putLongArray("Pixels", packPixels(pixels));
        return compound;
    }

    public void loadFromTag(CompoundNBT compound) {
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
}
