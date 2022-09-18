package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart.DispenserMinecartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;

public class DispenserMinecartItem extends Item {

    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
        private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

        /**
         * Dispense the specified stack, play the dispense sound and spawn particles.
         */
        @Override
        public ItemStack execute(BlockSource pSource, ItemStack pStack) {
            Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
            Level level = pSource.getLevel();
            double d0 = pSource.x() + (double) direction.getStepX() * 1.125D;
            double d1 = Math.floor(pSource.y()) + (double) direction.getStepY();
            double d2 = pSource.z() + (double) direction.getStepZ() * 1.125D;
            BlockPos blockpos = pSource.getPos().relative(direction);
            BlockState blockstate = level.getBlockState(blockpos);
            RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock railBlock ? ForgeHelper.getRailDirection(railBlock, blockstate, level, blockpos, null) : RailShape.NORTH_SOUTH;
            double d3;
            if (blockstate.is(BlockTags.RAILS)) {
                if (railshape.isAscending()) {
                    d3 = 0.6D;
                } else {
                    d3 = 0.1D;
                }
            } else {
                if (!blockstate.isAir() || !level.getBlockState(blockpos.below()).is(BlockTags.RAILS)) {
                    return this.defaultDispenseItemBehavior.dispense(pSource, pStack);
                }

                BlockState blockstate1 = level.getBlockState(blockpos.below());
                RailShape railshape1 = blockstate1.getBlock() instanceof BaseRailBlock ? blockstate1.getValue(((BaseRailBlock) blockstate1.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
                if (direction != Direction.DOWN && railshape1.isAscending()) {
                    d3 = -0.4D;
                } else {
                    d3 = -0.9D;
                }
            }

            AbstractMinecart abstractminecart = new DispenserMinecartEntity(level, d0, d1 + d3, d2);
            if (pStack.hasCustomHoverName()) {
                abstractminecart.setCustomName(pStack.getHoverName());
            }

            level.addFreshEntity(abstractminecart);
            pStack.shrink(1);
            return pStack;
        }

        /**
         * Play the dispense sound from the specified block.
         */
        protected void playSound(BlockSource p_42947_) {
            p_42947_.getLevel().levelEvent(1000, p_42947_.getPos(), 0);
        }
    };

    public DispenserMinecartItem(Properties pProperties) {
        super(pProperties);
    }

    /**
     * Called when this item is used when targetting a Block
     */
    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (!blockstate.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        } else {
            ItemStack itemstack = pContext.getItemInHand();
            if (!level.isClientSide) {
                RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock railBlock? ForgeHelper.getRailDirection(railBlock,blockstate, level, blockpos, null) : RailShape.NORTH_SOUTH;
                double d0 = 0.0D;
                if (railshape.isAscending()) {
                    d0 = 0.5D;
                }

                AbstractMinecart abstractminecart = new DispenserMinecartEntity(level, (double) blockpos.getX() + 0.5D, (double) blockpos.getY() + 0.0625D + d0, (double) blockpos.getZ() + 0.5D);
                if (itemstack.hasCustomHoverName()) {
                    abstractminecart.setCustomName(itemstack.getHoverName());
                }

                level.addFreshEntity(abstractminecart);
                level.gameEvent(pContext.getPlayer(), GameEvent.ENTITY_PLACE, blockpos);
            }

            itemstack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }
}
