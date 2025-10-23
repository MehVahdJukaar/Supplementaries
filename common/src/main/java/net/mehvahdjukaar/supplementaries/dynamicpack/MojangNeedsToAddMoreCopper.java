package net.mehvahdjukaar.supplementaries.dynamicpack;

import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.resources.textures.Palette;
import net.mehvahdjukaar.moonlight.api.resources.textures.Respriter;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MojangNeedsToAddMoreCopper {


    public static void run(ResourceManager manager, ResourceSink sink) {
        if (!MiscUtils.FESTIVITY.isAprilsFool()) return;

        try (TextureImage c0 = TextureImage.open(manager, RPUtils.findFirstBlockTextureLocation(manager, Blocks.COPPER_BLOCK));
             TextureImage c1 = TextureImage.open(manager, RPUtils.findFirstBlockTextureLocation(manager, Blocks.EXPOSED_COPPER));
             TextureImage c3 = TextureImage.open(manager, RPUtils.findFirstBlockTextureLocation(manager, Blocks.OXIDIZED_COPPER));
             TextureImage c2 = TextureImage.open(manager, RPUtils.findFirstBlockTextureLocation(manager, Blocks.WEATHERED_COPPER))
        ) {
            List<List<Palette>> textures = List.of(
                    Palette.fromAnimatedImage(c0),
                    Palette.fromAnimatedImage(c1),
                    Palette.fromAnimatedImage(c2),
                    Palette.fromAnimatedImage(c3)
            );
            Random random = new Random();

            var res = manager.listResources("textures/block", t -> t.getPath().endsWith(".png"));
            for (var r : res.entrySet()) {
                ResourceLocation relPat = r.getKey().withPath(p -> p.replace("textures/", "").replace(".png", ""));
                try (TextureImage toRecolor = TextureImage.open(manager, relPat)) {
                    Respriter resp = Respriter.of(toRecolor);
                    try(TextureImage recolored = resp.recolor(textures.get(random.nextInt(textures.size())))) {
                        sink.addTexture(relPat, recolored);
                    }
                } catch (Exception ignored) {
                }
            }


            var res3 = manager.listResources("textures/entity", t -> t.getPath().endsWith(".png"));
            for (var r : res3.entrySet()) {
                ResourceLocation relPat = r.getKey().withPath(p -> p.replace("textures/", "").replace(".png", ""));
                try (TextureImage toRecolor = TextureImage.open(manager, relPat)) {
                    Respriter resp = Respriter.of(toRecolor);
                    var recolored = resp.recolor(textures.get(random.nextInt(textures.size())));
                    sink.addAndCloseTexture(relPat, recolored);
                } catch (Exception e) {

                }
            }

            var res4 = manager.listResources("textures/models", t -> t.getPath().endsWith(".png"));
            for (var r : res4.entrySet()) {
                ResourceLocation relPat = r.getKey().withPath(p -> p.replace("textures/", "").replace(".png", ""));
                try (TextureImage toRecolor = TextureImage.open(manager, relPat)) {
                    Respriter resp = Respriter.of(toRecolor);
                   try(TextureImage recolored = resp.recolor(textures.get(random.nextInt(textures.size())))){
                        sink.addTexture(relPat, recolored);
                    }
                } catch (Exception ignored) {

                }
            }

            var white = Palette.fromArc(new RGBColor(-1), new RGBColor(-1).asHCL().withLuminance(0.99f).asRGB(), 4);
            var res2 = manager.listResources("textures/colormap", t -> t.getPath().endsWith(".png"));
            for (var r : res2.entrySet()) {
                ResourceLocation relPat = r.getKey().withPath(p -> p.replace("textures/", "").replace(".png", ""));
                try (TextureImage toRecolor = TextureImage.open(manager, relPat)) {
                    toRecolor.forEachFramePixel((frameIndex, globalX, globalY) -> {
                        toRecolor.setFramePixel(frameIndex, globalX, globalY, -1);
                    });
                    sink.addAndCloseTexture(relPat, toRecolor);
                } catch (Exception e) {

                }
            }

        } catch (Exception e) {
            int aa = 1;
        }


        try (TextureImage c0 = TextureImage.open(manager, RPUtils.findFirstItemTextureLocation(manager, Items.COPPER_INGOT))
        ) {
            List<Palette> textures =
                    Palette.fromAnimatedImage(c0);

            var res = manager.listResources("textures/item", t -> t.getPath().endsWith(".png"));
            for (var r : res.entrySet()) {
                ResourceLocation relPath = r.getKey().withPath(p -> p.replace("textures/", "").replace(".png", ""));
                try (TextureImage toRecolor = TextureImage.open(manager, relPath)) {
                    Respriter resp = Respriter.of(toRecolor);
                    try (TextureImage recolored = resp.recolor(textures)) {
                        sink.addTexture(relPath, recolored);
                    }
                } catch (Exception ignored) {

                }
            }

        } catch (Exception e) {
            int aa = 1;
        }

    }

    public static void runTranslations(AfterLanguageLoadEvent lang) {
        if (!MiscUtils.FESTIVITY.isAprilsFool()) return;
        Random random = new Random();
        try {
            @Deprecated(forRemoval = true)
            Field f = lang.getClass().getDeclaredField("languageLines");
            f.setAccessible(true);
            Map<String, String> ll = (Map<String, String>) f.get(lang);
            List<String> prefix = List.of("Copper ", "Slightly Weathered Copper ", "Weathered Copper ", "Exposed Copper ", "Oxidized Copper ");

            for (var b : BuiltInRegistries.ITEM) {
                var id = b.getDescriptionId();
                var existing = lang.getEntry(id);
                if (existing != null) {
                    var pr = prefix.get(random.nextInt(prefix.size()));
                    ll.put(id, pr + existing);
                }
            }
        } catch (Exception ignored) {

        }
    }
}
