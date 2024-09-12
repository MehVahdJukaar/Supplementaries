package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SimpleProjectileBehavior<T extends Projectile> extends GenericProjectileBehavior {

    private final EntityType<T> entityType;

    public SimpleProjectileBehavior(EntityType<T> entityType) {
        this.entityType = entityType;
    }

    @Override
    protected @Nullable Entity createEntity(ItemStack stack, ProjectileTestLevel testLevel, Vec3 facing) {
        return entityType.create(testLevel);
    }
}

