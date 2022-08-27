package net.mehvahdjukaar.supplementaries.fabric;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.mixins.fabric.MobBucketItemAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ForgeHelperImpl {

    public static boolean canEntityDestroy(Level level, BlockPos pos, Animal animal) {
        if (!level.isLoaded(pos)) {
            return false;
        } else {
            return PlatformHelper.isMobGriefingOn(level, animal);
        }
    }

    public static boolean onExplosionStart(Level level, Explosion explosion) {
        return false; //true if event cancelled
    }

    public static void onExplosionDetonate(Level level, Explosion explosion, List<Entity> entities, double diameter) {
    }

    public static void onLivingConvert(LivingEntity skellyHorseMixin, LivingEntity newHorse) {
    }

    public static boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> outcome, Consumer<Integer> timer) {
        return true;
    }

    public static double getReachDistance(LivingEntity entity) {
        return 4.5;
    }

    public static float getExplosionResistance(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        return state.getBlock().getExplosionResistance();
    }

    public static void onBlockExploded(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
        blockstate.getBlock().wasExploded(level, blockpos, explosion);
    }

    public static boolean areStacksEqual(ItemStack stack, ItemStack other, boolean sameNbt) {
        return stack.equals(other);
    }

    public static boolean isFireSource(BlockState blockState, Level level, BlockPos pos, Direction up) {
        return blockState.is(level.dimensionType().infiniburn());
    }

    public static boolean canDropFromExplosion(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        return blockstate.getBlock().dropFromExplosion(explosion);
    }

    public static boolean isDye(ItemStack itemstack) {
        return itemstack.getItem() instanceof DyeItem;
    }

    public static DyeColor getColor(ItemStack stack) {
        if (stack.getItem() instanceof DyeItem dyeItem) {
            return dyeItem.getDyeColor();
        }
        return null;
    }

    public static BlockState rotateBlock(BlockState state, Level world, BlockPos targetPos, Rotation rot) {
        return state.rotate(rot);
    }


    public static boolean isMultipartEntity(Entity e) {
        return e instanceof EnderDragon;
    }

    public static void setPoolName(LootPool.Builder pool, String name) {
    }

    public static RailShape getRailDirection(BaseRailBlock railBlock, BlockState blockstate, Level level, BlockPos blockpos, AbstractMinecart o) {
        return blockstate.getValue(railBlock.getShapeProperty());
    }

    public static Optional<ItemStack> getCraftingRemainingItem(ItemStack itemstack) {
        return Optional.ofNullable(itemstack.getItem().getCraftingRemainingItem()).map(Item::getDefaultInstance);
    }

    public static void reviveEntity(Entity entity) {
    }

    public static EntityType<?> getFishType(MobBucketItem bucketItem) {
        return ((MobBucketItemAccessor) bucketItem).getType();
    }

    public static boolean onCropsGrowPre(ServerLevel level, BlockPos pos, BlockState state, boolean b) {
        return b;
    }

    public static void onCropsGrowPost(ServerLevel level, BlockPos pos, BlockState state) {
    }

    @javax.annotation.Nullable
    public static InteractionResult onRightClickBlock(Player player, InteractionHand hand, BlockPos below, BlockHitResult rayTraceResult) {
        return null;
    }

    public static boolean canItemStack(ItemStack selected, ItemStack item) {
        return ItemStack.isSameIgnoreDurability(selected, item);
    }


}
