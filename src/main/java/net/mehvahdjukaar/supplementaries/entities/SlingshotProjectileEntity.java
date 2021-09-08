package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.events.ItemsOverrideHandler;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.IPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class SlingshotProjectileEntity extends ImprovedProjectileEntity {

    //only client
    private final float xRotInc;
    private final float yRotInc;
    private float particleCooldown = 0;

    public SlingshotProjectileEntity(LivingEntity thrower, World world, ItemStack item) {
        super(ModRegistry.SLINGSHOT_PROJECTILE.get(), thrower, world);
        this.setItem(item);
        this.maxAge = 500;
        xRotInc = 0;
        yRotInc = 0;
    }

    public SlingshotProjectileEntity(FMLPlayMessages.SpawnEntity spawnEntity, World world) {
        this(ModRegistry.SLINGSHOT_PROJECTILE.get(), world);

    }

    public SlingshotProjectileEntity(EntityType<SlingshotProjectileEntity> type, World world) {
        super(type, world);
        this.maxAge = 500;
        this.yRotInc = (this.random.nextBoolean()?1:-1) * (float) (4 * this.random.nextGaussian() + 7);
        this.xRotInc = (this.random.nextBoolean()?1:-1) * (float) (4 * this.random.nextGaussian() + 7);
        this.xRot = this.random.nextFloat()*360;
        this.yRot = this.random.nextFloat()*360;
        this.xRotO = this.xRot;
        this.yRotO = this.yRot;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.STONE;
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult hit) {
        super.onHitBlock(hit);
        Entity owner = this.getOwner();
        boolean success = false;
        if (owner == null || owner instanceof PlayerEntity && ((PlayerEntity) owner).abilities.mayBuild) {

            if (owner == null && level.isClientSide){
                //idk why but sound isn't played with null entity. also owner is not synced and idk how to get it from client
                //TODO: change this. this is will cause issues
                //owner = CommonUtil.getClientPlayer();
            }
            ItemStack stack = this.getItem();
            Item item = stack.getItem();

            //block override. mimic forge event
            if(owner != null) {
                PlayerInteractEvent.RightClickBlock blockPlaceEvent = new PlayerInteractEvent.RightClickBlock((PlayerEntity) owner, Hand.MAIN_HAND, hit.getBlockPos(), hit);
                ItemsOverrideHandler.tryPerformOverride(blockPlaceEvent, stack, true);

                if (blockPlaceEvent.isCanceled() && blockPlaceEvent.getCancellationResult().consumesAction()) {
                    success = true;
                }
            }
            if (!success && item instanceof BlockItem) {
                BlockItemUseContext ctx = new BlockItemUseContext(this.level, (PlayerEntity) owner, Hand.MAIN_HAND, this.getItem(), hit);
                success = ((BlockItem) item).place(ctx).consumesAction();
            }
        }

        if (!level.isClientSide && !success) {
            this.doStuffBeforeRemoving();
        }

        this.remove();
    }

    @Override
    public void doStuffBeforeRemoving() {
        super.doStuffBeforeRemoving();
        ItemEntity drop = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ() + 0.5, this.getItem());
        drop.setDefaultPickUpDelay();
        this.level.addFreshEntity(drop);
    }

    @Override
    protected void updateRotation() {
        if(this.level.isClientSide) {
            this.xRotO = this.xRot;
            this.yRotO = this.yRot;
            this.xRot += xRotInc;
            this.yRot += yRotInc;
            this.particleCooldown ++;
        }
    }

    @Override
    public void spawnTrailParticles(Vector3d currentPos, Vector3d newPos) {
        double d = this.getDeltaMovement().length();
        if(this.tickCount > 2 && d * this.tickCount > 1.8) {
            double p = 4 / (d * 0.95 + 0.05);
            if (this.particleCooldown > p) {
                this.particleCooldown -= p;
                double x = currentPos.x;
                double y = currentPos.y;
                double z = currentPos.z;
                this.level.addParticle(ModRegistry.SLINGSHOT_PARTICLE.get(), x, 0.5 + y, z, 0, 0.02, 0);
            }
        }
    }
}
