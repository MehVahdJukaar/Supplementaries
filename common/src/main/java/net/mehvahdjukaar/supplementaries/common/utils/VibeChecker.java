package net.mehvahdjukaar.supplementaries.common.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

public class VibeChecker {

    @ExpectPlatform
    public static void checkVibe() {
    }

    public static void checkVibe(Level level){
        try {
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
        }catch (Exception e){
            throw new OtherModError("An error caused by other mods has occurred (see below). This will prevent the mod from working correctly, possibly crashing at random times. Refusing to procede any further", e);
        }
    }

    //Its been prooven that CFTS is primarely made up of code shamelessly stolen from frozen up
    //enfoncing its ARR license
    private static void crashWhenStolenMod() {
        String s = "creaturesfromthesnow";
        if (PlatHelper.isModLoaded(s)) {
            Supplementaries.LOGGER.error("[!!!] The mod "+s+" contains stolen assets and code from Frozen Up which is ARR.");
        }
    }


    public static class OtherModError extends Error{

        public OtherModError(String s) {
            super(s);
        }

        public OtherModError(String s, Exception e) {
            super(s, e);
        }
    }

}
