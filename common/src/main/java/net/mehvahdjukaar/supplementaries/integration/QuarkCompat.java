package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.common.items.JarItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Contract;

public class QuarkCompat {

    @ExpectPlatform
    public static void init(){
    }

    @ExpectPlatform
    public static void tickPiston(Level level, BlockPos pos, AABB aabb, boolean sameDir, BlockEntity pistonBlockEntityMixin) {
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
    public static int getSacksInBackpack(ItemStack backpack) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean isVerticalSlabEnabled() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockEntity getMovingBlockEntity(BlockPos pos, Level level) {
        throw new AssertionError();
    }


    @Contract
    @ExpectPlatform
    public static boolean isJukeboxModuleOn() {
        throw new AssertionError();
    }
}
