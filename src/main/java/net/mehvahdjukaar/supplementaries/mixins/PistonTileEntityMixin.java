package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.compat.quark.BambooSpikesPistonMovement;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.PistonTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PistonTileEntity.class})
public abstract class PistonTileEntityMixin extends TileEntity implements IBlockHolder {

    public BlockState getHeldBlock() {
        return this.movedState;
    }

    public boolean setHeldBlock(BlockState state) {
        this.movedState = state;
        return true;
    }

    @Shadow
    private BlockState movedState;

    //lastProgress
    @Shadow
    private float progressO;

    public PistonTileEntityMixin(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo info) {
        if (this.progressO < 1.0F && movedState.getBlock() instanceof BambooSpikesBlock) {
            boolean sameDir = (movedState.getValue(BambooSpikesBlock.FACING).equals(this.getMovementDirection()));
            AxisAlignedBB axisalignedbb = this.moveByPositionAndProgress(VoxelShapes.block().bounds());
            BambooSpikesPistonMovement.tick(this.level, this.worldPosition, axisalignedbb, sameDir, this);
        }
    }

    @Shadow
    protected abstract AxisAlignedBB moveByPositionAndProgress(AxisAlignedBB bb);

    @Shadow
    public abstract Direction getMovementDirection();
}
