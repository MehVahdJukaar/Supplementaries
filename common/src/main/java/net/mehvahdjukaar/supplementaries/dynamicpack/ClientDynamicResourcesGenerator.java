package net.mehvahdjukaar.supplementaries.dynamicpack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynClientResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.api.resources.textures.Palette;
import net.mehvahdjukaar.moonlight.api.resources.textures.Respriter;
import net.mehvahdjukaar.moonlight.api.resources.textures.SpriteUtils;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.client.WallLanternTexturesManager;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GlobeBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;


public class ClientDynamicResourcesGenerator extends DynClientResourcesGenerator {

    public static final ClientDynamicResourcesGenerator INSTANCE = new ClientDynamicResourcesGenerator();

    public ClientDynamicResourcesGenerator() {
        super(new DynamicTexturePack(Supplementaries.res("generated_pack")));
        this.dynamicPack.addNamespaces("minecraft");
        this.dynamicPack.setGenerateDebugResources(PlatHelper.isDev() || CommonConfigs.General.DEBUG_RESOURCES.get());
    }

    @Override
    public Logger getLogger() {
        return Supplementaries.LOGGER;
    }

    @Override
    public boolean dependsOnLoadedPacks() {
        return true;
    }

    @Override
    public void generateStaticAssetsOnStartup(ResourceManager manager) {

        this.dynamicPack.addItemModel(new ResourceLocation("crossbow_arrow"), JsonParser.parseString(
                """ 
                        {
                            "parent": "item/crossbow",
                            "textures": {
                                "layer0": "item/crossbow_arrow_base",
                                "layer1": "item/crossbow_arrow_tip"
                            }
                        }
                        """));
    }

    public void addHangingSignLoaderModel(StaticResource resource, String woodTextPath, String logTexture) {
        String string = new String(resource.data, StandardCharsets.UTF_8);

        string = string.replace("wood_type", woodTextPath);
        string = string.replace("log_texture", logTexture);

        //adds modified under my namespace
        ResourceLocation newRes = Supplementaries.res("hanging_signs/" + woodTextPath + "_loader");
        dynamicPack.addBytes(newRes, string.getBytes(), ResType.BLOCK_MODELS);
    }


    //-------------resource pack dependant textures-------------


    @Override
    public void regenerateDynamicAssets(ResourceManager manager) {

        if (CommonConfigs.Tools.ROPE_ARROW_ENABLED.get()) {
            RPUtils.appendModelOverride(manager, this.dynamicPack, new ResourceLocation("crossbow"), e -> {
                e.add(new ItemOverride(new ResourceLocation("item/crossbow_rope_arrow"),
                        List.of(new ItemOverride.Predicate(new ResourceLocation("charged"), 1f),
                                new ItemOverride.Predicate(Supplementaries.res("rope_arrow"), 1f))));
            });
        }

        if (CommonConfigs.Tools.ANTIQUE_INK_ENABLED.get()) {
            RPUtils.appendModelOverride(manager, this.dynamicPack, new ResourceLocation("written_book"), e -> {
                e.add(new ItemOverride(new ResourceLocation("item/written_book_tattered"),
                        List.of(new ItemOverride.Predicate(Supplementaries.res("antique_ink"), 1))));
            });
            RPUtils.appendModelOverride(manager, this.dynamicPack, new ResourceLocation("filled_map"), e -> {
                e.add(new ItemOverride(new ResourceLocation("item/antique_map"),
                        List.of(new ItemOverride.Predicate(Supplementaries.res("antique_ink"), 1))));
            });
        }
        GlobeBlockTile.GlobeType.recomputeCache();
        RPUtils.appendModelOverride(manager, this.dynamicPack, Supplementaries.res("globe"), e -> {
            int i = 0;
            for (var s : GlobeBlockTile.GlobeType.textures) {
                String name = s.getPath().split("/")[3].split("\\.")[0];
                e.add(new ItemOverride(Supplementaries.res("item/" + name),
                        List.of(new ItemOverride.Predicate(Supplementaries.res("type"), i))));
                i++;
                this.dynamicPack.addItemModel(Supplementaries.res(name), JsonParser.parseString(
                        """ 
                                {
                                    "parent": "item/generated",
                                    "textures": {
                                        "layer0": "supplementaries:item/globes/""" + name + "\""+
                                """               
                                    }
                                }
                                """));
            }

        });
        //need this here for reasons I forgot
        WallLanternTexturesManager.reloadTextures(manager);
        GlobeManager.refreshColorsAndTextures(manager);

        //models are dynamic too as packs can change them

        //------hanging signs------
        {
            StaticResource hsBlockState = StaticResource.getOrLog(manager,
                    ResType.BLOCKSTATES.getPath(Supplementaries.res("hanging_sign_oak")));
            StaticResource hsModel = StaticResource.getOrLog(manager,
                    ResType.BLOCK_MODELS.getPath(Supplementaries.res("hanging_signs/hanging_sign_oak")));
            StaticResource hsLoader = StaticResource.getOrLog(manager,
                    ResType.BLOCK_MODELS.getPath(Supplementaries.res("hanging_signs/loader_template")));
            StaticResource hsItemModel = StaticResource.getOrLog(manager,
                    ResType.ITEM_MODELS.getPath(Supplementaries.res("hanging_sign_oak")));

            ModRegistry.HANGING_SIGNS.forEach((wood, sign) -> {
                //if(wood.isVanilla())return;

                String id = Utils.getID(sign).getPath();


                try {
                    addSimilarJsonResource(manager, hsBlockState, "hanging_sign_oak", id);
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Hanging Sign blockstate definition for {} : {}", sign, ex);
                }

                try {
                    addSimilarJsonResource(manager, hsModel, "hanging_sign_oak", id);
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Hanging Sign block model for {} : {}", sign, ex);
                }

                try {
                    addSimilarJsonResource(manager, hsItemModel, "hanging_sign_oak", id);
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Hanging Sign item model for {} : {}", sign, ex);
                }

                try {
                    ResourceLocation logTexture;
                    try {
                        logTexture = RPUtils.findFirstBlockTextureLocation(manager, wood.log, s -> !s.contains("top"));
                    } catch (Exception e1) {
                        logTexture = RPUtils.findFirstBlockTextureLocation(manager, wood.planks, s -> true);
                        getLogger().error("Could not properly generate Hanging Sign model for {}. Falling back to planks texture : {}", sign, e1);
                    }
                    addHangingSignLoaderModel(Objects.requireNonNull(hsLoader), id, logTexture.toString());
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Hanging Sign loader model for {} : {}", sign, ex);
                }
            });

        }

        //textures


        //------sing posts-----
        {
            StaticResource spItemModel = StaticResource.getOrLog(manager,
                    ResType.ITEM_MODELS.getPath(Supplementaries.res("sign_post_oak")));

            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> {
                //if (wood.isVanilla()) return;
                String id = Utils.getID(sign).getPath();
                //langBuilder.addEntry(sign, wood.getVariantReadableName("sign_post"));

                try {
                    addSimilarJsonResource(manager, spItemModel, "sign_post_oak", id);
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Sign Post item model for {} : {}", sign, ex);
                }
            });
        }


        //hanging signs block textures
        try (TextureImage template = TextureImage.open(manager,
                Supplementaries.res("block/hanging_signs/hanging_sign_oak"));
             TextureImage mask = TextureImage.open(manager,
                     Supplementaries.res("block/hanging_signs/board_mask"))) {

            Respriter respriter = Respriter.masked(template, mask);

            ModRegistry.HANGING_SIGNS.forEach((wood, sign) -> {
                //if (wood.isVanilla()) continue;
                ResourceLocation textureRes = Supplementaries.res("block/hanging_signs/" + Utils.getID(sign).getPath());
                if (alreadyHasTextureAtLocation(manager, textureRes)) return;
                try (TextureImage plankTexture = TextureImage.open(manager,
                        RPUtils.findFirstBlockTextureLocation(manager, wood.planks))) {

                    List<Palette> targetPalette = SpriteUtils.extrapolateSignBlockPalette(plankTexture);
                    TextureImage newImage = respriter.recolorWithAnimation(targetPalette, plankTexture.getMetadata());

                    dynamicPack.addAndCloseTexture(textureRes, newImage);
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Hanging Sign block texture for for {} : {}", sign, ex);
                }
            });
        } catch (Exception ex) {
            getLogger().error("Could not generate any Hanging Sign block texture : ", ex);
        }

        //hanging sign item textures
        try (TextureImage boardTemplate = TextureImage.open(manager,
                Supplementaries.res("item/hanging_signs/template"));
             TextureImage boardMask = TextureImage.open(manager,
                     Supplementaries.res("item/hanging_signs/board_mask"));
             TextureImage signMask = TextureImage.open(manager,
                     Supplementaries.res("item/hanging_signs/sign_board_mask"))) {

            Respriter respriter = Respriter.masked(boardTemplate, boardMask);

            ModRegistry.HANGING_SIGNS.forEach((wood, sign) -> {

                //if (wood.isVanilla()) continue;
                ResourceLocation textureRes = Supplementaries.res("item/hanging_signs/" + Utils.getID(sign).getPath());
                if (alreadyHasTextureAtLocation(manager, textureRes)) return;

                TextureImage newImage = null;
                Item vanillaSign = wood.getItemOfThis("sign");
                if (vanillaSign != null) {
                    try (TextureImage vanillaSignTexture = TextureImage.open(manager,
                            RPUtils.findFirstItemTextureLocation(manager, vanillaSign))) {

                        Palette targetPalette = Palette.fromImage(vanillaSignTexture, signMask);
                        newImage = respriter.recolor(targetPalette);

                        try (TextureImage scribbles = recolorFromVanilla(manager, vanillaSignTexture,
                                Supplementaries.res("item/hanging_signs/sign_scribbles_mask"),
                                Supplementaries.res("item/hanging_signs/scribbles_template"))) {
                            newImage.applyOverlay(scribbles);
                        } catch (Exception ex) {
                            getLogger().error("Could not properly color Hanging Sign texture for {} : {}", sign, ex);
                        }

                        try (TextureImage stick = recolorFromVanilla(manager, vanillaSignTexture,
                                Supplementaries.res("item/hanging_signs/sign_stick_mask"),
                                Supplementaries.res("item/hanging_signs/stick_template"))) {
                            newImage.applyOverlay(stick);
                        } catch (Exception ex) {
                            getLogger().error("Could not properly color Hanging Sign item texture for {} : {}", sign, ex);
                        }

                    } catch (Exception ex) {
                        //getLogger().error("Could not find sign texture for wood type {}. Using plank texture : {}", wood, ex);
                    }
                }
                //if it failed use plank one
                if (newImage == null) {
                    try (TextureImage plankPalette = TextureImage.open(manager,
                            RPUtils.findFirstBlockTextureLocation(manager, wood.planks))) {
                        Palette targetPalette = SpriteUtils.extrapolateWoodItemPalette(plankPalette);
                        newImage = respriter.recolor(targetPalette);
                    } catch (Exception ex) {
                        getLogger().error("Failed to generate Hanging Sign item texture for for {} : {}", sign, ex);
                    }
                }
                if (newImage != null) {
                    dynamicPack.addAndCloseTexture(textureRes, newImage);
                }
            });
        } catch (Exception ex) {
            getLogger().error("Could not generate any Hanging Sign item texture : ", ex);
        }

        //sign posts item textures
        try (TextureImage template = TextureImage.open(manager,
                Supplementaries.res("item/sign_posts/template"))) {

            Respriter respriter = Respriter.of(template);

            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> {
                //if (wood.isVanilla()) continue;

                ResourceLocation textureRes = Supplementaries.res("item/sign_posts/" + Utils.getID(sign).getPath());

                if (alreadyHasTextureAtLocation(manager, textureRes)) return;

                TextureImage newImage = null;
                Item signItem = wood.getItemOfThis("sign");
                if (signItem != null) {
                    try (TextureImage vanillaSign = TextureImage.open(manager,
                            RPUtils.findFirstItemTextureLocation(manager, signItem));
                         TextureImage signMask = TextureImage.open(manager,
                                 Supplementaries.res("item/hanging_signs/sign_board_mask"))) {

                        List<Palette> targetPalette = Palette.fromAnimatedImage(vanillaSign, signMask);
                        newImage = respriter.recolor(targetPalette);

                        try (TextureImage scribbles = recolorFromVanilla(manager, vanillaSign,
                                Supplementaries.res("item/hanging_signs/sign_scribbles_mask"),
                                Supplementaries.res("item/sign_posts/scribbles_template"))) {
                            newImage.applyOverlay(scribbles);
                        } catch (Exception ex) {
                            getLogger().error("Could not properly color Sign Post item texture for {} : {}", sign, ex);
                        }

                    } catch (Exception ex) {
                        //getLogger().error("Could not find sign texture for wood type {}. Using plank texture : {}", wood, ex);
                    }
                }
                //if it failed use plank one
                if (newImage == null) {
                    try (TextureImage plankPalette = TextureImage.open(manager,
                            RPUtils.findFirstBlockTextureLocation(manager, wood.planks))) {
                        Palette targetPalette = SpriteUtils.extrapolateWoodItemPalette(plankPalette);
                        newImage = respriter.recolor(targetPalette);

                    } catch (Exception ex) {
                        getLogger().error("Failed to generate Sign Post item texture for for {} : {}", sign, ex);
                    }
                }
                if (newImage != null) {
                    dynamicPack.addAndCloseTexture(textureRes, newImage);
                }
            });
        } catch (Exception ex) {
            getLogger().error("Could not generate any Sign Post item texture : ", ex);
        }

        //sign posts block textures
        try (TextureImage template = TextureImage.open(manager,
                Supplementaries.res("entity/sign_posts/sign_post_oak"))) {

            Respriter respriter = Respriter.of(template);

            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> {
                //if (wood.isVanilla()) continue;
                var textureRes = Supplementaries.res("entity/sign_posts/" + Utils.getID(sign).getPath());
                if (alreadyHasTextureAtLocation(manager, textureRes)) return;

                try (TextureImage plankTexture = TextureImage.open(manager,
                        RPUtils.findFirstBlockTextureLocation(manager, wood.planks))) {
                    Palette palette = Palette.fromImage(plankTexture);
                    TextureImage newImage = respriter.recolor(palette);

                    dynamicPack.addAndCloseTexture(textureRes, newImage);
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Sign Post block texture for for {} : {}", sign, ex);
                }
            });
        } catch (Exception ex) {
            getLogger().error("Could not generate any Sign Post block texture : ", ex);
        }
    }

    /**
     * helper method.
     * recolors the template image with the color grabbed from the given image restrained to its mask, if possible
     */
    @Nullable
    public static TextureImage recolorFromVanilla(ResourceManager manager, TextureImage vanillaTexture, ResourceLocation vanillaMask,
                                                  ResourceLocation templateTexture) {
        try (TextureImage scribbleMask = TextureImage.open(manager, vanillaMask);
             TextureImage template = TextureImage.open(manager, templateTexture)) {
            Respriter respriter = Respriter.of(template);
            Palette palette = Palette.fromImage(vanillaTexture, scribbleMask);
            return respriter.recolor(palette);
        } catch (Exception ignored) {
        }
        return null;
    }

    //TODO: invert scribble color if sign is darker than them

    @Override
    public void addDynamicTranslations(AfterLanguageLoadEvent lang) {
        ModRegistry.HANGING_SIGNS.forEach((type, block) ->
                LangBuilder.addDynamicEntry(lang, "block.supplementaries.hanging_sign", type, block));
        ModRegistry.SIGN_POST_ITEMS.forEach((type, item) ->
                LangBuilder.addDynamicEntry(lang, "item.supplementaries.sign_post", type, item));
    }

}
