package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.ProjectileStats;
import net.mehvahdjukaar.supplementaries.common.entities.SlimeBallEntity;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

class ThrowableSlimeballBehavior extends ProjectileBehavior {

    protected ThrowableSlimeballBehavior(Item item) {
        super(item);
    }

    @Override
    protected SoundEvent getSound() {
        return ModSounds.SLIMEBALL_THROW.get();
    }

    @Override
    protected Projectile getProjectileEntity(BlockSource source, Position position, ItemStack stackIn) {
        return new SlimeBallEntity(source.getLevel(), position.x(), position.y(), position.z());
    }

    @Override
    protected float getProjectileInaccuracy() {
        return ProjectileStats.SLIMEBALL_DISPENSER_INACCURACY;
    }

    @Override
    protected float getProjectileVelocity() {
        return ProjectileStats.SLIMEBALL_DISPENSER_SPEED;
    }

}

