package net.mehvahdjukaar.supplementaries.common.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.GenericProjectileBehavior;
import net.mehvahdjukaar.supplementaries.common.utils.fake_level.IEntityInterceptFakeLevel;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class VibeChecker {

    @ExpectPlatform
    public static void checkVibe() {
    }

    private static boolean checkedOnce = false;

    public static void checkVibe(ServerPlayer player) {

        if (checkedOnce) return;
        checkedOnce = true;
        //testCannonStuff(player);
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

    //It's been proven that CFTS is primarely made up of code shamelessly stolen from frozen up
    //enforcing its ARR license
    private static void crashWhenStolenMod() {
        String s = "creaturesfromthesnow";
        if (PlatHelper.isModLoaded(s)) {
            Supplementaries.LOGGER.error("[!!!] The mod {} contains stolen assets and code from Frozen Up which is ARR.", s);
        }
    }

    public static void assertSameLevel(Level level, Player player) {
        if (level.isClientSide != player.level().isClientSide) {
            throw new AssertionError("Some mod tried to pass a client side level with a server side player to a use item method! This breaks the method contract! \n Level: " + level + " Player: " + player);
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
