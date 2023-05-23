package net.mehvahdjukaar.supplementaries;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


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

    @ExpectPlatform
    public static BlockState getUnoxidised(Level level, BlockPos pos, BlockState state) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isEndermanMask(EnderMan enderMan, Player player, ItemStack itemstack) {
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
    public static int getLightEmission(BlockState state, Level level, BlockPos pos) {
        throw new ArrayStoreException();
    }

    @ExpectPlatform
    public static CreativeModeTab.Builder searchBar(CreativeModeTab.Builder c) {
        throw new ArrayStoreException();
    }

    @ExpectPlatform
    public static float getDownfall(Biome biome){
        throw new AssertionError();
    }
}