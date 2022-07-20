package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.common.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.common.items.JarItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class QuarkCompat {

    @ExpectPlatform
    public static void tickPiston(Level level, BlockPos pos, AABB aabb, boolean sameDir, IBlockHolder pistonBlockEntityMixin) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onItemTooltipEvent(ItemStack itemStack, TooltipFlag tooltipFlag, List<Component> components) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockState updateWoodPostShape(BlockState oldHeld, Direction facing, BlockState facingState) {
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
    public static BlockEntity getMovingBlockEntity(BlockPos pos, Level level) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canMoveBlockEntity(BlockState state) {
        //use quark logic if installed
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getSacksInBackpack(ItemStack backpack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isVerticalSlabEnabled() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canRenderBlackboardTooltip() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canRenderQuarkTooltip() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean shouldHaveButtonOnRight() {
        throw new AssertionError();
    }
}
