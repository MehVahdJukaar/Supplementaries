package net.mehvahdjukaar.supplementaries.dynamicpack;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.selene.resourcepack.*;
import net.mehvahdjukaar.selene.resourcepack.asset_generators.LangBuilder;
import net.mehvahdjukaar.selene.resourcepack.asset_generators.textures.Palette;
import net.mehvahdjukaar.selene.resourcepack.asset_generators.textures.Respriter;
import net.mehvahdjukaar.selene.resourcepack.asset_generators.textures.SpriteUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.WallLanternTexturesRegistry;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class ClientDynamicResourcesHandler extends RPAwareDynamicTextureProvider {

    public ClientDynamicResourcesHandler() {
        super(new DynamicTexturePack(Supplementaries.res("virtual_resourcepack")));
        this.dynamicPack.generateDebugResources = RegistryConfigs.reg.DEBUG_RESOURCES.get();
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
        //generate static resources

        LangBuilder langBuilder = new LangBuilder();

        //------hanging signs------
        {

            StaticResource hsBlockState = getResOrLog(manager,
                    ResType.BLOCKSTATES.getPath(Supplementaries.res("hanging_sign_oak")));
            StaticResource hsModel = getResOrLog(manager,
                    ResType.BLOCK_MODELS.getPath(Supplementaries.res("hanging_signs/hanging_sign_oak")));
            StaticResource hsLoader = getResOrLog(manager,
                    ResType.BLOCK_MODELS.getPath(Supplementaries.res("hanging_signs/loader_template")));
            StaticResource hsItemModel = getResOrLog(manager,
                    ResType.ITEM_MODELS.getPath(Supplementaries.res("hanging_sign_oak")));

            for (var e : ModRegistry.HANGING_SIGNS.entrySet()) {
                WoodType wood = e.getKey();
                if (true || !wood.isVanilla()) {
                    var v = e.getValue();

                    String id = wood.getVariantId("hanging_sign");
                    langBuilder.addEntry(v, wood.getNameForTranslation("hanging_sign"));

                    try {
                        dynamicPack.addSimilarJsonResource(hsBlockState, "hanging_sign_oak", id);
                    } catch (Exception ex) {
                        getLogger().error("Failed to generate Hanging Sign blockstate definition for {} : {}", v, ex);
                    }

                    try {
                        dynamicPack.addSimilarJsonResource(hsModel, "hanging_sign_oak", id);
                    } catch (Exception ex) {
                        getLogger().error("Failed to generate Hanging Sign block model for {} : {}", v, ex);
                    }

                    try {
                        dynamicPack.addSimilarJsonResource(hsItemModel, "hanging_sign_oak", id);
                    } catch (Exception ex) {
                        getLogger().error("Failed to generate Hanging Sign item model for {} : {}", v, ex);
                    }

                    try {
                        String logTexture;
                        try {
                            logTexture = RPUtils.findFirstBlockTextureLocation(manager, wood.logBlock, s -> !s.contains("top"));
                        } catch (Exception e1) {
                            logTexture = RPUtils.findFirstBlockTextureLocation(manager, wood.plankBlock, s -> true);
                            getLogger().error("Could not properly generate Hanging Sign model for {}. Falling back to planks texture : {}", v, e1);
                        }
                        addHangingSignLoaderModel(Objects.requireNonNull(hsLoader), id, logTexture);
                    } catch (Exception ex) {
                        getLogger().error("Failed to generate Hanging Sign loader model for {} : {}", v, ex);
                    }
                }
            }
        }


        //------sing posts-----
        {
            StaticResource spItemModel = getResOrLog(manager,
                    ResType.ITEM_MODELS.getPath(Supplementaries.res("sign_post_oak")));

            for (var e : ModRegistry.SIGN_POST_ITEMS.entrySet()) {
                WoodType wood = e.getKey();
                if (!wood.isVanilla() || true) {
                    var v = e.getValue();
                    langBuilder.addEntry(v, e.getKey().getNameForTranslation("sign_post"));

                    try {
                        dynamicPack.addSimilarJsonResource(spItemModel,
                                "sign_post_oak", wood.getVariantId("sign_post"));
                    } catch (Exception ex) {
                        getLogger().error("Failed to generate Sign Post item model for {} : {}", v, ex);
                    }
                }
            }
        }


        dynamicPack.addLang(Supplementaries.res("en_us"), langBuilder.build());
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

        WallLanternTexturesRegistry.onResourceReload(manager);

        //leather armor


        //hanging signs block textures
        try (NativeImage template = SpriteUtils.readImage(manager, Supplementaries.res(
                "textures/blocks/hanging_signs/hanging_sign_oak.png"));
             NativeImage mask = SpriteUtils.readImage(manager, Supplementaries.res(
                     "textures/blocks/hanging_signs/board_mask.png"))) {

            Palette palette = Palette.fromImage(template, mask);
            Respriter respriter = new Respriter(template, palette);

            for (var e : ModRegistry.HANGING_SIGNS.entrySet()) {
                WoodType wood = e.getKey();
                //if (wood.isVanilla()) continue;
                ResourceLocation textureRes = Supplementaries.res(
                        String.format("blocks/hanging_signs/%s", wood.getVariantId("hanging_sign")));
                if (alreadyHasTextureAtLocation(manager, textureRes)) continue;
                var v = e.getValue();
                try (NativeImage plankPalette = RPUtils.findFirstBlockTexture(manager, wood.plankBlock)) {

                    Palette targetPalette = SpriteUtils.extrapolateSignBlockPalette(plankPalette);
                    NativeImage newImage = respriter.recolorImage(targetPalette);

                    dynamicPack.addTexture(textureRes, newImage);
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Hanging Sign block texture for for {} : {}", v, ex);
                }

            }
        } catch (Exception ex) {
            getLogger().error("Could not generate any Hanging Sign block texture : ", ex);
        }

        //hanging sign item textures
        try (NativeImage boardTemplate = SpriteUtils.readImage(manager, Supplementaries.res(
                "textures/items/hanging_signs/template.png"));
             NativeImage boardMask = SpriteUtils.readImage(manager, Supplementaries.res(
                     "textures/items/hanging_signs/board_mask.png"))) {

            Palette palette = Palette.fromImage(boardTemplate, boardMask);
            Respriter respriter = new Respriter(boardTemplate, palette);

            for (var e : ModRegistry.HANGING_SIGNS.entrySet()) {
                WoodType wood = e.getKey();
                //if (wood.isVanilla()) continue;
                ResourceLocation textureRes = Supplementaries.res(String.format("items/hanging_signs/%s", wood.getVariantId("hanging_sign")));
                if (alreadyHasTextureAtLocation(manager, textureRes)) continue;
                var v = e.getValue();

                NativeImage newImage = null;
                if (wood.signItem != null) {
                    try (NativeImage vanillaSign = RPUtils.findFirstItemTexture(manager, wood.signItem.get());
                         NativeImage signMask = SpriteUtils.readImage(manager, Supplementaries.res(
                                 "textures/items/hanging_signs/sign_board_mask.png"))) {

                        Palette targetPalette = Palette.fromImage(vanillaSign, signMask);
                        newImage = respriter.recolorImage(targetPalette);

                        try (NativeImage scribbles = SpriteUtils.recolorFromVanilla(manager, vanillaSign,
                                Supplementaries.res("textures/items/hanging_signs/sign_scribbles_mask.png"),
                                Supplementaries.res("textures/items/hanging_signs/scribbles_template.png"));) {
                            SpriteUtils.mergeImages(newImage, scribbles);
                        } catch (Exception ex) {
                            getLogger().error("Could not properly color Hanging Sign texture for {} : {}", v, ex);
                        }

                        try (NativeImage stick = SpriteUtils.recolorFromVanilla(manager, vanillaSign,
                                Supplementaries.res("textures/items/hanging_signs/sign_stick_mask.png"),
                                Supplementaries.res("textures/items/hanging_signs/stick_template.png"))) {
                            SpriteUtils.mergeImages(newImage, stick);
                        } catch (Exception ex) {
                            getLogger().error("Could not properly color Hanging Sign item texture for {} : {}", v, ex);
                        }

                    } catch (Exception ex) {
                        //getLogger().error("Could not find sign texture for wood type {}. Using plank texture : {}", wood, ex);
                    }
                }
                //if it failed use plank one
                if (newImage == null) {
                    try (NativeImage plankPalette = RPUtils.findFirstBlockTexture(manager, wood.plankBlock)) {
                        Palette targetPalette = SpriteUtils.extrapolateWoodItemPalette(plankPalette);
                        newImage = respriter.recolorImage(targetPalette);
                    } catch (Exception ex) {
                        getLogger().error("Failed to generate Hanging Sign item texture for for {} : {}", v, ex);
                    }
                }
                if (newImage != null) {
                    dynamicPack.addTexture(textureRes, newImage);
                }
            }
        } catch (Exception ex) {
            getLogger().error("Could not generate any Hanging Sign item texture : ", ex);
        }

        //sign posts item textures
        try (NativeImage template = SpriteUtils.readImage(manager, Supplementaries.res(
                "textures/items/sign_posts/template.png"))) {

            Respriter respriter = new Respriter(template);

            for (var e : ModRegistry.SIGN_POST_ITEMS.entrySet()) {
                WoodType wood = e.getKey();
                //if (wood.isVanilla()) continue;
                ResourceLocation textureRes = Supplementaries.res(
                        String.format("items/sign_posts/%s", wood.getVariantId("sign_post")));
                if (alreadyHasTextureAtLocation(manager, textureRes)) continue;
                var v = e.getValue();

                NativeImage newImage = null;
                if (wood.signItem != null) {
                    try (NativeImage vanillaSign = RPUtils.findFirstItemTexture(manager, wood.signItem.get());
                         NativeImage signMask = SpriteUtils.readImage(manager, Supplementaries.res(
                                 "textures/items/hanging_signs/sign_board_mask.png"))) {

                        Palette targetPalette = Palette.fromImage(vanillaSign, signMask);
                        newImage = respriter.recolorImage(targetPalette);

                        try (NativeImage scribbles = SpriteUtils.recolorFromVanilla(manager, vanillaSign,
                                Supplementaries.res("textures/items/hanging_signs/sign_scribbles_mask.png"),
                                Supplementaries.res("textures/items/sign_posts/scribbles_template.png"));) {
                            SpriteUtils.mergeImages(newImage, scribbles);
                        } catch (Exception ex) {
                            getLogger().error("Could not properly color Sign Post item texture for {} : {}", v, ex);
                        }

                    } catch (Exception ex) {
                        //getLogger().error("Could not find sign texture for wood type {}. Using plank texture : {}", wood, ex);
                    }
                }
                //if it failed use plank one
                if (newImage == null) {
                    try (NativeImage plankPalette = RPUtils.findFirstBlockTexture(manager, wood.plankBlock)) {
                        Palette targetPalette = SpriteUtils.extrapolateWoodItemPalette(plankPalette);
                        newImage = respriter.recolorImage(targetPalette);

                    } catch (Exception ex) {
                        getLogger().error("Failed to generate Sign Post item texture for for {} : {}", v, ex);
                    }
                }
                if (newImage != null) {

                    dynamicPack.addTexture(textureRes, newImage);
                }
            }
        } catch (Exception ex) {
            getLogger().error("Could not generate any Sign Post item texture : ", ex);
        }

        //sign posts block textures
        try (NativeImage template = SpriteUtils.readImage(manager, Supplementaries.res(
                "textures/entity/sign_posts/sign_post_oak.png"))) {

            Respriter respriter = new Respriter(template);

            for (var e : ModRegistry.SIGN_POST_ITEMS.entrySet()) {
                WoodType wood = e.getKey();
                var textureRes = Supplementaries.res(String.format("entity/sign_posts/%s", wood.getVariantId("sign_post")));
                if (alreadyHasTextureAtLocation(manager, textureRes)) continue;
                //if (wood.isVanilla()) continue;
                var v = e.getValue();

                try (NativeImage plankPalette = RPUtils.findFirstBlockTexture(manager, wood.plankBlock)) {
                    NativeImage newImage = respriter.recolorImage(plankPalette, null);

                    dynamicPack.addTexture(textureRes, newImage);
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Sign Post block texture for for {} : {}", v, ex);
                }
            }
        } catch (Exception ex) {
            getLogger().error("Could not generate any Sign Post block texture : ", ex);
        }
    }


    //TODO: invert scribble color if sign is darker than them

}
