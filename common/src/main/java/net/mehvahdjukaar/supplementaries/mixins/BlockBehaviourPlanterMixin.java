package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.TurnTableBlock;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehaviourPlanterMixin {

    @Shadow
    public abstract Block getBlock();

    @Shadow
    public abstract boolean is(TagKey<Block> tagKey);

    @Inject(method = "getOffset", at = @At(
            value = "RETURN",
            ordinal = 1),
            cancellable = true)
    @SuppressWarnings("ConstantConditions")
    public void getOffset(BlockGetter world, BlockPos pos, CallbackInfoReturnable<Vec3> cir) {
        //null check for world since some mods like to throw a null world here...
        if (world != null && cir.getReturnValue() != Vec3.ZERO &&
                !world.isOutsideBuildHeight(pos.getY() - 2) && world instanceof RenderChunkRegion) {
            int b = 1;
            if (this.getBlock() instanceof DoublePlantBlock && ((BlockBehaviour.BlockStateBase) (Object) this).getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                b = 2;
            }
            if (world.getBlockState(pos.below(b)).getBlock() instanceof PlanterBlock){
                cir.setReturnValue(Vec3.ZERO);
            }
        }
    }
}
