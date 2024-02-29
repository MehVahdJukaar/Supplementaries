package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.Holder;

public record FluidOffer(SoftFluidStack fluid, int minAmount) {
    //min amount is minimum amount that offer will consume regardless of what happens if successful of course

    public static FluidOffer of(SoftFluidStack stack, int min) {
        if (stack.getCount() < min) {
            throw new IllegalStateException("Minimum fluid amount was bigger than actual fluid amount");
        }
        return new FluidOffer(stack, min);
    }

    public static FluidOffer of(SoftFluidStack stack) {
        return of(stack, 1);
    }

    public static FluidOffer of(Holder<SoftFluid> fluid, int amount, int minAmount) {
        return of(new SoftFluidStack(fluid, amount), minAmount);
    }

    public static FluidOffer of(Holder<SoftFluid> fluid, int amount) {
        return of(new SoftFluidStack(fluid, amount), 1);
    }

    public static FluidOffer of(Holder<SoftFluid> fluid) {
        return of(new SoftFluidStack(fluid, 1), 1);
    }
}
