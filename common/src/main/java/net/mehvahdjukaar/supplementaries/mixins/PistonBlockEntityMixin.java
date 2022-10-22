package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.mehvahdjukaar.supplementaries.common.block.IInstanceTick;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
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

@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonBlockEntityMixin extends BlockEntity implements IBlockHolder, IInstanceTick {

    @Shadow
    private Direction direction;
    @Shadow
    private float progress;
    @Shadow
    private float progressO;

    @Shadow
    public abstract BlockState getMovedState();

    @Shadow
    private BlockState movedState;

    public PistonBlockEntityMixin(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    @Override
    public BlockState getHeldBlock() {
        return this.movedState;
    }

    @Override
    public boolean setHeldBlock(BlockState state) {
        this.movedState = state;
        return true;
    }

    @Shadow
    protected abstract float getExtendedProgress(float pProgress);

    @Shadow
    public abstract Direction getMovementDirection();

    @Inject(method = "tick", at = @At("TAIL"))
    private static void tick(Level pLevel, BlockPos pPos, BlockState pState, PistonMovingBlockEntity tile, CallbackInfo info) {
        if (tile instanceof IInstanceTick t) {
            t.instanceTick(pLevel, pPos);
        }
    }

    public void instanceTick(Level level, BlockPos pos) {
        if (this.progressO < 1.0F && this.getMovedState().getBlock() instanceof BambooSpikesBlock) {
            boolean sameDir = (this.getMovedState().getValue(BambooSpikesBlock.FACING).equals(this.getMovementDirection()));
            AABB aabb = this.moveByPositionAndProgress(pos, Shapes.block().bounds());
            QuarkCompat.tickPiston(level, pos, aabb, sameDir, this);
        }
    }

    private AABB moveByPositionAndProgress(BlockPos pos, AABB aabb) {
        double d0 = this.getExtendedProgress(this.progress);
        return aabb.move(pos.getX() + d0 * this.direction.getStepX(), pos.getY() + d0 * this.direction.getStepY(), pos.getZ() + d0 * this.direction.getStepZ());
    }


}
