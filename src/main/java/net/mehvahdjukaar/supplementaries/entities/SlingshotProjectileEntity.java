package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.*;
import net.minecraft.network.IPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class SlingshotProjectileEntity extends ImprovedProjectileEntity {

    public SlingshotProjectileEntity(EntityType<? extends ProjectileItemEntity> type, World world) {
        super(ModRegistry.SLINGSHOT_PROJECTILE.get(), world);
        this.maxAge = 10000;
    }

    public SlingshotProjectileEntity(LivingEntity thrower, World world, ItemStack item) {
        super(ModRegistry.SLINGSHOT_PROJECTILE.get(), thrower, world);
        this.setItem(item);
        this.maxAge = 10000;
    }

    public SlingshotProjectileEntity(FMLPlayMessages.SpawnEntity spawnEntity, World world) {
        this(ModRegistry.SLINGSHOT_PROJECTILE.get(), world);
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
            //TODO: change this. this is will cause issues
            if (owner == null && level.isClientSide) owner = Minecraft.getInstance().player;
            Item item = this.getItem().getItem();
            if (item instanceof BlockItem) {
                BlockItemUseContext ctx = new BlockItemUseContext(this.level, (PlayerEntity) owner, Hand.MAIN_HAND, this.getItem(), hit);
                success = ((BlockItem) this.getItem().getItem()).place(ctx).consumesAction();
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
        this.xRotO = this.xRot;
        this.yRotO = this.yRot;
        this.xRot += 1.4;
        this.yRot += 1.75;
    }

}
