package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// Used for both presents and cannons
public interface IFireItemBehavior {

    default boolean fire(ItemStack stack, ServerLevel level, BlockPos pos,
                         float fireOffset, Vec3 direction,
                         float power, int inaccuracy, @Nullable Player owner) {
        return fire(stack, level, pos.getCenter(), fireOffset, direction, power, inaccuracy, owner);
    }

    default boolean fire(ItemStack stack, ServerLevel level, Vec3 pos,
                         float fireOffset, Vec3 direction,
                         float power, int inaccuracy, @Nullable Player owner) {
        return fire(stack, level, pos.add(direction.scale(fireOffset)),
                direction, power, inaccuracy, owner);
    }

    boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction,
                 float power, int inaccuracy, @Nullable Player owner);

}


