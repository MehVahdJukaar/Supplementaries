package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;

import org.jetbrains.annotations.NotNull;

public class FlanCompat {

    @Contract
    @ExpectPlatform
    public static boolean canBreak(@NotNull Player player, @NotNull BlockPos pos) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean canPlace(@NotNull Player player, @NotNull BlockPos pos) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canReplace(@NotNull Player player, @NotNull BlockPos pos) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean canAttack(@NotNull Player player, @NotNull Entity victim) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean canInteract(@NotNull Player player, @NotNull BlockPos targetPos) {
        throw new AssertionError();
    }

}
