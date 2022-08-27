package net.mehvahdjukaar.supplementaries.forge;

import net.blay09.mods.balm.forge.DeferredRegisters;
import net.mehvahdjukaar.supplementaries.mixins.forge.MobBucketItemAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import vazkii.quark.content.automation.module.JukeboxAutomationModule;
import vazkii.quark.content.tools.module.EndermoshMusicDiscModule;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ForgeHelperImpl {

    public static boolean canEntityDestroy(Level level, BlockPos blockPos, Animal animal) {
        return ForgeHooks.canEntityDestroy(level, blockPos, animal);
    }

    public static boolean onExplosionStart(Level level, Explosion explosion) {
        return ForgeEventFactory.onExplosionStart(level, explosion);
    }

    public static void onExplosionDetonate(Level level, Explosion explosion, List<Entity> entities, double diameter) {
        ForgeEventFactory.onExplosionDetonate(level, explosion, entities, diameter);
    }

    public static void onLivingConvert(LivingEntity skellyHorseMixin, LivingEntity newHorse) {
        ForgeEventFactory.onLivingConvert(newHorse, newHorse);
    }

    public static boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> outcome, Consumer<Integer> timer) {
        return ForgeEventFactory.canLivingConvert(entity, outcome, timer);
    }

    public static double getReachDistance(LivingEntity entity) {
        return entity.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
    }

    public static float getExplosionResistance(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        return state.getExplosionResistance(level, pos, explosion);
    }

    public static void onBlockExploded(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        blockstate.onBlockExploded(level, blockpos, explosion);
    }

    public static boolean areStacksEqual(ItemStack stack, ItemStack other, boolean sameNbt) {
        return stack.equals(other, sameNbt);
    }

    public static boolean isFireSource(BlockState blockState, Level level, BlockPos pos, Direction up) {
        return blockState.isFireSource(level, pos, up);
    }

    public static boolean canDropFromExplosion(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        return blockstate.canDropFromExplosion(level, blockpos, explosion);
    }

    public static boolean isDye(ItemStack itemstack) {
        return itemstack.is(Tags.Items.DYES);
    }

    public static DyeColor getColor(ItemStack stack) {
        return DyeColor.getColor(stack);
    }

    public static BlockState rotateBlock(BlockState state, Level world, BlockPos targetPos, Rotation rot) {
        return state.rotate(world, targetPos, rot);
    }

    public static boolean canHarvestBlock(BlockState state, ServerLevel level, BlockPos pos, ServerPlayer player) {
        return state.canHarvestBlock(level, pos, player);
    }

    public static boolean isMultipartEntity(Entity e) {
        return e.isMultipartEntity();
    }

    public static void setPoolName(LootPool.Builder pool, String name) {
        pool.name(name);
    }

    public static RailShape getRailDirection(BaseRailBlock railBlock, BlockState blockstate, Level level, BlockPos blockpos, AbstractMinecart o) {
        return railBlock.getRailDirection(blockstate, level, blockpos, o);
    }

    public static Optional<ItemStack> getCraftingRemainingItem(ItemStack itemstack) {
        return itemstack.hasCraftingRemainingItem() ? Optional.of(itemstack.getCraftingRemainingItem()) : Optional.empty();
    }

    public static void reviveEntity(Entity entity) {
        entity.revive();
    }

    public static EntityType<?> getFishType(MobBucketItem bucketItem) {
        return ((MobBucketItemAccessor) bucketItem).invokeGetFishType();
    }

    public static boolean isWildFlaxBiome(Holder<Biome> biome) {
        //these are empty now....
        return (biome.is(Tags.Biomes.IS_SANDY) && (biome.is(Tags.Biomes.IS_HOT) || biome.is(Tags.Biomes.IS_DRY)) || biome.is(BiomeTags.IS_RIVER));
    }

    public static boolean onCropsGrowPre(ServerLevel level, BlockPos pos, BlockState state, boolean b) {
        return ForgeHooks.onCropsGrowPre(level , pos, state,b);
    }

    public static void onCropsGrowPost(ServerLevel level, BlockPos pos, BlockState state) {
        ForgeHooks.onCropsGrowPost(level, pos, state);
    }

    @Nullable
    public static InteractionResult onRightClickBlock(Player player, InteractionHand hand, BlockPos below, BlockHitResult rayTraceResult) {
      var ev =  ForgeHooks.onRightClickBlock(player, hand, below, rayTraceResult);
        if(ev.isCanceled())return ev.getCancellationResult();
        return null;
    }

    public static boolean canItemStack(ItemStack selected, ItemStack item) {
        return ItemHandlerHelper.canItemStacksStack(selected,item);
    }

}
