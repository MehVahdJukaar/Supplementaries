package net.mehvahdjukaar.supplementaries.common.items;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;


public class OptionalTagBlockItem extends BlockItem {

    private final Supplier<Boolean> hidden;

    public OptionalTagBlockItem(Block pBlock, Properties pProperties, String emptyTagKey) {
        this(pBlock, pProperties, TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(emptyTagKey)));
    }

    public OptionalTagBlockItem(Block pBlock, Properties pProperties, TagKey<Item> emptyTag) {
        super(pBlock, pProperties);
        this.hidden = () -> !Registry.ITEM.getTagOrEmpty(emptyTag).iterator().hasNext();
    }

    @Override
    protected boolean allowdedIn(CreativeModeTab pCategory) {
        if (this.hidden.get()) return false;
        return super.allowdedIn(pCategory);
    }
}
