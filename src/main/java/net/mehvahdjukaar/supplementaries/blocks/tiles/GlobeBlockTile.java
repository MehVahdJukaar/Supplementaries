package net.mehvahdjukaar.supplementaries.blocks.tiles;

import net.mehvahdjukaar.supplementaries.blocks.GlobeBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.GlobeType;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class GlobeBlockTile extends TileEntity implements ITickableTileEntity, INameable {
    public float yaw = 0;
    public float prevYaw = 0;
    public int face = 0;
    public GlobeType type = GlobeType.DEFAULT;
    private ITextComponent customName;

    public GlobeBlockTile() {
        super(Registry.GLOBE_TILE);
    }


    public void setCustomName(ITextComponent name) {
        this.customName = name;
        this.type = GlobeType.getGlobeType(name.toString());
    }

    public ITextComponent getName() {
        return this.customName != null ? this.customName : this.getDefaultName();
    }

    public ITextComponent getCustomName() {
        return this.customName;
    }

    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.globe");
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 80;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        if (compound.contains("CustomName", 8)) {
            this.customName = ITextComponent.Serializer.getComponentFromJson(compound.getString("CustomName"));
        }
        this.face = compound.getInt("Face");
        this.yaw = compound.getFloat("Yaw");
        this.type = GlobeType.values()[compound.getInt("GlobeType")];
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if (this.customName != null) {
            compound.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
        }
        compound.putInt("Face",this.face);
        compound.putFloat("Yaw",this.yaw);
        compound.putInt("GlobeType", this.type.ordinal());
        return compound;
    }

    public void spin(){
        int spin = 360;
        int inc = 90;
        this.face=(this.face-=inc)%360;
        this.yaw=(this.yaw+spin+inc);
        this.prevYaw=(this.prevYaw+spin+inc);
        this.markDirty();
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            this.spin();
            return true;
        } else {
            return super.receiveClientEvent(id, type);
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(), pkt.getNbtCompound());
    }

    @Override
    public void tick() {
        this.prevYaw=this.yaw;
        if(this.yaw!=0){
            if(this.yaw<0){
                this.yaw=0;
                this.world.updateComparatorOutputLevel(this.pos, this.getBlockState().getBlock());
            }
            else {
                this.yaw = (this.yaw * 0.94f) - 0.7f;
            }
        }

    }

    public Direction getDirection(){
        return this.getBlockState().get(GlobeBlock.FACING);
    }
}