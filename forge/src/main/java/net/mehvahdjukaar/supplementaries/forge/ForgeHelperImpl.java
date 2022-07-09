package net.mehvahdjukaar.supplementaries.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.network.NetworkHooks;

public class ForgeHelperImpl {

    public static boolean canEntityDestroy(Level level, BlockPos blockPos, Animal animal) {
        Tags.Blocks
        return ForgeHooks.canEntityDestroy(level, blockPos, animal);
    }
}
