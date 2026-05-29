package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.MovingPulleyBlockEntity;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Pulley-side moving block. Behaves identically to {@link MovingPistonBlock} but routes
 * through {@link MovingPulleyBlockEntity} so the animation duration can be customised
 * per-instance (matched to the driving turn table's pulse period). Same null
 * {@code newBlockEntity} pattern as vanilla — the BE is created manually by
 * {@code PulleyMover} via {@link #newMovingBlockEntity}.
 */
public class MovingPulleyBlock extends MovingPistonBlock {

    public MovingPulleyBlock(Properties properties) {
        super(properties);
    }

    public static MovingPulleyBlockEntity newMovingBlockEntity(BlockPos pos, BlockState blockState,
                                                               BlockState movedState,
                                                               Direction direction,
                                                               boolean extending, boolean isSourcePiston) {
        return new MovingPulleyBlockEntity(pos, blockState, movedState, direction, extending, isSourcePiston);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModRegistry.MOVING_PULLEY_BLOCK_TILE.get(),
                MovingPulleyBlockEntity::tick);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }
}
