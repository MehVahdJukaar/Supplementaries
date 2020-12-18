package net.mehvahdjukaar.supplementaries.blocks.tiles;

import net.mehvahdjukaar.supplementaries.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.JarMobType;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class CageBlockTile extends TileEntity implements ITickableTileEntity {


    //mob jar code
    public Entity mob = null;
    public CompoundNBT entityData = null;
    public boolean entityChanged = true;
    public float yOffset = 1;
    public float scale = 1;
    public float jumpY = 0;
    public float prevJumpY = 0;
    public float yVel = 0;
    private final Random rand = new Random();
    public JarMobType animationType = CommonUtil.JarMobType.DEFAULT;

    public CageBlockTile() {
        super(Registry.CAGE_TILE);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 80;
    }


    public void saveToNbt(ItemStack stack){
        if(this.mob==null||entityData==null)return;
        //same as CommonUtil.createNBT
        CompoundNBT cmp = new CompoundNBT();
        cmp.putFloat("Scale", this.scale);
        cmp.putFloat("YOffset", this.yOffset);
        cmp.putString("Name",this.mob.getName().getString());
        stack.setTagInfo("CachedJarMobValues", cmp);
        stack.setTagInfo("JarMob", entityData);
    }


    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        //mob jar
        if(compound.contains("jar_mob")){
            this.entityData = compound.getCompound("jar_mob");
            //this.updateMob();
            this.entityChanged = true;
        }
        this.scale = compound.getFloat("scale");
        this.yOffset = compound.getFloat("y_offset");
        this.animationType = JarMobType.values()[compound.getInt("animation_type")];
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        //mob jar
        if(this.entityData!=null)
            compound.put("jar_mob", this.entityData);
        compound.putFloat("scale",this.scale);
        compound.putFloat("y_offset",this.yOffset);
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

    //mob jar code

    public Direction getDirection() {
        return this.getBlockState().get(ClockBlock.FACING);
    }

    public void tick() {
        if(this.entityChanged && this.entityData!=null)this.updateMob();
        if (!this.world.isRemote) return;
        //for client side animation
        if (this.mob != null) {
            this.mob.ticksExisted++;
            this.prevJumpY = this.jumpY;
            switch (this.animationType) {
                default:
                case DEFAULT:
                    break;
                case SLIME:
                case MAGMA_CUBE:
                    SlimeEntity slime = (SlimeEntity) this.mob;
                    slime.squishFactor += (slime.squishAmount - slime.squishFactor) * 0.5F;
                    slime.prevSquishFactor = slime.squishFactor;
                    //move
                    if (this.yVel != 0)
                        this.jumpY = Math.max(0, this.jumpY + this.yVel);
                    if (jumpY != 0) {
                        //decelerate
                        this.yVel = this.yVel - 0.010f;
                    }
                    //on ground
                    else {
                        if (this.yVel != 0) {
                            //land
                            this.yVel = 0;
                            slime.squishAmount = -0.5f;
                        }
                        if (this.rand.nextFloat() > 0.985) {
                            //jump
                            this.yVel = 0.08f;
                            slime.squishAmount = 1.0F;
                        }
                    }
                    slime.squishAmount *= 0.6F;
                    break;
                case VEX:
                    this.jumpY = 0.04f * MathHelper.sin(this.mob.ticksExisted / 10f) - 0.03f;
                    break;
                case ENDERMITE:
                    if (this.rand.nextFloat() > 0.7f) {
                        this.world.addParticle(ParticleTypes.PORTAL, this.pos.getX() + 0.5f, this.pos.getY() + 0.2f,
                                this.pos.getZ() + 0.5f, (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D);
                    }
                    break;
                case PARROT:
                    ParrotEntity parrot = (ParrotEntity) this.mob;
                    parrot.livingTick();
                    parrot.setOnGround(false);
                    this.jumpY=0.0625f;
                    break;
                case PIXIE:
                    LivingEntity le = ((LivingEntity)this.mob);
                    le.livingTick();
                    le.lastTickPosY=this.pos.getY();
                    le.setPosition(le.getPosX(),this.pos.getY(),le.getPosZ());
                    break;
                case RABBIT:
                    RabbitEntity rabbit = (RabbitEntity) this.mob;
                    //move
                    if (this.yVel != 0)
                        this.jumpY = Math.max(0, this.jumpY + this.yVel);
                    if (jumpY != 0) {
                        //decelerate
                        this.yVel = this.yVel - 0.017f;
                    }
                    //on ground
                    else {
                        if (this.yVel != 0) {
                            //land
                            this.yVel = 0;
                        }
                        if (this.rand.nextFloat() > 0.985) {
                            //jump
                            this.yVel = 0.093f;
                            rabbit.startJumping();
                        }
                    }
                    //handles actual animation without using reflections
                    rabbit.livingTick();
                    //TODO: living tick causes collisions to happen
                    break;
                case CAT:
                    CatEntity cat = (CatEntity) this.mob;
                    //cat.func_233687_w_(true);
                    cat.setSleeping(true);
                    //this.jumpY=0.0325f;
                    break;
                    //TODO: move jump position & stuff inside entity. merge with jar one
                case CHICKEN:
                    ChickenEntity ch = (ChickenEntity) this.mob;
                    /*
                    ch.oFlap = ch.wingRotation;
                    ch.oFlapSpeed = 1;
                    this.destPos = (float)((double)this.destPos + (double)(this.onGround ? -1 : 4) * 0.3D);
                    this.destPos = MathHelper.clamp(this.destPos, 0.0F, 1.0F);
                    if (!ch.isOnGround() && ch.wingRotDelta < 1.0F) {
                        ch.wingRotDelta = 1.0F;
                    }

                    ch.wingRotDelta = (float)((double)ch.wingRotDelta * 0.9D);
                    ch.wingRotation += ch.wingRotDelta * 2.0F;*/
                    ch.livingTick();
                    if(rand.nextFloat()>(ch.isOnGround()?0.99: 0.87))ch.setOnGround(!ch.isOnGround());
                    //TODO: don't call living tick
                    break;

            }
        }
    }

    //only client side. cached mob from entitydata
    public void updateMob(){
        if(this.entityData.contains("id")) {
            Entity entity;
            if(this.entityData.get("id").getString().equals("minecraft:bee")){
                entity = new BeeEntity(EntityType.BEE, this.world);
            }
            else{
                entity  = EntityType.loadEntityAndExecute(this.entityData, this.world, o -> o);
            }
            if (entity==null)return;
            //TODO: add shadows
            double px = this.pos.getX() + 0.5;
            double py = this.pos.getY() + 0.5 + 0.0625;
            double pz = this.pos.getZ() + 0.5;
            entity.setPosition(px, py, pz);
            //entity.setMotion(0,0,0);
            entity.lastTickPosX = px;
            entity.lastTickPosY = py;
            entity.lastTickPosZ = pz;
            entity.prevPosX = px;
            entity.prevPosY = py;
            entity.prevPosZ = pz;
            entity.ticksExisted+=this.rand.nextInt(40);

            this.mob = entity;
            this.animationType = CommonUtil.JarMobType.getJarMobType(entity);
            this.entityChanged = false;
        }
    }

    public boolean hasNoMob(){
        return this.entityData==null;
    }


}
