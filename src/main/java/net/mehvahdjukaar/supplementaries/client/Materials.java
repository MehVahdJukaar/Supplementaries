package net.mehvahdjukaar.supplementaries.client;

import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.common.Textures.BookColor;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.tileentity.BannerPattern;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.client.renderer.texture.AtlasTexture.LOCATION_BLOCKS;

public class Materials {

    public static final RenderMaterial BLACKBOARD_GRID = new RenderMaterial(LOCATION_BLOCKS, Textures.BLACKBOARD_GRID);

    public static final RenderMaterial BELLOWS_MATERIAL = new RenderMaterial(LOCATION_BLOCKS, Textures.BELLOWS_TEXTURE);

    public static final Map<BookColor, RenderMaterial> BOOK_MATERIALS = new HashMap<>();
    public static final RenderMaterial BOOK_ENCHANTED_MATERIAL = new RenderMaterial(Atlases.SHULKER_SHEET, Textures.BOOK_ENCHANTED_TEXTURES);
    public static final RenderMaterial BOOK_TOME_MATERIAL = new RenderMaterial(Atlases.SHULKER_SHEET, Textures.BOOK_TOME_TEXTURES);


    public static final Map<IWoodType, RenderMaterial> HANGING_SIGNS_MATERIALS = new HashMap<>();
    public static final Map<IWoodType, RenderMaterial> SIGN_POSTS_MATERIALS = new HashMap<>();
    public static final Map<BannerPattern, RenderMaterial> FLAG_MATERIALS = new HashMap<>();
    static {
        for(IWoodType type : WoodTypes.TYPES.values()){
            //HANGING_SIGNS_MATERIALS.put(type, new RenderMaterial(Atlases.SIGN_SHEET, Textures.HANGING_SIGNS_TEXTURES.get(type)));
            SIGN_POSTS_MATERIALS.put(type, new RenderMaterial(LOCATION_BLOCKS, Textures.SIGN_POSTS_TEXTURES.get(type)));
        }

        for(BannerPattern pattern : BannerPattern.values()){
            FLAG_MATERIALS.put(pattern, new RenderMaterial(Atlases.BANNER_SHEET, Textures.FLAG_TEXTURES.get(pattern)));
        }

        for(BookColor color : BookColor.values()){
            BOOK_MATERIALS.put(color, new RenderMaterial(Atlases.SHULKER_SHEET,Textures.BOOK_TEXTURES.get(color)));
        }
    }

}
