package net.mehvahdjukaar.supplementaries;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;


public class SuppPlatformStuff {

    @ExpectPlatform
    @Nullable
    @Contract
    public static <T> T getForgeCap(@NotNull Entity entity, Class<T> capClass) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    @Contract
    public static <T> T getForgeCap(@NotNull BlockEntity entity, Class<T> capClass) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    @Contract
    public static <T> T getForgeCap(Level level, BlockPos entity, Class<T> capClass) {
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
    public static void fireItemPickupPost(Player player, ItemEntity itemEntity, ItemStack copy) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CreativeModeTab.Builder searchBar(CreativeModeTab.Builder c) {
        throw new ArrayStoreException();
    }

    @ExpectPlatform
    public static float getDownfall(Biome biome) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void disableAMWarn() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void disableIMWarn() {
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

    @ExpectPlatform
    public static SoundType getSoundType(BlockState blockState, BlockPos pos, Level level, Entity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setParticlePos(BlockParticleOption blockParticleOption, BlockPos pos) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void tryBurningByFire(ServerLevel level, BlockPos pos, int chance, RandomSource random, int age, Direction direction) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canCatchFire(Level level, BlockPos pos, Direction direction) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static InteractionResultHolder<ItemStack> fireItemRightClickEvent(Player player, InteractionHand hand) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ItemStack finishUsingItem(ItemStack item, Level level, LivingEntity livingEntity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void dispenseContent(DispensibleContainerItem dc, ItemStack stack, BlockHitResult hit, Level level, @Nullable Player player) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static float getGrowthSpeed(BlockState state, ServerLevel level, BlockPos pos) {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static void releaseUsingItem(ItemStack stack, LivingEntity entity) {
        throw new AssertionError();
    }


}