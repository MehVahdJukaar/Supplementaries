package net.mehvahdjukaar.supplementaries.neoforge;

import io.netty.buffer.ByteBuf;
import net.mehvahdjukaar.moonlight.api.platform.configs.neoforge.ForgeConfigHolder;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.util.FakePlayerManager;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.mixins.neoforge.FireBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class SuppPlatformStuffImpl {

    /**
     * Does not check if its instance of ICapabilityProvider
     * Be sure to provide it with one, or it will fail
     */
    @Nullable
    public static <T> T getForgeCap(Entity entity, Class<T> capClass) {
        var t = CapabilityHandler.getToken(capClass);
        if (t != null) {
            return entity.getCapability((net.neoforged.neoforge.capabilities.EntityCapability<T, Void>) t);
        }
        return null;
    }

    @Nullable
    public static <T> T getForgeCap(Level level, BlockPos pos, Class<T> capClass) {
        var t = CapabilityHandler.getToken(capClass);
        if (t != null) {
            return level.getCapability((net.neoforged.neoforge.capabilities.BlockCapability<T, Void>) t, pos);
        }
        return null;
    }

    @Nullable
    public static <T> T getForgeCap(BlockEntity object, Class<T> capClass) {
        var t = CapabilityHandler.getToken(capClass);
        if (t != null) {
            return object.getLevel().getCapability((net.neoforged.neoforge.capabilities.BlockCapability<T, Void>) t,
                    object.getBlockPos(), object.getBlockState(), object);
        }
        return null;
    }

    @Nullable
    public static BlockState getUnoxidised(Level level, BlockPos pos, BlockState state) {
        Player fp = FakePlayerManager.getDefault(level);
        fp.setItemInHand(InteractionHand.MAIN_HAND, Items.IRON_AXE.getDefaultInstance());
        Block b = state.getBlock();
        var context = new UseOnContext(fp, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));

        var modified = state;
        modified = b.getToolModifiedState(modified, context, ItemAbilities.AXE_WAX_OFF, false);
        if (modified == null) modified = state;
        while (true) {
            var newMod = b.getToolModifiedState(modified, context, ItemAbilities.AXE_SCRAPE, false);

            if (newMod == null || newMod == modified) break;
            else modified = newMod;
        }
        if (modified == state) return null;
        return modified;
    }

    public static boolean isEndermanMask(@NotNull EnderMan enderMan, Player player, ItemStack itemstack) {
        try {
            if (itemstack.isEnderMask(player, enderMan)) return true;

            if (CompatObjects.END_VEIL.isPresent()) {
                var ench = itemstack.get(DataComponents.ENCHANTMENTS);
                return ench != null && ench.getLevel(CompatObjects.END_VEIL) > 0;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static int getItemLifeSpawn(ItemEntity itemEntity) {
        return itemEntity.lifespan;
    }

    public static void fireItemPickupPost(Player player, ItemEntity itemEntity, ItemStack copy) {
        EventHooks.fireItemPickupPost(itemEntity, player, copy);
    }

    public static CreativeModeTab.Builder searchBar(CreativeModeTab.Builder c) {
        return c.withSearchBar();
    }

    public static float getDownfall(Biome biome) {
        return biome.getModifiedClimateSettings().downfall();
    }

    public static void disableAMWarn() {
        ((ModConfigSpec.BooleanValue) ClientConfigs.General.NO_AMENDMENTS_WARN).set(true);
        ForgeConfigHolder fg = ((ForgeConfigHolder) ClientConfigs.CONFIG_HOLDER);
        fg.getSpec().save();

    }

    public static void disableIMWarn() {
        ((ModConfigSpec.BooleanValue) ClientConfigs.General.NO_INCOMPATIBLE_MODS).set(true);
        ForgeConfigHolder fg = ((ForgeConfigHolder) ClientConfigs.CONFIG_HOLDER);
        fg.getSpec().save();
    }

    public static void disableOFWarn(boolean on) {
        ((ModConfigSpec.BooleanValue) ClientConfigs.General.NO_OPTIFINE_WARN).set(on);
        ForgeConfigHolder fg = ((ForgeConfigHolder) ClientConfigs.CONFIG_HOLDER);
        fg.getSpec().save();
    }

    public static boolean canStickTo(BlockState movedState, BlockState blockState) {
        return movedState.canStickTo(blockState);
    }

    public static SlotReference getFirstInInventory(LivingEntity entity, Predicate<ItemStack> predicate) {
        var cap = entity.getCapability(Capabilities.ItemHandler.ENTITY);
        if (cap != null) {
            for (int i = 0; i < cap.getSlots(); i++) {
                ItemStack itemInSlot = cap.getStackInSlot(i);
                if (predicate.test(itemInSlot)) {
                    return new CapSlotReference(i);
                }
            }
        }
        return SlotReference.EMPTY;
    }

    public static FoodProperties getFoodProperties(ItemStack selected, LivingEntity entity) {
        return selected.getFoodProperties(entity);
    }

    public static SoundType getSoundType(BlockState blockState, BlockPos pos, Level level, Entity entity) {
        return blockState.getSoundType(level, pos, entity);
    }

    public static void setParticlePos(BlockParticleOption blockParticleOption, BlockPos pos) {
        blockParticleOption.setPos(pos);
    }

    public static boolean canCatchFire(Level level, BlockPos below, Direction direction) {
        return ((FireBlock) Blocks.FIRE).canCatchFire(level, below, direction);
    }

    public static void tryBurningByFire(ServerLevel level, BlockPos pos, int chance, RandomSource random, int age, Direction direction) {
        ((FireBlockAccessor) Blocks.FIRE).invokeTryCatchFire(level, pos, chance, random, age, direction);
    }


    public static InteractionResultHolder<ItemStack> fireItemUseEvent(Player player, InteractionHand hand) {
        var r = CommonHooks.onItemRightClick(player, hand);
        if (r == null) r = InteractionResult.PASS;
        return new InteractionResultHolder<>(r, player.getItemInHand(hand));
    }

    public static void dispenseContent(DispensibleContainerItem dc, ItemStack stack, BlockHitResult hit, Level level, @Nullable Player player) {
        dc.emptyContents(player, level, hit.getBlockPos(), hit, stack);
    }

    public record CapSlotReference(int slot) implements SlotReference {

        public static final StreamCodec<ByteBuf, CapSlotReference> CODEC = ByteBufCodecs.VAR_INT
                .map(CapSlotReference::new, CapSlotReference::slot);


        @Override
        public ItemStack get(LivingEntity player) {
            var cap = player.getCapability(Capabilities.ItemHandler.ENTITY);
            if (cap != null) {
                return cap.getStackInSlot(slot);
            }
            return null;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ? extends SlotReference> getCodec() {
            return CODEC;
        }
    }

    static {
        SlotReference.REGISTRY.register("cap_slot", CapSlotReference.CODEC);
    }

    public static float getGrowthSpeed(BlockState state, ServerLevel level, BlockPos pos) {
        return CropAccessor.callGetGrowthSpeed(state, level, pos);
    }


    private static class CropAccessor {
        public static float callGetGrowthSpeed(BlockState state, ServerLevel level, BlockPos pos) {
            return getGrowthSpeed(state, level, pos);
        }
    }
}
