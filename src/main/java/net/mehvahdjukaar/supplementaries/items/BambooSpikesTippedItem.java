package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.item.Item.Properties;

public class BambooSpikesTippedItem extends BurnableBlockItem implements SimpleWaterloggedBlock {


    public BambooSpikesTippedItem(Block blockIn, Properties builder) {
        super(blockIn, builder, 150);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        PotionUtils.addPotionTooltip(stack, tooltip, BambooSpikesBlockTile.POTION_MULTIPLIER);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return PotionUtils.getColor(stack);
        //return MathHelper.rgb(106,81,178);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if(ClientConfigs.block.TIPPED_BAMBOO_SPIKES_TAB.get()) {
            if (this.allowdedIn(group)) {
                items.add(makeSpikeItem(Potions.POISON));
                items.add(makeSpikeItem(Potions.LONG_POISON));
                items.add(makeSpikeItem(Potions.STRONG_POISON));
                for (Potion potion : net.minecraft.core.Registry.POTION) {
                    if (potion == Potions.POISON || potion == Potions.LONG_POISON || potion == Potions.STRONG_POISON)
                        continue;
                    if (!potion.getEffects().isEmpty() && potion != Potions.EMPTY) {
                        items.add(makeSpikeItem(potion));
                    }
                }
            }
        }
    }

    public static ItemStack makeSpikeItem(Potion potion) {
        ItemStack stack = new ItemStack(ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get());
        PotionUtils.setPotion(stack, potion);
        return stack;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return "item.supplementaries.bamboo_spikes_tipped";
        //return PotionUtils.getPotionTypeFromNBT(stack.getChildTag("BlockEntityTag")).getNamePrefixed(super.getTranslationKey() + ".effect.");
    }

    @Override
    public Component getName(ItemStack stack) {
        Potion p = PotionUtils.getPotion(stack);
        BaseComponent arrowName = new TranslatableComponent(p.getName("item.minecraft.tipped_arrow.effect."));
        String s = arrowName.getString();
        if (s.contains("Arrow of ")) {
            return new TranslatableComponent("item.supplementaries.bamboo_spikes_tipped_effect",
                    s.replace("Arrow of ", ""));
        }
        return new TranslatableComponent(this.getDescriptionId(stack));
        //String effectName = new TranslationTextComponent(p.getNamePrefixed("effect.minecraft.")).getString();
        //return new TranslationTextComponent("item.supplementaries.bamboo_spikes_tipped_effect",effectName);

        //return new TranslationTextComponent("item.supplementaries.bamboo_spikes_tipped_effect",
        //        TextUtil.format(PotionUtils.getPotionTypeFromNBT(stack.getChildTag("BlockEntityTag")).getNamePrefixed("")));

    }
}
