package net.mehvahdjukaar.supplementaries.integration.neoforge;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.violetmoon.quark.addons.oddities.item.BackpackItem;

public class QuarkCompatImpl {

    public static float getEncumbermentFromBackpack(ItemStack stack) {
        float j = 0;

        if (stack.getItem() instanceof BackpackItem) {
            var handlerOpt = stack.getCapability(Capabilities.ItemHandler.ITEM, null);
            if (handlerOpt != null) {
                for (int i = 0; i < handlerOpt.getSlots(); ++i) {
                    ItemStack slotItem = handlerOpt.getStackInSlot(i);
                    j += SackItem.getEncumber(slotItem);
                }
            }
        }
        return j;
    }

    public static void init() {
    }
}
