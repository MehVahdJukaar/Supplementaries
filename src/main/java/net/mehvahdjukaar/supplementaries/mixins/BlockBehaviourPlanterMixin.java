package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.biome.Biomes;
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

    @Inject(method = "getOffset", at = @At(
            value = "RETURN",
            ordinal = 1),
            cancellable = true)
    public void getOffset(BlockGetter world, BlockPos pos, CallbackInfoReturnable<Vec3> cir) {
        //null check for world since some mods like to throw a null world here...
        if (world != null && cir.getReturnValue() != Vec3.ZERO) {
            int b = 1;
            if (this.getBlock() instanceof DoublePlantBlock && world.getBlockState(pos).getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                b = 2;
            }
            if (world.getBlockState(pos.below(b)).is(ModRegistry.PLANTER.get())) {
                cir.setReturnValue(Vec3.ZERO);
            }
        }
    }
}
