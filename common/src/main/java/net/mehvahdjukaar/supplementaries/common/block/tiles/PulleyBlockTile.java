package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.Winding;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.common.inventories.PulleyContainerMenu;
import net.mehvahdjukaar.supplementaries.common.utils.RopeHelper;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class PulleyBlockTile extends ItemDisplayTile {

    // Cooldown between analog-driven pulls. 0 = fire on this call. Transient; not persisted.
    private int analogCooldown = 0;
    // Game tick of the last driveAnalog call. Used to detect gaps so a restart fires immediately
    // instead of waiting for whatever residual cooldown was left from a previous drive.
    private long lastAnalogDriveTick = -1L;

    public PulleyBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.PULLEY_BLOCK_TILE.get(), pos, state);
    }

    public static Winding getContentType(Item item) {
        Winding type = Winding.NONE;
        if (item instanceof BlockItem bi && bi.getBlock() instanceof ChainBlock || item.builtInRegistryHolder().is(ModTags.CHAINS))
            type = Winding.CHAIN;
        else if (item.builtInRegistryHolder().is(ModTags.ROPES)) type = Winding.ROPE;
        return type;
    }

    @Override
    public void updateTileOnInventoryChanged() {
        Winding type = getContentType(this.getDisplayedItem().getItem());
        BlockState state = this.getBlockState();
        if (state.getValue(PulleyBlock.TYPE) != type) {
            level.setBlockAndUpdate(this.worldPosition, state.setValue(PulleyBlock.TYPE, type));
        }
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new PulleyContainerMenu(i, inventory, this);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return (getContentType(stack.getItem()) != Winding.NONE);
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


    public boolean rotateDirectly(Rotation rot) {
        if (rot == Rotation.CLOCKWISE_90) return this.pullRopeUp();
        else if (rot == Rotation.COUNTERCLOCKWISE_90) return this.releaseRopeDown();
        else return false;
    }

    /**
     * No-driver path: rotation-source unknown, animation runs at vanilla 2-tick speed.
     */
    public boolean pullRopeUp() {
        return pullRopeUp(0);
    }

    /**
     * @param animationTicks ticks the animation should last. 0 = vanilla 2-tick default. A
     *                       driver (e.g. {@link net.mehvahdjukaar.supplementaries.common.block.tiles.TurnTableBlockTile})
     *                       that knows its own pulse period passes it directly so the animation
     *                       lasts the same wall-clock time the legacy instant move would've.
     */
    public boolean pullRopeUp(int animationTicks) {
        if (CommonConfigs.Redstone.PULLEY_CONTINUOUS.get()) {
            // Client side is a no-op: server is authoritative; the client will receive the block
            // event and run triggerEvent locally to spawn the moving-piston BEs.
            if (!(level instanceof ServerLevel)) return false;
            return fireContinuousStep(Direction.UP, animationTicks);
        }
        return pullRope(Direction.DOWN, Integer.MAX_VALUE, true);
    }

    /**
     * Fires a single animated step. The pulley is purely an event initiator — moving-piston
     * BEs spawned by {@link PulleyBlock#triggerEvent} handle the animation autonomously.
     * Re-firing while a previous animation is in flight is fine: the next resolver will see
     * MOVING_PISTON in the chain's adjacent slot and return false (chain busy), so the event
     * is silently dropped. That's the "automatic" behavior — no busy tracking needed here.
     *
     * @param pushDir UP = retract step, DOWN = extend step
     */
    /**
     * @param pushDir        UP for retract, DOWN for extend.
     * @param animationTicks how long the animation should last in ticks. 0 = vanilla 2-tick speed.
     *                       Encoded in the blockEvent param so triggerEvent on both sides can size
     *                       the moving-piston BE's progress increment to match.
     */
    private boolean fireContinuousStep(Direction pushDir, int animationTicks) {
        if (!(level instanceof ServerLevel sl)) return false;
        // Param layout (8-bit limit per ClientboundBlockEventPacket): bit 0 = extending flag
        // (0 = retract, 1 = extend), bits 1-7 = animationTicks clamped to 0..127. 0 = vanilla speed.
        int extendingBit = pushDir == Direction.DOWN ? 1 : 0;
        int param = extendingBit | ((Math.min(animationTicks, 127) & 0x7F) << 1);
        sl.blockEvent(worldPosition, getBlockState().getBlock(), PulleyBlock.EVENT_PULL_STEP, param);
        return true;
    }

    /**
     * Resolves the rope block this pulley winds: displayed item if present, else the block
     * directly along the rope-hang direction. Used by {@link PulleyBlock#triggerEvent}.
     */
    @Nullable
    public Block resolveRopeBlock(Direction ropeDir) {
        ItemStack stack = getDisplayedItem();
        if (!stack.isEmpty() && stack.getItem() instanceof BlockItem bi) {
            return bi.getBlock();
        }
        if (level == null) return null;
        Block adjacent = level.getBlockState(worldPosition.relative(ropeDir)).getBlock();
        if (getContentType(adjacent.asItem()) != Winding.NONE) return adjacent;
        return null;
    }

    public boolean pullRope(Direction moveDir, int maxDist, boolean addItem) {
        ItemStack stack = this.getDisplayedItem();
        boolean addNewItem = false;
        if (stack.isEmpty()) {
            Item i = level.getBlockState(worldPosition.below()).getBlock().asItem();
            if (getContentType(i) == Winding.NONE) return false;
            stack = new ItemStack(i);
            addNewItem = true;
        }
        if (stack.getCount() + 1 > stack.getMaxStackSize() || !(stack.getItem() instanceof BlockItem)) return false;
        Block ropeBlock = ((BlockItem) stack.getItem()).getBlock();
        boolean success = RopeHelper.removeRope(worldPosition.relative(moveDir), level, ropeBlock, moveDir, maxDist);
        if (success) {
            SoundType soundtype = ropeBlock.defaultBlockState().getSoundType();
            level.playSound(null, worldPosition, soundtype.getBreakSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            if (addNewItem) this.setDisplayedItem(stack);
            else if (addItem) stack.grow(1);
            this.setChanged();
        }
        return success;
    }

    public boolean releaseRopeDown() {
        return releaseRopeDown(0);
    }

    /**
     * @see #pullRopeUp(int)
     */
    public boolean releaseRopeDown(int animationTicks) {
        if (CommonConfigs.Redstone.PULLEY_CONTINUOUS.get()) {
            if (!(level instanceof ServerLevel)) return false;
            return fireContinuousStep(Direction.DOWN, animationTicks);
        } else return releaseRope(Direction.DOWN, Integer.MAX_VALUE, true);
    }

    /**
     * Driven by an external analog rotator (e.g. {@link PulleyBlock#rotateAnalog}, called once
     * per tick by a {@link net.mehvahdjukaar.supplementaries.common.block.tiles.TurnTableBlockTile}).
     * First call after a gap fires a pull immediately; subsequent calls within the same drive
     * decrement a cooldown so pulls land at the same wall-clock cadence the old instant-mode
     * cooldown would have produced.
     *
     * @param speed the driver's analog speed value. For a turn table this is its redstone power
     *              (0-15); the period formula here mirrors {@code TurnTableBlock.getPeriod}.
     */
    public void driveAnalog(Level level, boolean ccw, float speed) {
        long now = level.getGameTime();
        if (this.lastAnalogDriveTick != now - 1) {
            // Either a fresh start or the driver paused — reset so we fire on this call.
            this.analogCooldown = 0;
        }
        this.lastAnalogDriveTick = now;

        if (this.analogCooldown > 0) {
            this.analogCooldown--;
            return;
        }
        // Mirrors TurnTableBlock.getPeriod(power): power 15 → 4 ticks, power 1 → 60 ticks.
        int period = Math.max(2, (int) ((60 - speed * 4) + 4));
        // Register intent on BOTH server and client at the same local tick so the matching
        // triggerEvent (which arrives on the client a few ticks late) can still see all
        // attempted pulleys and run them through the resolver as one cooperative group.
        // Server uses the per-level WorldSavedData; client uses a static side-channel.
        Direction pushDir = ccw ? Direction.DOWN : Direction.UP;
        if (level instanceof ServerLevel sl) {
            net.mehvahdjukaar.supplementaries.reg.ModData.COOPERATIVE_PULLEYS.getData(sl)
                    .markAttempting(this.worldPosition, period, pushDir, now);
        } else {
            net.mehvahdjukaar.supplementaries.common.misc.PulleyCooperation
                    .markAttemptingClient(this.worldPosition, period, pushDir, now);
        }
        if (ccw) {
            releaseRopeDown(period);
        } else {
            pullRopeUp(period);
        }
        this.analogCooldown = period;
    }

    public boolean releaseRope(Direction dir, int maxDist, boolean removeItem) {

        ItemStack stack = this.getDisplayedItem();
        if (stack.getCount() < 1 || !(stack.getItem() instanceof BlockItem bi)) return false;
        Block ropeBlock = bi.getBlock();

        boolean success = RopeHelper.addRope(worldPosition.relative(dir), level, null, InteractionHand.MAIN_HAND, ropeBlock, dir, maxDist);
        if (success) {
            SoundType soundtype = ropeBlock.defaultBlockState().getSoundType();
            level.playSound(null, worldPosition, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            if (removeItem) {
                stack.shrink(1);
                this.setChanged();
            }
        }
        return success;
    }


    //called when another pulley indirectly rotates this through a rope or chain
    public boolean rotateIndirect(Player player, InteractionHand hand, Block ropeBlock, Direction moveDir, boolean retracting) {
        // Cross-pulley chaining is disabled in continuous mode (per user spec): in that mode
        // a pulley is purely a fire-and-forget event initiator and shouldn't recursively
        // poke other pulleys. The animation system isn't designed to drive multi-pulley chains.
        if (CommonConfigs.Redstone.PULLEY_CONTINUOUS.get()) return false;
        ItemStack stack = getDisplayedItem();
        if (stack.isEmpty()) {
            if (retracting) {
                return false;
            } else {
                this.setDisplayedItem(new ItemStack(ropeBlock));
                return true;
            }
        }

        if (!stack.is(ropeBlock.asItem())) return false;
        BlockState state = getBlockState();
        Direction.Axis axis = state.getValue(PulleyBlock.AXIS);
        if (axis == moveDir.getAxis()) return false;

        level.setBlockAndUpdate(worldPosition, state.cycle(PulleyBlock.FLIPPED));

        Direction[] order = moveDir.getAxis().isHorizontal() ? new Direction[]{Direction.DOWN} :
                new Direction[]{moveDir, moveDir.getClockWise(axis), moveDir.getCounterClockWise(axis)};

        List<Direction> remaining = new ArrayList<>();
        int maxSideDist = 7;
        for (var d : order) {
            if (RopeHelper.isCorrectRope(ropeBlock, level.getBlockState(worldPosition.relative(d)), d)) {
                if (moveConnected(retracting, maxSideDist, d)) {
                    return true;
                }
                //returns if we found a rope but failed
                return false;
            } else remaining.add(d);
        }
        for (var d : remaining) {
            if (moveConnected(retracting, maxSideDist, d)) {
                return true;
            }
        }
        if (retracting) {
            stack.shrink(1);
            this.setChanged();
            return true;
        }
        return false;
    }

    private boolean moveConnected(boolean retracting, int maxSideDist, Direction d) {
        int dist = d == Direction.DOWN ? Integer.MAX_VALUE : maxSideDist;
        if (retracting) {
            return pullRope(d, dist, false);
        } else {
            return releaseRope(d, dist, false);
        }
    }


    //no need since it doesn't display stuff
    @Override
    public boolean needsToUpdateClientWhenChanged() {
        return false;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return null;
    }
}
