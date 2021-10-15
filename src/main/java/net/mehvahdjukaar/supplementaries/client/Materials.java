package net.mehvahdjukaar.supplementaries.client;

import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.common.Textures.BookColor;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.BannerPattern;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;

public class Materials {

    public static final Material BLACKBOARD_GRID = new Material(LOCATION_BLOCKS, Textures.BLACKBOARD_GRID);


    public static final Material BELLOWS_MATERIAL = new Material(LOCATION_BLOCKS, Textures.BELLOWS_TEXTURE);

    public static final Map<BookColor, Material> BOOK_MATERIALS = new HashMap<>();
    public static final Material BOOK_ENCHANTED_MATERIAL = new Material(Sheets.SHULKER_SHEET, Textures.BOOK_ENCHANTED_TEXTURES);
    public static final Material BOOK_TOME_MATERIAL = new Material(Sheets.SHULKER_SHEET, Textures.BOOK_TOME_TEXTURES);


    public static final Map<IWoodType, Material> HANGING_SIGNS_MATERIALS = new HashMap<>();
    public static final Map<IWoodType, Material> SIGN_POSTS_MATERIALS = new HashMap<>();
    public static final Map<BannerPattern, Material> FLAG_MATERIALS = new HashMap<>();
    static {
        for(IWoodType type : WoodTypes.TYPES.values()){
            //HANGING_SIGNS_MATERIALS.put(type, new RenderMaterial(Atlases.SIGN_SHEET, Textures.HANGING_SIGNS_TEXTURES.get(type)));
            SIGN_POSTS_MATERIALS.put(type, new Material(LOCATION_BLOCKS, Textures.SIGN_POSTS_TEXTURES.get(type)));
        }

        for(BannerPattern pattern : BannerPattern.values()){
            FLAG_MATERIALS.put(pattern, new Material(Sheets.BANNER_SHEET, Textures.FLAG_TEXTURES.get(pattern)));
        }

        for(BookColor color : BookColor.values()){
            BOOK_MATERIALS.put(color, new Material(Sheets.SHULKER_SHEET,Textures.BOOK_TEXTURES.get(color)));
        }
    }

}
