package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class CannonballEntity extends ImprovedProjectileEntity {

    protected CannonballEntity(EntityType<? extends ThrowableItemProjectile> type, Level world) {
        super(type, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModRegistry.CANNONBALL.get().asItem();
    }

    @Override
    protected float getGravity() {
        return super.getGravity();
    }

    @Override
    protected float getDeceleration() {
        return super.getDeceleration();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        float radius = 4;

        Explosion exp = level().explode(this, getX(), getY(), getZ(), radius, Level.ExplosionInteraction.TNT);
        //exp.finalizeExplosion();
        super.onHitBlock(result);
    }
}
