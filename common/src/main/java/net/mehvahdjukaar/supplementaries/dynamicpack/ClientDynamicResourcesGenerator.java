package net.mehvahdjukaar.supplementaries.dynamicpack;

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
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.resources.textures.Palette;
import net.mehvahdjukaar.moonlight.api.resources.textures.Respriter;
import net.mehvahdjukaar.moonlight.api.resources.textures.SpriteUtils;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.BlackboardTextureManager;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.client.renderers.SlimedRenderTypes;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.ColoredMapHandler;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;


public class ClientDynamicResourcesGenerator extends DynClientResourcesGenerator {

    public static final ClientDynamicResourcesGenerator INSTANCE = new ClientDynamicResourcesGenerator();

    public ClientDynamicResourcesGenerator() {
        super(new DynamicTexturePack(Supplementaries.res("generated_pack")));
        this.dynamicPack.setGenerateDebugResources(PlatHelper.isDev() || CommonConfigs.General.DEBUG_RESOURCES.get());
    }

    @Override
    public Collection<String> additionalNamespaces() {
        return List.of("minecraft");
    }

    @Override
    public Logger getLogger() {
        return Supplementaries.LOGGER;
    }

    //-------------resource pack dependant textures-------------

    @Override
    public void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
        //generateTagTranslations();

        //need this here for reasons I forgot
        ColoredMapHandler.clearCache();
        SlimedRenderTypes.clear();
        BlackboardTextureManager.closeAll();

        executor.accept((manager, sink) -> {
            GlobeManager.refreshColorsAndTextures(manager);
            ColorHelper.refreshBubbleColors(manager);
            addEndermanHead(manager, sink);
            addRopeArrowModel(manager, sink);
            addTatteredBook(manager, sink);
            addGlobeItemModels(manager, sink);
            addSignPostAssets(manager, sink);
        });
    }

    private void addSignPostAssets(ResourceManager manager, ResourceSink sink) {
        {
            StaticResource spItemModel = StaticResource.getOrLog(manager,
                    ResType.ITEM_MODELS.getPath(Supplementaries.res("way_sign_oak")));
            StaticResource spBlockModel = StaticResource.getOrLog(manager,
                    ResType.BLOCK_MODELS.getPath(Supplementaries.res("way_signs/way_sign_oak")));
            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> {
                String id = Utils.getID(sign).getPath();
                //langBuilder.addEntry(sign, wood.getVariantReadableName("way_sign"));

                try {
                    sink.addSimilarJsonResource(manager, spItemModel, "way_sign_oak", id);
                    sink.addSimilarJsonResource(manager, spBlockModel, "way_sign_oak", id);
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Sign Post item model for {} : {}", sign, ex);
                }
            });
        }

        //sign posts item textures
        try (TextureImage template = TextureImage.open(manager,
                Supplementaries.res("item/way_signs/template"))) {

            Respriter respriter = Respriter.of(template);

            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> {
                ResourceLocation textureRes = Supplementaries.res("item/way_signs/" + Utils.getID(sign).getPath());
                if (sink.alreadyHasTextureAtLocation(manager, textureRes)) return;

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
                                Supplementaries.res("item/way_signs/sign_scribbles_mask"),
                                Supplementaries.res("item/way_signs/scribbles_template"))) {
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
                    sink.addAndCloseTexture(textureRes, newImage);
                }
            });
        } catch (Exception ex) {
            getLogger().error("Could not generate any Sign Post item texture : ", ex);
        }

        //sign posts block textures
        try (TextureImage template = TextureImage.open(manager,
                Supplementaries.res("block/way_signs/way_sign_oak"))) {

            Respriter respriter = Respriter.of(template);

            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> {
                var textureRes = Supplementaries.res("block/way_signs/" + Utils.getID(sign).getPath());
                if (sink.alreadyHasTextureAtLocation(manager, textureRes)) return;

                try (TextureImage plankTexture = TextureImage.open(manager,
                        RPUtils.findFirstBlockTextureLocation(manager, wood.planks))) {
                    Palette palette = Palette.fromImage(plankTexture);

                    TextureImage newImage = respriter.recolor(palette);

                    sink.addAndCloseTexture(textureRes, newImage);
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Sign Post block texture for for {} : {}", sign, ex);
                }
            });
        } catch (Exception ex) {
            getLogger().error("Could not generate any Sign Post block texture : ", ex);
        }
    }

    private void addGlobeItemModels(ResourceManager manager, ResourceSink sink) {
        RPUtils.appendModelOverride(manager, sink, Supplementaries.res("globe"), e -> {
            int i = 0;
            for (var text : GlobeManager.TEXTURES) {
                String name = text.getPath().split("/")[3].split("\\.")[0];
                e.add(new ItemOverride(Supplementaries.res("item/" + name),
                        List.of(new ItemOverride.Predicate(Supplementaries.res("type"), i))));
                i++;
                sink.addItemModel(Supplementaries.res(name), JsonParser.parseString(
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
    }

    private void addTatteredBook(ResourceManager manager, ResourceSink sink) {
        if (CommonConfigs.Tools.ANTIQUE_INK_ENABLED.get()) {
            RPUtils.appendModelOverride(manager, sink, new ResourceLocation("written_book"), e -> {
                e.add(new ItemOverride(new ResourceLocation("item/written_book_tattered"),
                        List.of(new ItemOverride.Predicate(Supplementaries.res("antique_ink"), 1))));
            });
            RPUtils.appendModelOverride(manager, sink, new ResourceLocation("filled_map"), e -> {
                e.add(new ItemOverride(new ResourceLocation("item/antique_map"),
                        List.of(new ItemOverride.Predicate(Supplementaries.res("antique_ink"), 1))));
            });
        }
    }

    private void addRopeArrowModel(ResourceManager manager, ResourceSink sink) {
        if (CommonConfigs.Tools.ROPE_ARROW_ENABLED.get()) {
            RPUtils.appendModelOverride(manager, sink, new ResourceLocation("crossbow"), e -> {
                e.add(new ItemOverride(new ResourceLocation("item/crossbow_rope_arrow"),
                        List.of(new ItemOverride.Predicate(new ResourceLocation("charged"), 1f),
                                new ItemOverride.Predicate(Supplementaries.res("rope_arrow"), 1f))));
            });
        }
    }

    private void addEndermanHead(ResourceManager manager, ResourceSink sink) {
        if (CommonConfigs.Redstone.ENDERMAN_HEAD_ENABLED.get()) {
            try (var text = TextureImage.open(manager, new ResourceLocation("entity/enderman/enderman"));
                 var eyeText = TextureImage.open(manager, new ResourceLocation("entity/enderman/enderman_eyes"))) {
                sink.addAndCloseTexture(Supplementaries.res("entity/enderman_head"), text, false);
                sink.addAndCloseTexture(Supplementaries.res("entity/enderman_head_eyes"), eyeText, false);
            } catch (Exception ignored) {
            }
        }
    }

    private static void generateTagTranslations() {
        JsonObject jo = new JsonObject();
        for (var e : ServerDynamicResourcesGenerator.R.entrySet()) {
            ResourceLocation id = e.getKey();
            if (id.getNamespace().equals("supplementaries")) {
                String path = id.getPath();
                path = path.replace("tags/", "").replace(".json", "");
                String tr = path.substring(path.lastIndexOf("/") + 1);
                jo.addProperty("supplementaries:" + path, LangBuilder.getReadableName(tr));
            }
        }
    }


    /**
     * helper method.
     * recolors the template image with the color grabbed from the given image restrained to its mask, if possible
     */
    @Nullable
    private static TextureImage recolorFromVanilla(ResourceManager manager, TextureImage
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
                LangBuilder.addDynamicEntry(lang, "item.supplementaries.way_sign", type, item));

        String bambooSpikes = lang.getEntry("item.supplementaries.bamboo_spikes_tipped.effect");
        if (bambooSpikes == null) return;
        for (var p : BuiltInRegistries.POTION) {
            Optional<Holder<Potion>> holder = Optional.of(BuiltInRegistries.POTION.wrapAsHolder(p));
            String key = Potion.getName(holder, "item.supplementaries.bamboo_spikes_tipped.effect.");
            String arrowName = lang.getEntry(Potion.getName(holder, "item.minecraft.tipped_arrow.effect."));
            if (arrowName == null) {
                lang.addEntry(key, String.format(bambooSpikes, LangBuilder.getReadableName(Utils.getID(p).getPath())));
            } else lang.addEntry(key, String.format(bambooSpikes,
                    LangBuilder.getReadableName(arrowName.toLowerCase(Locale.ROOT)
                            .replace("arrow of ", ""))));
        }


    }


}
