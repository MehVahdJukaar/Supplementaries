package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class BambooSpikesTippedItem extends BlockItem implements SimpleWaterloggedBlock {


    public BambooSpikesTippedItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltips, tooltipFlag);
        getPotion(stack).addPotionTooltip(tooltips::add, BambooSpikesBlockTile.POTION_MULTIPLIER, context.tickRate());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return getPotion(stack).getColor();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !CommonConfigs.Functional.ONLY_ALLOW_HARMFUL_INFINITE.get();
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        PotionContents p = getPotion(stack);
        return Potion.getName(p.potion(),
                "item.supplementaries.bamboo_spikes_tipped.effect.");
    }

    public static boolean isPotionValid(PotionContents potion) {
        if (!potion.hasEffects()) return false;
        Boolean alternativeMode = CommonConfigs.Functional.ONLY_ALLOW_HARMFUL_INFINITE.get();
        if (alternativeMode) {
            for (var e : potion.getAllEffects()) {
                if (e.getEffect().value().isBeneficial()) return false;
            }
        }
        Optional<Holder<Potion>> holder = potion.potion();
        return holder.isEmpty() || !holder.get().is(alternativeMode ?
                ModTags.TIPPED_SPIKES_POTION_BLACKLIST : ModTags.TIPPED_SPIKES_FINITE_POTION_BLACKLIST);
    }

    public static @NotNull PotionContents getPotion(ItemStack stack) {
        return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    }

    public static @NotNull PotionContents getPotion(SoftFluidStack stack) {
        return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    }
}
