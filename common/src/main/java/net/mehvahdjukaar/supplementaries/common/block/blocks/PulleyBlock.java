package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.IAnalogRotatable;
import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.Winding;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PulleyCooperationData;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PulleyMover;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PulleyStructureResolver;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModData;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PulleyBlock extends RotatedPillarBlock implements EntityBlock, IRotatable, IAnalogRotatable {
    public static final EnumProperty<Winding> TYPE = ModBlockProperties.WINDING;
    public static final BooleanProperty FLIPPED = ModBlockProperties.FLIPPED;

    //block event for one chain step. runs on both sides like vanilla pistons, so the client gets
    //its moving block entities too. the param carries direction and animation duration
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


    //true while a step is still animating, so we ignore further input. without this an extend
    //re-fire reads the transient air below the pulley as empty and dumps a rope every tick
    public static boolean isChainAnimating(Level level, BlockPos pulleyPos, Direction ropeHangDir) {
        Block moving = ModRegistry.MOVING_PULLEY_BLOCK.get();
        BlockPos firstSlot = pulleyPos.relative(ropeHangDir);
        return level.getBlockState(firstSlot).is(moving)
                || level.getBlockState(firstSlot.relative(ropeHangDir)).is(moving);
    }

    //runs one resolve and move locally, on both sides. moving piston BEs are never synced so the
    //client only gets one by running this itself. server also does the item and sound bookkeeping
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

        //one rotation at a time. true because we drop the input on purpose, it's not a failure
        if (isChainAnimating(level, pos, ropeHangDir)) return true;

        //if we were already absorbed into another pulley's resolve this tick, drop our event
        long now = level.getGameTime();
        boolean coopEnabled = CommonConfigs.Redstone.COOPERATIVE_PULLEYS.get();
        PulleyCooperationData serverData = coopEnabled && level instanceof ServerLevel serverLevel
                ? ModData.COOPERATIVE_PULLEYS.getData(serverLevel) : null;
        if (coopEnabled) {
            boolean alreadyConsumed = serverData != null
                    ? serverData.wasConsumed(pos, now)
                    : PulleyCooperationData.wasConsumedClient(pos, now);
            if (alreadyConsumed) return true;
        }

        // Gather cooperators (same period, same push dir, registered this tick window, in range)
        // and resolve all their chains in one structure pass so a shared anchor moves as one unit.
        Set<BlockPos> cooperatorPositions = !coopEnabled ? Set.of()
                : serverData != null
                    ? serverData.getCooperators(pos, animationTicks, pushDir, now)
                  : PulleyCooperationData.getCooperatorsClient(pos, animationTicks, pushDir, now);
        List<PulleyStructureResolver.PulleyInfo> infos = new ArrayList<>();
        infos.add(new PulleyStructureResolver.PulleyInfo(pos, ropeBlock, ropeHangDir, extending));
        for (BlockPos cooperatorPos : cooperatorPositions) {
            if (!(level.getBlockEntity(cooperatorPos) instanceof PulleyBlockTile cooperatorTile)) continue;
            // Skip a cooperator whose own chain is still animating: pulling it into our resolve
            // pass would hit the same transient-air extend trap its own gate just dodged.
            if (isChainAnimating(level, cooperatorPos, ropeHangDir)) continue;
            Block cooperatorRope = cooperatorTile.resolveRopeBlock(ropeHangDir);
            // Only cooperate when the same rope-block type is wound: different blocks (chain vs
            // rope) would break the resolver's single-ropeBlock assumption per chain walk.
            if (cooperatorRope != ropeBlock) continue;
            infos.add(new PulleyStructureResolver.PulleyInfo(cooperatorPos, cooperatorRope, ropeHangDir, extending));
        }
        // Build the rope-block map from infos before any world mutation.
        Map<BlockPos, Block> ropeBlockByPulley = new HashMap<>();
        for (PulleyStructureResolver.PulleyInfo info : infos) {
            ropeBlockByPulley.put(info.pulleyPos(), info.ropeBlock());
        }
        PulleyStructureResolver resolver = new PulleyStructureResolver(level, infos);

        // Resolve first so we know which pulleys contribute and can update inventories
        // immediately when the move begins, before the animation plays.
        if (!resolver.resolve()) return false;
        if (resolver.getToPush().isEmpty() && resolver.getToDestroy().isEmpty()
                && resolver.getDirectRopePlacements().isEmpty()) return false;

        if (!level.isClientSide) {
            for (BlockPos contributorPos : resolver.getContributedPulleys()) {
                if (!(level.getBlockEntity(contributorPos) instanceof PulleyBlockTile contributorTile)) continue;
                Block contributorRope = ropeBlockByPulley.get(contributorPos);
                if (contributorRope == null) continue;
                if (extending) {
                    serverFinaliseExtend(level, contributorPos, contributorTile, contributorRope);
                } else {
                    serverFinaliseRetract(level, contributorPos, contributorTile, contributorRope);
                }
            }
        }

        // moveOneStep re-resolves internally (idempotent: the world is unchanged since our call).
        boolean moved = PulleyMover.moveOneStep(level, resolver, animationTicks);
        if (!moved) return false;

        // Mark every cooperator (including self) consumed so their own incoming blockEvents this
        // tick group are no-ops.
        if (coopEnabled) {
            if (serverData != null) {
                serverData.markConsumed(pos, now);
                for (BlockPos cooperatorPos : cooperatorPositions) serverData.markConsumed(cooperatorPos, now);
            } else {
                PulleyCooperationData.markConsumedClient(pos, now);
                for (BlockPos cooperatorPos : cooperatorPositions) {
                    PulleyCooperationData.markConsumedClient(cooperatorPos, now);
                }
            }
        }
        return true;
    }

    // Retract bookkeeping: +1 to displayed item, play break sound.
    private static void serverFinaliseRetract(Level level, BlockPos pos, PulleyBlockTile tile, Block ropeBlock) {
        ItemStack stack = tile.getDisplayedItem();
        if (stack.isEmpty()) {
            tile.setDisplayedItem(new ItemStack(ropeBlock));
        } else if (stack.is(ropeBlock.asItem()) && stack.getCount() < stack.getMaxStackSize()) {
            stack.grow(1);
            tile.setChanged();
        }
        SoundType soundType = ropeBlock.defaultBlockState().getSoundType();
        level.playSound(null, pos, soundType.getBreakSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
    }

    //just the item count and sound, the rope itself is placed by the moving block when it lands
    private static void serverFinaliseExtend(Level level, BlockPos pos, PulleyBlockTile tile,
                                             Block ropeBlock) {
        ItemStack stack = tile.getDisplayedItem();
        if (stack.isEmpty() || !stack.is(ropeBlock.asItem())) {
            // Nothing to spend, so extending is a no-op on the world: the move animation already
            // happened but we shouldn't leave an unanchored rope.
            return;
        }
        stack.shrink(1);
        tile.setChanged();
        SoundType soundType = ropeBlock.defaultBlockState().getSoundType();
        level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
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
        return CommonConfigs.Redstone.PULLEY_CONTINUOUS.get()
                && fromDir.getAxis().isHorizontal()
                && state.getValue(AXIS) == fromDir.getAxis();
    }

    @Override
    public void rotateAnalog(BlockState state, Level level, BlockPos pos, Direction fromDir, boolean ccw, float speed) {
        if (!CommonConfigs.Redstone.PULLEY_CONTINUOUS.get()) return;
        if (!fromDir.getAxis().isHorizontal() || state.getValue(AXIS) != fromDir.getAxis()) return;
        if (level.getBlockEntity(pos) instanceof PulleyBlockTile tile) {
            tile.driveAnalog(level, ccw, speed);
        }
    }

}