package net.mehvahdjukaar.supplementaries.integration.fabric;
/*
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import javax.annotation.Nonnull;
import nanolive.eventhelper.EventHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public final class EventUtils {
    public EventUtils() {
    }

    public static boolean cantBreak(@Nonnull Player player, @Nonnull BlockPos pos) {
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel)player.f_19853_);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            return !claim.canInteract((ServerPlayer)player, PermissionRegistry.BREAK, pos, true);
        } catch (Throwable var4) {
            EventHelper.LOGGER.error("Failed call break block event: [Player: {}, Pos: {}]", player, pos);
            if (EventHelper.debug) {
                var4.printStackTrace();
            }

            return true;
        }
    }

    public static boolean cantPlace(@Nonnull Player player, @Nonnull BlockPos pos, @Nonnull BlockState blockState) {
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel)player.f_19853_);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            return !claim.canInteract((ServerPlayer)player, PermissionRegistry.PLACE, pos);
        } catch (Throwable var5) {
            EventHelper.LOGGER.error("Failed call place block event: [Player: {}, Pos: {}, Block State: {}]", player, pos, blockState);
            if (EventHelper.debug) {
                var5.printStackTrace();
            }

            return true;
        }
    }

    public static boolean cantReplace(@Nonnull Player player, @Nonnull BlockPos pos, @Nonnull BlockState blockState) {
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel)player.f_19853_);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            return !claim.canInteract((ServerPlayer)player, PermissionRegistry.PLACE, pos);
        } catch (Throwable var5) {
            EventHelper.LOGGER.error("Failed call replace block event: [Player: {}, Pos: {}, Block State: {}]", player, pos, blockState);
            if (EventHelper.debug) {
                var5.printStackTrace();
            }

            return true;
        }
    }

    public static boolean cantAttack(@Nonnull Player player, @Nonnull Entity victim) {
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel)player.f_19853_);
            IPermissionContainer claim = storage.getForPermissionCheck(victim.m_142538_());
            return !claim.canInteract((ServerPlayer)player, PermissionRegistry.HURTANIMAL, victim.m_142538_());
        } catch (Throwable var4) {
            EventHelper.LOGGER.error("Failed call attack entity event: [Player: {}, Victim: {}]", player, victim);
            if (EventHelper.debug) {
                var4.printStackTrace();
            }

            return true;
        }
    }

    public static boolean cantInteract(@Nonnull Player player, @Nonnull HumanoidArm hand, @Nonnull BlockPos targetPos, @Nonnull Direction targetSide) {
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel)player.f_19853_);
            IPermissionContainer claim = storage.getForPermissionCheck(targetPos);
            return !claim.canInteract((ServerPlayer)player, PermissionRegistry.INTERACTBLOCK, targetPos);
        } catch (Throwable var6) {
            EventHelper.LOGGER.error("Failed call interact event: [Player: {}, Hand: {}, Pos: {}, Side: {}]", player, hand, targetPos, targetSide);
            if (EventHelper.debug) {
                var6.printStackTrace();
            }

            return true;
        }
    }

    public static boolean cantInteract(@Nonnull Player player, @Nonnull HumanoidArm hand, @Nonnull BlockPos interactionPos, @Nonnull BlockPos targetPos, @Nonnull Direction targetSide) {
        try {
            ClaimStorage storage = ClaimStorage.get((ServerLevel)player.f_19853_);
            IPermissionContainer claim = storage.getForPermissionCheck(targetPos);
            return !claim.canInteract((ServerPlayer)player, PermissionRegistry.INTERACTBLOCK, targetPos);
        } catch (Throwable var7) {
            EventHelper.LOGGER.error("Failed call interact event: [Player: {}, Hand: {}, Pos: {}, Side: {}]", player, hand, targetPos, targetSide);
            if (EventHelper.debug) {
                var7.printStackTrace();
            }

            return true;
        }
    }
}
*/