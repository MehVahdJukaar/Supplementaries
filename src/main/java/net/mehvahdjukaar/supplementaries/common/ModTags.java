package net.mehvahdjukaar.supplementaries.common;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class ModTags {

    //public static final ResourceLocation SHULKER_BLACKLIST_TAG = new ResourceLocation(Supplementaries.MOD_ID, "shulker_blacklist");
    //public static final ResourceLocation BELLOWS_TICKABLE_TAG = new ResourceLocation(Supplementaries.MOD_ID, "bellows_tickable");
    //public static final ResourceLocation ROPE_TAG = new ResourceLocation(Supplementaries.MOD_ID, "ropes");
    //public static final ResourceLocation POSTS = new ResourceLocation(Supplementaries.MOD_ID, "posts");
    //ITag<Block> tag = BlockTags.getCollection().get(ModTags.POSTS);return (tag!= null && b.isIn(tag));

    //block tags
    public static final Tags.IOptionalNamedTag<Block> POSTS = blockTag("posts");
    public static final Tags.IOptionalNamedTag<Block> ROPE_TAG = blockTag("rope_support");
    public static final Tags.IOptionalNamedTag<Block> ROPE_HANG_TAG = blockTag("hang_from_ropes");
    public static final Tags.IOptionalNamedTag<Block> BELLOWS_TICKABLE_TAG = blockTag("bellows_tickable");
    public static final Tags.IOptionalNamedTag<Block> WATER_HOLDER = blockTag("water_holder");
    //item tags
    public static final Tags.IOptionalNamedTag<Item> SHULKER_BLACKLIST_TAG = itemTag("shulker_blacklist");
    public static final Tags.IOptionalNamedTag<Item> COOKIES = itemTag("cookies");
    public static final Tags.IOptionalNamedTag<Item> BRICKS = itemTag("throwable_bricks");

    private static Tags.IOptionalNamedTag<Item> itemTag(String name) {
        return ItemTags.createOptional(new ResourceLocation(Supplementaries.MOD_ID, name));
    }
    private static Tags.IOptionalNamedTag<Block> blockTag(String name) {
        return BlockTags.createOptional(new ResourceLocation(Supplementaries.MOD_ID, name));
    }

    public static boolean isTagged(ITag<Item> tag, Item i){
        try {
            return tag != null && i.isIn(tag);
        }
        catch (Exception ignored){
            return false;
        }
    }

    public static boolean isTagged(ITag<Block> tag, Block b){
        try {
            return tag != null && b.isIn(tag);
        }
        catch (Exception ignored){
            return false;
        }
    }

}
