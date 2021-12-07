package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.util.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.block.util.TextHolder;
import net.mehvahdjukaar.supplementaries.client.gui.SignPostGui;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public class SignPostBlockTile extends MimicBlockTile implements ITextHolderProvider, IOwnerProtected {
    private UUID owner = null;

    //is holding a framed fence (for framed blocks mod compat)
    public boolean framed = false;
    public static final ModelProperty<Boolean> FRAMED = BlockProperties.FRAMED;

    //TODO: make sing class and clean this up
    public TextHolder textHolder;

    public float yawUp = 0;
    public float yawDown = 0;
    public boolean leftUp = true;
    public boolean leftDown = false;
    public boolean up = false;
    public boolean down = false;

    public IWoodType woodTypeUp = VanillaWoodTypes.OAK;
    public IWoodType woodTypeDown = VanillaWoodTypes.OAK;

    public SignPostBlockTile() {
        super(ModRegistry.SIGN_POST_TILE.get());
        this.textHolder = new TextHolder(2);
    }

    @Override
    public IModelData getModelData() {
        //return data;
        return new ModelDataMap.Builder()
                .withInitial(FRAMED,this.framed)
                .withInitial(MIMIC, this.getHeldBlock())
                .build();
    }

    @Override
    public TextHolder getTextHolder(){ return this.textHolder; }

    @OnlyIn(Dist.CLIENT)
    public Screen getScreen() {
        return new SignPostGui(this);
    }

    @Override
    public double getViewDistance() {
        return 96;
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
        this.framed = compound.getBoolean("Framed");

        this.textHolder.read(compound);

        //TODO: remove in the future
        if(compound.contains("Fence"))
            this.mimic = NBTUtil.readBlockState(compound.getCompound("Fence"));

        this.yawUp = compound.getFloat("YawUp");
        this.yawDown = compound.getFloat("YawDown");
        this.leftUp = compound.getBoolean("LeftUp");
        this.leftDown = compound.getBoolean("LeftDown");
        this.up = compound.getBoolean("Up");
        this.down = compound.getBoolean("Down");
        this.woodTypeUp = WoodTypes.fromNBT(compound.getString("TypeUp"));
        this.woodTypeDown = WoodTypes.fromNBT(compound.getString("TypeDown"));
        this.loadOwner(compound);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.putBoolean("Framed",this.framed);

        this.textHolder.write(compound);

        compound.putFloat("YawUp",this.yawUp);
        compound.putFloat("YawDown",this.yawDown);
        compound.putBoolean("LeftUp",this.leftUp);
        compound.putBoolean("LeftDown",this.leftDown);
        compound.putBoolean("Up", this.up);
        compound.putBoolean("Down", this.down);
        compound.putString("TypeUp", this.woodTypeUp.toNBT());
        compound.putString("TypeDown", this.woodTypeDown.toNBT());
        this.saveOwner(compound);
        return compound;
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