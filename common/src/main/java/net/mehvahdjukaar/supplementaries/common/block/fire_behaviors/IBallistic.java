package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Provides stats for a ballistic trajectory
public interface IBallistic {

    static Data line() {
        return new Data(1, 0);
    }

    Data calculateData(ItemStack stack, Level level);

    record Data(float drag, float gravity) {
    }
}
