package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.ProjectileStats;
import net.mehvahdjukaar.supplementaries.common.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

class ThrowableBricksBehavior extends ProjectileBehavior {

    protected ThrowableBricksBehavior(Item item) {
        super(item);
    }

    @Override
    protected Projectile getProjectileEntity(BlockSource source, Position position, ItemStack stackIn) {
        var entity = new ThrowableBrickEntity(source.level(), position.x(), position.y(), position.z());
        entity.setItem(stackIn.copyWithCount(1));
        return entity;
    }

    @Override
    protected SoundEvent getSound() {
        return ModSounds.BRICK_THROW.get();
    }

    @Override
    protected float getProjectileInaccuracy() {
        return ProjectileStats.BRICKS_DISPENSER_INACCURACY;
    }

    @Override
    protected float getProjectileVelocity() {
        return ProjectileStats.BRICKS_DISPENSER_SPEED;
    }

}

