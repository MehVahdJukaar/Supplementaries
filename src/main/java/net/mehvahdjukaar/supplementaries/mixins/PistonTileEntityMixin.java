package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.compat.quark.BambooSpikesPistonMovement;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PistonMovingBlockEntity.class})
public abstract class PistonTileEntityMixin extends BlockEntity implements IBlockHolder {

    public BlockState getHeldBlock(){
        return this.movedState;
    }
    public boolean setHeldBlock(BlockState state){
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

    public PistonTileEntityMixin(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Inject(method = "tick", at = @At("TAIL"), cancellable = true)
    public void tick(CallbackInfo info) {
        if (this.progressO < 1.0F && movedState.getBlock() instanceof BambooSpikesBlock) {
            boolean sameDir = (movedState.getValue(BambooSpikesBlock.FACING).equals(this.getMovementDirection()));
            AABB axisalignedbb = this.moveByPositionAndProgress(Shapes.block().bounds());
            BambooSpikesPistonMovement.tick(this.level,this.worldPosition,axisalignedbb,sameDir, this);
        }
    }

    @Shadow
    protected abstract AABB moveByPositionAndProgress(AABB bb);

    @Shadow
    public abstract Direction getMovementDirection();
}
