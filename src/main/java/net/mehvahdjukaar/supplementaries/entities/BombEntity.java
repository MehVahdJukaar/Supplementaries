package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.TNTBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Random;

@OnlyIn(value = Dist.CLIENT, _interface = IRendersAsItem.class)
public class BombEntity extends ProjectileItemEntity implements IRendersAsItem{
    private double prevX = 0;
    private double prevY = 0;
    private double prevZ = 0;
    private int age = 0;
    private boolean active = true;

    public BombEntity(EntityType<? extends BombEntity> type, World world) {
        super(type, world);
        this.prevX = this.getPosX();
        this.prevY = this.getPosY();
        this.prevZ = this.getPosZ();
    }

    public BombEntity(World worldIn, LivingEntity throwerIn) {
        super(Registry.BOMB.get(), throwerIn, worldIn);
        this.prevX = this.getPosX();
        this.prevY = this.getPosY();
        this.prevZ = this.getPosZ();
    }

    public BombEntity(World worldIn, double x, double y, double z) {
        super(Registry.BOMB.get(), x, y, z, worldIn);
    }

    public BombEntity(FMLPlayMessages.SpawnEntity packet, World world) {
        super(Registry.BOMB.get(), world);
        this.prevX = this.getPosX();
        this.prevY = this.getPosY();
        this.prevZ = this.getPosZ();
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putBoolean("Active",this.active);
        compound.putInt("Age",this.age);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.active = compound.getBoolean("Active");
        this.age = compound.getInt("Age");
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ACACIA_BUTTON;
        //return Registry.BOMB_ITEM_ON.get();
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(this.active?Registry.BOMB_ITEM_ON.get():Registry.BOMB_ITEM.get());
    }
    private IParticleData makeParticle() {
        return  new ItemParticleData(ParticleTypes.ITEM, new ItemStack(Registry.BOMB_ITEM.get()));
    }

    public void handleStatusUpdate(byte id) {
        if (id == 3) {
            IParticleData iparticledata = this.makeParticle();
            for(int i = 0; i < 8; ++i) {
                this.world.addParticle(iparticledata, this.getPosX(), this.getPosY(), this.getPosZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public double r(){
        return (rand.nextFloat()-0.5)*0.3;
    }

    @Override
    public void tick() {
        if (this.active && this.isInWater()) {
            this.turnOff();
        }
        if (this.world.isRemote && this.active) {
            if(this.rand.nextFloat()<1) {
                double x = this.getPosX() ;
                double y = this.getPosY() ;
                double z = this.getPosZ() ;
                double x2 = (x-this.prevX);
                double y2 = (y-this.prevY);
                double z2 = (z-this.prevZ);
                world.addParticle(Registry.BOMB_SMOKE_PARTICLE.get(),
                        x+r(), y + 0.5+r(), z+r(), 0, 0.01, 0);
                world.addParticle(Registry.BOMB_SMOKE_PARTICLE.get(),
                        x+(x2/2)+r(), 0.5 + y+(y2/2), z+(z2/2)+r(), 0, 0., 0);
                world.addParticle(Registry.BOMB_SMOKE_PARTICLE.get(),
                        x+(x2/4)+r(), 0.5 + y+(y2/4), z+(z2/4)+r(), 0, 0, 0);
                world.addParticle(Registry.BOMB_SMOKE_PARTICLE.get(),
                        x+(x2*0.75)+r(), 0.5 + y+(y2*0.75), z+(z2*0.75)+r(), 0, 0, 0);
            }
            this.prevX = this.getPosX();
            this.prevY = this.getPosY();
            this.prevZ = this.getPosZ();

        }
        else{
            this.age++;
            if(this.age >=200)this.explode();
        }
        super.tick();

    }

    @Override
    protected float getGravityVelocity() {
        return 0.05F;
    }

    public static boolean canBreakBlock(IBlockReader world, BlockPos pos, BlockState state, float power){
        return state.isReplaceable(Fluids.WATER) || state.getBlock() instanceof TNTBlock;
    }

    public void explode(){
        Explosion explosion = new Explosion(this.world, this, null, new ExplosionContext(){
            public boolean canExplosionDestroyBlock(Explosion explosion, IBlockReader reader, BlockPos pos, BlockState state, float power) {
                return canBreakBlock(reader,pos,state,power);
            }},
                this.getPosX(),this.getPosY()+0.25,this.getPosZ(), 2f, false, Explosion.Mode.BREAK);

        explosion.doExplosionA();
        explosion.doExplosionB(false);

        this.world.playSound(null, this.getPosX(),this.getPosY(),this.getPosZ(), Registry.BOMB_SOUND.get(), SoundCategory.NEUTRAL, 3F, (1.2F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) );
    }

    //explode
    public void explodeOrBreak(){
        if(this.active)this.explode();
        this.world.playSound(null, this.getPosX(),this.getPosY(),this.getPosZ(), SoundEvents.BLOCK_NETHERITE_BLOCK_BREAK, SoundCategory.NEUTRAL, 1.5F, 1.5f);
        this.world.setEntityState(this, (byte)3);
        this.remove();
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult hit) {
        super.onEntityHit(hit);
        hit.getEntity().attackEntityFrom(DamageSource.causeThrownDamage(this, this.func_234616_v_()), 1);
    }

    public void turnOff(){
        if (!world.isRemote()) {
            world.playSound(null, this.getPosX(),this.getPosY(),this.getPosZ(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.5F, 1.5F);
        }
        else{
            Random random = world.getRandom();
            for (int i = 0; i < 10; ++i) {
                world.addParticle(ParticleTypes.SMOKE,this.getPosX()+0.25f-random.nextFloat()*0.5f,this.getPosY()+0.45f-random.nextFloat()*0.5f,this.getPosZ()+0.25f-random.nextFloat()*0.5f,0, 0.005, 0);
            }
        }
        this.active = false;
    }

    public void onCollideWithPlayer(PlayerEntity entityIn) {
        if (!this.world.isRemote) {
            if (!this.active && entityIn.inventory.addItemStackToInventory(this.getItemStack())) {
                entityIn.onItemPickup(this, 1);
                this.remove();
            }
        }
    }

    private ItemStack getItemStack(){
        return new ItemStack(Registry.BOMB_ITEM.get());
    }

    //onBlockHit
    protected void func_230299_a_(BlockRayTraceResult hit) {
        super.func_230299_a_(hit);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        super.onImpact(result);
        Vector3d v = result.getHitVec();

        if(!this.removed && !this.world.isRemote)this.explodeOrBreak();

        if (this.world.isRemote && this.active) {
            world.addParticle(Registry.BOMB_EXPLOSION_PARTICLE_EMITTER.get(),v.x,v.y+0.5,v.z,0,0,0);
        }

    }

}
