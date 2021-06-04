package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.block.util.ITextHolder;
import net.mehvahdjukaar.supplementaries.block.util.TextHolder;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;


public class FenceSignBlockTile extends TileEntity implements ITextHolder, IBlockHolder {

    public TextHolder textHolder;

    public BlockState fenceBlock = Blocks.AIR.defaultBlockState();
    public Direction signFacing = Direction.NORTH;
    public BlockState signBlock = Blocks.AIR.defaultBlockState();

    public static final int LINES = 4;

    public FenceSignBlockTile() {
        super(Registry.SIGN_POST_TILE.get());
        this.textHolder = new TextHolder(LINES);
    }

    @Override
    public BlockState getHeldBlock() {
        return this.fenceBlock;
    }

    @Override
    public boolean setHeldBlock(BlockState state) {
        this.fenceBlock = state;
        return true;
    }

    @Override
    public TextHolder getTextHolder(){ return this.textHolder; }

    @Override
    public double getViewDistance() {
        return 128;
    }

    @Override
    public void setChanged() {
        if(this.level==null)return;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        super.setChanged();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return new AxisAlignedBB(this.getBlockPos().offset(-0.25,0,-0.25), this.getBlockPos().offset(1.25,1,1.25));
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);

        this.textHolder.read(compound);
        this.fenceBlock = NBTUtil.readBlockState(compound.getCompound("Fence"));
        this.signBlock = NBTUtil.readBlockState(compound.getCompound("Sign"));
        this.signFacing = Direction.from3DDataValue(compound.getInt("Facing"));

    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);

        this.textHolder.write(compound);
        compound.put("Fence", NBTUtil.writeBlockState(fenceBlock));
        compound.put("Sign", NBTUtil.writeBlockState(signBlock));
        compound.putInt("Facing", this.signFacing.get3DDataValue());

        return compound;
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
}