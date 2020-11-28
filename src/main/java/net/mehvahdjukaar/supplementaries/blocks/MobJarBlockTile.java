package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.JarMobType;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.monster.MagmaCubeEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.Random;


public class MobJarBlockTile extends TileEntity implements ITickableTileEntity {
    public Entity mob = null;
    public CompoundNBT entityData = null;
    public boolean entityChanged = false;
    public float yOffset = 1;
    public float scale = 1;
    public float jumpY = 0;
    public float prevJumpY = 0;
    public float yVel = 0;
    private Random rand = new Random();
    public JarMobType animationType = JarMobType.DEFAULT;

    public MobJarBlockTile() {
        super(Registry.MOB_JAR_TILE);
    }

    public void updateMob(){

        Entity entity  = EntityType.loadEntityAndExecute(this.entityData, this.world, o -> o);
        if(entity==null && this.entityData.contains("id")){
            boolean flag = this.entityData.get("id").getString().equals("minecraft:bee");
            if(flag) entity = new BeeEntity(EntityType.BEE, this.world);
        }
        this.mob = entity;
        this.animationType = JarMobType.getJarMobType(entity);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        if(compound.contains("jar_mob")){
            this.entityData = compound.getCompound("jar_mob");
            this.entityChanged = true;
        }
        //TODO: reformat all nbts to be consistent
        this.scale = compound.getFloat("scale");
        this.yOffset = compound.getFloat("yOffset");
        this.animationType = JarMobType.values()[compound.getInt("animation_type")];

    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if(this.entityData!=null)
            compound.put("jar_mob", this.entityData);
        compound.putFloat("scale",this.scale);
        compound.putFloat("yOffset",this.yOffset);
        compound.putInt("animation_type", this.animationType.ordinal());
        return compound;
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

    public void tick() {


        if(this.entityChanged&& this.entityData!=null){
            this.updateMob();
            this.entityChanged=false;
        }
        if(!this.world.isRemote)return;
        //for client side animation
        if(this.mob!=null) {

            this.mob.ticksExisted++;
            this.prevJumpY=this.jumpY;
            switch (this.animationType){
                default:
                case DEFAULT:
                    break;
                case SLIME:
                case MAGMA_CUBE:
                    SlimeEntity slime = (SlimeEntity)this.mob;
                    slime.squishFactor += (slime.squishAmount - slime.squishFactor) * 0.5F;
                    slime.prevSquishFactor = slime.squishFactor;
                    //move
                    if(this.yVel!=0)
                        this.jumpY=Math.max(0,this.jumpY+this.yVel);
                    if(jumpY!=0){
                        //decelerate
                        this.yVel = this.yVel-0.005f;
                    }
                    //on ground
                    else {
                        if(this.yVel!=0){
                            //land
                            this.yVel=0;
                            slime.squishAmount = -0.5f;
                        }
                        if (this.rand.nextFloat() > 0.98) {
                            //jump
                            this.yVel = 0.05f;
                            slime.squishAmount = 1.0F;
                        }
                    }
                    slime.squishAmount *= 0.6F;
                    break;
                case VEX:
                    this.jumpY = 0.04f*MathHelper.sin(this.mob.ticksExisted/10f) -0.03f;
                    break;
                case ENDERMITE:
                    if(this.rand.nextFloat()>0.7f){
                        this.world.addParticle(ParticleTypes.PORTAL, this.pos.getX()+0.5f, this.pos.getY()+0.2f,
                                this.pos.getZ()+0.5f, (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D);
                    }
            }

        }
    }

    public Direction getDirection() {
        return this.getBlockState().get(ClockBlock.FACING);
    }

}