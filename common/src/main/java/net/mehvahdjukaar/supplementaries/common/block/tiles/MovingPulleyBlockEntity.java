package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.misc.block_movement.ICarryingMovingPiston;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PistonMovementHelper;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

// like the vanilla moving piston but with a configurable animation speed
public class MovingPulleyBlockEntity extends PistonMovingBlockEntity {

    private float progressStep = 0.5F;
    @Nullable
    private BlockState leadingState;
    private boolean extendPhantom = false;

    public MovingPulleyBlockEntity(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }
    public MovingPulleyBlockEntity(BlockPos pos, BlockState blockState, BlockState movedState,
                                   Direction direction, boolean extending, boolean isSourcePiston) {
        super(pos, blockState, movedState, direction, extending, isSourcePiston);
    }

    public void setAnimationDuration(int ticks) {
        this.progressStep = ticks > 1 ? 1.0F / ticks : 0.5F;
    }

    public void setLeadingState(@Nullable BlockState state) {
        this.leadingState = state;
    }

    @Nullable
    public BlockState getLeadingState() {
        return this.leadingState;
    }

    public void setExtendPhantom(boolean value) {
        this.extendPhantom = value;
    }

    public boolean isExtendPhantom() {
        return this.extendPhantom;
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModRegistry.MOVING_PULLEY_BLOCK_TILE.get();
    }

    @Override
    public boolean isValidBlockState(BlockState state) {
        return getType().isValid(state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.progressStep != 0.5F) tag.putFloat("supp_progress_step", this.progressStep);
        if (this.leadingState != null) {
            tag.put("supp_leading_state", NbtUtils.writeBlockState(this.leadingState));
        }
        if (this.extendPhantom) tag.putBoolean("supp_extend_phantom", true);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("supp_progress_step", CompoundTag.TAG_FLOAT)) {
            this.progressStep = tag.getFloat("supp_progress_step");
        }
        if (tag.contains("supp_leading_state", CompoundTag.TAG_COMPOUND)) {
            this.leadingState = NbtUtils.readBlockState(
                    registries.lookupOrThrow(Registries.BLOCK),
                    tag.getCompound("supp_leading_state"));
        }
        this.extendPhantom = tag.getBoolean("supp_extend_phantom");
    }

    //copy of PistonMovingBlockEntity.tick with progressStep instead of the hardcoded 0.5F.
    public static void tick(Level level, BlockPos pos, BlockState state, MovingPulleyBlockEntity be) {
        be.lastTicked = level.getGameTime();
        be.progressO = be.progress;
        if (be.progressO >= 1.0F) {
            if (level.isClientSide && be.deathTicks < 5) {
                be.deathTicks++;
            } else {
                level.removeBlockEntity(pos);
                be.setRemoved();
                if (level.getBlockState(pos).is(ModRegistry.MOVING_PULLEY_BLOCK.get())) {
                    // place the new rope where the phantom rendering ended
                    if (be.extendPhantom && be.leadingState != null) {
                        BlockPos phantomLandPos = pos.relative(be.getDirection().getOpposite());
                        if (level.getBlockState(phantomLandPos).isAir()) {
                            level.setBlock(phantomLandPos, be.leadingState, 3);
                        }
                    }
                    BlockState movedAfter = Block.updateFromNeighbourShapes(be.getMovedState(), level, pos);
                    if (movedAfter.isAir()) {
                        level.setBlock(pos, be.getMovedState(), 84);
                        Block.updateOrDestroy(be.getMovedState(), movedAfter, level, pos, 3);
                    } else {
                        if (movedAfter.hasProperty(BlockStateProperties.WATERLOGGED)
                                && movedAfter.getValue(BlockStateProperties.WATERLOGGED)) {
                            movedAfter = movedAfter.setValue(BlockStateProperties.WATERLOGGED, false);
                        }
                        level.setBlock(pos, movedAfter, 67);
                        level.neighborChanged(pos, movedAfter.getBlock(), pos);
                        CompoundTag carriedNbt = ((ICarryingMovingPiston) be).supp$getCarriedBlockEntityNbt();
                        if (carriedNbt != null) {
                            PistonMovementHelper.restoreBlockEntity(level, pos, movedAfter, carriedNbt);
                        }
                    }
                }
            }
        } else {
            float f = be.progress + be.progressStep;
            moveCollidedEntities(level, pos, f, be);
            moveStuckEntities(level, pos, f, be);
            be.progress = f;
            if (be.progress >= 1.0F) be.progress = 1.0F;
        }
    }
}
