package net.mehvahdjukaar.supplementaries.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * For modders if you want to have compat for a plugin or mod that adds chunk claims and such inject here
 */
public class PermissionsUtils {
    //TODO: add
    public static boolean canBreak(Player player, BlockPos pos) {
        return Utils.mayBuild(player,hit.getBlockPos());
    }

    public static boolean cantPlace(Player player, BlockPos pos, BlockState blockState) {
        return Utils.mayBuild(player,hit.getBlockPos());
    }

    public static boolean cantReplace(Player player, BlockPos pos, BlockState blockState) {
        return Utils.mayBuild(player,hit.getBlockPos());
    }

    public static boolean cantAttack(Player player, Entity victim) {
        return Utils.mayBuild(player,hit.getBlockPos());
    }

    public static boolean cantInteract(Player player, InteractionHand hand, BlockPos targetPos, Direction targetSide) {
        return Utils.mayBuild(player,hit.getBlockPos());
    }


}
