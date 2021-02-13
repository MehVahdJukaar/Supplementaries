package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.PotionTooltipHelper;
import net.mehvahdjukaar.supplementaries.client.renderers.TextUtil;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
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
        PotionTooltipHelper.addPotionTooltip(stack.getChildTag("BlockEntityTag"), tooltip, BambooSpikesBlockTile.POTION_MULTIPLIER);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return PotionTooltipHelper.getColor(stack.getChildTag("BlockEntityTag"));
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

    /*it's better not to add every type to creative inv so we don't clutter it
    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        TippedArrowItem
        if (this.isInGroup(group)) {
            items.add(new ItemStack(Registry.BAMBOO_SPIKES_ITEM.get()));
            ItemStack stack = new ItemStack(Registry.BAMBOO_SPIKES_TIPPED_ITEM.get());
            CompoundNBT com = new CompoundNBT();
            ResourceLocation resourcelocation = net.minecraft.util.registry.Registry.POTION.getKey(Potions.POISON);
            com.putString("Potion", resourcelocation.toString());
            stack.setTagInfo("BlockEntityTag",com);
            items.add(stack);
        }
    }
        @Override
    public String getTranslationKey() {
        return "item.supplementaries.bamboo_spikes_tipped";
    }
    */

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if(group==ItemGroup.DECORATIONS&&Registry.MOD_TAB==null){
            items.add(new ItemStack(Registry.BAMBOO_SPIKES_ITEM.get()));
        }
        if (this.isInGroup(group)) {
            for(Potion potion : net.minecraft.util.registry.Registry.POTION) {
                if (!potion.getEffects().isEmpty()) {
                    ItemStack stack = new ItemStack(Registry.BAMBOO_SPIKES_TIPPED_ITEM.get());
                    CompoundNBT com = new CompoundNBT();
                    ResourceLocation resourcelocation = net.minecraft.util.registry.Registry.POTION.getKey(potion);
                    com.putString("Potion", resourcelocation.toString());
                    stack.setTagInfo("BlockEntityTag", com);
                    items.add(stack);
                }
            }
        }

    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return "item.supplementaries.bamboo_spikes_tipped";
        //return PotionUtils.getPotionTypeFromNBT(stack.getChildTag("BlockEntityTag")).getNamePrefixed(super.getTranslationKey() + ".effect.");
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return new TranslationTextComponent("item.supplementaries.bamboo_spikes_tipped_effect",
                TextUtil.format(PotionUtils.getPotionTypeFromNBT(stack.getChildTag("BlockEntityTag")).getNamePrefixed("")));
    }
}
