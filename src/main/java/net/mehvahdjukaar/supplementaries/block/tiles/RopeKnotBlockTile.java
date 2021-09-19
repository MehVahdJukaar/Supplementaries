package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.supplementaries.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.common.util.Constants;

import static net.mehvahdjukaar.supplementaries.block.blocks.RopeKnotBlock.*;


public class RopeKnotBlockTile extends MimicBlockTile {

    private VoxelShape collisionShape = null;
    private VoxelShape shape = null;

    public RopeKnotBlockTile() {
        super(ModRegistry.ROPE_KNOT_TILE.get());
        this.setHeldBlock(Blocks.AIR.defaultBlockState());
    }

    public VoxelShape getCollisionShape() {
        if (collisionShape == null) this.recalculateShapes(this.getBlockState());
        return collisionShape;
    }

    public VoxelShape getShape() {
        if (shape == null) this.recalculateShapes(this.getBlockState());
        return shape;
    }

    private static final VoxelShape DOWN_SHAPE = Block.box(6, 0, 6, 10, 6, 10);

    public void recalculateShapes(BlockState state) {
        if (!state.is(ModRegistry.ROPE_KNOT.get())) return;
        BlockState mimic = this.getHeldBlock();
        boolean up = state.getValue(UP);
        boolean down = state.getValue(DOWN);
        VoxelShape r;
        if (down && !up) {
            r = DOWN_SHAPE;
        } else {
            BlockState rope = ModRegistry.ROPE.get().defaultBlockState()
                    .setValue(RopeBlock.KNOT, false)
                    .setValue(UP, up)
                    .setValue(DOWN, down)
                    .setValue(NORTH, state.getValue(NORTH))
                    .setValue(SOUTH, state.getValue(SOUTH))
                    .setValue(EAST, state.getValue(EAST))
                    .setValue(WEST, state.getValue(WEST));
            r = rope.getShape(this.level, this.worldPosition);
        }
        VoxelShape c = mimic.getCollisionShape(this.level, this.worldPosition);
        VoxelShape s = mimic.getShape(this.level, this.worldPosition);
        c = VoxelShapes.or(c, r);
        s = VoxelShapes.or(s, r);
        this.collisionShape = c.optimize();
        this.shape = s.optimize();
    }

    @Override
    public void setChanged() {
        if (this.level == null) return;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(),
                Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.NOTIFY_NEIGHBORS);
        this.requestModelDataUpdate();
        super.setChanged();
    }

    @Override
    public void requestModelDataUpdate() {
        this.recalculateShapes(this.getBlockState());
        super.requestModelDataUpdate();
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        if (state != null) {
            this.recalculateShapes(state);
        }
    }
}