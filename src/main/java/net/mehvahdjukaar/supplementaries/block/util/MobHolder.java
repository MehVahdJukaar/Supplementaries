package net.mehvahdjukaar.supplementaries.block.util;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.UUID;

public class MobHolder {
    private final Random rand = new Random();

    private World world;
    private BlockPos pos;

    public boolean entityChanged = true;

    public CompoundNBT entityData = null;
    public MobHolderType animationType = MobHolderType.DEFAULT;
    public UUID uuid = null;
    public float yOffset = 1;
    public float scale = 1;
    public String name;
    //client only
    public LivingEntity mob = null;
    public float jumpY = 0;
    public float prevJumpY = 0;
    public float yVel = 0;

    public MobHolder(World world, BlockPos pos){
        this.world = world;
        this.pos = pos;
    }

    public void setWorldAndPos(World world, BlockPos pos){
        this.world = world;
        this.pos = pos;
    }

    public void setPartying(BlockPos pos, boolean isPartying){
        if(this.mob!=null){
            this.mob.setPartying(pos, isPartying);
        }
    }

    public void read(CompoundNBT compound) {
        //remove in the future
        if(compound.contains("jar_mob")){
            this.entityData = compound.getCompound("jar_mob");
            this.entityChanged = true;
            this.scale=0.15f;
            this.yOffset=0;
            this.animationType=MobHolderType.DEFAULT;
            this.name="reload needed";
        }
        if(compound.contains("animation_type")) this.animationType=MobHolderType.values()[compound.getInt("animation_type")];
        if(compound.contains("scale"))this.scale=compound.getFloat("scale");



        if(compound.contains("MobHolder")){
            CompoundNBT cmp = compound.getCompound("MobHolder");
            this.entityData = cmp.getCompound("EntityData");
            this.scale = cmp.getFloat("Scale");
            this.yOffset = cmp.getFloat("YOffset");
            this.animationType = MobHolderType.values()[cmp.getInt("AnimationType")];
            if(cmp.contains("UUID"))
                this.uuid = cmp.getUniqueId("UUID");
            this.name = cmp.getString("Name");

            this.entityChanged = true;
        }



    }

    public CompoundNBT write(CompoundNBT compound) {
        if(this.entityData!=null) {
            saveMobToNBT(compound, this.entityData,this.scale,this.yOffset,this.animationType.ordinal(),this.name,this.uuid);
        }
        return compound;
    }


    public void tick() {


        if(this.entityChanged && this.entityData!=null && this.world!=null)this.updateMob();

        if (this.mob != null) {
            //needed for eggs
            if (!this.world.isRemote) {
                if (this.animationType == MobHolderType.CHICKEN) {
                    ChickenEntity ch = (ChickenEntity) this.mob;
                    if (--ch.timeUntilNextEgg <= 0) {
                        ch.entityDropItem(Items.EGG);
                        ch.timeUntilNextEgg = this.rand.nextInt(6000) + 6000;
                    }
                }
                return;
            }

            //client side animation

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
                    this.mob.livingTick();
                    boolean p = ((ParrotEntity)this.mob).isPartying();
                        this.mob.setOnGround(p);
                        this.jumpY=p?0:0.0625f;
                    break;
                case PIXIE:
                    mob.livingTick();
                    this.mob.lastTickPosY=this.pos.getY();
                    this.mob.setPosition(this.mob.getPosX(),this.pos.getY(),this.mob.getPosZ());
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
                    ch.livingTick();
                    if (rand.nextFloat() > (ch.isOnGround() ? 0.99 : 0.88)) ch.setOnGround(!ch.isOnGround());
                    break;
                case MOTH:
                    mob.livingTick();
                    this.mob.lastTickPosY=this.pos.getY();
                    this.mob.setPosition(this.mob.getPosX(),this.pos.getY(),this.mob.getPosZ());
                    this.jumpY = 0.04f * MathHelper.sin(this.mob.ticksExisted / 10f) - 0.03f;
                    break;
            }
        }
    }

    //TODO: react to fluid change
    public void setWaterMobInWater(boolean w){
        if(this.mob != null && this.mob instanceof WaterMobEntity && this.mob.isInWater()!=w){
            try {
                Field f = ObfuscationReflectionHelper.findField(Entity.class, "field_70171_ac");
                f.setAccessible(true);
                f.setBoolean(this.mob,w);
            }
            catch (Exception ignored){};
        }
    }


    //todo: replace with markdirty like liquid holder
    //client and server. cached mob from entitydata
    public void updateMob(){
        if(this.entityData.contains("id")) {
            Entity entity;
            if(this.entityData.get("id").getString().equals("minecraft:bee")){
                entity = new BeeEntity(EntityType.BEE, this.world);
            }
            else{
                entity  = EntityType.loadEntityAndExecute(this.entityData, this.world, o -> o);
            }
            if (!(entity instanceof LivingEntity))return;

            if(this.uuid!=null) {
                entity.setUniqueId(this.uuid);
            }

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

            this.mob = (LivingEntity) entity;
            this.animationType = MobHolderType.getType(entity);
            if(!this.world.isRemote){
                int light = this.animationType.getLightLevel();
                BlockState state = this.world.getBlockState(this.pos);
                if(state.get(BlockProperties.LIGHT_LEVEL_0_15)!=light){
                    this.world.setBlockState(this.pos, state.with(BlockProperties.LIGHT_LEVEL_0_15,light),2|4|16);
                }
            }
            this.entityChanged = false;
        }

        this.setWaterMobInWater(!this.world.getFluidState(pos).isEmpty());
    }

    public boolean isEmpty(){
        return this.entityData==null;
    }

    public static void saveMobToNBT(CompoundNBT compound, CompoundNBT entityData, float scale, float yOffset, int type, String name, UUID id){
        CompoundNBT cmp = new CompoundNBT();
        cmp.put("EntityData", entityData);
        cmp.putFloat("Scale", scale);
        cmp.putFloat("YOffset", yOffset);
        cmp.putInt("AnimationType", type);
        if(id!=null)
            cmp.putUniqueId("UUID", id);
        cmp.putString("Name", name);

        compound.put("MobHolder", cmp);
    }

    //called by the item
    public static void createMobHolderItemNBT(ItemStack stack, Entity mob, float blockh, float blockw){
        if(mob==null)return;
        if(mob instanceof LivingEntity){
            LivingEntity le = (LivingEntity) mob;
            le.prevRotationYawHead = 0;
            le.rotationYawHead = 0;
            le.limbSwingAmount = 0;
            le.prevLimbSwingAmount = 0;
            le.limbSwing = 0;
            le.hurtTime=0;
            le.maxHurtTime=0;
            le.hurtTime=0;
        }
        mob.rotationYaw = 0;
        mob.prevRotationYaw = 0;
        mob.prevRotationPitch = 0;
        mob.rotationPitch = 0;
        mob.extinguish();
        mob.hurtResistantTime=0;

        UUID id = mob.getUniqueID();

        CompoundNBT mobCompound = new CompoundNBT();
        mob.writeUnlessPassenger(mobCompound);
        if (!mobCompound.isEmpty()) {

            mobCompound.remove("Passengers");
            mobCompound.remove("Leash");
            mobCompound.remove("UUID");

            //TODO: improve for acquatic entities to react and not fly when not in water
            boolean flag = mob.hasNoGravity() || mob instanceof IFlyingAnimal || mob.doesEntityNotTriggerPressurePlate() || mob instanceof WaterMobEntity;

            MobHolderType type = MobHolderType.getType(mob);
            float babyscale = 1;
            //non ageable

            if(mob instanceof AgeableEntity && ((AgeableEntity) mob).isChild()) babyscale = 2f;
            if(mobCompound.contains("IsBaby")&&mobCompound.getBoolean("IsBaby")||
                    (mob instanceof VillagerEntity && ((VillagerEntity) mob).isChild())) babyscale = 1.125f;

            float s = 1;
            float w = mob.getWidth() *babyscale;
            float h = mob.getHeight() *babyscale;
            //float maxh = flag ? 0.5f : 0.75f;
            //1 px border
            float maxh = blockh - (flag ? 0.25f : 0.125f) - type.adjHeight;
            float maxw = blockw - 0.25f - type.adjWidth;
            if (w > maxw || h > maxh) {
                if (w - maxw > h - maxh)
                    s = maxw / w;
                else
                    s = maxh / h;
            }
            //TODO: rewrite this to account for adjValues
            float y = flag ? (blockh/2f) - h * s / 2f : 0.0626f;

            //ice&fire dragons
            String name = mob.getType().getRegistryName().toString();
            if(name.equals("iceandfire:fire_dragon")||name.equals("iceandfire:ice_dragon")||name.equals("iceandfire:lightning_dragon")){
                s*=0.45;
            }
            CompoundNBT cmp = new CompoundNBT();
            saveMobToNBT(cmp, mobCompound, s, y, 0, mob.getName().getString(), id);
            stack.setTagInfo("BlockEntityTag", cmp);

        }
    }

    //used for animation only (maybe caching item renderer later)
    public enum MobHolderType {
        DEFAULT(null,0,0),
        SLIME("minecraft:slime",0,0),
        MAGMA_CUBE("minecraft:magma_cube",0,0),
        BEE("minecraft:bee",0.3125f,0),
        BAT("minecraft:bat",0,0),
        VEX("minecraft:vex",0,0.125f),
        ENDERMITE("minecraft:endermite",0,0),
        SILVERFISH("minecraft:silverfish",0,0.25f),
        PARROT("minecraft:parrot",0,0),
        CAT("minecraft:cat",0,0.1875f),
        RABBIT("minecraft:rabbit",0,0),
        CHICKEN("minecraft:chicken",0.25f,0.3125f),
        PIXIE("iceandfire:pixie",0,0),
        MOTH("druidcraft:lunar_moth",0.375f,0.1375f),
        WATER_MOB("minecraft:tropical_fish",0,0.125f);


        public final String type;
        public final float adjHeight;
        public final float adjWidth;

        MobHolderType(String type, float h, float w){
            this.type = type;
            this.adjHeight =h;
            this.adjWidth = w;
        }

        //maybe move into enum
        public int getLightLevel(){
            if(this==PIXIE)return 10;
            if(this==ENDERMITE)return 5;
            if(this==MOTH)return 10;
            return 0;
        }

        public static MobHolderType getType(Entity e){
            if(e instanceof WaterMobEntity)return WATER_MOB;
            String name = e.getType().getRegistryName().toString();
            for (MobHolderType n : MobHolderType.values()){
                if(name.equals(n.type)){
                    return n;
                }
            }
            return MobHolderType.DEFAULT;
        }
    }

}