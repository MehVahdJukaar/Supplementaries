package net.mehvahdjukaar.supplementaries.dynamicpack;

import net.mehvahdjukaar.selene.client.asset_generators.LangBuilder;
import net.mehvahdjukaar.selene.client.asset_generators.textures.Palette;
import net.mehvahdjukaar.selene.client.asset_generators.textures.Respriter;
import net.mehvahdjukaar.selene.client.asset_generators.textures.SpriteUtils;
import net.mehvahdjukaar.selene.client.asset_generators.textures.TextureImage;
import net.mehvahdjukaar.selene.resourcepack.*;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.WallLanternTexturesRegistry;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;


public class ClientDynamicResourcesHandler extends RPAwareDynamicTextureProvider {

    public ClientDynamicResourcesHandler() {
        super(new DynamicTexturePack(Supplementaries.res("generated_pack")));
        this.dynamicPack.generateDebugResources = !FMLLoader.isProduction() || RegistryConfigs.Reg.DEBUG_RESOURCES.get();
    }

    @Override
    public Logger getLogger() {
        return Supplementaries.LOGGER;
    }

    @Override
    public boolean dependsOnLoadedPacks() {
        return ClientConfigs.general.RESOURCE_PACK_SUPPORT.get();
    }

    @Override
    public void generateStaticAssetsOnStartup(ResourceManager manager) {


        //hack. I need this for texture stitch
        WallLanternTexturesRegistry.reloadTextures(manager);

        //generate static resources

        //LangBuilder langBuilder = new LangBuilder();

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

                String id = sign.getRegistryName().getPath();
                //langBuilder.addEntry(sign, wood.getVariantReadableName("block_type.supplementaries.hanging_sign"));

                try {
                    dynamicPack.addSimilarJsonResource(hsBlockState, "hanging_sign_oak", id);
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Hanging Sign blockstate definition for {} : {}", sign, ex);
                }

                try {
                    dynamicPack.addSimilarJsonResource(hsModel, "hanging_sign_oak", id);
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Hanging Sign block model for {} : {}", sign, ex);
                }

                try {
                    dynamicPack.addSimilarJsonResource(hsItemModel, "hanging_sign_oak", id);
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


        //------sing posts-----
        {
            StaticResource spItemModel = StaticResource.getOrLog(manager,
                    ResType.ITEM_MODELS.getPath(Supplementaries.res("sign_post_oak")));

            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> {
                //if (wood.isVanilla()) return;
                String id = sign.getRegistryName().getPath();
                //langBuilder.addEntry(sign, wood.getVariantReadableName("sign_post"));

                try {
                    dynamicPack.addSimilarJsonResource(spItemModel, "sign_post_oak", id);
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Sign Post item model for {} : {}", sign, ex);
                }
            });
        }


        // dynamicPack.addLang(Supplementaries.res("en_us"), langBuilder.build());
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

        //hanging signs block textures
        try (TextureImage template = TextureImage.open(manager,
                Supplementaries.res("blocks/hanging_signs/hanging_sign_oak"));
             TextureImage mask = TextureImage.open(manager,
                     Supplementaries.res("blocks/hanging_signs/board_mask"))) {


            Respriter respriter = Respriter.masked(template, mask);

            ModRegistry.HANGING_SIGNS.forEach((wood, sign) -> {
                //if (wood.isVanilla()) continue;
                ResourceLocation textureRes = Supplementaries.res("blocks/hanging_signs/" + sign.getRegistryName().getPath());
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
                Supplementaries.res("items/hanging_signs/template"));
             TextureImage boardMask = TextureImage.open(manager,
                     Supplementaries.res("items/hanging_signs/board_mask"));
             TextureImage signMask = TextureImage.open(manager,
                     Supplementaries.res("items/hanging_signs/sign_board_mask"))) {

            Respriter respriter = Respriter.masked(boardTemplate, boardMask);

            ModRegistry.HANGING_SIGNS.forEach((wood, sign) -> {

                //if (wood.isVanilla()) continue;
                ResourceLocation textureRes = Supplementaries.res("items/hanging_signs/" + sign.getRegistryName().getPath());
                if (alreadyHasTextureAtLocation(manager, textureRes)) return;

                TextureImage newImage = null;
                if (wood.signItem != null) {
                    try (TextureImage vanillaSign = TextureImage.open(manager,
                            RPUtils.findFirstItemTextureLocation(manager, wood.signItem.get()))) {

                        Palette targetPalette = Palette.fromImage(vanillaSign, signMask);
                        newImage = respriter.recolor(targetPalette);

                        try (TextureImage scribbles = recolorFromVanilla(manager, vanillaSign,
                                Supplementaries.res("items/hanging_signs/sign_scribbles_mask"),
                                Supplementaries.res("items/hanging_signs/scribbles_template"))) {
                            newImage.applyOverlay(scribbles);
                        } catch (Exception ex) {
                            getLogger().error("Could not properly color Hanging Sign texture for {} : {}", sign, ex);
                        }

                        try (TextureImage stick = recolorFromVanilla(manager, vanillaSign,
                                Supplementaries.res("items/hanging_signs/sign_stick_mask"),
                                Supplementaries.res("items/hanging_signs/stick_template"))) {
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
                Supplementaries.res("items/sign_posts/template"))) {

            Respriter respriter = Respriter.of(template);

            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> {
                //if (wood.isVanilla()) continue;

                ResourceLocation textureRes = Supplementaries.res("items/sign_posts/" + sign.getRegistryName().getPath());

                if (alreadyHasTextureAtLocation(manager, textureRes)) return;

                TextureImage newImage = null;
                Item signItem = wood.getItemOfThis("sign");
                if (signItem != null) {
                    try (TextureImage vanillaSign = TextureImage.open(manager,
                            RPUtils.findFirstItemTextureLocation(manager, signItem));
                         TextureImage signMask = TextureImage.open(manager,
                                 Supplementaries.res("items/hanging_signs/sign_board_mask"))) {

                        List<Palette> targetPalette = Palette.fromAnimatedImage(vanillaSign, signMask);
                        newImage = respriter.recolor(targetPalette);

                        try (TextureImage scribbles = recolorFromVanilla(manager, vanillaSign,
                                Supplementaries.res("items/hanging_signs/sign_scribbles_mask"),
                                Supplementaries.res("items/sign_posts/scribbles_template"))) {
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
                var textureRes = Supplementaries.res("entity/sign_posts/" + sign.getRegistryName().getPath());
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
    public void addDynamicTranslations(DynamicLanguageManager.LanguageAccessor lang) {
        ModRegistry.HANGING_SIGNS.forEach((type, block) ->
                LangBuilder.addDynamicEntry(lang, "block.supplementaries.hanging_sign", type, block));
        ModRegistry.SIGN_POST_ITEMS.forEach((type, item) ->
                LangBuilder.addDynamicEntry(lang, "item.supplementaries.sign_post", type, item));
    }

}
