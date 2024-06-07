package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface ICannonBehavior {

    default boolean hasBallisticProjectile() {
        return true;
    }

    float getDrag();

    float getGravity();

    boolean fire(ItemStack stack, ServerLevel level, BlockPos pos, Vec3 facing, int power, float drag, int inaccuracy, @Nullable Player owner);

}
