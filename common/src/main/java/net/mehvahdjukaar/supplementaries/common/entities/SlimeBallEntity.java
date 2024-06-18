package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class SlimeBallEntity extends ImprovedProjectileEntity {
    protected SlimeBallEntity(EntityType<? extends ThrowableItemProjectile> type, Level world) {
        super(type, world);
    }

    protected SlimeBallEntity(EntityType<? extends ThrowableItemProjectile> type, double x, double y, double z, Level world) {
        super(type, x, y, z, world);
    }

    protected SlimeBallEntity(EntityType<? extends ThrowableItemProjectile> type, LivingEntity thrower, Level world) {
        super(type, thrower, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL;
    }
}
