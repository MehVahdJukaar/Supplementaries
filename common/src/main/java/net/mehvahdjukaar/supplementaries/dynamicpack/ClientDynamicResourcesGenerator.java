package net.mehvahdjukaar.supplementaries.dynamicpack;

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
import net.mehvahdjukaar.moonlight.api.resources.textures.*;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.BlackboardManager;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.client.renderers.SlimedRenderTypes;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
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

    //-------------resource pack dependant textures-------------

    @Override
    public void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
        //generateTagTranslations();

        //need this here for reasons I forgot
        SlimedRenderTypes.clear();
        BlackboardManager.closeAll();

        executor.accept((manager, sink) -> {
            GlobeManager.refreshColorsAndTextures(manager);
            ColorHelper.refreshBubbleColors(manager);
            addEndermanHead(manager, sink);
            addRopeArrowModel(manager, sink);
            addTatteredBook(manager, sink);
            addSignPostAssets(manager, sink);
        });
    }

    private void addSignPostAssets(ResourceManager manager, ResourceSink sink) {
        {
            StaticResource spItemModel = StaticResource.getOrThrow(manager,
                    ResType.ITEM_MODELS.getPath(Supplementaries.res("sign_post_oak")));
            StaticResource spBlockModel = StaticResource.getOrThrow(manager,
                    ResType.BLOCK_MODELS.getPath(Supplementaries.res("sign_posts/sign_post_oak")));
            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> {
                String id = Utils.getID(sign).getPath();
                //langBuilder.addEntry(sign, wood.getVariantReadableName("way_sign"));

                sink.addSimilarJsonResource(manager, spItemModel, "sign_post_oak", id);
                sink.addSimilarJsonResource(manager, spBlockModel, "sign_post_oak", id);
            });
        }

        //sign posts item textures
        try (TextureImage template = TextureImage.open(manager,
                Supplementaries.res("item/sign_posts/template"))) {

            Respriter respriter = Respriter.of(template);

            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> {
                ResourceLocation textureRes = Supplementaries.res("item/sign_posts/" + Utils.getID(sign).getPath());
                if (sink.alreadyHasTextureAtLocation(manager, textureRes)) return;

                Item signItem = wood.getItemOfThis("sign");
                if (signItem != null) {
                    try (TextureImage vanillaSign = TextureImage.open(manager,
                            RPUtils.findFirstItemTextureLocation(manager, signItem));
                         TextureImage signMask = TextureImage.open(manager,
                                 Supplementaries.res("item/sign_posts/sign_board_mask"))) {

                        List<Palette> targetPalette = Palette.fromAnimatedImage(vanillaSign, signMask);
                        try (TextureImage newImage = respriter.recolor(targetPalette)) {
                            try (TextureImage scribbles = recolorFromVanilla(manager, vanillaSign,
                                    Supplementaries.res("item/sign_posts/sign_scribbles_mask"),
                                    Supplementaries.res("item/sign_posts/scribbles_template"))) {
                                TextureOps.applyOverlay(newImage, scribbles);
                            } catch (Exception ex) {
                                getLogger().error("Could not properly color Sign Post item texture for {} : {}", sign, ex);
                            }
                            sink.addTexture(textureRes, newImage);
                        }

                    } catch (Exception ex) {
                        getLogger().error("Could not find sign texture for wood explosionType {}. Using plank texture : {}", wood, ex);
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
                        getLogger().error("Failed to generate Sign Post item texture for for {} : {}", sign, ex);
                    }
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
                var textureRes = Supplementaries.res("block/sign_posts/" + Utils.getID(sign).getPath());
                if (sink.alreadyHasTextureAtLocation(manager, textureRes)) return;

                try (TextureImage plankTexture = TextureImage.open(manager,
                        RPUtils.findFirstBlockTextureLocation(manager, wood.planks))) {
                    Palette palette = Palette.fromImage(plankTexture);

                    try (TextureImage newImage = respriter.recolor(palette)) {
                        sink.addTexture(textureRes, newImage);
                    }
                } catch (Exception ex) {
                    getLogger().error("Failed to generate Sign Post block texture for for {} : {}", sign, ex);
                }
            });
        } catch (Exception ex) {
            getLogger().error("Could not generate any Sign Post block texture : ", ex);
        }
    }

    private void addTatteredBook(ResourceManager manager, ResourceSink sink) {
        if (CommonConfigs.Tools.ANTIQUE_INK_ENABLED.get()) {
            sink.appendModelOverride(manager, new ResourceLocation("written_book"), e -> {
                e.add(new ItemOverride(new ResourceLocation("item/written_book_tattered"),
                        List.of(new ItemOverride.Predicate(Supplementaries.res("antique_ink"), 1))));
            });
            sink.appendModelOverride(manager, new ResourceLocation("filled_map"), e -> {
                e.add(new ItemOverride(new ResourceLocation("item/antique_map"),
                        List.of(new ItemOverride.Predicate(Supplementaries.res("antique_ink"), 1))));
            });
        }
    }

    private void addRopeArrowModel(ResourceManager manager, ResourceSink sink) {
        if (CommonConfigs.Tools.ROPE_ARROW_ENABLED.get()) {
            sink.appendModelOverride(manager, new ResourceLocation("crossbow"), e -> {
                e.add(new ItemOverride(new ResourceLocation("item/crossbow_rope_arrow"),
                        List.of(new ItemOverride.Predicate(new ResourceLocation("charged"), 1f),
                                new ItemOverride.Predicate(Supplementaries.res("rope_arrow"), 1f))));
            });
        }
    }

    private void addEndermanHead(ResourceManager manager, ResourceSink sink) {
        if (CommonConfigs.Redstone.ENDERMAN_HEAD_ENABLED.get() && ClientConfigs.Tweaks.ENDERMAN_HEAD_VANILLA.get()) {
            try (var text = TextureImage.open(manager, new ResourceLocation("entity/enderman/enderman"));
                 var eyeText = TextureImage.open(manager, new ResourceLocation("entity/enderman/enderman_eyes"))) {
                sink.addTexture(Supplementaries.res("entity/enderman_head"), text, false);
                sink.addTexture(Supplementaries.res("entity/enderman_head_eyes"), eyeText, false);
            } catch (Exception ignored) {
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
        ModRegistry.SIGN_POST_ITEMS.forEach((type, item) ->
                LangBuilder.addDynamicEntry(lang, "item.supplementaries.sign_post", type, item));
        if (MiscUtils.FESTIVITY.isAprilsFool()) {
            lang.addEntry("block.suppsquared.metal_frame", "Galvanized Square Steel Frame");
            lang.addEntry("block.suppsquared.metal_brace", "Galvanized Square Steel Brace");
            lang.addEntry("block.suppsquared.metal_cross_brace", "Galvanized Square Steel Cross Brace");
        }

        String bambooSpikes = lang.getEntry("item.supplementaries.bamboo_spikes_tipped.effect");
        if (bambooSpikes == null) return;
        for (var p : BuiltInRegistries.POTION) {
            String key = p.getName("item.supplementaries.bamboo_spikes_tipped.effect.");
            String arrowName = lang.getEntry(p.getName("item.minecraft.tipped_arrow.effect."));
            if (arrowName == null) {
                lang.addEntry(key, String.format(bambooSpikes, LangBuilder.getReadableName(Utils.getID(p).getPath())));
            } else lang.addEntry(key, String.format(bambooSpikes,
                    LangBuilder.getReadableName(arrowName.toLowerCase(Locale.ROOT)
                            .replace("arrow of ", ""))));
        }

    }


}
