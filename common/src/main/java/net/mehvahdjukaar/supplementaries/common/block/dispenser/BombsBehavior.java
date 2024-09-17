package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.ProjectileStats;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.common.items.BombItem;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

class BombsBehavior extends ProjectileDispenseBehavior {

    @Override
    protected Projectile getProjectile(Level worldIn, Position position, ItemStack stackIn) {
        return new BombEntity(worldIn, position.x(), position.y(), position.z(), ((BombItem) stackIn.getItem()).getType());
    }

    @Override
    protected float getUncertainty() {
        return ProjectileStats.BOMB_DISPENSER_INACCURACY;
    }

    @Override
    protected float getPower() {
        return ProjectileStats.BOMB_DISPENSER_SPEED;
    }
}

