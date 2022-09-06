package net.mehvahdjukaar.supplementaries.common.items.additional_behaviors;

import net.mehvahdjukaar.supplementaries.api.IAdditionalPlacement;
import net.mehvahdjukaar.supplementaries.common.items.BlockPlacerItem;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class WallLanternPlacement implements IAdditionalPlacement {

    @Override
    public BlockState overrideGetPlacementState(BlockPlaceContext pContext) {
        if (CompatHandler.torchslab) {
            double y = pContext.getClickLocation().y() % 1;
            if (y < 0.5) return null;
        }
        BlockState state = ModRegistry.WALL_LANTERN.get().getStateForPlacement(pContext);
        return (state != null && this.getMimic().canPlace(pContext,state)) ? state : null;
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (ClientConfigs.cached.PLACEABLE_TOOLTIPS) {
            pTooltipComponents.add(new TranslatableComponent("message.supplementaries.wall_lantern").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        }
    }

    BlockPlacerItem getMimic() {
        return ModRegistry.BLOCK_PLACER.get();
    }
}
