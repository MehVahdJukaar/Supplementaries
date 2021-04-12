package net.mehvahdjukaar.supplementaries.plugins.inspirations;

import knightminer.inspirations.recipes.recipe.cauldron.contents.PotionContentType;
import knightminer.inspirations.recipes.tileentity.CauldronTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.tileentity.TileEntity;

public class CauldronPlugin {
    public static ItemStack tryExtractFluid(TileEntity te){
        if(te instanceof CauldronTileEntity){
            CauldronTileEntity cauldron = ((CauldronTileEntity) te);
            if(cauldron.getContents() instanceof PotionContentType){
                //cauldron.getContents().getTintColor()
                ItemStack newStack = cauldron.handleDispenser(new ItemStack(Items.GLASS_BOTTLE),  (i) -> {});
                if(newStack.getItem() instanceof PotionItem)return newStack;
            }
        }
        return ItemStack.EMPTY;
    }
}
