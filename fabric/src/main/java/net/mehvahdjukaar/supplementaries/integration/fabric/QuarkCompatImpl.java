package net.mehvahdjukaar.supplementaries.integration.fabric;

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

public class QuarkCompatImpl {
    public static void tickPiston(Level level, BlockPos pos, AABB aabb, boolean sameDir, BlockEntity pistonBlockEntityMixin) {
    }

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

    public static int getSacksInBackpack(ItemStack backpack) {
        return 0;
    }

    public static boolean isVerticalSlabEnabled() {
        return true;
    }

    public static BlockEntity getMovingBlockEntity(BlockPos pos, Level level) {
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
}
