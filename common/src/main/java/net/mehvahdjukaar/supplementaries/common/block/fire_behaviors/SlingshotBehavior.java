package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.entities.SlingshotProjectileEntity;
import net.mehvahdjukaar.supplementaries.common.utils.fake_level.ProjectileTestLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SlingshotBehavior extends GenericProjectileBehavior {

    @Override
    protected @Nullable Entity createEntity(ItemStack projectile, ProjectileTestLevel testLevel, Vec3 facing) {
        var s = new SlingshotProjectileEntity(testLevel, projectile, ItemStack.EMPTY);
        s.setDeltaMovement(facing.normalize().scale(ProjectileStats.SLINGSHOT_SPEED));
        return s;
    }
}
