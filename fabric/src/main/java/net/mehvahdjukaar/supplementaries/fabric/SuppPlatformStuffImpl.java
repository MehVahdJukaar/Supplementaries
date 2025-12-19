package net.mehvahdjukaar.supplementaries.fabric;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.values.BoolConfigValue;
import net.mehvahdjukaar.supplementaries.api.IFireItemBehaviorProvider;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehaviorRegistry;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.FlanCompat;
import net.mehvahdjukaar.supplementaries.mixins.fabric.BiomeAccessor;
import net.mehvahdjukaar.supplementaries.mixins.fabric.FireBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class SuppPlatformStuffImpl {

    @Nullable
    public static <T> T getForgeCap(Entity object, Class<T> capClass) {
        return null;
    }

    @Nullable
    public static <T> T getForgeCap(Level level, BlockPos pos, Class<T> capClass) {
        return null;
    }

    @Nullable
    public static <T> T getForgeCap(BlockEntity object, Class<T> capClass) {
        return null;
    }

    public static BlockState getUnoxidised(Level level, BlockPos pos, BlockState state) {
        return null;
    }

    public static boolean isEndermanMask(EnderMan enderman, Player player, ItemStack itemstack) {
        if (itemstack.is(Items.CARVED_PUMPKIN)) return true;
        if (CompatObjects.END_VEIL.isPresent(player.level())) {
            var ench = itemstack.get(DataComponents.ENCHANTMENTS);
            return ench != null && ench.getLevel(CompatObjects.END_VEIL.getHolder(player)) > 0;
        }
        return false;
    }

    public static int getItemLifeSpawn(ItemEntity itemEntity) {
        return 6000;
    }

    public static void fireItemPickupPost(Player player, ItemEntity itemEntity, ItemStack copy) {
    }

    public static CreativeModeTab.Builder searchBar(CreativeModeTab.Builder c) {
        return c;
    }

    public static float getDownfall(Biome biome) {
        return ((BiomeAccessor) (Object) biome).getClimateSettings().downfall();
    }

    public static void disableAMWarn() {
        ((BoolConfigValue) ClientConfigs.General.NO_AMENDMENTS_WARN).set(true);
        ((FabricConfigHolder) ClientConfigs.CONFIG_HOLDER).saveConfig();
    }

    public static void disableOFWarn(boolean on) {
        ((BoolConfigValue) ClientConfigs.General.NO_OPTIFINE_WARN).set(on);
        ((FabricConfigHolder) ClientConfigs.CONFIG_HOLDER).saveConfig();
    }

    public static void disableIMWarn() {
        ((BoolConfigValue) ClientConfigs.General.NO_INCOMPATIBLE_MODS).set(true);
        ((FabricConfigHolder) ClientConfigs.CONFIG_HOLDER).saveConfig();
    }

    public static boolean canStickTo(BlockState movedState, BlockState maybeSticky) {
        return maybeSticky.getBlock() == Blocks.SLIME_BLOCK || maybeSticky.getBlock() == Blocks.HONEY_BLOCK;
    }

    public static SlotReference getFirstInInventory(LivingEntity entity, Predicate<ItemStack> predicate) {
        ItemStack mainHand = entity.getMainHandItem();
        if (predicate.test(mainHand)) {
            return SlotReference.slot(EquipmentSlot.MAINHAND);
        }
        ItemStack offHand = entity.getOffhandItem();
        if (predicate.test(offHand)) {
            return SlotReference.slot(EquipmentSlot.OFFHAND);
        }

        if (entity instanceof Player player) {
            var inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (predicate.test(s)) {
                    return SlotReference.inv(i);
                }
            }
        }
        return SlotReference.EMPTY;
    }

    public static FoodProperties getFoodProperties(ItemStack selected, LivingEntity entity) {
        return selected.get(DataComponents.FOOD);
    }

    public static SoundType getSoundType(BlockState blockState, BlockPos pos, Level level, Entity entity) {
        return blockState.getSoundType();
    }

    public static void setParticlePos(BlockParticleOption blockParticleOption, BlockPos pos) {
    }

    public static boolean canCatchFire(Level level, BlockPos pos, Direction direction) {
        return ((FireBlockAccessor) Blocks.FIRE).invokeCanBurn(level.getBlockState(pos));
    }

    public static void tryBurningByFire(ServerLevel level, BlockPos pos, int chance, RandomSource random, int age, Direction direction) {
        ((FireBlockAccessor) Blocks.FIRE).invokeCheckBurnOut(level, pos, chance, random, age);
    }

    public static InteractionResultHolder<ItemStack> fireItemRightClickEvent(Player player, InteractionHand hand) {
        return UseItemCallback.EVENT.invoker().interact(player, player.level(), hand);
    }

    public static void dispenseContent(DispensibleContainerItem dc, ItemStack stack, BlockHitResult hit, Level level, @Nullable Player player) {
        dc.emptyContents(player, level, hit.getBlockPos(), hit);
    }

    public static float getGrowthSpeed(BlockState state, ServerLevel level, BlockPos pos) {
        return CropAccessor.callGetGrowthSpeed(state, level, pos);
    }


    private static abstract class CropAccessor extends CropBlock {
        public CropAccessor(Properties properties) {
            super(properties);
        }

        public static float callGetGrowthSpeed(BlockState state, ServerLevel level, BlockPos pos) {
            return getGrowthSpeed(state.getBlock(), level, pos);
        }
    }

    public static void releaseUsingItem(ItemStack stack, LivingEntity entity) {
        stack.releaseUsing(entity.level(), entity, entity.getUseItemRemainingTicks());
        if (stack.useOnRelease()) {
            // entity.updatingUsingItem();
        }

        //entity.stopUsingItem();
    }

    public static ItemStack finishUsingItem(ItemStack item, Level level, LivingEntity entity) {
        //no event here because of fabric
        return item.finishUsingItem(level, entity);
    }

    public static InteractionResult placeBlockItem(BlockItem bi, BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        BlockPos blockPos = context.getClickedPos();

        //not needed on forge as forge has so many events for this shit
        if (player != null && CompatHandler.FLAN && FlanCompat.canPlace(player, blockPos)) {
            return InteractionResult.PASS;
        }

        if (player != null && !player.getAbilities().mayBuild && !stack.canPlaceOnBlockInAdventureMode(
                new BlockInWorld(context.getLevel(), blockPos, false))) {
            return InteractionResult.PASS;
        } else {
            Item item = stack.getItem();
            //instead o use on so no events are fired
            InteractionResult interactionResult = bi.place(new BlockPlaceContext(context));
            if (player != null && interactionResult.indicateItemUse()) {
                player.awardStat(Stats.ITEM_USED.get(item));
            }

            return interactionResult;
        }
    }

    public static void registerFireBehaviours(RegistryAccess registry, IFireItemBehaviorRegistry event) {
        FabricLoader.getInstance().invokeEntrypoints("supplementaries:register_fire_behaviours", IFireItemBehaviorProvider.class, provider -> {
            provider.register(registry, event);
        });
    }
}
