package net.mehvahdjukaar.supplementaries.mixins.forge.self;

import net.mehvahdjukaar.supplementaries.common.fluids.FiniteFluid;
import net.mehvahdjukaar.supplementaries.common.fluids.LumiseneFluid;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

import static net.mehvahdjukaar.supplementaries.reg.forge.ModFluidsImpl.LUMISENE_FLUID_TYPE;

@Mixin(LumiseneFluid.class)
public abstract class SelfLumiseneFluidMixin extends FiniteFluid {


    public SelfLumiseneFluidMixin(int maxLayers, Supplier<? extends Block> block, Supplier<? extends BucketItem> bucket) {
        super(maxLayers, block, bucket);
    }

    @Override
    public FluidType getFluidType() {
        return LUMISENE_FLUID_TYPE.get();
    }
}
