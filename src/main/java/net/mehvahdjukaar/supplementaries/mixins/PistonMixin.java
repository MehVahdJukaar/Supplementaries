package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.plugins.quark.BambooSpikesPistonMovement;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.PistonTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShapes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PistonTileEntity.class})
public abstract class PistonMixin extends TileEntity implements IBlockHolder {

    public BlockState getHeldBlock(){
        return this.pistonState;
    }
    public boolean setHeldBlock(BlockState state){
        this.pistonState = state;
        return true;
    }

    @Final
    @Shadow
    private BlockState pistonState;

    @Final
    @Shadow
    private boolean extending;

    @Final
    @Shadow
    private float lastProgress;

    public PistonMixin(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Inject(method = "tick", at = @At("TAIL"), cancellable = true)
    public void tick(CallbackInfo info) {
        if (this.lastProgress < 1.0F && pistonState.getBlock() instanceof BambooSpikesBlock) {
            boolean sameDir = (pistonState.get(BambooSpikesBlock.FACING).equals(this.getMotionDirection()));
            AxisAlignedBB axisalignedbb = this.moveByPositionAndProgress(VoxelShapes.fullCube().getBoundingBox());
            BambooSpikesPistonMovement.tick(this.world,this.pos,axisalignedbb,sameDir, this);
        }
    }

    @Shadow
    private AxisAlignedBB moveByPositionAndProgress(AxisAlignedBB bb) {
        return bb;
    }

    @Shadow
    public Direction getMotionDirection() {
        return Direction.UP;
    }
}
