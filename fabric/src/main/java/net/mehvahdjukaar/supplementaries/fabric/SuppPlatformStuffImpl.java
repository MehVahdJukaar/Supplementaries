package net.mehvahdjukaar.supplementaries.fabric;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.values.BoolConfigValue;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.mixins.fabric.BiomeAccessor;
import net.mehvahdjukaar.supplementaries.mixins.fabric.FireBlockAccessor;
import net.mehvahdjukaar.supplementaries.mixins.fabric.MobBucketItemAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

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
        return itemstack.getItem() == Blocks.CARVED_PUMPKIN.asItem() ||
                EnchantmentHelper.getEnchantments(itemstack)
                        .containsKey(CompatObjects.END_VEIL.get());
    }

    public static int getItemLifeSpawn(ItemEntity itemEntity) {
        return 6000;
    }

    public static void onItemPickup(Player player, ItemEntity itemEntity, ItemStack copy) {
    }

    public static CreativeModeTab.Builder searchBar(CreativeModeTab.Builder c) {
        return c;
    }

    public static float getDownfall(Biome biome) {
        return ((BiomeAccessor) (Object) biome).getClimateSettings().downfall();
    }

    public static void disableAMWarn() {
        ((BoolConfigValue) ClientConfigs.General.NO_AMENDMENTS_WARN).set(true);
        ((FabricConfigSpec) ClientConfigs.CONFIG_HOLDER).saveConfig();
    }

    public static void disableOFWarn(boolean on) {
        ((BoolConfigValue) ClientConfigs.General.NO_OPTIFINE_WARN).set(on);
        ((FabricConfigSpec) ClientConfigs.CONFIG_HOLDER).saveConfig();
    }

    public static boolean canStickTo(BlockState movedState, BlockState maybeSticky) {
        return maybeSticky.getBlock() == Blocks.SLIME_BLOCK || maybeSticky.getBlock() == Blocks.HONEY_BLOCK;
    }

    public static SlotReference getFirstInInventory(LivingEntity entity, Predicate<ItemStack> predicate) {
        ItemStack mainHand = entity.getMainHandItem();
        if (predicate.test(mainHand)) {
            return SlotReference.slot(entity, EquipmentSlot.MAINHAND);
        }
        ItemStack offHand = entity.getOffhandItem();
        if (predicate.test(offHand)) {
            return SlotReference.slot(entity, EquipmentSlot.OFFHAND);
        }

        if (entity instanceof Player player) {
            var inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (predicate.test(s)) {
                    return SlotReference.inv(player, i);
                }
            }
        }
        return SlotReference.EMPTY;
    }

    public static FoodProperties getFoodProperties(ItemStack selected, LivingEntity entity) {
        return selected.getItem().getFoodProperties();
    }

    public static SoundType getSoundType(BlockState blockState, BlockPos pos, Level level, Entity entity) {
        return blockState.getSoundType();
    }

    public static void setParticlePos(BlockParticleOption blockParticleOption, BlockPos pos) {
    }

    @Deprecated(forRemoval = true)
    public static boolean isSlimeball(Item item) {
        return item == Items.SLIME_BALL;
    }


    public static boolean canCatchFire(Level level, BlockPos pos, Direction direction) {
        return ((FireBlockAccessor) Blocks.FIRE).invokeCanBurn(level.getBlockState(pos));
    }

    public static void tryBurningByFire(ServerLevel level, BlockPos pos, int chance, RandomSource random, int age, Direction direction) {
        ((FireBlockAccessor) Blocks.FIRE).invokeCheckBurnOut(level, pos, chance, random, age);
    }

    public static InteractionResultHolder<ItemStack> fireItemUseEvent(Player player, InteractionHand hand) {
        return UseItemCallback.EVENT.invoker().interact(player, player.level(), hand);
    }

    public static void dispenseContent(DispensibleContainerItem dc, ItemStack stack, BlockHitResult hit, Level level, @Nullable Player player) {
        dc.emptyContents(player, level, hit.getBlockPos(), hit);
    }
}
