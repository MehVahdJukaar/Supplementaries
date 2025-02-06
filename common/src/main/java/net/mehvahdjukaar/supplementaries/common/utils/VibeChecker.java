package net.mehvahdjukaar.supplementaries.common.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.GenericProjectileBehavior;
import net.mehvahdjukaar.supplementaries.common.utils.fake_level.IEntityInterceptFakeLevel;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;

public class VibeChecker {

    @ExpectPlatform
    public static void checkVibe() {
    }

    private static boolean checkedOnce = false;

    public static void checkVibe(ServerPlayer player) {

        if (checkedOnce) return;
        checkedOnce = true;
        Level level = player.level();
        testCannonStuff(player);

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

    private static void testCannonStuff(ServerPlayer pl) {
        if (!CommonConfigs.General.SANITY_CHECKS_MESSAGES.get()) return;
        try {
            Level level = pl.level();
            var b = new GenericProjectileBehavior();
            var pt = IEntityInterceptFakeLevel.get(level);
            b.createEntity(Items.DIAMOND.getDefaultInstance(), pt,
                    new Vec3(0, 1, 0));

        } catch (Exception e) {
            pl.sendSystemMessage(Component.literal("Supplementaries detected a possible crash caused by another mod. Check the logs.")
                    .withStyle(ChatFormatting.DARK_RED));
            Supplementaries.LOGGER.error("An error caused by other mods has occurred. Supplementaries might not work as intended. Check the log to find the culprit mod and report there.", e);
        }

    }

    private static void clientStuff() {
        for (var v : BuiltInRegistries.BANNER_PATTERN.registryKeySet()) {
            if (!Sheets.BANNER_MATERIALS.containsKey(v)) {
                var a = new ArrayList<>(BuiltInRegistries.BANNER_PATTERN.registryKeySet());
                a.removeAll(Sheets.BANNER_MATERIALS.keySet());
                throw new BadModError("Some OTHER mod loaded the Sheets class to early, causing modded banner patterns and sherds textures to not include modded ones.\n" +
                        "Refusing to proceed further.\n" +
                        "Missing entries: " + a + " (mods listed here are NOT the cause of this, merely the ones that got broken because of it)\n" +
                        "Check previous forge log lines to find the offending mod. "+ Arrays.toString(stackTraceElements));
            }
        }
        for (var v : BuiltInRegistries.DECORATED_POT_PATTERNS.registryKeySet()) {
            if (!Sheets.DECORATED_POT_MATERIALS.containsKey(v)) {
                var a = new ArrayList<>(BuiltInRegistries.DECORATED_POT_PATTERNS.registryKeySet());
                a.removeAll(Sheets.DECORATED_POT_MATERIALS.keySet());
                throw new BadModError("Some OTHER mod loaded the Sheets class to early, causing modded banner patterns and sherds textures to not include modded ones.\n" +
                        "Refusing to proceed further.\n" +
                        "Missing entries: " + a + " (mods listed here are NOT the cause of this, merely the ones that got broken because of it)\n" +
                        "Check previous forge log lines to find the offending mod. "+ Arrays.toString(stackTraceElements));
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

    private static StackTraceElement[] stackTraceElements;

    public static void setSusStackTrace(StackTraceElement[] s) {
        stackTraceElements = s;
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
