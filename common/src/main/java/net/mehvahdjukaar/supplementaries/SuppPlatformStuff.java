package net.mehvahdjukaar.supplementaries;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmInstance;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;


public class SuppPlatformStuff {

    @ExpectPlatform
    public static EntityType<?> getFishType(MobBucketItem bucketItem) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    @Contract
    public static <T> T getForgeCap(@NotNull Object object, Class<T> capClass) {
        throw new AssertionError();
    }

    @Nullable
    @Contract
    @ExpectPlatform
    public static BlockState getUnoxidised(Level level, BlockPos pos, BlockState state) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isEndermanMask(@NotNull EnderMan enderMan, Player player, ItemStack itemstack) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static int getItemLifeSpawn(ItemEntity itemEntity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onItemPickup(Player player, ItemEntity itemEntity, ItemStack copy) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CreativeModeTab.Builder searchBar(CreativeModeTab.Builder c) {
        throw new ArrayStoreException();
    }

    @ExpectPlatform
    public static float getDownfall(Biome biome){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void disableAMWarn() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void disableOFWarn(boolean on) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canStickTo(BlockState movedState, BlockState blockState) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SlotReference getFirstInInventory(LivingEntity entity, Predicate<ItemStack> predicate) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static FoodProperties getFoodProperties(ItemStack selected, LivingEntity entity) {
        throw new AssertionError();
    }
}