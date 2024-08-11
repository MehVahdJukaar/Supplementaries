package net.mehvahdjukaar.supplementaries.common.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

import java.util.ArrayList;

public class VibeChecker {


    @ExpectPlatform
    public static void checkVibe() {
    }

    public static void checkVibe(Level level) {

        //check sheets class
        if (PlatHelper.getPhysicalSide().isClient()) clientStuff();
        if (true) return;
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
        } catch (Exception e) {
            Supplementaries.LOGGER.error("An error caused by other mods has occurred. Supplementaries might not work as intended", e);
        }
    }

    private static void clientStuff() {
        if (PlatHelper.isDev()) return;
        for (var v : BuiltInRegistries.BANNER_PATTERN.registryKeySet()) {
            if (!Sheets.BANNER_MATERIALS.containsKey(v)) {
                var a = new ArrayList<>(BuiltInRegistries.BANNER_PATTERN.registryKeySet());
                a.removeAll(Sheets.BANNER_MATERIALS.keySet());
                throw new BadModError("Some OTHER mod loaded the Sheets class to early, causing modded banner patterns and sherds textures to not include modded ones.\n" +
                        "Refusing to proceed further.\n" +
                        "Missing entries: " + a + " (unrelated to the mod that caused this)\n" +
                        "Check previous forge log lines to find the offending mod.");
            }
        }
        for (var v : BuiltInRegistries.DECORATED_POT_PATTERNS.registryKeySet()) {
            if (!Sheets.DECORATED_POT_MATERIALS.containsKey(v)) {
                var a = new ArrayList<>(BuiltInRegistries.DECORATED_POT_PATTERNS.registryKeySet());
                a.removeAll(Sheets.DECORATED_POT_MATERIALS.keySet());
                throw new BadModError("Some OTHER mod loaded the Sheets class to early, causing modded banner patterns and sherds textures to not include modded ones.\n" +
                        "Refusing to proceed further.\n" +
                        "Missing entries: " + a + " (unrelated to the mod that caused this)\n" +
                        "Check previous forge log lines to find the offending mod.");
            }
        }
    }

    //It's been proven that CFTS is primarely made up of code shamelessly stolen from frozen up
    //enforcing its ARR license
    private static void crashWhenStolenMod() {
        String s = "creaturesfromthesnow";
        if (PlatHelper.isModLoaded(s)) {
            Supplementaries.LOGGER.error("[!!!] The mod {} contains stolen assets and code from Frozen Up which is ARR.", s);
        }
    }


    public static class BadModError extends Error {

        public BadModError(String s) {
            super(s);
        }

        public BadModError(String s, Exception e) {
            super(s, e);
        }
    }

}
