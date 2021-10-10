package net.mehvahdjukaar.supplementaries.compat.dynamictrees;

import com.ferreusveritas.dynamictrees.blocks.DynamicSaplingBlock;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public class DynamicTreesCompat {

    public static @Nullable Block getOptionalDynamicSapling(Item item, World world, BlockPos pos) {
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
        return null;
    }

}
