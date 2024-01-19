package net.mehvahdjukaar.supplementaries.common.items.additional_placements;

import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class SuppAdditionalPlacement extends AdditionalItemPlacement {

    public SuppAdditionalPlacement(Block placeable) {
        super(placeable);
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (MiscUtils.showsPlaceableHints(pLevel, pIsAdvanced)) {
            pTooltipComponents.add(Component.translatable("message.supplementaries.placeable").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        }
    }
}
