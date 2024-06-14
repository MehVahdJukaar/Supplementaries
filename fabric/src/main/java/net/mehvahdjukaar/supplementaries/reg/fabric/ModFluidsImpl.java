package net.mehvahdjukaar.supplementaries.reg.fabric;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.client.renderers.fabric.LumiseneFluidRendererImpl;
import net.mehvahdjukaar.supplementaries.common.fluids.FiniteFluid;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;

public class ModFluidsImpl {
    public static BucketItem createLumiseneBucket() {
        return new BucketItem(Fluids.WATER, new Item.Properties());
    }

    public static FiniteFluid createLumisene() {
        return new LumiseneFluid();
    }

    public static Item createLumiseneBottle() {
        return new Item(new Item.Properties());
    }

    public static class LumiseneFluid extends FiniteFluid {
        public LumiseneFluid() {
            super(16, ModFluids.LUMISENE_BLOCK, ModFluids.LUMISENE_BUCKET);

            if (PlatHelper.getPhysicalSide().isClient()) {
                FluidRenderHandlerRegistry.INSTANCE.register(this, new LumiseneFluidRendererImpl());
            }
        }

    }

}
