package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.supplementaries.common.fluids.FiniteFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class FiniteFluidInteraction implements FaucetTarget.Fluid, FaucetSource.Fluid {

    @Override
    public Integer fill(Level level, BlockPos pos, FluidState existing, SoftFluidStack fluid, int minAmount) {
        var vanillaF = fluid.getVanillaFluid();
        if (vanillaF instanceof FiniteFluid ff) {
            int oldLayers = 0;
            if (existing.getType().isSame(vanillaF)) {
                oldLayers = existing.getAmount();
            } else if (!existing.isEmpty() || !level.getBlockState(pos).isAir()) {
                return null;
            }
            int maxLayers = ff.getLayersPerBlock();
            int missingLayers = maxLayers - oldLayers;
            if (missingLayers <= 0) return 0; // exit and no further processing
            // 1 block = 1 bucket = 4 bottles
            float offerLayers = bottlesToLayers(ff, 1); //consumes 1 bottle at a time

            float extraLayers = Math.min(missingLayers, offerLayers);
            float consumed = layersToBottles(ff, extraLayers);
            level.setBlockAndUpdate(pos, ff.makeState((int) (oldLayers + extraLayers)).createLegacyBlock());
            return Math.min(Mth.floor(consumed), fluid.getCount());
        }
        return null;
    }

    @Override
    public @Nullable FluidOffer getProvidedFluid(Level level, BlockPos pos, Direction dir, FluidState source) {
        if (source.getType() instanceof FiniteFluid ff) {
            int amount = layersToBottles(ff, source.getAmount());
            if (amount > 0) {
                return FluidOffer.of(SoftFluidStack.fromFluid(ff, amount, null));
            }
        }
        return null;
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, FluidState source, int amount) {
        FiniteFluid ff = (FiniteFluid) source.getType();
        int drainedLayers = bottlesToLayers(ff, amount);
        int newLayers = Math.max(0, source.getAmount() - drainedLayers);
        if (newLayers == 0) {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        } else level.setBlockAndUpdate(pos, ff.makeState(newLayers).createLegacyBlock());
    }

    private int layersToBottles(FiniteFluid ff, float layers) {
        int maxLayers = ff.getLayersPerBlock();
        float bottleToLayers = maxLayers / 4f;
        return (int) (layers / bottleToLayers);
    }

    private int bottlesToLayers(FiniteFluid ff, int bottles) {
        int maxLayers = ff.getLayersPerBlock();
        float bottleToLayers = maxLayers / 4f;
        return (int) (bottles * bottleToLayers);
    }
}
