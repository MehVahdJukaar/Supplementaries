package net.mehvahdjukaar.supplementaries.reg.fabric;

import net.frozenblock.lib.event.api.PlayerJoinEvents;
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
        }

    }

}
