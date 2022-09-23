package net.mehvahdjukaar.supplementaries.integration.forge;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public final class FlanCompatImpl {

    public static boolean canBreak(@Nonnull Player player, @Nonnull BlockPos pos) {
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            return claim.canInteract((ServerPlayer) player, PermissionRegistry.BREAK, pos, true);
        } catch (Exception e) {
            Supplementaries.LOGGER.error("Failed call break block event: [Player: {}, Pos: {}]", player, pos, e);
            return true;
        }
    }

    public static boolean canPlace(@Nonnull Player player, @Nonnull BlockPos pos, @Nonnull BlockState blockState) {
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            return claim.canInteract((ServerPlayer) player, PermissionRegistry.PLACE, pos);
        } catch (Exception e) {
            Supplementaries.LOGGER.error("Failed call place block event: [Player: {}, Pos: {}, Block State: {}]", player, pos, blockState, e);
            return true;
        }
    }

    public static boolean canReplace(@Nonnull Player player, @Nonnull BlockPos pos, @Nonnull BlockState blockState) {
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            return claim.canInteract((ServerPlayer) player, PermissionRegistry.PLACE, pos);
        } catch (Exception e) {
            Supplementaries.LOGGER.error("Failed call replace block event: [Player: {}, Pos: {}, Block State: {}]", player, pos, blockState, e);
            return true;
        }
    }

    public static boolean canAttack(@Nonnull Player player, @Nonnull Entity victim) {
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
            IPermissionContainer claim = storage.getForPermissionCheck(victim.blockPosition());
            return claim.canInteract((ServerPlayer) player, PermissionRegistry.HURTANIMAL, victim.blockPosition());
        } catch (Exception e) {
            Supplementaries.LOGGER.error("Failed call attack entity event: [Player: {}, Victim: {}]", player, victim, e);
            return true;
        }
    }

    public static boolean canInteract(@Nonnull Player player, @Nonnull HumanoidArm hand, @Nonnull BlockPos targetPos, @Nonnull Direction targetSide) {
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
            IPermissionContainer claim = storage.getForPermissionCheck(targetPos);
            return claim.canInteract((ServerPlayer) player, PermissionRegistry.INTERACTBLOCK, targetPos);
        } catch (Exception e) {
            Supplementaries.LOGGER.error("Failed call interact event: [Player: {}, Hand: {}, Pos: {}, Side: {}]", player, hand, targetPos, targetSide, e);
            return true;
        }
    }

    public static boolean canInteract(@Nonnull Player player, @Nonnull HumanoidArm hand, @Nonnull BlockPos interactionPos, @Nonnull BlockPos targetPos, @Nonnull Direction targetSide) {
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
            IPermissionContainer claim = storage.getForPermissionCheck(targetPos);
            return claim.canInteract((ServerPlayer) player, PermissionRegistry.INTERACTBLOCK, targetPos);
        } catch (Exception e) {
            Supplementaries.LOGGER.error("Failed call interact event: [Player: {}, Hand: {}, Pos: {}, Side: {}]", player, hand, targetPos, targetSide, e);
            return true;
        }
    }
}