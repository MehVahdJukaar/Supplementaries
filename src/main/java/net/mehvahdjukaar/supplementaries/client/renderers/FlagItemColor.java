package net.mehvahdjukaar.supplementaries.client.renderers;

import net.mehvahdjukaar.supplementaries.items.FlagItem;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

public class FlagItemColor implements IItemColor {

    @Override
    public int getColor(ItemStack stack, int tint) {
        if(tint==1) return ((FlagItem)stack.getItem()).getColor().getColorValue();
        return -1;
    }
}

