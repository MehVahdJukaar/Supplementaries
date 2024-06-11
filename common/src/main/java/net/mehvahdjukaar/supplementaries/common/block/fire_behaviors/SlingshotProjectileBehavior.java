package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.entities.SlingshotProjectileEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SlingshotProjectileBehavior extends GenericProjectileBehavior {

    @Override
    protected @Nullable Entity createEntity(ItemStack stack, Level level, Vec3 facing) {
        return new SlingshotProjectileEntity(level, stack, ItemStack.EMPTY);
    }

}
