package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.item.WoodBasedBlockItem;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BambooSpikesTippedItem extends WoodBasedBlockItem implements SimpleWaterloggedBlock {


    public BambooSpikesTippedItem(Block blockIn, Properties builder) {
        super(blockIn, builder, 150);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltips, tooltipFlag);
        getPotion(stack).addPotionTooltip(tooltips::add, BambooSpikesBlockTile.POTION_MULTIPLIER);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @ForgeOverride
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return getPotion(stack).getColor();
    }

    private static @NotNull PotionContents getPotion(ItemStack stack) {
        return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !CommonConfigs.Functional.ONLY_ALLOW_HARMFUL.get();
    }

    public static boolean isPotionValid(Potion potion) {
        List<MobEffectInstance> effects = potion.getEffects();
        if (CommonConfigs.Functional.ONLY_ALLOW_HARMFUL.get()) {
            for (var e : effects) {
                if (e.getEffect().value().isBeneficial()) return false;
            }
        }
        return !BuiltInRegistries.POTION.wrapAsHolder(potion).is(ModTags.TIPPED_SPIKES_POTION_BLACKLIST);
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @ForgeOverride
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }


    @Override
    public String getDescriptionId(ItemStack stack) {
        return "item.supplementaries.bamboo_spikes_tipped";
        //return PotionUtils.getPotionTypeFromNBT(stack.getChildTag("BlockEntityTag")).getNamePrefixed(super.getTranslationKey() + ".effect.");
    }

    @Override
    public Component getName(ItemStack stack) {
        PotionContents p = getPotion(stack);
        Component arrowName = Component.translatable(p.getName("item.minecraft.tipped_arrow.effect."));
        String s = arrowName.getString();
        if (s.contains("Arrow of ")) {
            return Component.translatable("item.supplementaries.bamboo_spikes_tipped_effect",
                    s.replace("Arrow of ", ""));
        }
        return Component.translatable(this.getDescriptionId(stack));
    }

    @Override
    public ItemStack getDefaultInstance() {
        //replace with item constructor default component
        return makeSpikeItem(Potions.POISON);
    }


    public static ItemStack makeSpikeItem(Potion potion) {
        ItemStack stack = new ItemStack(ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get());
        PotionUtils.setPotion(stack, potion);
        return stack;
    }
}
