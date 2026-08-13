package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehaviorRegistry;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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

    //called by ItemStack:useOn but only takes into account placement so we dont fire item used events and such
    //basically this mimics what happens when onUse is called but for a block item only WITHOUT firing events related to onUse
    @PlatformImpl
    public static InteractionResult placeBlockItem(BlockItem bi, BlockPlaceContext context) {
        throw new AssertionError();
    }

    @PlatformImpl
    @Nullable
    @Contract
    public static <T> T getForgeCap(@NotNull Entity entity, Class<T> capClass) {
        throw new AssertionError();
    }

    @PlatformImpl
    @Nullable
    @Contract
    public static <T> T getForgeCap(@NotNull BlockEntity entity, Class<T> capClass) {
        throw new AssertionError();
    }

    @PlatformImpl
    @Nullable
    @Contract
    public static <T> T getForgeCap(Level level, BlockPos entity, Class<T> capClass) {
        throw new AssertionError();
    }

    @Nullable
    @Contract
    @PlatformImpl
    public static BlockState getUnoxidised(Level level, BlockPos pos, BlockState state) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean isEndermanMask(@NotNull EnderMan enderMan, Player player, ItemStack itemstack) {
        throw new AssertionError();
    }

    @Contract
    @PlatformImpl
    public static int getItemLifeSpawn(ItemEntity itemEntity) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void fireItemPickupPost(Player player, ItemEntity itemEntity, ItemStack copy) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static CreativeModeTab.Builder searchBar(CreativeModeTab.Builder c) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static float getDownfall(Biome biome) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean canStickTo(BlockState movedState, BlockState blockState) {
        throw new AssertionError();
    }

    // NeoForge goes through IBlockExtension.isStickyBlock so modded sticky blocks work, Fabric falls
    // back to vanilla's slime-or-honey check
    @PlatformImpl
    public static boolean isSticky(BlockState state) {
        throw new AssertionError();
    }

    // NeoForge does a bidirectional IBlockExtension.canStickTo, Fabric mirrors vanilla's private
    // canStickToEachOther: honey and slime never stick to each other, otherwise either side sticky wins
    @PlatformImpl
    public static boolean canStickToEachOther(BlockState a, BlockState b) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static SlotReference getFirstInInventory(LivingEntity entity, Predicate<ItemStack> predicate) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static FoodProperties getFoodProperties(ItemStack selected, LivingEntity entity) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static SoundType getSoundType(BlockState blockState, BlockPos pos, Level level, Entity entity) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void setParticlePos(BlockParticleOption blockParticleOption, BlockPos pos) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void tryBurningByFire(ServerLevel level, BlockPos pos, int chance, RandomSource random, int age, Direction direction) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean canCatchFire(Level level, BlockPos pos, Direction direction) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static InteractionResultHolder<ItemStack> fireItemRightClickEvent(Player player, InteractionHand hand) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static ItemStack finishUsingItem(ItemStack item, Level level, LivingEntity livingEntity) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void dispenseContent(DispensibleContainerItem dc, ItemStack stack, BlockHitResult hit, Level level, @Nullable Player player) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static float getGrowthSpeed(BlockState state, ServerLevel level, BlockPos pos) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void releaseUsingItem(ItemStack stack, LivingEntity entity) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void registerFireBehaviors(RegistryAccess registry, IFireItemBehaviorRegistry event) {
        throw new AssertionError();
    }

    // mirrors what NeoForge's patched PistonBaseBlock.moveBlocks calls on every block it destroys,
    // so mods like Quark and Create can react. No-op on Fabric, the hook doesn't exist there
    @PlatformImpl
    public static void onDestroyedByPushReaction(BlockState state, Level level, BlockPos pos,
                                                 Direction pushDir) {
        throw new AssertionError();
    }
}