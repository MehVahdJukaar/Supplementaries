package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.common.items.JarItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Contract;

public class QuarkCompat {

    @ExpectPlatform
    public static void init() {
    }

    @ExpectPlatform
    public static int getBannerPatternLimit(int current) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void tickPiston(Level level, BlockPos pos, BlockState state, AABB aabb, boolean sameDir, BlockEntity tile) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockState updateWoodPostShape(BlockState oldHeld, Direction facing, BlockState facingState) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isFastSlideModuleEnabled() {
        throw new AssertionError();
    }


    @ExpectPlatform
    public static InteractionResult tryCaptureTater(JarItem jarItem, UseOnContext context) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isDoubleDoorEnabled() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canMoveBlockEntity(BlockState state) {
        //use quark logic if installed
        throw new AssertionError();
    }

    @ExpectPlatform
    public static float getEncumbermentFromBackpack(ItemStack backpack) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean isVerticalSlabEnabled() {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Contract
    public static boolean shouldHideOverlay(ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockEntity getMovingBlockEntity(BlockPos pos, BlockState state, Level level) {
        throw new AssertionError();
    }


    @Contract
    @ExpectPlatform
    public static boolean isJukeboxModuleOn() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockState getMagnetStateForFlintBlock(BlockEntity be, Direction dir) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ItemStack getSlimeBucket(Entity entity) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean isShulkerDropInOn() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean tryRotateStool(Level level, BlockState state, BlockPos pos) {
        throw new AssertionError();
    }
}
