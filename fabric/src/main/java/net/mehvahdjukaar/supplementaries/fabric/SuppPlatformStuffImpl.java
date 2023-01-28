package net.mehvahdjukaar.supplementaries.fabric;

import net.mehvahdjukaar.supplementaries.mixins.fabric.MobBucketItemAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class SuppPlatformStuffImpl {


    public static EntityType<?> getFishType(MobBucketItem bucketItem) {
        return ((MobBucketItemAccessor) bucketItem).getType();
    }

    @Nullable
    public static <T> T getForgeCap(Object object, Class<T> capClass) {
        return null;
    }

    public static BlockState getUnoxidised(Level level, BlockPos pos, BlockState state) {
        return null;
    }

    public static boolean isEndermanMask(EnderMan enderman, Player player, ItemStack itemstack) {
        return itemstack.getItem() == Blocks.CARVED_PUMPKIN.asItem();
    }

    public static int getItemLifeSpawn(ItemEntity itemEntity) {
        return 6000;
    }

    public static void onItemPickup(Player player, ItemEntity itemEntity, ItemStack copy) {
    }

}
