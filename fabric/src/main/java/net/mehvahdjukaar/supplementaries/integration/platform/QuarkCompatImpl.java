package net.mehvahdjukaar.supplementaries.integration.platform;

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

public class QuarkCompatImpl {
    public static void init() {

    }

    public static float getEncumbermentFromBackpack(ItemStack stack) {
        return 0;
    }

    public static boolean isGoldBarsOn() {
        // TODO: Implement for platform
        return false;
    }

    public static void enableFenceGateStuff() {
    }

    public static void disableFenceGateStuff() {
    }

    public static ItemStack makeAdventurerQuill(ServerLevel serverLevel, HolderSet<Structure> targets, int radius, boolean skipKnown, int zoom, ResourceLocation destinationType, String name, int color) {
        return null;
    }

    public static ItemStack makeAdventurerQuill(ServerLevel serverLevel, TagKey<Structure> destination, int radius, boolean skipKnown, int zoom, ResourceLocation destinationType, String name, int color) {
        return null;
    }

    public static boolean tryRotateStool(Level level, BlockState state, BlockPos pos) {
        return false;
    }

    public static boolean isShulkerDropInOn() {
        return false;
    }

    public static ItemStack getSlimeBucket(Entity entity) {
        return ItemStack.EMPTY;
    }

    public static BlockState getMagnetStateForFlintBlock(BlockEntity be, Direction dir) {
        return null;
    }

    public static InteractionResult tryCaptureTater(JarItem item, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public static BlockEntity getMovingBlockEntity(BlockPos pos, BlockState state, Level level) {
        return null;
    }

    public static void tickPiston(Level level, BlockPos pos, BlockState spikes, AABB pistonBB, boolean sameDir, BlockEntity movingTile) {
    }

    public static int getBannerPatternLimit(int current) {
        return 0;
    }

    public static boolean shouldHideOverlay(ItemStack stack) {
        return false;
    }

    public static boolean isDoubleDoorEnabled() {
        return false;
    }

    public static boolean canMoveBlockEntity(BlockState state) {
        return false;
    }

    public static boolean isFastSlideModuleEnabled() {
        return false;
    }

}
