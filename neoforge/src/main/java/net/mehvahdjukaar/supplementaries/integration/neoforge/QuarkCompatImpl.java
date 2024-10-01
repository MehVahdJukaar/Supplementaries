package net.mehvahdjukaar.supplementaries.integration.neoforge;

import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.violetmoon.quark.addons.oddities.item.BackpackItem;

public class QuarkCompatImpl {

    public static float getEncumbermentFromBackpack(ItemStack stack) {
        float j = 0;
        //TODO: add back
        /*
        if (stack.getItem() instanceof BackpackItem) {
            LazyOptional<IItemHandler> handlerOpt = stack.getCapability(Capabilities.ItemHandler.ITEM, null);
            if (handlerOpt.isPresent()) {
                IItemHandler handler = handlerOpt.resolve().get();
                for (int i = 0; i < handler.getSlots(); ++i) {
                    ItemStack slotItem = handler.getStackInSlot(i);
                    j += SackItem.getEncumber(slotItem);
                }
            }
        }*/
        return j;
    }

    public static void init() {
    }
}
