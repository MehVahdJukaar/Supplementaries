package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.utils.fake_level.IEntityInterceptFakeLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SimpleProjectileBehavior<T extends Projectile> extends GenericProjectileBehavior {

    private final EntityType<T> entityType;
    private final float initialSpeed;

    public SimpleProjectileBehavior(EntityType<T> entityType, float initialSpeed) {
        this.entityType = entityType;
        this.initialSpeed = initialSpeed;
    }

    @Override
    public @Nullable Entity createEntity(ItemStack stack, IEntityInterceptFakeLevel testLevel, Vec3 facing) {
        var e = entityType.create(testLevel.cast());
        e.setDeltaMovement(facing.normalize().scale(initialSpeed));
        if (e instanceof ThrowableItemProjectile ip) {
            ip.setItem(stack);
        }
        return e;
    }
}

