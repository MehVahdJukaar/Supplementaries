package net.mehvahdjukaar.supplementaries.setup;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.selene.block_set.wood.WoodTypeRegistry;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.common.util.NonNullLazy;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;

public class ClientRegistry {

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
        Validate.isTrue(!WoodTypeRegistry.WOOD_TYPES.values().isEmpty());
        ModRegistry.HANGING_SIGNS.forEach((wood, block) -> HANGING_SIGNS_BLOCK_MODELS
                .put(wood, Supplementaries.res("block/hanging_signs/" + block.getRegistryName().getPath())));

        ModRegistry.SIGN_POST_ITEMS.forEach((wood, item) -> SIGN_POSTS_MATERIALS
                .put(wood, new Material(LOCATION_BLOCKS, Supplementaries.res("entity/sign_posts/" + item.getRegistryName().getPath()))));

        for (BannerPattern pattern : BannerPattern.values()) {
            FLAG_MATERIALS.put(pattern, new Material(Sheets.BANNER_SHEET, Textures.FLAG_TEXTURES.get(pattern)));
        }

        for (BookPileBlockTile.BookColor color : BookPileBlockTile.BookColor.values()) {
            BOOK_MATERIALS.put(color, new Material(Sheets.SHULKER_SHEET, Textures.BOOK_TEXTURES.get(color)));
        }
    }

    //entity models
    public static ModelLayerLocation BELLOWS_MODEL = loc("bellows");
    public static ModelLayerLocation BOOK_MODEL = loc("book");
    public static ModelLayerLocation CLOCK_HANDS_MODEL = loc("clock_hands");
    public static ModelLayerLocation GLOBE_BASE_MODEL = loc("globe");
    public static ModelLayerLocation GLOBE_SPECIAL_MODEL = loc("globe_special");
    public static ModelLayerLocation SIGN_POST_MODEL = loc("sign_post");
    public static ModelLayerLocation RED_MERCHANT_MODEL = loc("red_merchant");
    public static ModelLayerLocation SKULL_CANDLE_OVERLAY = loc("skull_candle");
    public static ModelLayerLocation JARVIS_MODEL = loc("jarvis");
    public static ModelLayerLocation PICKLE_MODEL = loc("pickle");
    //public static ModelLayerLocation BELL_EXTENSION = loc("bell_extension");

    //TODO: merge with materials and client setup

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BELLOWS_MODEL, BellowsBlockTileRenderer::createMesh);
        event.registerLayerDefinition(BOOK_MODEL, BookPileBlockTileRenderer::createMesh);
        event.registerLayerDefinition(CLOCK_HANDS_MODEL, ClockBlockTileRenderer::createMesh);
        event.registerLayerDefinition(GLOBE_BASE_MODEL, GlobeBlockTileRenderer::createBaseMesh);
        event.registerLayerDefinition(GLOBE_SPECIAL_MODEL, GlobeBlockTileRenderer::createSpecialMesh);
        event.registerLayerDefinition(SIGN_POST_MODEL, SignPostBlockTileRenderer::createMesh);
        event.registerLayerDefinition(RED_MERCHANT_MODEL, RedMerchantRenderer::createMesh);
        event.registerLayerDefinition(SKULL_CANDLE_OVERLAY, SkullCandleOverlayModel::createMesh);
        event.registerLayerDefinition(JARVIS_MODEL, JarredModel::createMesh);
        event.registerLayerDefinition(PICKLE_MODEL, PickleModel::createMesh);
        //event.registerLayerDefinition(BELL_EXTENSION, BellTileMixinRenderer::createMesh);
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

    public static void registerISTER(Consumer<IItemRenderProperties> consumer, BiFunction<BlockEntityRenderDispatcher, EntityModelSet, BlockEntityWithoutLevelRenderer> factory) {
        consumer.accept(new IItemRenderProperties() {
            final NonNullLazy<BlockEntityWithoutLevelRenderer> renderer = NonNullLazy.of(
                    () -> factory.apply(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                            Minecraft.getInstance().getEntityModels()));

            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return renderer.get();
            }
        });
    }

    public static class RR extends RenderType {

        public RR(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
            super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
        }

        public static RenderType createGenericRenderType(String name, VertexFormat format, VertexFormat.Mode mode, RenderStateShard.ShaderStateShard shader,
                                                         RenderStateShard.TransparencyStateShard transparency, RenderStateShard.EmptyTextureStateShard texture) {
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
                        new RenderStateShard.TextureStateShard(texture,false, false)));
    }
}
