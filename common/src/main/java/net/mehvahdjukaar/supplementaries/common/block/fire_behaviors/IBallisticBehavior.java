package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// Provides stats for a ballistic trajectory
public interface IBallisticBehavior extends IFireItemBehavior {

    BallisticData calculateData(ItemStack stack, Level level);

    // dont override this
    @Override
    default boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction, float power, int inaccuracy, @Nullable Player owner) {
        var data = calculateData(stack, level);
        return fireInner(stack, level, firePos, direction,
                power * data.drag() * data.initialSpeed(),
                inaccuracy, owner);
    }

    boolean fireInner(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction, float scaledPower,
                      int inaccuracy, @Nullable Player owner);


}
