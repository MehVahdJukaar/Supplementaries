package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.common.items.JarItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

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

    public static ItemStack makeAdventurerQuill(ServerLevel serverLevel, @Nullable TagKey<Structure> destination,
                                                int radius, boolean skipKnown, int zoom,
                                                MapDecoration.Type destinationType, @Nullable String name, int color) {
        HolderSet<Structure> targets = null;
        if (destination != null) {
            var v = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(destination);
            if (v.isEmpty()) {
                return ItemStack.EMPTY;
            } else targets = v.get();
        }
        return makeAdventurerQuill(serverLevel, targets, radius, skipKnown, zoom, destinationType, name, color);
    }

    @ExpectPlatform
    public static ItemStack makeAdventurerQuill(ServerLevel serverLevel, @Nullable HolderSet<Structure> targets,
                                                int radius, boolean skipKnown, int zoom,
                                                MapDecoration.Type destinationType, @Nullable String name, int color) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void addItemsToTabs(RegHelper.ItemToTabEvent event) {
    }
}