package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.IAnalogRotatable;
import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.Winding;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.common.misc.PulleyCooperation;
import net.mehvahdjukaar.supplementaries.common.misc.PulleyMover;
import net.mehvahdjukaar.supplementaries.common.misc.PulleyStructureResolver;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PulleyBlock extends RotatedPillarBlock implements EntityBlock, IRotatable, IAnalogRotatable {
    public static final EnumProperty<Winding> TYPE = ModBlockProperties.WINDING;
    public static final BooleanProperty FLIPPED = ModBlockProperties.FLIPPED;

    /**
     * Block-event id meaning "do one chain step." Param bits 0-2 encode the push direction
     * via {@link Direction#get3DDataValue()}: {@link Direction#UP} = retract step (chain moves
     * toward pulley, top rope consumed), {@link Direction#DOWN} = extend step (anchor moves
     * away, new rope appears in vacated slot). Fired by
     * {@link PulleyBlockTile#pullRopeUp} / {@link PulleyBlockTile#releaseRopeDown} under
     * the continuous config. Handled by {@link #triggerEvent} on BOTH server AND client
     * (vanilla piston pattern) so the moving-piston block entities exist on the client.
     */
    public static final int EVENT_PULL_STEP = 0;

    public PulleyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y).setValue(TYPE, Winding.NONE).setValue(FLIPPED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE, FLIPPED);
    }

    /**
     * simplified rotate method that only rotates pulley on its axis
     * if direction is null assumes default orientation
     *
     * @return true if rotation was successful
     */
    public boolean windPulley(BlockState state, BlockPos pos, LevelAccessor world, Rotation rot, @Nullable Direction dir) {
        Direction.Axis axis = state.getValue(AXIS);
        if (axis == Direction.Axis.Y) return false;
        if (dir == null) dir = axis == Direction.Axis.Z ? Direction.NORTH : Direction.WEST;
        return this.rotateOverAxis(state, world, pos, rot, dir, null).isPresent();
    }

    @Override
    public Optional<BlockState> getRotatedState(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation, Direction axis, Vec3 hit) {
        Direction.Axis myAxis = state.getValue(RotatedPillarBlock.AXIS);
        Direction.Axis targetAxis = axis.getAxis();
        if (myAxis == targetAxis) return Optional.of(state.cycle(FLIPPED));
        if (myAxis == Direction.Axis.X) {
            return Optional.of(state.setValue(AXIS, targetAxis == Direction.Axis.Y ? Direction.Axis.Z : Direction.Axis.Y));
        } else if (myAxis == Direction.Axis.Z) {
            return Optional.of(state.setValue(AXIS, targetAxis == Direction.Axis.Y ? Direction.Axis.X : Direction.Axis.Y));
        } else if (myAxis == Direction.Axis.Y) {
            return Optional.of(state.setValue(AXIS, targetAxis == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z));
        }
        return Optional.of(state);
    }

    //actually unwinds ropes & rotate connected
    @Override
    public void onRotated(BlockState newState, BlockState oldState, LevelAccessor world, BlockPos pos, Rotation originalRot, Direction axis, @Nullable Vec3 hit) {
        if (axis.getAxis().isHorizontal() && axis.getAxis() == oldState.getValue(AXIS)) {

            Rotation rot = originalRot;
            if (world.getBlockEntity(pos) instanceof PulleyBlockTile pulley) {
                if (axis.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
                    rot = rot.getRotated(Rotation.CLOCKWISE_180);
                pulley.rotateDirectly(rot);
            }
            //try turning connected
            BlockPos connectedPos = pos.relative(axis);
            BlockState connected = world.getBlockState(connectedPos);
            if (connected.is(this) && newState.getValue(AXIS) == connected.getValue(AXIS)) {
                this.windPulley(connected, connectedPos, world, originalRot, axis);
            }
        }
    }


    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof PulleyBlockTile tile) {
            if (player instanceof ServerPlayer sp) {
                if (!(player.isShiftKeyDown() && this.windPulley(state, pos, level, Rotation.COUNTERCLOCKWISE_90, null))) {
                    PlatHelper.openCustomMenu(sp, tile);
                    PiglinAi.angerNearbyPiglins(player, true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level blockEntity, BlockPos pos) {
        BlockEntity tileEntity = blockEntity.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider mp ? mp : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PulleyBlockTile(pPos, pState);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    /**
     * Handles {@link #EVENT_PULL_STEP}: runs one resolver+move locally. Called on both
     * server (via the level's runBlockEvents) and client (after the server's
     * {@code ClientboundBlockEventPacket} arrives). Each side spawns its own moving-piston
     * block entities so the client sees the slide animation — vanilla pistons rely on
     * exactly this dual-side execution because {@code PistonMovingBlockEntity.getUpdatePacket}
     * is null and {@code MovingPistonBlock.newBlockEntity} also returns null, so the only way
     * the client gets a moving BE is by running the move logic locally.
     * <p>
     * Param bits 0-2 encode the push direction: UP for retract, DOWN for extend. The rope
     * hangs DOWN from the pulley in either case (vertical-pulley assumption). Server-side
     * bookkeeping happens here too: +1 item / sound on retract, place new rope + −1 item on
     * extend.
     */
    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id != EVENT_PULL_STEP) return super.triggerEvent(state, level, pos, id, param);
        if (!(level.getBlockEntity(pos) instanceof PulleyBlockTile tile)) return false;

        // Param layout matches PulleyBlockTile.fireContinuousStep.
        int rawParam = param & 0xFF;
        boolean extending = (rawParam & 1) != 0;
        int animationTicks = (rawParam >>> 1) & 0x7F;
        Direction ropeHangDir = Direction.DOWN;
        Direction pushDir = extending ? ropeHangDir : ropeHangDir.getOpposite();
        Block ropeBlock = tile.resolveRopeBlock(ropeHangDir);
        if (ropeBlock == null) return false;

        // Cooperative pulleys: dispatch to the per-level WorldSavedData on the server, the
        // client static side-channel otherwise. Same semantics either way — if this pulley was
        // already absorbed into an earlier triggerEvent's resolver call this tick, swallow our
        // own event so we don't double-shift the chain. Gated by config: when off, each pulley
        // resolves its own chain only.
        long now = level.getGameTime();
        boolean coopEnabled = net.mehvahdjukaar.supplementaries.configs.CommonConfigs.Redstone.COOPERATIVE_PULLEYS.get();
        PulleyCooperation serverData = coopEnabled && level instanceof net.minecraft.server.level.ServerLevel sl
                ? ModData.COOPERATIVE_PULLEYS.getData(sl) : null;
        if (coopEnabled) {
            boolean alreadyConsumed = serverData != null
                    ? serverData.wasConsumed(pos, now)
                    : PulleyCooperation.wasConsumedClient(pos, now);
            if (alreadyConsumed) return true;
        }

        // Gather cooperators (same period, same push dir, registered this tick window, in range)
        // and resolve all their chains in one structure pass so a shared anchor moves as one unit.
        Set<BlockPos> cooperatorPositions = !coopEnabled ? Set.of()
                : serverData != null
                    ? serverData.getCooperators(pos, animationTicks, pushDir, now)
                    : PulleyCooperation.getCooperatorsClient(pos, animationTicks, pushDir, now);
        List<PulleyStructureResolver.PulleyInfo> infos = new ArrayList<>();
        infos.add(new PulleyStructureResolver.PulleyInfo(pos, ropeBlock, ropeHangDir, extending));
        for (BlockPos cp : cooperatorPositions) {
            if (!(level.getBlockEntity(cp) instanceof PulleyBlockTile ct)) continue;
            Block cRope = ct.resolveRopeBlock(ropeHangDir);
            // Only cooperate when the same rope-block type is wound — different blocks (chain vs
            // rope) would break the resolver's single-ropeBlock assumption per chain walk.
            if (cRope == null || cRope != ropeBlock) continue;
            infos.add(new PulleyStructureResolver.PulleyInfo(cp, cRope, ropeHangDir, extending));
        }
        // Build rope-block map from infos before any world mutation.
        java.util.Map<BlockPos, Block> preMovRopeBlocks = new java.util.HashMap<>();
        for (PulleyStructureResolver.PulleyInfo info : infos) {
            preMovRopeBlocks.put(info.pulleyPos(), info.ropeBlock());
        }
        PulleyStructureResolver resolver = new PulleyStructureResolver(level, infos);

        // Resolve first so we know which pulleys contribute and can update inventories
        // immediately when the move begins, before the animation plays.
        if (!resolver.resolve()) return false;
        if (resolver.getToPush().isEmpty() && resolver.getToDestroy().isEmpty()
                && resolver.getDirectRopePlacements().isEmpty()) return false;

        if (!level.isClientSide) {
            for (BlockPos contributorPos : resolver.getContributedPulleys()) {
                if (!(level.getBlockEntity(contributorPos) instanceof PulleyBlockTile ct)) continue;
                Block cRope = preMovRopeBlocks.get(contributorPos);
                if (cRope == null) continue;
                if (extending) {
                    serverFinaliseExtend(level, contributorPos, ct, cRope, ropeHangDir);
                } else {
                    serverFinaliseRetract(level, contributorPos, ct, cRope);
                }
            }
        }

        // moveOneStep re-resolves internally (idempotent — world unchanged since our resolve call).
        boolean moved = PulleyMover.moveOneStep(level, resolver, animationTicks);
        if (!moved) return false;

        // Mark every cooperator (including self) consumed so their own incoming blockEvents this
        // tick group are no-ops.
        if (coopEnabled) {
            if (serverData != null) {
                serverData.markConsumed(pos, now);
                for (BlockPos cp : cooperatorPositions) serverData.markConsumed(cp, now);
            } else {
                PulleyCooperation.markConsumedClient(pos, now);
                for (BlockPos cp : cooperatorPositions) PulleyCooperation.markConsumedClient(cp, now);
            }
        }
        return true;
    }

    /**
     * Retract bookkeeping: +1 to displayed item, play break sound.
     */
    private static void serverFinaliseRetract(Level level, BlockPos pos, PulleyBlockTile tile, Block ropeBlock) {
        ItemStack stack = tile.getDisplayedItem();
        if (stack.isEmpty()) {
            tile.setDisplayedItem(new ItemStack(ropeBlock));
        } else if (stack.is(ropeBlock.asItem()) && stack.getCount() < stack.getMaxStackSize()) {
            stack.grow(1);
            tile.setChanged();
        }
        SoundType st = ropeBlock.defaultBlockState().getSoundType();
        level.playSound(null, pos, st.getBreakSound(), SoundSource.BLOCKS,
                (st.getVolume() + 1.0F) / 2.0F, st.getPitch() * 0.8F);
    }

    /**
     * Extend bookkeeping: place a new rope in the anchor's vacated slot, −1 displayed item,
     * play place sound. The new rope position is just past the lowest existing rope (where
     * the anchor was before moving). Walked here rather than threaded back from the resolver
     * — small re-walk, no perf concern, keeps the resolver focused on toPush.
     */
    private static void serverFinaliseExtend(Level level, BlockPos pos, PulleyBlockTile tile,
                                             Block ropeBlock, Direction ropeHangDir) {
        ItemStack stack = tile.getDisplayedItem();
        if (stack.isEmpty() || !stack.is(ropeBlock.asItem()) || stack.getCount() <= 0) {
            // Nothing to spend — extend is a no-op on the world (the move animation already
            // happened but we shouldn't leave an unanchored rope).
            return;
        }
        // The new rope at firstSlot is placed by the topmost MOVING_PULLEY's extend phantom
        // at the end of its animation — see MovingPulleyBlockEntity.tick. We only decrement
        // the spool and play the sound here.
        stack.shrink(1);
        tile.setChanged();
        SoundType st = ropeBlock.defaultBlockState().getSoundType();
        level.playSound(null, pos, st.getPlaceSound(), SoundSource.BLOCKS,
                (st.getVolume() + 1.0F) / 2.0F, st.getPitch() * 0.8F);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
    }

    @Override
    public boolean canRotateAnalog(BlockState state, Level level, BlockPos pos, Direction fromDir) {
        // Analog driving only makes sense for the continuous animated path. In legacy mode the
        // driver should fall through and call our IRotatable instead.
        return CommonConfigs.Redstone.PULLEY_CONTINUOUS.get();
    }

    @Override
    public void rotateAnalog(BlockState state, Level level, BlockPos pos, Direction fromDir, boolean ccw, float speed) {
        if (!CommonConfigs.Redstone.PULLEY_CONTINUOUS.get()) return;
        if (level.getBlockEntity(pos) instanceof PulleyBlockTile tile) {
            tile.driveAnalog(level, ccw, speed);
        }
    }

}