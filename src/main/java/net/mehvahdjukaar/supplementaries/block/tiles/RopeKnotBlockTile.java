package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Constants;

import static net.mehvahdjukaar.supplementaries.block.blocks.RopeKnotBlock.*;


public class RopeKnotBlockTile extends MimicBlockTile {

    private VoxelShape collisionShape = null;
    private VoxelShape shape = null;

    public RopeKnotBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.ROPE_KNOT_TILE.get(), pos, state);
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
        try {
            if (state == null || !state.is(ModRegistry.ROPE_KNOT.get()) || this.level == null) return;
            BlockState mimic = this.getHeldBlock();
            if (mimic.isAir()) mimic = Blocks.STONE.defaultBlockState();
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
            c = Shapes.or(c, r);
            s = Shapes.or(s, r);
            this.collisionShape = c.optimize();
            this.shape = s.optimize();
        } catch (Exception e) {
            Supplementaries.LOGGER.warn("failed to calculate roped fence hitbox: " + e);
        }
    }

    @Override
    public void setChanged() {
        if (this.level == null) return;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(),
                Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.NOTIFY_NEIGHBORS);
        //not sure if needed here
        this.requestModelDataUpdate();
        this.collisionShape = null;
        this.shape = null;
        super.setChanged();
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.collisionShape = null;
        this.shape = null;
    }
}