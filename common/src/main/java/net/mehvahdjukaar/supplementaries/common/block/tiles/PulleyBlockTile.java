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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class PulleyBlockTile extends ItemDisplayTile {

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

    public boolean pullRopeUp() {
        boolean continuous = CommonConfigs.Redstone.PULLEY_CONTINUOUS.get();
        boolean isServer = level instanceof ServerLevel;
        net.mehvahdjukaar.supplementaries.Supplementaries.LOGGER.info(
                "[PulleyTile @ {}] pullRopeUp called — continuous_config={} isServerLevel={} levelClass={}",
                worldPosition, continuous, isServer, level == null ? "null" : level.getClass().getSimpleName());
        if (continuous) {
            // Client side is a no-op: server is authoritative for the move, client will receive
            // the block event and run triggerEvent locally to spawn moving-piston BEs. Falling
            // through to legacy pullRope here would let the client mutate its world directly
            // (the bug we saw in the diagnostic logs).
            if (!isServer) return false;
            return fireContinuousStep(Direction.UP);
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
    private boolean fireContinuousStep(Direction pushDir) {
        if (!(level instanceof ServerLevel sl)) return false;
        int param = pushDir.get3DDataValue() & 7;
        net.mehvahdjukaar.supplementaries.Supplementaries.LOGGER.info(
                "[PulleyTile @ {}] fireContinuousStep pushDir={} param={} blockEvent queued",
                worldPosition, pushDir, param);
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
        boolean continuous = CommonConfigs.Redstone.PULLEY_CONTINUOUS.get();
        boolean isServer = level instanceof ServerLevel;
        net.mehvahdjukaar.supplementaries.Supplementaries.LOGGER.info(
                "[PulleyTile @ {}] releaseRopeDown called — continuous_config={} isServerLevel={} levelClass={}",
                worldPosition, continuous, isServer, level == null ? "null" : level.getClass().getSimpleName());
        if (continuous) {
            // See pullRopeUp for the rationale: client side is a no-op in continuous mode.
            if (!isServer) return false;
            return fireContinuousStep(Direction.DOWN);
        }
        return releaseRope(Direction.DOWN, Integer.MAX_VALUE, true);
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
