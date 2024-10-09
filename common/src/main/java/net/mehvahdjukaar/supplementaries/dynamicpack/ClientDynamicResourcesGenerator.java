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
import net.mehvahdjukaar.moonlight.api.resources.textures.Palette;
import net.mehvahdjukaar.moonlight.api.resources.textures.Respriter;
import net.mehvahdjukaar.moonlight.api.resources.textures.SpriteUtils;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.client.renderers.SlimedRenderTypes;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.ColoredMapHandler;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;


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
        GlobeManager.refreshColorsAndTextures(manager);
        ColorHelper.refreshBubbleColors(manager);
        ColoredMapHandler.onResourceReload();
        SlimedRenderTypes.clear();

        if (CommonConfigs.Redstone.ENDERMAN_HEAD_ENABLED.get()) {
            try (var text = TextureImage.open(manager, ResourceLocation.withDefaultNamespace("entity/enderman/enderman"));
                 var eyeText = TextureImage.open(manager, ResourceLocation.withDefaultNamespace("entity/enderman/enderman_eyes"))) {
                dynamicPack.addAndCloseTexture(Supplementaries.res("entity/enderman_head"), text, false);
                dynamicPack.addAndCloseTexture(Supplementaries.res("entity/enderman_head_eyes"), eyeText, false);
            } catch (Exception ignored) {
            }
        }
        if (CommonConfigs.Tools.ROPE_ARROW_ENABLED.get()) {
            RPUtils.appendModelOverride(manager, this.dynamicPack, ResourceLocation.withDefaultNamespace("crossbow"), e -> {
                e.add(new ItemOverride(ResourceLocation.withDefaultNamespace("item/crossbow_rope_arrow"),
                        List.of(new ItemOverride.Predicate(ResourceLocation.withDefaultNamespace("charged"), 1f),
                                new ItemOverride.Predicate(Supplementaries.res("rope_arrow"), 1f))));
            });
        }

        if (CommonConfigs.Tools.ANTIQUE_INK_ENABLED.get()) {
            RPUtils.appendModelOverride(manager, this.dynamicPack, ResourceLocation.withDefaultNamespace("written_book"), e -> {
                e.add(new ItemOverride(ResourceLocation.withDefaultNamespace("item/written_book_tattered"),
                        List.of(new ItemOverride.Predicate(Supplementaries.res("antique_ink"), 1))));
            });
            RPUtils.appendModelOverride(manager, this.dynamicPack, ResourceLocation.withDefaultNamespace("filled_map"), e -> {
                e.add(new ItemOverride(ResourceLocation.withDefaultNamespace("item/antique_map"),
                        List.of(new ItemOverride.Predicate(Supplementaries.res("antique_ink"), 1))));
            });
        }

        RPUtils.appendModelOverride(manager, this.dynamicPack, Supplementaries.res("globe"), e -> {
            int i = 0;
            for (var text : GlobeManager.TEXTURES) {
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
                        //getLogger().error("Could not find sign texture for wood explosionType {}. Using plank texture : {}", wood, ex);
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
    }


    /**
     * helper method.
     * recolors the template image with the color grabbed from the given image restrained to its mask, if possible
     */
    @Nullable
    public static TextureImage recolorFromVanilla(ResourceManager manager, TextureImage
            vanillaTexture, ResourceLocation vanillaMask,
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
