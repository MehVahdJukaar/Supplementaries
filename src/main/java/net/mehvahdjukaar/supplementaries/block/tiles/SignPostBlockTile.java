package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.block.util.ITextHolder;
import net.mehvahdjukaar.supplementaries.block.util.TextHolder;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.TempWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;


public class SignPostBlockTile extends TileEntity implements ITextHolder, IBlockHolder {

    public TextHolder textHolder;

    public BlockState fenceBlock = Blocks.AIR.defaultBlockState();
    public float yawUp = 0;
    public float yawDown = 0;
    public boolean leftUp = true;
    public boolean leftDown = false;
    public boolean up = false;
    public boolean down = false;

    public IWoodType woodTypeUp = VanillaWoodTypes.OAK;
    public IWoodType woodTypeDown = VanillaWoodTypes.OAK;

    public SignPostBlockTile() {
        super(Registry.SIGN_POST_TILE.get());
        this.textHolder = new TextHolder(2);
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
        return 156;
    }

    @Override
    public void setChanged() {
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        super.setChanged();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return new AxisAlignedBB(this.getBlockPos().offset(-0.25,0,-0.25), this.getBlockPos().offset(1.25,1,1.25));
    }

    //TODO: maybe add constraints to this so it snaps to 22.5deg
    public void pointToward(BlockPos targetPos, boolean up){
        //int r = MathHelper.floor((double) ((180.0F + yaw) * 16.0F / 360.0F) + 0.5D) & 15;
        // r*-22.5f;
        float yaw = (float)(Math.atan2(targetPos.getX() - worldPosition.getX(), targetPos.getZ() - worldPosition.getZ()) * 180d / Math.PI);
        if(up){
            this.yawUp = MathHelper.wrapDegrees(yaw - (this.leftUp ? 180 : 0));
        }
        else {
            this.yawDown = MathHelper.wrapDegrees(yaw - (this.leftDown ? 180 : 0));
        }
    }

    public float getPointingYaw(boolean up){
        if(up){
            return MathHelper.wrapDegrees(-this.yawUp - (this.leftUp ? 180 : 0));
        }
        else {
            return MathHelper.wrapDegrees(-this.yawDown - (this.leftDown ? 180 : 0));
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);

        this.textHolder.read(compound);

        this.fenceBlock = NBTUtil.readBlockState(compound.getCompound("Fence"));
        this.yawUp = compound.getFloat("YawUp");
        this.yawDown = compound.getFloat("YawDown");
        this.leftUp = compound.getBoolean("LeftUp");
        this.leftDown = compound.getBoolean("LeftDown");
        this.up = compound.getBoolean("Up");
        this.down = compound.getBoolean("Down");
        this.woodTypeUp = WoodTypes.fromNBT(compound.getString("TypeUp"));
        this.woodTypeDown = WoodTypes.fromNBT(compound.getString("TypeDown"));


        //remove in the future
        if(compound.contains("WoodTypeUp"))
        this.woodTypeUp = TempWoodType.values()[compound.getInt("WoodTypeUp")].convertWoodType();
        if(compound.contains("WoodTypeDown"))
        this.woodTypeDown = TempWoodType.values()[compound.getInt("WoodTypeDown")].convertWoodType();

        if(compound.contains("Wood_type_up"))this.woodTypeUp = TempWoodType.values()[compound.getInt("Wood_type_up")].convertWoodType();
        if(compound.contains("Wood_type_down"))this.woodTypeDown = TempWoodType.values()[compound.getInt("Wood_type_down")].convertWoodType();
        if(compound.contains("Left_up"))this.leftUp=compound.getBoolean("Left_up");
        if(compound.contains("Left_down"))this.leftDown=compound.getBoolean("Left_down");
        if(compound.contains("Yaw_up"))this.yawUp=compound.getFloat("Yaw_up");
        if(compound.contains("Yaw_down"))this.yawDown=compound.getFloat("Yaw_down");

    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);

        this.textHolder.write(compound);

        compound.put("Fence", NBTUtil.writeBlockState(fenceBlock));
        compound.putFloat("YawUp",this.yawUp);
        compound.putFloat("YawDown",this.yawDown);
        compound.putBoolean("LeftUp",this.leftUp);
        compound.putBoolean("LeftDown",this.leftDown);
        compound.putBoolean("Up", this.up);
        compound.putBoolean("Down", this.down);
        compound.putString("TypeUp", this.woodTypeUp.toNBT());
        compound.putString("TypeDown", this.woodTypeDown.toNBT());

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