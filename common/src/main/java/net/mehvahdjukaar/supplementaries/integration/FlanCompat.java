package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;

public class FlanCompat {

    @Contract
    @ExpectPlatform
    public static boolean canBreak(@Nonnull Player player, @Nonnull BlockPos pos) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean canPlace(@Nonnull Player player, @Nonnull BlockPos pos, @Nonnull BlockState blockState) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canReplace(@Nonnull Player player, @Nonnull BlockPos pos, @Nonnull BlockState blockState) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canAttack(@Nonnull Player player, @Nonnull Entity victim) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean canInteract(@Nonnull Player player, @Nonnull HumanoidArm hand, @Nonnull BlockPos targetPos, @Nonnull Direction targetSide) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canInteract(@Nonnull Player player, @Nonnull HumanoidArm hand, @Nonnull BlockPos interactionPos, @Nonnull BlockPos targetPos, @Nonnull Direction targetSide) {
        throw new AssertionError();
    }
}
