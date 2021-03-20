package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BambooSpikesTippedItem extends BlockItem implements IWaterLoggable {


    public BambooSpikesTippedItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public int getBurnTime(ItemStack itemStack) {return 150;}

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        PotionUtils.addPotionTooltip(stack,tooltip,BambooSpikesBlockTile.POTION_MULTIPLIER);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return PotionUtils.getColor(stack);
        //return MathHelper.rgb(106,81,178);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemEnchantability() {
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
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            items.add(makeSpikeItem(Potions.POISON));
            items.add(makeSpikeItem(Potions.LONG_POISON));
            items.add(makeSpikeItem(Potions.STRONG_POISON));
            for(Potion potion : net.minecraft.util.registry.Registry.POTION) {
                if(potion==Potions.POISON || potion==Potions.LONG_POISON || potion==Potions.STRONG_POISON)continue;
                if (!potion.getEffects().isEmpty()&&potion!=Potions.EMPTY) {
                    items.add(makeSpikeItem(potion));
                }
            }
        }
    }

    public static ItemStack makeSpikeItem(Potion potion){
        ItemStack stack = new ItemStack(Registry.BAMBOO_SPIKES_TIPPED_ITEM.get());
        PotionUtils.addPotionToItemStack(stack,potion);
        return stack;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return "item.supplementaries.bamboo_spikes_tipped";
        //return PotionUtils.getPotionTypeFromNBT(stack.getChildTag("BlockEntityTag")).getNamePrefixed(super.getTranslationKey() + ".effect.");
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        Potion p = PotionUtils.getPotionFromItem(stack);
        TextComponent arrowName = new TranslationTextComponent(p.getNamePrefixed("item.minecraft.tipped_arrow.effect."));
        String s = arrowName.getString();
        if(s.contains("Arrow of ")){
            return new TranslationTextComponent("item.supplementaries.bamboo_spikes_tipped_effect",
                    s.replace("Arrow of ",""));
        }
        return new TranslationTextComponent(this.getTranslationKey(stack));
        //String effectName = new TranslationTextComponent(p.getNamePrefixed("effect.minecraft.")).getString();
        //return new TranslationTextComponent("item.supplementaries.bamboo_spikes_tipped_effect",effectName);

        //return new TranslationTextComponent("item.supplementaries.bamboo_spikes_tipped_effect",
        //        TextUtil.format(PotionUtils.getPotionTypeFromNBT(stack.getChildTag("BlockEntityTag")).getNamePrefixed("")));
    }
}
