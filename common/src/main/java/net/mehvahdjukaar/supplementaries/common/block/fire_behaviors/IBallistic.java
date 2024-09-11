package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.level.Level;

// Provides stats for a ballistic trajectory
public interface IBallistic {

    Data LINE = new Data(0, 0, 1);

    Data calculateData(ItemStack stack, Level level);

    record Data(float drag, float gravity, float initialSpeed) {
    }
}
