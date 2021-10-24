package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.block.util.IInstanceTick;
import net.mehvahdjukaar.supplementaries.compat.quark.BambooSpikesPistonMovement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PistonMovingBlockEntity.class})
public abstract class PistonBlockEntityMixin extends BlockEntity implements IBlockHolder, IInstanceTick {

    @Shadow
    private float progress;
    @Shadow
    private Direction direction;

    @Shadow
    public abstract BlockState getMovedState();

    public PistonBlockEntityMixin(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    public BlockState getHeldBlock() {
        return this.movedState;
    }

    public boolean setHeldBlock(BlockState state) {
        this.movedState = state;
        return true;
    }

    @Shadow
    private BlockState movedState;

    @Shadow
    private boolean extending;

    //lastProgress
    @Shadow
    private float progressO;


    @Inject(method = "tick", at = @At("TAIL"))
    private static void tick(Level pLevel, BlockPos pPos, BlockState pState, PistonMovingBlockEntity tile, CallbackInfo info) {
        if(tile instanceof IInstanceTick t){
            t.instanceTick(pLevel, pPos);
        }
    }

    public void instanceTick(Level level, BlockPos pos) {
        if (this.progressO < 1.0F && this.getMovedState().getBlock() instanceof BambooSpikesBlock) {
            boolean sameDir = (this.getMovedState().getValue(BambooSpikesBlock.FACING).equals(this.getMovementDirection()));
            AABB aabb = this.moveByPositionAndProgress(pos, Shapes.block().bounds());
            BambooSpikesPistonMovement.tick(level, pos, aabb, sameDir, this);
        }
    }

    private AABB moveByPositionAndProgress(BlockPos pos, AABB aabb) {
        double d0 = this.getExtendedProgress(this.progress);
        return aabb.move((double)pos.getX() + d0 * (double)this.direction.getStepX(), (double)pos.getY() + d0 * (double)this.direction.getStepY(), (double)pos.getZ() + d0 * (double)this.direction.getStepZ());
    }

    @Shadow
    protected abstract float getExtendedProgress(float pProgress);

    @Shadow
    public abstract Direction getMovementDirection();
}
