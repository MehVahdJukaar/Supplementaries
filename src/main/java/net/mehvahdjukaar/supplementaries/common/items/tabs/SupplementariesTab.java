package net.mehvahdjukaar.supplementaries.common.items.tabs;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class SupplementariesTab extends CreativeModeTab {

    public SupplementariesTab(String label) {
        super(label);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(ModRegistry.GLOBE_ITEM.get());
    }
}
