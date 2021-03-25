package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

@OnlyIn(value = Dist.CLIENT, _interface = IRendersAsItem.class)
public class ThrowableBrickEntity extends ProjectileItemEntity implements IRendersAsItem{
    public ThrowableBrickEntity(EntityType<? extends ThrowableBrickEntity> type, World world) {
        super(type, world);
    }

    public ThrowableBrickEntity(World worldIn, LivingEntity throwerIn) {
        super(Registry.THROWABLE_BRICK.get(), throwerIn, worldIn);
    }

    public ThrowableBrickEntity(World worldIn, double x, double y, double z) {
        super(Registry.THROWABLE_BRICK.get(), x, y, z, worldIn);
    }

    public ThrowableBrickEntity(FMLPlayMessages.SpawnEntity packet, World world) {
        super(Registry.THROWABLE_BRICK.get(), world);
    }

    @Override
    protected float getGravity() {
        return 0.035F;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    @Override
    protected Item getDefaultItem() {
        return Items.BRICK;
    }


    private IParticleData makeParticle() {
        ItemStack itemstack = this.getItemRaw();
        return itemstack.isEmpty() ? new ItemParticleData(ParticleTypes.ITEM, new ItemStack(this.getDefaultItem())) : new ItemParticleData(ParticleTypes.ITEM, itemstack);
    }


    //
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            IParticleData iparticledata = this.makeParticle();

            for(int i = 0; i < 8; ++i) {
                this.level.addParticle(iparticledata, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

    @Override
    protected void onHitBlock(BlockRayTraceResult rayTraceResult) {
        super.onHitBlock(rayTraceResult);
        if (!this.level.isClientSide) {
            Entity entity = this.getOwner();
            if(entity instanceof PlayerEntity && !((PlayerEntity) entity).mayBuild())return;
            if (!(entity instanceof MobEntity) || this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this.getEntity())) {

                BlockPos pos = rayTraceResult.getBlockPos();
                if(level.getBlockState(pos).getBlock() instanceof JarBlock){
                    level.destroyBlock(pos,true);
                }
                else {
                    breakGlass(pos, 6);
                }

            }

        }
    }

    private static boolean isGlass(BlockState s){
        try {
            return ((Tags.Blocks.GLASS_PANES != null && s.is(Tags.Blocks.GLASS_PANES))
                    || (Tags.Blocks.GLASS != null && s.is(Tags.Blocks.GLASS)));
        }
        catch (Exception e){
            return false;
        }
    }

    private void breakGlass(BlockPos pos, int chance){
        int c = chance -1 -this.random.nextInt(4);
        BlockState state = level.getBlockState(pos);

        if(c < 0 || !isGlass(state))return;

        level.destroyBlock(pos,true);
        breakGlass(pos.above(),c);
        breakGlass(pos.below(),c);
        breakGlass(pos.east(),c);
        breakGlass(pos.west(),c);
        breakGlass(pos.north(),c);
        breakGlass(pos.south(),c);

    }


    @Override
    protected void onHitEntity(EntityRayTraceResult p_213868_1_) {
        super.onHitEntity(p_213868_1_);
        Entity entity = p_213868_1_.getEntity();
        int i = 1;
        entity.hurt(DamageSource.thrown(this, this.getOwner()), (float)i);
    }


    @Override
    protected void onHit(RayTraceResult result) {
        super.onHit(result);
        if (!this.level.isClientSide) {
            Vector3d v = result.getLocation();
            this.level.playSound(null, v.x,v.y,v.z, SoundEvents.NETHER_BRICKS_BREAK, SoundCategory.NEUTRAL, 0.75F, 1 );
            this.level.broadcastEntityEvent(this, (byte)3);
            this.remove();
        }

    }
}
