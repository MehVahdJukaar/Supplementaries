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

/**
 * Pulley-flavoured moving-piston block entity. Same animation contract as
 * {@link PistonMovingBlockEntity} (progress 0 → 1, BE renderer slides {@code movedState}
 * between source and destination), but the progress step is configurable so the animation
 * can be sized to match a driver's pulse period.
 * <p>
 * Subclassed instead of mixin'd into vanilla so non-pulley pistons keep their normal speed
 * and we get a clean place to host per-instance state (the carried BE NBT for moving blocks
 * with inventories, plus the progress step). Sticky-block mods that mixin into
 * {@code PistonMovingBlockEntity} still apply to us via inheritance.
 */
public class MovingPulleyBlockEntity extends PistonMovingBlockEntity {

    /** Per-tick progress step. 0.5 = vanilla 2-tick animation. {@code 1.0/N} = N-tick animation. */
    private float progressStep = 0.5F;

    /**
     * Optional phantom block rendered with the same progress curve as the carried block,
     * positioned relative to the carried block based on {@link #extendPhantom}.
     * <ul>
     *   <li>Retract ({@code extendPhantom=false}): rendered one slot AHEAD of carried in the
     *   push direction, i.e. the consumed top rope sliding into the pulley body. Never placed.</li>
     *   <li>Extend ({@code extendPhantom=true}): rendered one slot BEHIND the carried's source
     *   in the push direction, i.e. a new rope sliding from the pulley body down into the chain.
     *   Placed at firstSlot when the animation finishes.</li>
     * </ul>
     */
    @Nullable
    private BlockState leadingState;

    /** See {@link #leadingState}. Distinguishes the two phantom variants. */
    private boolean extendPhantom = false;

    public MovingPulleyBlockEntity(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }

    public MovingPulleyBlockEntity(BlockPos pos, BlockState blockState, BlockState movedState,
                                   Direction direction, boolean extending, boolean isSourcePiston) {
        super(pos, blockState, movedState, direction, extending, isSourcePiston);
    }

    /** Sets the animation duration in ticks. Call right after construction. */
    public void setAnimationDuration(int ticks) {
        this.progressStep = ticks > 1 ? 1.0F / ticks : 0.5F;
    }

    /** Sets the leading phantom state. See {@link #leadingState} for semantics. */
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

    /**
     * {@link PistonMovingBlockEntity}'s constructor hardcodes {@code BlockEntityType.PISTON} into
     * the block entity's own {@code type} field, so the vanilla constructor-time validation would
     * check PISTON against our {@code moving_pulley_block} state and throw. Validate against our
     * real type instead (matches the overridden {@link #getType()}). Safe to call during super
     * construction, since it's a static registry lookup with no instance state.
     */
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

    /**
     * Mirrors vanilla {@link PistonMovingBlockEntity#tick} byte-for-byte except for the one line
     * that advances progress, which uses {@link #progressStep} instead of the hardcoded 0.5F.
     * Keeping the rest identical means entity interactions, finalisation (including the
     * moving-block to real-block transition) and chunk lifecycle all match vanilla exactly.
     * <p>
     * <b>Why a copy and not a call.</b> Vanilla's finalisation is guarded by
     * {@code level.getBlockState(pos).is(Blocks.MOVING_PISTON)}, which our {@code MOVING_PULLEY}
     * block fails, so delegating would animate and then never place the moved block. Reusing it
     * would take mixins on both {@code tick} and {@code finalTick} to widen that check plus another
     * for the progress step, which is more injection surface on a method other mods already crowd
     * than the copy is worth. Keep this in sync when porting to a new Minecraft version.
     */
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
                    // Extend phantom: settle the new rope at the carried block's source slot
                    // (i.e. firstSlot, the air slot just below the pulley) before the carried
                    // block lands at our position. Phantom rendering ends at this same slot at
                    // progress=1, so the placement is visually seamless.
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
                        // Restore the source block's BE data (chest contents, sign text, etc.).
                        // The mixin's finalTick TAIL does this for vanilla MOVING_PISTON; since
                        // our subclass inlines its own finalisation, we replay the same logic.
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
