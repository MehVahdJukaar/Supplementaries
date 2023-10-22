package net.mehvahdjukaar.supplementaries.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public abstract class ClaimCompat {
    private static  ClaimCompat INSTANCE ;
    public static  boolean ON;
    static {
        if (CompatHandler.FLAN) {
            ON = true;
            //INSTANCE = new FlanCompat();
        } else {
            ON = false;
            INSTANCE = new ClaimCompat(){};
        }
    }

    //static stuff


    public static boolean canBreak(@NotNull Player player, @NotNull BlockPos pos) {
        return INSTANCE._canBreak(player, pos);
    }

    public static boolean canPlace(@NotNull Player player, @NotNull BlockPos pos) {
        return INSTANCE._canPlace(player, pos);
    }

    public static boolean canReplace(@NotNull Player player, @NotNull BlockPos pos) {
        return true;
    }

    public static boolean canAttack(@NotNull Player player, @NotNull Entity victim) {
        return true;
    }

    public static boolean canInteract(@NotNull Player player, @NotNull BlockPos targetPos) {
        return true;
    }

    //instance stuff

    public boolean _canBreak(@NotNull Player player, @NotNull BlockPos pos) {
        return true;
    }

    public boolean _canPlace(@NotNull Player player, @NotNull BlockPos pos) {
        return true;
    }

    public boolean _canReplace(@NotNull Player player, @NotNull BlockPos pos) {
        return true;
    }

    public boolean _canAttack(@NotNull Player player, @NotNull Entity victim) {
        return true;
    }

    public boolean _canInteract(@NotNull Player player, @NotNull BlockPos targetPos) {
        return true;
    }
}
