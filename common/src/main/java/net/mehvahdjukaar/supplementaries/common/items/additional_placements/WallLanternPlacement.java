package net.mehvahdjukaar.supplementaries.common.items.additional_placements;

import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class WallLanternPlacement extends AdditionalItemPlacement {

    public WallLanternPlacement(Block placeable) {
        super(placeable);
    }

    @Override
    public BlockState overrideGetPlacementState(BlockPlaceContext pContext) {
        if (CompatHandler.TORCHSLAB) {
            double y = pContext.getClickLocation().y() % 1;
            if (y < 0.5) return null;
        }
        BlockState state = ModRegistry.WALL_LANTERN.get().getStateForPlacement(pContext);
        return (state != null && getBlockPlacer().canPlace(pContext,state)) ? state : null;
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (MiscUtils.showsPlaceableHints(pLevel, pIsAdvanced)) {
            pTooltipComponents.add(Component.translatable("message.supplementaries.wall_lantern").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        }
    }
}
