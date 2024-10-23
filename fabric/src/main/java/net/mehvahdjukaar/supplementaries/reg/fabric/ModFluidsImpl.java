package net.mehvahdjukaar.supplementaries.reg.fabric;

import net.mehvahdjukaar.supplementaries.common.items.fabric.LumiseneBucketItem;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import static net.mehvahdjukaar.supplementaries.reg.ModFluids.LUMISENE_MAX_LAYERS;

public class ModFluidsImpl {

    public static BucketItem createLumiseneBucket() {
        return new LumiseneBucketItem(ModFluids.LUMISENE_FLUID.get(), new Item.Properties()
                .stacksTo(1)
                .craftRemainder(Items.BUCKET), LUMISENE_MAX_LAYERS);
    }

}
