package net.mehvahdjukaar.supplementaries.client;

import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.selene.block_set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.setup.RegistryConstants;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BannerPattern;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;

public class Materials {

    //TODO: clean & reorganize this, textures, client registerBus and client setup
    public static final Material BLACKBOARD_OUTLINE = new Material(LOCATION_BLOCKS, Textures.BLACKBOARD_GRID);
    public static final Material BELLOWS_MATERIAL = new Material(LOCATION_BLOCKS, Textures.BELLOWS_TEXTURE);
    public static final Material BUBBLE_BLOCK_MATERIAL = new Material(LOCATION_BLOCKS, Textures.BUBBLE_BLOCK_TEXTURE);
    public static final Material BOOK_ENCHANTED_MATERIAL = new Material(Sheets.SHULKER_SHEET, Textures.BOOK_ENCHANTED_TEXTURES);
    public static final Material BOOK_TOME_MATERIAL = new Material(Sheets.SHULKER_SHEET, Textures.BOOK_TOME_TEXTURES);
    public static final Material BOOK_WRITTEN_MATERIAL = new Material(Sheets.SHULKER_SHEET, Textures.BOOK_WRITTEN_TEXTURES);
    public static final Material BOOK_AND_QUILL_MATERIAL = new Material(Sheets.SHULKER_SHEET, Textures.BOOK_AND_QUILL_TEXTURES);
    public static final Map<BookPileBlockTile.BookColor, Material> BOOK_MATERIALS = new HashMap<>();

    public static final Map<WoodType, Material> SIGN_POSTS_MATERIALS = new HashMap<>();
    public static final Map<BannerPattern, Material> FLAG_MATERIALS = new HashMap<>();

    public static final Map<WoodType, ResourceLocation> HANGING_SIGNS_BLOCK_MODELS = new HashMap<>();
    public static final ResourceLocation WIND_VANE_BLOCK_MODEL = new ResourceLocation(
            Supplementaries.MOD_ID + ":block/" + RegistryConstants.WIND_VANE_NAME + "_up");
    //TODO: add this to baked model
    public static final ResourceLocation HANGING_POT_BLOCK_MODEL = new ResourceLocation(
            Supplementaries.MOD_ID + ":block/" + RegistryConstants.HANGING_FLOWER_POT_NAME);

    static {
        for (WoodType type : WoodTypeRegistry.WOOD_TYPES.values()) {

            HANGING_SIGNS_BLOCK_MODELS.put(type, Supplementaries.res("block/hanging_signs/" +
                    type.getVariantId("hanging_sign")));

            SIGN_POSTS_MATERIALS.put(type, new Material(LOCATION_BLOCKS, Supplementaries.res("entity/sign_posts/" +
                    type.getVariantId("sign_post"))));
        }

        for (BannerPattern pattern : BannerPattern.values()) {
            FLAG_MATERIALS.put(pattern, new Material(Sheets.BANNER_SHEET, Textures.FLAG_TEXTURES.get(pattern)));
        }

        for (BookPileBlockTile.BookColor color : BookPileBlockTile.BookColor.values()) {
            BOOK_MATERIALS.put(color, new Material(Sheets.SHULKER_SHEET, Textures.BOOK_TEXTURES.get(color)));
        }
    }

}
