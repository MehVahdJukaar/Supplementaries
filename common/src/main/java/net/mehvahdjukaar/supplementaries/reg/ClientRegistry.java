package net.mehvahdjukaar.supplementaries.reg;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.WallLanternTexturesRegistry;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.RedMerchantRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.SkullCandleOverlayModel;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.pickle.JarredModel;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.pickle.PickleModel;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.*;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.LabelEntity;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.minecraft.Util;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.model.ForgeModelBakery;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;

public class ClientRegistry {

    //move out of here!
    public static GlobeBlockTileRenderer GLOBE_RENDERER_INSTANCE = null;

    private static ModelLayerLocation loc(String name) {
        return new ModelLayerLocation(Supplementaries.res(name), name);
    }

    //materials
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


    //special models locations
    public static final ResourceLocation FLUTE_3D_MODEL = Supplementaries.res("item/flute_in_hand");
    public static final ResourceLocation FLUTE_2D_MODEL = Supplementaries.res("item/flute_gui");
    public static final ResourceLocation BOAT_MODEL = Supplementaries.res("block/jar_boat_ship");
    public static final ResourceLocation WIND_VANE_BLOCK_MODEL = Supplementaries.res("block/wind_vane_up");
    public static final ResourceLocation HANGING_POT_BLOCK_MODEL = Supplementaries.res("block/hanging_flower_pot");
    public static final Map<WoodType, ResourceLocation> HANGING_SIGNS_BLOCK_MODELS = new HashMap<>();
    public static final Map<LabelEntity.AttachType, ResourceLocation> LABEL_MODELS = new HashMap<>() {{
        put(LabelEntity.AttachType.BLOCK, Supplementaries.res("block/label"));
        put(LabelEntity.AttachType.CHEST, Supplementaries.res("block/label_chest"));
        put(LabelEntity.AttachType.JAR, Supplementaries.res("block/label_jar"));
    }};


    static {
        ModRegistry.HANGING_SIGNS.forEach((wood, block) -> HANGING_SIGNS_BLOCK_MODELS
                .put(wood, Supplementaries.res("block/hanging_signs/" + Utils.getID(block).getPath())));

        ModRegistry.SIGN_POST_ITEMS.forEach((wood, item) -> SIGN_POSTS_MATERIALS
                .put(wood, new Material(LOCATION_BLOCKS, Supplementaries.res("entity/sign_posts/" + Utils.getID(item).getPath()))));

        for (BannerPattern pattern : Registry.BANNER_PATTERN) {
            FLAG_MATERIALS.put(pattern, new Material(Sheets.BANNER_SHEET, Textures.FLAG_TEXTURES.get(pattern)));
        }

        for (BookPileBlockTile.BookColor color : BookPileBlockTile.BookColor.values()) {
            BOOK_MATERIALS.put(color, new Material(Sheets.SHULKER_SHEET, Textures.BOOK_TEXTURES.get(color)));
        }
    }


    public static void registerSpecialModels() {
        FlowerPotHandler.CUSTOM_MODELS.forEach(ForgeModelBakery::addSpecialModel);
        WallLanternTexturesRegistry.SPECIAL_TEXTURES.values().forEach(ForgeModelBakery::addSpecialModel);

        HANGING_SIGNS_BLOCK_MODELS.values().forEach(ForgeModelBakery::addSpecialModel);
        LABEL_MODELS.values().forEach(ForgeModelBakery::addSpecialModel);

        ForgeModelBakery.addSpecialModel(HANGING_POT_BLOCK_MODEL);
        ForgeModelBakery.addSpecialModel(WIND_VANE_BLOCK_MODEL);
        ForgeModelBakery.addSpecialModel(FLUTE_3D_MODEL);
        ForgeModelBakery.addSpecialModel(FLUTE_2D_MODEL);
        ForgeModelBakery.addSpecialModel(BOAT_MODEL);
    }


    //unused
    public static class SlimeLayer extends RenderType {

        public SlimeLayer(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
            super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
        }

        public static RenderType createGenericRenderType(String name, VertexFormat format, VertexFormat.Mode mode, ShaderStateShard shader,
                                                         TransparencyStateShard transparency, EmptyTextureStateShard texture) {
            return RenderType.create(
                    name, format, mode, 256, false, false, CompositeState.builder()
                            .setShaderState(shader)
                            .setWriteMaskState(new WriteMaskStateShard(true, true))
                            .setLightmapState(new LightmapStateShard(false))
                            .setTransparencyState(transparency)
                            .setTextureState(texture)
                            .setCullState(new CullStateShard(true))
                            .createCompositeState(true)
            );
        }

        //this internally contains a map of render types
        public static final Function<ResourceLocation, RenderType> TRANSPARENT_TEXTURE = Util.memoize((texture) ->
                createGenericRenderType("test_texture", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                        VertexFormat.Mode.QUADS, RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER,
                        RenderStateShard.TRANSLUCENT_TRANSPARENCY,
                        new TextureStateShard(texture, false, false)));
    }
}
