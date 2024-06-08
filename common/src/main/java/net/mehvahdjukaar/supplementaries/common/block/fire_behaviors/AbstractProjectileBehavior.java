package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

public abstract class AbstractProjectileBehavior implements IFireItemBehavior {

    @Override
    public Optional<ItemStack> useItem(BlockSource source, ItemStack stack) {
        Level level = source.getLevel();
        Position position = IPresentItemBehavior.getDispensePosition(source);
        Projectile projectile = this.getProjectile(level, position, stack);
        projectile.shoot(0, 1, 0, this.getPower(), this.getUncertainty());
        level.addFreshEntity(projectile);

        return Optional.of(stack);
    }

    protected abstract Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack);

    protected float getUncertainty() {
        return 6.0F;
    }

    protected float getPower() {
        return 0.4F;
    }

}