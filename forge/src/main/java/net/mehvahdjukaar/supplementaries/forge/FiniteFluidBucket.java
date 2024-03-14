package net.mehvahdjukaar.supplementaries.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public class FiniteFluidBucket extends BucketItem {
    public FiniteFluidBucket(Supplier<? extends FiniteFluid> supplier, Properties builder) {
        super(supplier, builder);
    }

    @Override
    public boolean emptyContents(@Nullable Player arg, Level arg2, BlockPos arg3, @Nullable BlockHitResult arg4, @Nullable ItemStack container) {
        Fluid content = this.getFluid();
        BlockState blockstate = arg2.getBlockState(arg3);
        Block block = blockstate.getBlock();
        boolean flag = blockstate.canBeReplaced(content);
        boolean flag1 = blockstate.isAir() || flag || block instanceof LiquidBlockContainer lc && lc.canPlaceLiquid(arg2, arg3, blockstate, content);
        Optional<FluidStack> containedFluidStack = Optional.ofNullable(container).flatMap(FluidUtil::getFluidContained);
        if (!flag1) {
            return arg4 != null && this.emptyContents(arg, arg2, arg4.getBlockPos().relative(arg4.getDirection()), null, container);
        } else if (containedFluidStack.isPresent() && content.getFluidType().isVaporizedOnPlacement(arg2, arg3, containedFluidStack.get())) {
            content.getFluidType().onVaporize(arg, arg2, arg3, containedFluidStack.get());
            return true;
        } else if (arg2.dimensionType().ultraWarm() && content.is(FluidTags.WATER)) {
            //TODO: instant catch fire here
            int i = arg3.getX();
            int j = arg3.getY();
            int k = arg3.getZ();
            arg2.playSound(arg, arg3, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (arg2.random.nextFloat() - arg2.random.nextFloat()) * 0.8F);

            for (int l = 0; l < 8; ++l) {
                arg2.addParticle(ParticleTypes.LARGE_SMOKE, (double) i + Math.random(), (double) j + Math.random(), (double) k + Math.random(), 0.0, 0.0, 0.0);
            }

            return true;
        } else if (block instanceof LiquidBlockContainer lc && lc.canPlaceLiquid(arg2, arg3, blockstate, content)) {
            lc.placeLiquid(arg2, arg3, blockstate, content.defaultFluidState());
            this.playEmptySound(arg, arg2, arg3);
            return true;
        } else {
            if (!arg2.isClientSide && flag && !blockstate.liquid()) {
                arg2.destroyBlock(arg3, true);
            }

            if (!arg2.setBlock(arg3, content.defaultFluidState().createLegacyBlock(), 11) && !blockstate.getFluidState().isSource()) {
                return false;
            } else {
                this.playEmptySound(arg, arg2, arg3);
                return true;
            }
        }
    }

}
