package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

public class VibeCheck {
    //fix your bugs. all reported stuff that somehow ends up to me. people then gets frustrated when stuff randomly crashes.
    // better to crash right on when shit is wrong
    public static void checkVibe(Level level) {
        if (PlatformHelper.isDev()) return;
        var m = new Spider(EntityType.SPIDER, level);
        var m2 = new Spider(EntityType.SPIDER, level);

        m.setOnGround(true);
        Path path = m.getNavigation().createPath(BlockPos.ZERO, 0);
        if (path != null) {
            m.setTarget(m2);
        }
        var i = new ItemEntity(EntityType.ITEM, level);
        i.setItem(ModRegistry.FLAX_SEEDS_ITEM.get().getDefaultInstance());
        i.tickCount = 21;
        var v = level.getSharedSpawnPos();
        i.setNoGravity(true);
        i.setPos(v.getX(), level.getMinBuildHeight() + 1d, v.getZ());
        for (int j = 0; j < 42; j++) {
            i.tick();
        }
    }
}
