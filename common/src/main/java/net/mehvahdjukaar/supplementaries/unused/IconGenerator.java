package net.mehvahdjukaar.supplementaries.unused;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.textures.SpriteUtils;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.FlowerBoxModelsManager;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class IconGenerator {

    public static void generateIcons() {
        if (!PlatHelper.isDev() || Minecraft.getInstance().level == null) return;
        if (Minecraft.getInstance().level.getGameTime() % 400 != 0) {
            return;
        }

        if (Minecraft.getInstance().level != null) {
            ResourceManager res = Minecraft.getInstance().getResourceManager();
            try {
                var plus = TextureImage.open(res, Supplementaries.res("plus"));
                var unseen = TextureImage.open(res, Supplementaries.res("unseen"));

                Set<Item> items = new HashSet<>();
                items.add(ModRegistry.PLANTER.get().asItem());
                items.add(ModRegistry.PEDESTAL.get().asItem());

                for (var item : items) {
                    var id = Utils.getID(item);
                    makeTexture("", item);
                    makeTexture("_unseen", item, unseen);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private static void makeTexture(String postfix, Item item, @Nullable TextureImage... overlays) {
        var model = Minecraft.getInstance().getItemRenderer().getModel(item.getDefaultInstance(), null, null, 0);
        int s = model.isGui3d() ? 16 : 1;
        var t = RenderedTexturesManager.requestFlatItemTexture(
                Utils.getID(item).withSuffix(postfix),
                item,
                18 * s, nativeImage -> {
                    //flip imaeg
                    SpriteUtils.forEachPixel(nativeImage, (x, y) -> {
                                if (y < nativeImage.getHeight() / 2) return;
                                int currentColor = nativeImage.getPixelRGBA(x, y);
                                int oppositeYColor = nativeImage.getPixelRGBA(x, nativeImage.getHeight() - 1 - y);
                                nativeImage.setPixelRGBA(x, y, oppositeYColor);
                                nativeImage.setPixelRGBA(x, nativeImage.getHeight() - 1 - y, currentColor);
                            }

                    );
                    addOutline(nativeImage, FastColor.ABGR32.color(255, 0), s);
                    for (var plus : overlays) {
                        SpriteUtils.forEachPixel(nativeImage, (x, y) -> {
                            int xx = -1 + x / s;
                            int yy = -1 + y / s;
                            if (xx >= plus.getImage().getWidth() || yy >= plus.getImage().getHeight() ||
                                    xx < 0 || yy < 0) return;
                            int color = plus.getImage().getPixelRGBA(xx, yy);
                            if (color != 0) {
                                nativeImage.setPixelRGBA(x, y, color);
                            }
                        });
                    }
                }, false);
        if (t.isInitialized()) {
            try {
                t.saveTextureToFile(PlatHelper.getGamePath().resolve("guide"));
            } catch (Exception e) {
                Supplementaries.LOGGER.error(e);
            }
        }
    }

    private static void addOutline(NativeImage nativeImage, int color, int thickness) {
        int[][] temp = new int[nativeImage.getWidth()][nativeImage.getHeight()];
        SpriteUtils.forEachPixel(nativeImage, (x, y) -> {
            int currentColor = nativeImage.getPixelRGBA(x, y);
            if (FastColor.ABGR32.alpha(currentColor) != 0) {
                for (int i = -thickness; i <= thickness; i++) {
                    for (int j = -thickness; j <= thickness; j++) {
                        if (i * i + j * j <= thickness * thickness) {
                            if (x + i < 0 || x + i >= nativeImage.getWidth() || y + j < 0 || y + j >= nativeImage.getHeight())
                                continue;
                            var currentColor2 = nativeImage.getPixelRGBA(x + i, y + j);
                            if (FastColor.ABGR32.alpha(currentColor2) == 0) {
                                temp[x + i][y + j] = color;
                            }
                        }
                    }
                }
            }
        });
        for (int x = 0; x < nativeImage.getWidth(); x++) {
            for (int y = 0; y < nativeImage.getHeight(); y++) {
                if (temp[x][y] != 0) {
                    nativeImage.setPixelRGBA(x, y, temp[x][y]);
                }
            }
        }

    }

}
