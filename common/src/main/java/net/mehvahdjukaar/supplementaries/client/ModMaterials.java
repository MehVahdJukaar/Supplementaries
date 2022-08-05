package net.mehvahdjukaar.supplementaries.client;

import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.ModTextures;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BannerPattern;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;

public class ModMaterials {

    //materials
    public static final Material BLACKBOARD_OUTLINE = new Material(LOCATION_BLOCKS, ModTextures.BLACKBOARD_GRID);
    public static final Material BELLOWS_MATERIAL = new Material(LOCATION_BLOCKS, ModTextures.BELLOWS_TEXTURE);
    public static final Material BUBBLE_BLOCK_MATERIAL = new Material(LOCATION_BLOCKS, ModTextures.BUBBLE_BLOCK_TEXTURE);
    public static final Material BOOK_ENCHANTED_MATERIAL = new Material(Sheets.SHULKER_SHEET, ModTextures.BOOK_ENCHANTED_TEXTURES);
    public static final Material BOOK_TOME_MATERIAL = new Material(Sheets.SHULKER_SHEET, ModTextures.BOOK_TOME_TEXTURES);
    public static final Material BOOK_WRITTEN_MATERIAL = new Material(Sheets.SHULKER_SHEET, ModTextures.BOOK_WRITTEN_TEXTURES);
    public static final Material BOOK_AND_QUILL_MATERIAL = new Material(Sheets.SHULKER_SHEET, ModTextures.BOOK_AND_QUILL_TEXTURES);
    public static final Map<BookPileBlockTile.BookColor, Material> BOOK_MATERIALS = new HashMap<>();
    public static final Map<WoodType, Material> SIGN_POSTS_MATERIALS = new HashMap<>();
    public static final Map<BannerPattern, Material> FLAG_MATERIALS = new HashMap<>();

    //needs static initializer as models are loaded before client setup
    static{
        ModRegistry.SIGN_POST_ITEMS.forEach((wood, item) -> SIGN_POSTS_MATERIALS
                .put(wood, new Material(LOCATION_BLOCKS, Supplementaries.res("entity/sign_posts/" + Utils.getID(item).getPath()))));

        for (var e : ModTextures.FLAG_TEXTURES.entrySet()) {
            FLAG_MATERIALS.put(e.getKey(), new Material(Sheets.BANNER_SHEET, e.getValue()));
        }

        for (var e : ModTextures.BOOK_TEXTURES.entrySet()) {
            BOOK_MATERIALS.put(e.getKey(), new Material(Sheets.SHULKER_SHEET, e.getValue()));
        }
    }


}
