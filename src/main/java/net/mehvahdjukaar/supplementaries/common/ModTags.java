package net.mehvahdjukaar.supplementaries.common;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class ModTags {

    //block tags
    public static final Tags.IOptionalNamedTag<Block> POSTS = blockTag("posts");
    public static final Tags.IOptionalNamedTag<Block> ENCHANTMENT_BYPASS = blockTag("enchantment_bypass");
    public static final Tags.IOptionalNamedTag<Block> PALISADES = blockTag("palisades");
    public static final Tags.IOptionalNamedTag<Block> BEAMS = blockTag("beams");
    public static final Tags.IOptionalNamedTag<Block> ROPE_SUPPORT_TAG = blockTag("rope_support");
    public static final Tags.IOptionalNamedTag<Block> ROPE_HANG_TAG = blockTag("hang_from_ropes");
    public static final Tags.IOptionalNamedTag<Block> BELLOWS_TICKABLE_TAG = blockTag("bellows_tickable");
    public static final Tags.IOptionalNamedTag<Block> WATER_HOLDER = blockTag("water_holder");
    public static final Tags.IOptionalNamedTag<Block> POURING_TANK = blockTag("pouring_tank");
    public static final Tags.IOptionalNamedTag<Block> WALL_LANTERNS = blockTag("wall_lanterns");
    public static final Tags.IOptionalNamedTag<Block> VINE_SUPPORT = blockTag("vine_support");
    public static final Tags.IOptionalNamedTag<Block> PANE_CONNECTION = blockTag("pane_connection");
    public static final Tags.IOptionalNamedTag<Block> CONCRETE_POWDERS = BlockTags.createOptional(
            new ResourceLocation("forge:concrete_powders"));
    //item tags
    public static final Tags.IOptionalNamedTag<Item> SHULKER_BLACKLIST_TAG = itemTag("shulker_blacklist");
    public static final Tags.IOptionalNamedTag<Item> COOKIES = itemTag("cookies");
    public static final Tags.IOptionalNamedTag<Item> CANDLES = itemTag("candles");
    public static final Tags.IOptionalNamedTag<Item> BRICKS = itemTag("throwable_bricks");
    public static final Tags.IOptionalNamedTag<Item> ROPES = itemTag("ropes");
    public static final Tags.IOptionalNamedTag<Item> CHAINS = itemTag("chains");
    public static final Tags.IOptionalNamedTag<Item> PEDESTAL_UPRIGHT = itemTag("pedestal_upright");
    public static final Tags.IOptionalNamedTag<Item> PEDESTAL_DOWNRIGHT = itemTag("pedestal_downright");
    public static final Tags.IOptionalNamedTag<Item> CHOCOLATE_BARS = itemTag("chocolate_bars");
    public static final Tags.IOptionalNamedTag<Item> FIRE_SOURCES = itemTag("fire_sources");
    public static final Tags.IOptionalNamedTag<Item> FLOWER_BOX_PLANTABLE = itemTag("flower_box_plantable");
    //entity tags
    public static final Tags.IOptionalNamedTag<EntityType<?>> JAR_CATCHABLE = entityTag("jar_catchable");
    public static final Tags.IOptionalNamedTag<EntityType<?>> TINTED_JAR_CATCHABLE = entityTag("jar_tinted_catchable");
    public static final Tags.IOptionalNamedTag<EntityType<?>> CAGE_CATCHABLE = entityTag("cage_catchable");
    public static final Tags.IOptionalNamedTag<EntityType<?>> CAGE_BABY_CATCHABLE = entityTag("cage_baby_catchable");
    public static final Tags.IOptionalNamedTag<EntityType<?>> FLUTE_PET = entityTag("flute_pet");

    private static Tags.IOptionalNamedTag<Item> itemTag(String name) {
        return ItemTags.createOptional(new ResourceLocation(Supplementaries.MOD_ID, name));
    }
    private static Tags.IOptionalNamedTag<Block> blockTag(String name) {
        return BlockTags.createOptional(new ResourceLocation(Supplementaries.MOD_ID, name));
    }
    private static Tags.IOptionalNamedTag<EntityType<?>> entityTag(String name) {
        return EntityTypeTags.createOptional(new ResourceLocation(Supplementaries.MOD_ID, name));
    }

}
