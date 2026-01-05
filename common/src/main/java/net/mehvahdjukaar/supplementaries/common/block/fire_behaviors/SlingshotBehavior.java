package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.entities.SlingshotProjectileEntity;
import net.mehvahdjukaar.supplementaries.common.utils.fake_level.IEntityInterceptFakeLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SlingshotBehavior extends GenericProjectileBehavior {

    @Override
    public @Nullable Entity createEntity(ItemStack projectile, IEntityInterceptFakeLevel testLevel, Vec3 facing) {
        var s = new SlingshotProjectileEntity(testLevel.cast(), projectile, ItemStack.EMPTY);
        s.setDeltaMovement(facing.normalize().scale(ProjectileStats.SLINGSHOT_SPEED));
        return s;
    }
}
