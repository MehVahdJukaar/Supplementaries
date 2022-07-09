package net.mehvahdjukaar.supplementaries.integration.dynamictrees;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

public class DynamicTreesCompat {

    public static @Nullable
    Block getOptionalDynamicSapling(Item item, Level world, BlockPos pos) {
        /*
        if(item instanceof Seed) {
            Seed seed = ((Seed)item);
            Species species = seed.getSpecies().selfOrLocationOverride(world, pos);

            Optional<DynamicSaplingBlock> s = species.getSapling();
            if(s.isPresent())return s.get();
            else{
                Optional<DynamicSaplingBlock> c = species.getCommonSpecies().getSapling();
                if(c.isPresent())return c.get();
            }
        }
        */

        return null;
    }

}
