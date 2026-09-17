package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.Winding;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.TurnTableBlock;
import net.mehvahdjukaar.supplementaries.common.inventories.PulleyContainerMenu;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.ContinuousPulleyMover;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.InstantPulleyMover;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PulleyCooperationData;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PulleyStructureResolver;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PulleyStructureResolver.RopeColumn;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundPulleyAttemptPacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModData;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class PulleyBlockTile extends ItemDisplayTile {

    public static final Direction ROPE_HANG_DIRECTION = Direction.DOWN;

    private static final int MOVE_ROPE_COLUMN_ONE_STEP_EVENT = 0;
    private static final int MAX_ANIMATION_TICKS_FITTING_IN_EVENT_PARAM = 127;

    private static final int VANILLA_PISTON_ANIMATION_TICKS = 2;
    private static final int CRANK_ANIMATION_TICKS = 4;
    private static final int TICKS_CRANK_KEEPS_WINDING_AFTER_LAST_USE = 5;
    private static final int MAX_SIDEWAYS_ROPE_LENGTH = 7;

    private int ticksUntilNextAnalogStep = 0;
    private long lastAnalogDriveTick = -1L;
    private long lastCrankTick = -100L;
    private boolean crankExtending = false;

    public PulleyBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.PULLEY_BLOCK_TILE.get(), pos, state);
    }

    public static Winding getWindingOf(Item item) {
        boolean isChain = item instanceof BlockItem bi && bi.getBlock() instanceof ChainBlock || item.builtInRegistryHolder().is(ModTags.CHAINS);
        if (isChain) return Winding.CHAIN;
        if (item.builtInRegistryHolder().is(ModTags.ROPES)) return Winding.ROPE;
        return Winding.NONE;
    }

    public static boolean canBeWound(Item item) {
        return getWindingOf(item) != Winding.NONE;
    }

    @Override
    public void serverSideUpdateWhenChanged(HolderLookup.Provider registries) {
        super.serverSideUpdateWhenChanged(registries);
        Winding winding = getWindingOf(this.getDisplayedItem().getItem());
        BlockState state = this.getBlockState();
        if (state.getValue(PulleyBlock.WINDING) != winding) {
            level.setBlockAndUpdate(this.worldPosition, state.setValue(PulleyBlock.WINDING, winding));
        }
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new PulleyContainerMenu(i, inventory, this);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return canBeWound(stack.getItem());
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean needsToUpdateClientWhenChanged() {
        return true;
    }


    public boolean windByRotation(Rotation rot) {
        if (rot == Rotation.CLOCKWISE_90) return moveRopeOneBlock(false, VANILLA_PISTON_ANIMATION_TICKS);
        if (rot == Rotation.COUNTERCLOCKWISE_90) return moveRopeOneBlock(true, VANILLA_PISTON_ANIMATION_TICKS);
        return false;
    }

    public void windByAnalogRotation(boolean extending, float speed) {
        long now = level.getGameTime();
        boolean driverPaused = this.lastAnalogDriveTick != now - 1;
        if (driverPaused) this.ticksUntilNextAnalogStep = 0;
        this.lastAnalogDriveTick = now;

        if (this.ticksUntilNextAnalogStep > 0) {
            this.ticksUntilNextAnalogStep--;
            return;
        }
        int ticksPerStep = Math.max(2, TurnTableBlock.getPeriod((int) speed));
        moveRopeOneBlock(extending, ticksPerStep);
        this.ticksUntilNextAnalogStep = ticksPerStep;
    }

    public void windByCrank(boolean extending) {
        this.lastCrankTick = level.getGameTime();
        this.crankExtending = extending;
        keepWindingWhileCranked();
    }

    public void keepWindingWhileCranked() {
        boolean stillCranking = level.getGameTime() - this.lastCrankTick <= TICKS_CRANK_KEEPS_WINDING_AFTER_LAST_USE;
        if (!stillCranking) return;
        moveRopeOneBlock(this.crankExtending, CRANK_ANIMATION_TICKS);
        level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
    }

    private boolean moveRopeOneBlock(boolean extending, int animationTicks) {
        if (CommonConfigs.Redstone.PULLEY_CONTINUOUS.get()) {
            return postMoveRopeColumnOneStepEvent(extending, animationTicks);
        }
        if (extending) return extendInstantly(ROPE_HANG_DIRECTION, Integer.MAX_VALUE, true);
        return retractInstantly(ROPE_HANG_DIRECTION, Integer.MAX_VALUE, true);
    }


    private boolean postMoveRopeColumnOneStepEvent(boolean extending, int animationTicks) {
        if (!(level instanceof ServerLevel serverLevel)) return false;
        if (isRopeColumnAnimating()) return false;

        if (CommonConfigs.Redstone.COOPERATIVE_PULLEYS.get()) {
            registerAttemptSoCooperatorsCanJoin(serverLevel, animationTicks, pushDirection(extending));
        }
        serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), MOVE_ROPE_COLUMN_ONE_STEP_EVENT,
                packEventParam(extending, animationTicks));
        return true;
    }

    private void registerAttemptSoCooperatorsCanJoin(ServerLevel serverLevel, int animationTicks, Direction pushDir) {
        long now = serverLevel.getGameTime();
        ModData.COOPERATIVE_PULLEYS.getData(serverLevel).markAttempting(worldPosition, animationTicks, pushDir, now);
        NetworkHelper.sendToAllClientPlayersInDefaultRange(serverLevel, worldPosition,
                new ClientBoundPulleyAttemptPacket(worldPosition, animationTicks, pushDir, now));
    }

    private static int packEventParam(boolean extending, int animationTicks) {
        return (extending ? 1 : 0) | (Math.min(animationTicks, MAX_ANIMATION_TICKS_FITTING_IN_EVENT_PARAM) << 1);
    }

    private static boolean unpackExtending(int eventParam) {
        return (eventParam & 1) != 0;
    }

    private static int unpackAnimationTicks(int eventParam) {
        return (eventParam >>> 1) & MAX_ANIMATION_TICKS_FITTING_IN_EVENT_PARAM;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != MOVE_ROPE_COLUMN_ONE_STEP_EVENT) return super.triggerEvent(id, param);
        return resolveAndMoveRopeColumnOnBothSides(unpackExtending(param), unpackAnimationTicks(param));
    }

    private boolean resolveAndMoveRopeColumnOnBothSides(boolean extending, int animationTicks) {
        Block ropeBlock = getWoundOrHangingRopeBlock();
        if (ropeBlock == null) return false;

        long now = level.getGameTime();
        boolean cooperative = CommonConfigs.Redstone.COOPERATIVE_PULLEYS.get();
        boolean alreadyMovedByCooperator = cooperative && PulleyCooperationData.wasMovedThisTick(level, worldPosition, now);
        boolean inputIsDroppedNotFailed = isRopeColumnAnimating() || alreadyMovedByCooperator;
        if (inputIsDroppedNotFailed) return true;

        List<RopeColumn> columnsMovingTogether = new ArrayList<>();
        columnsMovingTogether.add(new RopeColumn(worldPosition, ropeBlock, ROPE_HANG_DIRECTION, extending));
        if (cooperative) addCooperatingRopeColumns(columnsMovingTogether, ropeBlock, extending, animationTicks, now);

        PulleyStructureResolver resolver = new PulleyStructureResolver(level, columnsMovingTogether);
        if (!resolver.resolve() || resolver.hasNothingToMove()) return false;

        if (!level.isClientSide) {
            for (BlockPos movingPulleyPos : resolver.getPulleysWhoseColumnMoves()) {
                if (level.getBlockEntity(movingPulleyPos) instanceof PulleyBlockTile movingPulley) {
                    movingPulley.onRopeColumnStartedMoving(ropeBlock, extending);
                }
            }
        }
        ContinuousPulleyMover.moveOneStep(level, resolver, animationTicks);

        if (cooperative) {
            for (RopeColumn column : columnsMovingTogether) {
                PulleyCooperationData.markMovedThisTick(level, column.pulleyPos(), now);
            }
        }
        return true;
    }

    private void addCooperatingRopeColumns(List<RopeColumn> columns, Block ropeBlock, boolean extending, int animationTicks, long now) {
        for (BlockPos cooperatorPos : PulleyCooperationData.getCooperators(level, worldPosition, animationTicks, pushDirection(extending), now)) {
            if (!level.isLoaded(cooperatorPos)) continue;
            if (level.getBlockEntity(cooperatorPos) instanceof PulleyBlockTile cooperator && cooperator.canMoveTogetherWithColumnOf(ropeBlock)) {
                columns.add(new RopeColumn(cooperatorPos, ropeBlock, ROPE_HANG_DIRECTION, extending));
            }
        }
    }

    private boolean canMoveTogetherWithColumnOf(Block otherRopeBlock) {
        return !isRopeColumnAnimating() && getWoundOrHangingRopeBlock() == otherRopeBlock;
    }

    private boolean isRopeColumnAnimating() {
        Block moving = ModRegistry.MOVING_PULLEY_BLOCK.get();
        BlockPos firstSlot = worldPosition.relative(ROPE_HANG_DIRECTION);
        return level.getBlockState(firstSlot).is(moving)
                || level.getBlockState(firstSlot.relative(ROPE_HANG_DIRECTION)).is(moving);
    }

    private static Direction pushDirection(boolean extending) {
        return extending ? ROPE_HANG_DIRECTION : ROPE_HANG_DIRECTION.getOpposite();
    }

    @Nullable
    private Block getWoundOrHangingRopeBlock() {
        ItemStack stack = getDisplayedItem();
        if (!stack.isEmpty() && stack.getItem() instanceof BlockItem bi) {
            return bi.getBlock();
        }
        if (level == null) return null;
        Block hanging = level.getBlockState(worldPosition.relative(ROPE_HANG_DIRECTION)).getBlock();
        if (canBeWound(hanging.asItem())) return hanging;
        return null;
    }

    private void onRopeColumnStartedMoving(Block ropeBlock, boolean extending) {
        ItemStack stack = getDisplayedItem();
        if (extending) {
            if (stack.isEmpty() || !stack.is(ropeBlock.asItem())) return;
            stack.shrink(1);
            setChanged();
        } else if (stack.isEmpty()) {
            setDisplayedItem(new ItemStack(ropeBlock));
        } else if (stack.is(ropeBlock.asItem()) && stack.getCount() < stack.getMaxStackSize()) {
            stack.grow(1);
            setChanged();
        }
        playWindingSound(ropeBlock, extending);
    }

    private void playWindingSound(Block ropeBlock, boolean extending) {
        SoundType soundType = ropeBlock.defaultBlockState().getSoundType();
        level.playSound(null, worldPosition, extending ? soundType.getPlaceSound() : soundType.getBreakSound(),
                SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
    }


    public boolean retractInstantly(Direction ropeDir, int maxDist, boolean storeRetractedRope) {
        ItemStack stack = this.getDisplayedItem();
        boolean startsNewStack = false;
        if (stack.isEmpty()) {
            Item hangingItem = level.getBlockState(worldPosition.below()).getBlock().asItem();
            if (!canBeWound(hangingItem)) return false;
            stack = new ItemStack(hangingItem);
            startsNewStack = true;
        }
        if (stack.getCount() + 1 > stack.getMaxStackSize() || !(stack.getItem() instanceof BlockItem bi)) return false;
        Block ropeBlock = bi.getBlock();
        boolean success = InstantPulleyMover.removeRope(worldPosition.relative(ropeDir), level, ropeBlock, ropeDir, maxDist);
        if (success) {
            playWindingSound(ropeBlock, false);
            if (startsNewStack) this.setDisplayedItem(stack);
            else if (storeRetractedRope) stack.grow(1);
            this.setChanged();
        }
        return success;
    }

    public boolean extendInstantly(Direction ropeDir, int maxDist, boolean spendWoundRope) {
        ItemStack stack = this.getDisplayedItem();
        if (stack.getCount() < 1 || !(stack.getItem() instanceof BlockItem bi)) return false;
        Block ropeBlock = bi.getBlock();

        boolean success = InstantPulleyMover.addRope(worldPosition.relative(ropeDir), level, null, InteractionHand.MAIN_HAND, ropeBlock, ropeDir, maxDist);
        if (success) {
            playWindingSound(ropeBlock, true);
            if (spendWoundRope) {
                stack.shrink(1);
                this.setChanged();
            }
        }
        return success;
    }

    public boolean passRopeThroughInstantly(Block ropeBlock, Direction incomingRopeDir, boolean extending) {
        if (CommonConfigs.Redstone.PULLEY_CONTINUOUS.get()) return false;
        ItemStack stack = getDisplayedItem();
        if (stack.isEmpty()) {
            if (!extending) return false;
            this.setDisplayedItem(new ItemStack(ropeBlock));
            return true;
        }

        if (!stack.is(ropeBlock.asItem())) return false;
        BlockState state = getBlockState();
        Direction.Axis axis = state.getValue(PulleyBlock.AXIS);
        if (axis == incomingRopeDir.getAxis()) return false;

        level.setBlockAndUpdate(worldPosition, state.cycle(PulleyBlock.FLIPPED));

        Direction[] ropeExitOrder = incomingRopeDir.getAxis().isHorizontal() ? new Direction[]{Direction.DOWN} :
                new Direction[]{incomingRopeDir, incomingRopeDir.getClockWise(axis), incomingRopeDir.getCounterClockWise(axis)};

        List<Direction> exitsWithoutRope = new ArrayList<>();
        for (var exitDir : ropeExitOrder) {
            boolean ropeAlreadyExitsHere = InstantPulleyMover.isCorrectRope(ropeBlock, level.getBlockState(worldPosition.relative(exitDir)), exitDir);
            if (ropeAlreadyExitsHere) return moveRopeInstantlyAtExit(exitDir, extending);
            exitsWithoutRope.add(exitDir);
        }
        for (var exitDir : exitsWithoutRope) {
            if (moveRopeInstantlyAtExit(exitDir, extending)) return true;
        }
        boolean unwindsOwnRopeInstead = !extending;
        if (unwindsOwnRopeInstead) {
            stack.shrink(1);
            this.setChanged();
            return true;
        }
        return false;
    }

    private boolean moveRopeInstantlyAtExit(Direction exitDir, boolean extending) {
        int maxDist = exitDir == Direction.DOWN ? Integer.MAX_VALUE : MAX_SIDEWAYS_ROPE_LENGTH;
        if (extending) return extendInstantly(exitDir, maxDist, false);
        return retractInstantly(exitDir, maxDist, false);
    }
}
