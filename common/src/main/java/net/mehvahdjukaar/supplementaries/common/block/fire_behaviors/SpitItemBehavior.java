package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SpitItemBehavior implements IFireItemBehavior {

    @Override
    public boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction,
                        float power, int inaccuracy, @Nullable Player owner) {


        ItemEntity itementity = new ItemEntity(level, firePos.x(), firePos.y(), firePos.z(), stack);

        itementity.setDeltaMovement(
                level.random.nextGaussian() * 0.0075 * direction.x * power,
                level.random.nextGaussian() * 0.0075 * direction.y * power + 0.3,
                level.random.nextGaussian() * 0.0075 * direction.z * power);
        level.addFreshEntity(itementity);
        return true;
    }
}
