package net.mehvahdjukaar.supplementaries.common.items.forge;

import net.mehvahdjukaar.supplementaries.common.fluids.FiniteFluid;
import net.mehvahdjukaar.supplementaries.common.fluids.FlammableLiquidBlock;
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
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class FiniteFluidBucket extends BucketItem {
    private final int capacity;

    public FiniteFluidBucket(Supplier<? extends FiniteFluid> supplier, Properties builder, int capacity) {
        super(supplier, builder);
        this.capacity = capacity;
    }
//TODO: floats on water here
    @Override
    public boolean emptyContents(@Nullable Player arg, Level level, BlockPos pos, @Nullable BlockHitResult arg4, @Nullable ItemStack container) {
        Fluid content = this.getFluid();
        BlockState blockstate = level.getBlockState(pos);
        Block block = blockstate.getBlock();
        boolean flag = blockstate.canBeReplaced(content);
        boolean flag1 = blockstate.isAir() || flag || block instanceof LiquidBlockContainer lc && lc.canPlaceLiquid(level, pos, blockstate, content);
        Optional<FluidStack> containedFluidStack = Optional.ofNullable(container).flatMap(FluidUtil::getFluidContained);
        if (!flag1) {
            return arg4 != null && this.emptyContents(arg, level, arg4.getBlockPos().relative(arg4.getDirection()), null, container);
        } else if (containedFluidStack.isPresent() && content.getFluidType().isVaporizedOnPlacement(level, pos, containedFluidStack.get())) {
            content.getFluidType().onVaporize(arg, level, pos, containedFluidStack.get());
            return true;
        } else if (level.dimensionType().ultraWarm() && content.is(FluidTags.WATER)) {
            //TODO: instant catch fire here
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            level.playSound(arg, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

            for (int l = 0; l < 8; ++l) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, (double) i + Math.random(), (double) j + Math.random(), (double) k + Math.random(), 0.0, 0.0, 0.0);
            }

            return true;
        } else if (block instanceof LiquidBlockContainer lc && lc.canPlaceLiquid(level, pos, blockstate, content)) {
            lc.placeLiquid(level, pos, blockstate, content.defaultFluidState()
                    .setValue(FiniteFluid.LEVEL, capacity));
            this.playEmptySound(arg, level, pos);
            return true;
        } else {
            if (!level.isClientSide && flag && !blockstate.liquid()) {
                level.destroyBlock(pos, true);
            }

            BlockState newState = content.defaultFluidState().createLegacyBlock()
                    .setValue(FlammableLiquidBlock.MISSING_LEVELS, 16 - capacity);
            if (!level.setBlock(pos, newState, 11) && !blockstate.getFluidState().isSource()) {
                return false;
            } else {
                this.playEmptySound(arg, level, pos);
                return true;
            }
        }
    }



}
