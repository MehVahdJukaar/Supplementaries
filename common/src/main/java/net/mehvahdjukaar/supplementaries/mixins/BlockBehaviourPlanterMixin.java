package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehaviourPlanterMixin {

    @Shadow
    public abstract Block getBlock();

    @Shadow
    public abstract boolean is(TagKey<Block> tagKey);

    @ModifyReturnValue(method = "getOffset", at = @At(
            value = "RETURN"))
    @SuppressWarnings("ConstantConditions")
    public Vec3 getOffset(Vec3 original, @Local(argsOnly = true) BlockGetter level, @Local(argsOnly = true) BlockPos pos) {
        //null check for world since some mods like to throw a null world here...
        // be sure you aren't checking other chunks
        if (level != null && !original.equals(Vec3.ZERO)) {

            if (level instanceof LevelReader l && (!l.isClientSide() || !l.hasChunkAt(pos.below(2)))) {
                return original;
            }
            int b = 1;
            if (this.getBlock() instanceof DoublePlantBlock && ((BlockBehaviour.BlockStateBase) (Object) this).getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                b = 2;
            }
            if (level.getBlockState(pos.below(b)).is(ModTags.PREVENTS_OFFSET_ABOVE)) {
                return Vec3.ZERO;
            }
        }
        return original;
    }
}
