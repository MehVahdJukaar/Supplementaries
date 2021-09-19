package net.mehvahdjukaar.supplementaries.items.tabs;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class SupplementariesTab extends ItemGroup {

    public SupplementariesTab(String label) {
        super(label);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(ModRegistry.GLOBE_ITEM.get());
    }
}
