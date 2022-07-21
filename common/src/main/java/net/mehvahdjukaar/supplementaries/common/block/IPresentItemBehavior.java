package net.mehvahdjukaar.supplementaries.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

public interface IPresentItemBehavior {

    default ItemStack trigger(BlockSource pSource, ItemStack pStack) {
        ItemStack itemstack = performSpecialAction(pSource, pStack).orElseGet(() -> this.spitItem(pSource, pStack));
        this.playAnimation(pSource);
        return itemstack;
    }

    /**
     * Actual implementation
     *
     * @return Optional.empty() to fallback to default spit behavior
     */
    Optional<ItemStack> performSpecialAction(BlockSource source, ItemStack stack);

    private ItemStack spitItem(BlockSource pSource, ItemStack pStack) {
        ItemStack itemstack = pStack.split(1);
        spawnItem(pSource.getLevel(), itemstack, 7, pSource);
        return pStack;
    }

    static void spawnItem(Level pLevel, ItemStack pStack, double pSpeed, BlockSource source) {

        var p = getDispensePosition(source);
        ItemEntity itementity = new ItemEntity(pLevel, p.x(), p.y(), p.z(), pStack);

        itementity.setDeltaMovement(pLevel.random.nextGaussian() * 0.0075 * pSpeed,
                pLevel.random.nextGaussian() * 0.0075 * pSpeed + 0.3,
                pLevel.random.nextGaussian() * 0.0075 * pSpeed);
        pLevel.addFreshEntity(itementity);
    }


    default void playAnimation(BlockSource pSource) {
        Level level = pSource.getLevel();
        BlockPos pos = pSource.getPos();
        level.blockEvent(pos, pSource.getBlockState().getBlock(), 0, 0);
    }

    static Position getDispensePosition(BlockSource source) {
        double d0 = source.x();
        double d1 = source.y() + 2 / 16f + 0.0001;
        double d2 = source.z();
        return new PositionImpl(d0, d1, d2);
    }
}
