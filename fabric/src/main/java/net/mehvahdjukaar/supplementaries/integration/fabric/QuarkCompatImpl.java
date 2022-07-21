package net.mehvahdjukaar.supplementaries.integration.fabric;

import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
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

public class QuarkCompatImpl {
    public static void tickPiston(Level level, BlockPos pos, AABB aabb, boolean sameDir, BlockEntity pistonBlockEntityMixin) {
    }

    public static void onItemTooltipEvent(ItemStack itemStack, TooltipFlag tooltipFlag, List<Component> components) {
    }

    public static BlockState updateWoodPostShape(BlockState oldHeld, Direction facing, BlockState facingState) {
        throw new UnsupportedOperationException();
    }

    public static InteractionResult tryCaptureTater(JarItem jarItem, UseOnContext context) {
        throw new UnsupportedOperationException();
    }

    public static boolean isDoubleDoorEnabled() {
        throw new UnsupportedOperationException();
    }

    public static boolean canMoveBlockEntity(BlockState state) {
        throw new UnsupportedOperationException();
    }

    public static int getSacksInBackpack(ItemStack backpack) {
        throw new UnsupportedOperationException();
    }

    public static boolean isVerticalSlabEnabled() {
        throw new UnsupportedOperationException();
    }

    public static boolean canRenderBlackboardTooltip() {
        throw new UnsupportedOperationException();
    }

    public static boolean canRenderQuarkTooltip() {
        throw new UnsupportedOperationException();
    }

    public static boolean shouldHaveButtonOnRight() {
        throw new UnsupportedOperationException();
    }

    public static BlockEntity getMovingBlockEntity(BlockPos pos, Level level) {
        throw new UnsupportedOperationException();
    }

    public static void registerTooltipComponent(ClientPlatformHelper.TooltipComponentEvent event) {
    }
}
