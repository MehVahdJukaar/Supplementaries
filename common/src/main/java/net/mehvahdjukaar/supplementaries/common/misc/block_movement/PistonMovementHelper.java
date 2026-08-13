package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PistonMovementHelper {

    public static boolean BEMovementHandledByUs() {
        return CommonConfigs.Tweaks.PUSH_BLOCK_ENTITIES.get() && !BEMovementHandledByQuark();
    }

    public static boolean BEMovementHandledByQuark() {
        return CompatHandler.QUARK && QuarkCompat.isMovingTileEntitiesEnabled();
    }

    public static int perPistonPushLimit() {
        if (CompatHandler.QUARK) return QuarkCompat.getPistonPushLimit();
        return PistonStructureResolver.MAX_PUSH_DEPTH;
    }

    public static boolean isMovementBlacklisted(BlockState state) {
        if (state.is(ModTags.RELOCATION_NOT_SUPPORTED)) return true;
        //honour quark's list even when its own module is off
        return state.hasBlockEntity() && CompatHandler.QUARK && QuarkCompat.blacklistsBlockMovement(state);
    }

    //for our own movers. piston rules plus the blacklist, whatever the push reaction says
    public static boolean isPushableByOurMovers(BlockState state, Level level, BlockPos pos,
                                                Direction movementDirection, boolean allowDestroy,
                                                Direction moverFacing) {
        return !isMovementBlacklisted(state) &&
                PistonBaseBlock.isPushable(state, level, pos, movementDirection, allowDestroy, moverFacing);
    }

    @Nullable
    public static CompoundTag captureAndDetachBlockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return null;
        CompoundTag nbt = be.saveWithFullMetadata(level.registryAccess());
        level.removeBlockEntity(pos);
        return nbt;
    }

    public static void restoreBlockEntity(Level level, BlockPos pos, BlockState placedState, CompoundTag nbt) {
        if (!placedState.hasBlockEntity() || !(placedState.getBlock() instanceof EntityBlock entityBlock)) return;
        BlockEntity restored = entityBlock.newBlockEntity(pos, placedState);
        if (restored == null || !matchesCapturedType(restored, nbt)) return;
        restored.loadWithComponents(nbt, level.registryAccess());
        level.setBlockEntity(restored);
    }

    public static boolean matchesCapturedType(BlockEntity be, CompoundTag nbt) {
        String id = nbt.getString("id");
        //old data has no id saved
        if (id.isEmpty()) return true;
        ResourceLocation type = ResourceLocation.tryParse(id);
        return type != null && be.getType() == BuiltInRegistries.BLOCK_ENTITY_TYPE.get(type);
    }
}
