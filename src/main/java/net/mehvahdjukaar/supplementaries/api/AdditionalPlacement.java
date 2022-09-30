package net.mehvahdjukaar.supplementaries.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * something called by mixin which allows performing extra action when an item is used
 */
public interface AdditionalPlacement {

    @Nullable
    default BlockState overrideGetPlacementState(BlockPlaceContext pContext) {
        return null;
    }

    default InteractionResult overrideUseOn(UseOnContext pContext, FoodProperties foodProperties) {
        return InteractionResult.PASS;
    }

    default InteractionResult overridePlace(BlockPlaceContext pContext) {
        return InteractionResult.PASS;
    }

    default void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
    }

    @Nullable
    default BlockPlaceContext overrideUpdatePlacementContext(BlockPlaceContext pContext) {
        return null;
    }

}
