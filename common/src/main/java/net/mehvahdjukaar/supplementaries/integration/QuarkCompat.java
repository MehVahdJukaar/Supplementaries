package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.supplementaries.common.items.JarItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class QuarkCompat {

    @PlatformImpl
    public static void init() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean isFastSlideModuleEnabled() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean isDoubleDoorEnabled() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean canMoveBlockEntity(BlockState state) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static float getEncumbermentFromBackpack(ItemStack stack) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean shouldHideOverlay(ItemStack stack) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static int getBannerPatternLimit(int current) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void tickPiston(Level level, BlockPos pos, BlockState spikes, AABB pistonBB, boolean sameDir, BlockEntity movingTile) {
        throw new AssertionError();
    }

    @Nullable
    @PlatformImpl
    public static BlockEntity getMovingBlockEntity(BlockPos pos, BlockState state, Level level) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static InteractionResult tryCaptureTater(JarItem item, UseOnContext context) {
        throw new AssertionError();
    }

    @Nullable
    @PlatformImpl
    public static BlockState getMagnetStateForFlintBlock(BlockEntity be, Direction dir) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static ItemStack getSlimeBucket(Entity entity) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean isShulkerDropInOn() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean tryRotateStool(Level level, BlockState state, BlockPos pos) {
        throw new AssertionError();
    }

    @Nullable
    @PlatformImpl
    public static ItemStack makeAdventurerQuill(ServerLevel serverLevel, @Nullable TagKey<Structure> destination,
                                                int radius, boolean skipKnown, int zoom,
                                                ResourceLocation destinationType, @Nullable String name, int color) {
        throw new AssertionError();
    }

    @Nullable
    @PlatformImpl
    public static ItemStack makeAdventurerQuill(ServerLevel serverLevel, HolderSet<Structure> targets,
                                                int radius, boolean skipKnown, int zoom,
                                                ResourceLocation destinationType, @Nullable String name, int color) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void disableFenceGateStuff() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void enableFenceGateStuff() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean isGoldBarsOn() {
        throw new AssertionError();
    }
}