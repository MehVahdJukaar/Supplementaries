package net.mehvahdjukaar.supplementaries;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

//todo: move to lib
public class ForgeHelper {

    @Contract
    @ExpectPlatform
    public static boolean canEntityDestroy(Level level, BlockPos blockPos, Animal animal) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean onExplosionStart(Level level, Explosion explosion) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onLivingConvert(LivingEntity frFom, LivingEntity to) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> outcome, Consumer<Integer> timer) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onExplosionDetonate(Level level, Explosion explosion, List<Entity> entities, double diameter) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static double getReachDistance(LivingEntity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static float getExplosionResistance(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onBlockExploded(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean areStacksEqual(ItemStack stack, ItemStack other, boolean sameNbt) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isFireSource(BlockState blockState, Level level, BlockPos pos, Direction up) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canDropFromExplosion(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isDye(ItemStack itemstack) {
        throw new AssertionError();
    }

    @Nullable
    @ExpectPlatform
    public static DyeColor getColor(ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static EntityType<?> getFishType(MobBucketItem bucketItem) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockState rotateBlock(BlockState state, Level world, BlockPos targetPos, Rotation rot) {
        throw new AssertionError();
    }


    @ExpectPlatform
    public static boolean isMultipartEntity(Entity e) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setPoolName(LootPool.Builder pool, String name) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static RailShape getRailDirection(BaseRailBlock railBlock, BlockState blockstate, Level level, BlockPos blockpos, @Nullable AbstractMinecart o) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Optional<ItemStack> getCraftingRemainingItem(ItemStack itemstack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void reviveEntity(Entity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean onCropsGrowPre(ServerLevel level, BlockPos pos, BlockState state, boolean b) {
        throw new AssertionError();

    }

    @ExpectPlatform
    public static void onCropsGrowPost(ServerLevel level, BlockPos pos, BlockState state) {
    }

    @ExpectPlatform
    @Nullable
    public static InteractionResult onRightClickBlock(Player player, InteractionHand hand, BlockPos below, BlockHitResult rayTraceResult) {
        throw new AssertionError();
    }
}
