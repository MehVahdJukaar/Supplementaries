package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.entities.SlingshotProjectileEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SlingshotProjectileBehavior implements IFireItemBehavior {

    @Override
    public boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 facing,
                        float power, float drag, int inaccuracy, @Nullable Player owner) {
        SlingshotProjectileEntity entity = new SlingshotProjectileEntity(level, stack, ItemStack.EMPTY, owner);

        facing.scale(0.01f);

        entity.shoot(facing.x, facing.y, facing.z, -drag * power, inaccuracy);
        entity.setPos(firePos.x, firePos.y, firePos.z);

        level.addFreshEntity(entity);
        return true;
    }
}
