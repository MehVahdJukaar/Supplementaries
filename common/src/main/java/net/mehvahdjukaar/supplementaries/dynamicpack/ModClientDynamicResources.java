package net.mehvahdjukaar.supplementaries.dynamicpack;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.misc.IProgressTracker;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicClientResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.resources.textures.*;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.BlackboardTextureManager;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.client.renderers.SlimedRenderTypes;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.common.items.CannonBoatItem;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.ColoredMapHandler;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;


public class ModClientDynamicResources extends DynamicClientResourceProvider {

    public ModClientDynamicResources() {
        super(Supplementaries.res("generated_pack"), ClientConfigs.General.DYNAMIC_ASSETS_GEN_MODE.get().toStrategy());
    }

    @Override
    protected Collection<String> gatherSupportedNamespaces() {
        return List.of("minecraft");
    }
    //-------------resource pack dependant textures-------------


    @Override
    public void reload(ResourceManager manager, IProgressTracker reporter) {
        //need this here for reasons I forgot
        ColoredMapHandler.clearCache();
        SlimedRenderTypes.clear();
        BlackboardTextureManager.closeAll();

        super.reload(manager, reporter);

        GlobeManager.refreshColorsAndTextures(manager);
        ColorHelper.refreshBubbleColors(manager);
    }

    @Override
    public void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
        //generateTagTranslations();
        executor.accept((manager, sink) -> {

            addEndermanHead(manager, sink);
            addRopeArrowModel(manager, sink);
            addTatteredBook(manager, sink);
            addMissingFlagPatterns(manager, sink);
            //addGlobeItemModels(manager, sink);

            MojangNeedsToAddMoreCopper.run(manager, sink);
        });

        executor.accept(this::addSignPostAssets);
        executor.accept(this::generateBoatTextures);
    }

    private void addMissingFlagPatterns(ResourceManager manager, ResourceSink sink) {

        var textures = manager.listResources("textures/entity/banner", resourceLocation ->
                resourceLocation.getPath().endsWith(".png") && !resourceLocation.getPath()
                        .replace("textures/entity/banner/", "").contains("/"));
        TextureCollager collager = TextureCollager.builder(64, 64, 32, 16)
                .copyFrom(3, 13, 16, 16)
                .to(6, 2, 12, 12)
                .bilinearScaling()
                .build();
        for (ResourceLocation id : textures.keySet()) {
            String namespace = id.getNamespace();
            if (namespace.equals("minecraft")) continue;
            String name = id.getPath().replace("textures/entity/banner/", "");
            ResourceLocation newPath = Supplementaries.res("entity/banner/flags/"
                    + id.getNamespace() + "/" + name.substring(0, name.length() - 4));
            sink.addTextureIfNotPresent(manager, newPath, () -> {
                try (TextureImage oldText = TextureImage.open(manager, id.withPath(p -> p.replace("textures/", "")))) {
                    TextureImage newImage = TextureOps.createScaled(oldText, 0.5f, 0.25f);
                    newImage.clear();
                    collager.apply(oldText, newImage);
                    return newImage;

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void addSignPostAssets(ResourceManager manager, ResourceSink sink) {
        {
            StaticResource spItemModel = StaticResource.getOrThrow(manager,
                    ResType.ITEM_MODELS.getPath(Supplementaries.res("way_sign_oak")));
            StaticResource spBlockModel = StaticResource.getOrThrow(manager,
                    ResType.BLOCK_MODELS.getPath(Supplementaries.res("way_signs/way_sign_oak")));
            ModRegistry.WAY_SIGN_ITEMS.forEach((wood, sign) -> {
                String id = Utils.getID(sign).getPath();
                //langBuilder.addEntry(sign, wood.getVariantReadableName("way_sign"));

                sink.addSimilarJsonResource(manager, spItemModel, "way_sign_oak", id);
                sink.addSimilarJsonResource(manager, spBlockModel, "way_sign_oak", id);
            });
        }

        //sign posts item textures
        try (TextureImage template = TextureImage.open(manager,
                Supplementaries.res("item/way_signs/template"))) {

            Respriter respriter = Respriter.of(template);

            ModRegistry.WAY_SIGN_ITEMS.forEach((wood, sign) -> {
                ResourceLocation textureRes = Supplementaries.res("item/way_signs/" + Utils.getID(sign).getPath());
                if (sink.alreadyHasTextureAtLocation(manager, textureRes)) return;

                Item signItem = wood.getItemOfThis("sign");
                if (signItem != null) {
                    try (TextureImage vanillaSign = TextureImage.open(manager,
                            RPUtils.findFirstItemTextureLocation(manager, signItem));
                         TextureImage signMask = TextureImage.open(manager,
                                 Supplementaries.res("item/way_signs/sign_board_mask"))) {

                        List<Palette> targetPalette = Palette.fromAnimatedImage(vanillaSign, signMask);
                        try (TextureImage newImage = respriter.recolor(targetPalette)) {
                            try (TextureImage scribbles = recolorFromVanilla(manager, vanillaSign,
                                    Supplementaries.res("item/way_signs/sign_scribbles_mask"),
                                    Supplementaries.res("item/way_signs/scribbles_template"))) {
                                TextureOps.applyOverlay(newImage, scribbles);
                            } catch (Exception ex) {
                                Supplementaries.LOGGER.error("Could not properly color Sign Post item texture for {} : {}", sign, ex);
                            }
                            sink.addTexture(textureRes, newImage);
                        }

                    } catch (Exception ex) {
                        Supplementaries.LOGGER.error("Could not find sign texture for wood explosionType {}. Using plank texture : {}", wood, ex);
                    }
                } else {
                    //if it failed use plank one
                    try (TextureImage plankPalette = TextureImage.open(manager,
                            RPUtils.findFirstBlockTextureLocation(manager, wood.planks))) {
                        Palette targetPalette = SpriteUtils.extrapolateWoodItemPalette(plankPalette);
                        try (TextureImage newImage = respriter.recolor(targetPalette)) {
                            sink.addTexture(textureRes, newImage);
                        }
                    } catch (Exception ex) {
                        Supplementaries.LOGGER.error("Failed to generate Sign Post item texture for for {} : {}", sign, ex);
                    }
                }
            });
        } catch (Exception ex) {
            Supplementaries.LOGGER.error("Could not generate any Sign Post item texture : ", ex);
        }

        //sign posts block textures
        try (TextureImage template = TextureImage.open(manager,
                Supplementaries.res("block/way_signs/way_sign_oak"))) {

            Respriter respriter = Respriter.of(template);

            ModRegistry.WAY_SIGN_ITEMS.forEach((wood, sign) -> {
                var textureRes = Supplementaries.res("block/way_signs/" + Utils.getID(sign).getPath());
                if (sink.alreadyHasTextureAtLocation(manager, textureRes)) return;

                try (TextureImage plankTexture = TextureImage.open(manager,
                        RPUtils.findFirstBlockTextureLocation(manager, wood.planks))) {
                    Palette palette = Palette.fromImage(plankTexture);

                    try (TextureImage newImage = respriter.recolor(palette)) {
                        sink.addTexture(textureRes, newImage);
                    }
                } catch (Exception ex) {
                    Supplementaries.LOGGER.error("Failed to generate Way Sign block texture for for {} : {}", sign, ex);
                }
            });
        } catch (Exception ex) {
            Supplementaries.LOGGER.error("Could not generate any Way Sign block texture : ", ex);
        }
    }

    /*
    private void addGlobeItemModels(ResourceManager manager, ResourceSink sink) {
        sink.appendModelOverride(manager, sink, Supplementaries.res("globe"), e -> {
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
    }*/

    private void addTatteredBook(ResourceManager manager, ResourceSink sink) {
        if (CommonConfigs.Tools.ANTIQUE_INK_ENABLED.get()) {
            sink.appendModelOverride(manager, ResourceLocation.withDefaultNamespace("written_book"), e -> {
                e.add(new ItemOverride(ResourceLocation.withDefaultNamespace("item/written_book_tattered"),
                        List.of(new ItemOverride.Predicate(Supplementaries.res("antique_ink"), 1))));
            });
            sink.appendModelOverride(manager, ResourceLocation.withDefaultNamespace("filled_map"), e -> {
                e.add(new ItemOverride(ResourceLocation.withDefaultNamespace("item/antique_map"),
                        List.of(new ItemOverride.Predicate(Supplementaries.res("antique_ink"), 1))));
            });
        }
    }

    private void addRopeArrowModel(ResourceManager manager, ResourceSink sink) {
        if (CommonConfigs.Tools.ROPE_ARROW_ENABLED.get()) {
            sink.appendModelOverride(manager, ResourceLocation.withDefaultNamespace("crossbow"), e -> {
                e.add(new ItemOverride(ResourceLocation.withDefaultNamespace("item/crossbow_rope_arrow"),
                        List.of(new ItemOverride.Predicate(ResourceLocation.withDefaultNamespace("charged"), 1f),
                                new ItemOverride.Predicate(Supplementaries.res("rope_arrow"), 1f))));
            });
        }
    }

    private void addEndermanHead(ResourceManager manager, ResourceSink sink) {
        if (CommonConfigs.Redstone.ENDERMAN_HEAD_ENABLED.get() && ClientConfigs.Tweaks.ENDERMAN_HEAD_VANILLA.get()) {
            try (TextureImage text = TextureImage.open(manager, ResourceLocation.withDefaultNamespace("entity/enderman/enderman"));
                 TextureImage eyeText = TextureImage.open(manager, ResourceLocation.withDefaultNamespace("entity/enderman/enderman_eyes"))) {
                sink.addTexture(Supplementaries.res("entity/enderman_head"), text);
                sink.addTexture(Supplementaries.res("entity/enderman_head_eyes"), eyeText);
            } catch (Exception ignored) {
            }
        }
    }

    public void generateBoatTextures(ResourceManager manager, ResourceSink sink) {
        StaticResource itemModel = StaticResource.getOrLog(manager,
                ResType.ITEM_MODELS.getPath(Supplementaries.res("cannon_boat_oak")));

        ModRegistry.CANNON_BOAT_ITEMS.forEach((wood, sled) -> {
            try {
                sink.addSimilarJsonResource(manager, itemModel, "cannon_boat_oak", wood.getVariantId("cannon_boat"));
            } catch (Exception ex) {
                Supplementaries.LOGGER.error("Failed to generate Cannon Boat item model for {} : {}", sled, ex);
            }
        });

        //entity textures
        try (TextureImage template = TextureImage.open(manager, ResourceLocation.withDefaultNamespace("entity/boat/oak"))) {

            Respriter respriter = Respriter.of(template);

            //unfortunately theres no programmatic way to get all boat textures of a given boat. also because entities might be for multiple wod types. we can only generate them from scratc
            ModRegistry.CANNON_BOAT_ITEMS.forEach((wood, sled) -> {
                ResourceLocation textureRes = Supplementaries.res("entity/cannon_boat/" + wood.getTexturePath());

                sink.addTextureIfNotPresent(manager, textureRes, () -> {
                    try (TextureImage plankTexture = TextureImage.open(manager,
                            RPUtils.findFirstBlockTextureLocation(manager, wood.planks))) {
                        //Palette targetPalette = SpriteUtils.extrapolateWoodItemPalette(plankTexture);
                        Palette targetPalette = Palette.fromImage(plankTexture);
                        //TextureImage newImage = respriter.recolorWithAnimationOf(plankTexture);
                        return respriter.recolor(targetPalette);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });


            });
        } catch (Exception ex) {
            Supplementaries.LOGGER.error("Could not generate any sled entity texture : ", ex);
        }

        //item textures
        try (TextureImage template = TextureImage.open(manager, Supplementaries.res("item/cannon_boat/cannon_boat_oak"));
             TextureImage boatMask = TextureImage.open(manager, Supplementaries.res("item/cannon_boat/mask"))) {

            Palette palette = Palette.fromImage(template, boatMask);
            Respriter respriter = Respriter.ofPalette(template, palette);

            ModRegistry.CANNON_BOAT_ITEMS.forEach((wood, boat) -> {
                ResourceLocation textureRes = Supplementaries.res("item/cannon_boat/" + Utils.getID(boat).getPath());
                sink.addTextureIfNotPresent(manager, textureRes, () ->
                        createBoatTexture(manager, wood, boat, respriter));
            });
        } catch (Exception ex) {
            Supplementaries.LOGGER.error("Could not generate any Cannon Boat item texture : ", ex);
        }
    }

    private @Nullable TextureImage createBoatTexture(ResourceManager manager, WoodType wood, CannonBoatItem cannonBoat, Respriter respriter) {
        TextureImage newImage = null;
        Item boat = wood.getItemOfThis("boat");
        if (boat != null) {
            try (TextureImage vanillaBoat = TextureImage.open(manager,
                    RPUtils.findFirstItemTextureLocation(manager, boat))) {

                Palette targetPalette = Palette.fromImage(vanillaBoat);
                newImage = respriter.recolor(targetPalette);

            } catch (Exception ex) {
                Supplementaries.LOGGER.warn("Could not find boat texture for wood type {}. Using plank texture : {}", wood, ex);
            }
        }
        //if it failed use plank one
        if (newImage == null) {
            try (TextureImage plankPalette = TextureImage.open(manager,
                    RPUtils.findFirstBlockTextureLocation(manager, wood.planks))) {
                Palette targetPalette = SpriteUtils.extrapolateWoodItemPalette(plankPalette);
                newImage = respriter.recolor(targetPalette);

            } catch (Exception ex) {
                Supplementaries.LOGGER.error("Failed to generate Cannon Boat item texture for for {} : {}", cannonBoat, ex);
            }
        }
        return newImage;
    }

    private static void generateTagTranslations() {
        JsonObject jo = new JsonObject();
        for (var e : ModServerDynamicResources.TAG_TRANSLATION_HACK.entrySet()) {
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
    private static TextureImage recolorFromVanilla(ResourceManager manager,
                                                   TextureImage vanillaTexture,
                                                   ResourceLocation vanillaMask,
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
        MojangNeedsToAddMoreCopper.runTranslations(lang);
        ModRegistry.WAY_SIGN_ITEMS.forEach((type, item) ->
                LangBuilder.addDynamicEntry(lang, "item.supplementaries.way_sign", type, item));
        ModRegistry.CANNON_BOAT_ITEMS.forEach((type, item) ->
                LangBuilder.addDynamicEntry(lang, "item.supplementaries.cannon_boat", type, item));

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
