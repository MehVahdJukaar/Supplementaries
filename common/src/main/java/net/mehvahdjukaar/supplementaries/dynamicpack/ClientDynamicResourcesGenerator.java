package net.mehvahdjukaar.supplementaries.dynamicpack;

import com.google.gson.JsonParser;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynClientResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.api.resources.textures.*;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.ClientSpecialModelsManager;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.common.misc.CakeRegistry;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Rotation;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;


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

    //-------------resource pack dependant textures-------------


    @Override
    public void regenerateDynamicAssets(ResourceManager manager) {

        //need this here for reasons I forgot
        ClientSpecialModelsManager.refreshModels(manager);
        GlobeManager.refreshColorsAndTextures(manager);
        ColorHelper.refreshBubbleColors(manager);

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

        RPUtils.appendModelOverride(manager, this.dynamicPack, Supplementaries.res("globe"), e -> {
            int i = 0;
            for (var text : GlobeManager.Type.textures) {
                String name = text.getPath().split("/")[3].split("\\.")[0];
                e.add(new ItemOverride(Supplementaries.res("item/" + name),
                        List.of(new ItemOverride.Predicate(Supplementaries.res("type"), i))));
                i++;
                this.dynamicPack.addItemModel(Supplementaries.res(name), JsonParser.parseString(
                        """ 
                                {
                                    "parent": "item/generated",
                                    "textures": {
                                        "layer0": "supplementaries:item/globes/""" + name + "\"" +
                                """               
                                            }
                                        }
                                        """));
            }

        });


        //models are dynamic too as packs can change them

        //textures


        //------sing posts-----
        {
            StaticResource spItemModel = StaticResource.getOrLog(manager,
                    ResType.ITEM_MODELS.getPath(Supplementaries.res("sign_post_oak")));
            StaticResource spBlockModel = StaticResource.getOrLog(manager,
                    ResType.BLOCK_MODELS.getPath(Supplementaries.res("sign_posts/sign_post_oak")));
            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> {
                //if (wood.isVanilla()) return;
                String id = Utils.getID(sign).getPath();
                //langBuilder.addEntry(sign, wood.getVariantReadableName("sign_post"));

                try {
                    addSimilarJsonResource(manager, spItemModel, "sign_post_oak", id);
                    addSimilarJsonResource(manager, spBlockModel, "sign_post_oak", id);
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Sign Post item model for {} : {}", sign, ex);
                }
            });
        }

        //sign posts item textures
        try (TextureImage template = TextureImage.open(manager,
                Supplementaries.res("item/sign_posts/template"))) {

            Respriter respriter = Respriter.of(template);

            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> {
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
                Supplementaries.res("block/sign_posts/sign_post_oak"))) {

            Respriter respriter = Respriter.of(template);

            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> {
                //if (wood.isVanilla()) continue;
                var textureRes = Supplementaries.res("block/sign_posts/" + Utils.getID(sign).getPath());
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


        ImageTransformer transformer = ImageTransformer.builder(32, 64, 16, 16)
                .copyRect(26, 0, 2, 4, 4, 0)
                .copyRect(26, 8, 6, 8, 4, 4)
                .copyRect(28, 24, 4, 8, 0, 4)
                .copyRect(26, 20, 2, 4, 6, 0)
                //cheaty as it has to be flipped. todo: find a way to rotate it instead as this work wotk with packs
                .copyRect(26, 28, 1, 8, 11, 4)
                .copyRect(27, 28, 1, 8, 10, 4)
                .build();

        for (WoodType w : WoodTypeRegistry.getTypes()) {
            //hanging sign extension textures
            try (TextureImage vanillaTexture = TextureImage.open(manager,
                    Sheets.getHangingSignMaterial(w.toVanilla()).texture())) {
                TextureImage flipped = vanillaTexture.createRotated(Rotation.CLOCKWISE_90);
                TextureImage newIm = flipped.createResized(0.5f, 0.25f);
                newIm.clear();

                transformer.apply(flipped, newIm);
                flipped.close();
                this.dynamicPack.addAndCloseTexture(Supplementaries.res("entity/signs/hanging/" + w.getVariantId("extension")), newIm);
            } catch (Exception e) {
                Supplementaries.LOGGER.warn("Failed to generate hanging sign extension texture for {}. Could be that the target mod isnt registering their wood type properly", w, e);
            }
        }
        if (CompatHandler.FARMERS_DELIGHT) {
            //hanging sign extension textures
            try (TextureImage vanillaTexture = TextureImage.open(manager,
                    new ResourceLocation("farmersdelight:entity/signs/hanging/canvas"))) {
                TextureImage flipped = vanillaTexture.createRotated(Rotation.CLOCKWISE_90);
                TextureImage newIm = flipped.createResized(0.5f, 0.25f);
                newIm.clear();

                transformer.apply(flipped, newIm);
                flipped.close();
                this.dynamicPack.addAndCloseTexture(Supplementaries.res("entity/signs/hanging/farmersdelight/extension_canvas"), newIm);
            } catch (Exception e) {
                Supplementaries.LOGGER.warn("Failed to generate hanging sign extension texture for {}, ", "canvas sign", e);
            }
        }

        {
            StaticResource[] cakeModels = Stream.of("full","slice1","slice2", "slice3", "slice4", "slice5", "slice6")
                    .map(s-> StaticResource.getOrLog(manager,
                            ResType.BLOCK_MODELS.getPath(Supplementaries.res("double_cake/vanilla_"+s)))).toArray(StaticResource[]::new);

            StaticResource doubleCakeModelState = StaticResource.getOrLog(manager,
                    ResType.BLOCKSTATES.getPath(Supplementaries.res("double_cake")));
            for (var t : CakeRegistry.INSTANCE.getValues()) {
                if (!t.isVanilla()) {
                    try {
                        ResourceLocation dcId = Utils.getID(t.getBlockOfThis("double_cake"));
                        ResourceLocation top = RPUtils.findFirstBlockTextureLocation(manager, t.cake, s -> s.contains("top"));
                        ResourceLocation side = RPUtils.findFirstBlockTextureLocation(manager, t.cake, s -> s.contains("side"));
                        ResourceLocation bottom = RPUtils.findFirstBlockTextureLocation(manager, t.cake, s -> s.contains("bottom"));
                        ResourceLocation inner = RPUtils.findFirstBlockTextureLocation(manager, t.cake, s -> s.contains("inner"));

                        for(var m  : cakeModels) {
                            addSimilarJsonResource(manager, m, s -> s
                                            .replace("supplementaries:block/double_cake", "")
                                            .replace("supplementaries:block/cake", "")
                                            .replace("\"/", "\"supplementaries:block/double_cake/")
                                            .replace("_top", top.toString())
                                            .replace("_side", side.toString())
                                            .replace("_inner", inner.toString())
                                            .replace("_bottom", bottom.toString()),
                                    s -> s.replace("vanilla", dcId.getPath()));
                        }
                        addSimilarJsonResource(manager, doubleCakeModelState,
                                s -> s.replace("vanilla", dcId.getPath()),
                                s -> s.replace("double_cake", dcId.getPath()));
                    } catch (Exception e) {
                        Supplementaries.LOGGER.error("Failed to generate model for double cake {},", t, e);
                    }

                }
            }
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
        ModRegistry.SIGN_POST_ITEMS.forEach((type, item) ->
                LangBuilder.addDynamicEntry(lang, "item.supplementaries.sign_post", type, item));

    }


}
