package net.mehvahdjukaar.supplementaries.integration.fabric;

import net.mehvahdjukaar.supplementaries.common.items.JarItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
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
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

public class QuarkCompatImpl {

    public static BlockState updateWoodPostShape(BlockState oldHeld, Direction facing, BlockState facingState) {
        return oldHeld;
    }

    public static InteractionResult tryCaptureTater(JarItem jarItem, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public static boolean isDoubleDoorEnabled() {
        return false;
    }

    public static boolean canMoveBlockEntity(BlockState state) {
        return false;
    }

    public static boolean isVerticalSlabEnabled() {
        return true;
    }

    public static BlockEntity getMovingBlockEntity(BlockPos pos, BlockState state, Level level) {
        return null;
    }

    public static boolean isJukeboxModuleOn() {
        return false;
    }

    public static void init() {
    }

    public static boolean isFastSlideModuleEnabled() {
        return false;
    }

    public static BlockState getMagnetStateForFlintBlock(BlockEntity be, Direction dir) {
        return null;
    }

    public static ItemStack getSlimeBucket(Entity entity) {
        return ItemStack.EMPTY;
    }

    public static float getEncumbermentFromBackpack(ItemStack backpack) {
        return 0;
    }

    public static boolean isShulkerDropInOn() {
        return true;
    }

    @org.jetbrains.annotations.Contract
    public static boolean shouldHideOverlay(ItemStack stack) {
        return false;
    }

    public static void tickPiston(Level level, BlockPos pos, BlockState state, AABB aabb, boolean sameDir, BlockEntity tile) {
    }

    public static int getBannerPatternLimit(int current) {
        return current;
    }

    public static boolean tryRotateStool(Level level, BlockState state, BlockPos pos) {
        return false;
    }

    public static ItemStack makeAdventurerQuill(ServerLevel serverLevel, HolderSet<Structure> destination, int radius, boolean skipKnown, int zoom, MapDecoration.Type destinationType, @Nullable String name, int color) {
        return ItemStack.EMPTY;
    }


}
