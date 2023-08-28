package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
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

    // TODO: test
    @ModifyReturnValue(
            method = "method_49227",
            at = @At("RETURN")
    )
    private Vec3 ignoreOffset(Vec3 original, BlockGetter level, BlockPos pos,
                              BlockBehaviour.OffsetFunction offsetFunction) {
        //null check for world since some mods like to throw a null world here...
        if (level != null && original != Vec3.ZERO &&
                !level.isOutsideBuildHeight(pos.getY() - 2) && level instanceof RenderChunkRegion) {
            int b = 1;
            if (this.getBlock() instanceof DoublePlantBlock && ((BlockBehaviour.BlockStateBase) (Object) this).getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                b = 2;
            }
            if (level.getBlockState(pos.below(b)).is(ModTags.PREVENTS_OFFSET_ABOVE)){
                return Vec3.ZERO;
            }
        }
        return original;
    }
}
