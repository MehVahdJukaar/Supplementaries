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
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
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

    //one chain step. runs on both sides like vanilla piston events, see packStepParam
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


    //true while a step is still animating, so further input is ignored. without this an extend
    //re-fire reads the transient air below the pulley as empty and dumps a rope every tick
    public static boolean isChainAnimating(Level level, BlockPos pulleyPos, Direction ropeHangDir) {
        Block moving = ModRegistry.MOVING_PULLEY_BLOCK.get();
        BlockPos firstSlot = pulleyPos.relative(ropeHangDir);
        return level.getBlockState(firstSlot).is(moving)
                || level.getBlockState(firstSlot.relative(ropeHangDir)).is(moving);
    }

    //block event param is 8 bits: bit 0 extending, bits 1-7 animation ticks (0 = vanilla speed)
    public static int packStepParam(boolean extending, int animationTicks) {
        return (extending ? 1 : 0) | ((Math.min(animationTicks, 127) & 0x7F) << 1);
    }

    private static boolean unpackExtending(int param) {
        return (param & 1) != 0;
    }

    private static int unpackAnimationTicks(int param) {
        return (param >>> 1) & 0x7F;
    }

    //runs one resolve and move locally, on both sides. moving piston BEs are never synced so the
    //client only gets one by running this itself. server also does the item and sound bookkeeping
    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id != EVENT_PULL_STEP) return super.triggerEvent(state, level, pos, id, param);
        if (!(level.getBlockEntity(pos) instanceof PulleyBlockTile tile)) return false;

        boolean extending = unpackExtending(param);
        int animationTicks = unpackAnimationTicks(param);
        Direction ropeHangDir = Direction.DOWN;
        Direction pushDir = extending ? ropeHangDir : ropeHangDir.getOpposite();
        Block ropeBlock = tile.resolveRopeBlock(ropeHangDir);
        if (ropeBlock == null) return false;

        //one rotation at a time. true because we drop the input on purpose, it's not a failure
        if (isChainAnimating(level, pos, ropeHangDir)) return true;

        long now = level.getGameTime();
        boolean coop = CommonConfigs.Redstone.COOPERATIVE_PULLEYS.get();
        //another pulley's resolve already moved our chain this tick
        if (coop && PulleyCooperationData.wasConsumed(level, pos, now)) return true;

        //all cooperating chains resolve in one pass so a shared anchor moves as one
        Set<BlockPos> cooperators = coop
                ? PulleyCooperationData.getCooperators(level, pos, animationTicks, pushDir, now)
                : Set.of();
        List<PulleyStructureResolver.PulleyInfo> infos = new ArrayList<>();
        infos.add(new PulleyStructureResolver.PulleyInfo(pos, ropeBlock, ropeHangDir, extending));
        for (BlockPos cooperatorPos : cooperators) {
            if (canJoinResolve(level, cooperatorPos, ropeBlock, ropeHangDir)) {
                infos.add(new PulleyStructureResolver.PulleyInfo(cooperatorPos, ropeBlock, ropeHangDir, extending));
            }
        }

        PulleyStructureResolver resolver = new PulleyStructureResolver(level, infos);
        if (!resolver.resolve() || resolver.hasNothingToMove()) return false;

        //inventories update as the move starts, not when the animation lands
        if (!level.isClientSide) {
            for (BlockPos contributorPos : resolver.getContributedPulleys()) {
                if (level.getBlockEntity(contributorPos) instanceof PulleyBlockTile contributorTile) {
                    updateWoundItem(level, contributorPos, contributorTile, ropeBlock, extending);
                }
            }
        }
        PulleyMover.moveOneStep(level, resolver, animationTicks);

        //so the joined pulleys drop their own event this tick
        if (coop) {
            for (PulleyStructureResolver.PulleyInfo info : infos) {
                PulleyCooperationData.markConsumed(level, info.pulleyPos(), now);
            }
        }
        return true;
    }

    private static boolean canJoinResolve(Level level, BlockPos cooperatorPos, Block ropeBlock, Direction ropeHangDir) {
        //getBlockEntity would force load the chunk
        if (!level.isLoaded(cooperatorPos)) return false;
        if (!(level.getBlockEntity(cooperatorPos) instanceof PulleyBlockTile cooperatorTile)) return false;
        //its own gate just skipped this step, pulling it in would hit the same transient air
        if (isChainAnimating(level, cooperatorPos, ropeHangDir)) return false;
        //the resolver walks one rope block type per chain
        return cooperatorTile.resolveRopeBlock(ropeHangDir) == ropeBlock;
    }

    //extend spends one rope item, retract stores one. the rope block itself is placed or eaten by
    //the moving block when it lands
    private static void updateWoundItem(Level level, BlockPos pos, PulleyBlockTile tile, Block ropeBlock, boolean extending) {
        ItemStack stack = tile.getDisplayedItem();
        SoundType soundType = ropeBlock.defaultBlockState().getSoundType();
        if (extending) {
            if (stack.isEmpty() || !stack.is(ropeBlock.asItem())) return;
            stack.shrink(1);
            tile.setChanged();
            level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        } else {
            if (stack.isEmpty()) {
                tile.setDisplayedItem(new ItemStack(ropeBlock));
            } else if (stack.is(ropeBlock.asItem()) && stack.getCount() < stack.getMaxStackSize()) {
                stack.grow(1);
                tile.setChanged();
            }
            level.playSound(null, pos, soundType.getBreakSound(), SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        }
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
        //legacy mode has no animation to pace, so the driver falls through to IRotatable
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