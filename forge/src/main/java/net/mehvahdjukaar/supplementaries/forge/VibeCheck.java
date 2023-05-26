package net.mehvahdjukaar.supplementaries.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

public class VibeCheck {
    public static void checkVibe(Level level) {
        var m = new Spider(EntityType.SPIDER, level);
        var m2 = new Spider(EntityType.SPIDER, level);

        m.setOnGround(true);
        Path path = m.getNavigation().createPath(BlockPos.ZERO, 0);
        if (path != null) {
            m.setTarget(m2);
        }
    }
}
