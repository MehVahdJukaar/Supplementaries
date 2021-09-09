package net.mehvahdjukaar.supplementaries.compat.dynamictrees;

import com.ferreusveritas.dynamictrees.blocks.DynamicSaplingBlock;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class DynamicTreesCompat {

    public static @Nullable Block getOptionalDynamicSapling(Item item, World world, BlockPos pos) {
        if(item instanceof Seed) {
            Seed seed = ((Seed)item);
            Species species = seed.getSpecies().selfOrLocationOverride(world, pos);
            DynamicSaplingBlock sapling = species.getSapling().orElse(species.getCommonSpecies().getSapling().get());

            return sapling;
        }
        return null;
    }

}
