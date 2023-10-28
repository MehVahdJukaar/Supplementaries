package net.mehvahdjukaar.supplementaries.common.utils.fabric;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.client.DummySprite;
import net.mehvahdjukaar.supplementaries.client.block_models.WallLanternBakedModel;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


public class VibeCheckerImpl {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void checkVibe() {
        crashIfFabricRenderingAPIHasBeenNuked();

        //fixSodiumDeps();unfixSodiumDeps();
        vibeCheckModels();
    }

    private static void unfixSodiumDeps() {
        JsonElement obj = null;
        var file = FabricLoader.getInstance().getConfigDir().resolve("fabric_loader_dependencies.json").toFile();
        if (file.exists() && file.isFile()) {
            try (FileInputStream fileInputStream = new FileInputStream(file);
                 InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                obj = GSON.fromJson(bufferedReader, JsonElement.class);
            } catch (IOException ignored) {
            }
        }
        if (obj instanceof JsonObject jo) {
            if (jo.has("overrides")) {
                JsonObject overrides = jo.getAsJsonObject("overrides");
                overrides.remove("sodium");
                jo.add("overrides", overrides);
            }
        }
        if (obj != null) {
            try (FileOutputStream stream = new FileOutputStream(file);
                 Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {

                GSON.toJson(obj, writer);
            } catch (IOException ignored) {
            }
        }
    }


    private static void fixSodiumDeps() {
        JsonElement obj = new JsonObject();
        var file = FabricLoader.getInstance().getConfigDir().resolve("fabric_loader_dependencies.json").toFile();
        if (file.exists() && file.isFile()) {
            try (FileInputStream fileInputStream = new FileInputStream(file);
                 InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                obj = GSON.fromJson(bufferedReader, JsonElement.class);
            } catch (IOException ignored) {
            }
        }
        if (obj instanceof JsonObject jo) {
            if (!jo.has("version")) {
                jo.addProperty("version", 1);
            }
            JsonObject overrides = new JsonObject();
            if (jo.has("overrides")) {
                overrides = jo.getAsJsonObject("overrides");
            }
            JsonObject prop = new JsonObject();
            JsonObject dep = new JsonObject();
            dep.addProperty("indium", "*");
            prop.add("+depends", dep);
            JsonObject dep2 = new JsonObject();
            dep2.addProperty("fabric-renderer-indigo", "*");
            prop.add("+conflicts", dep2);
            overrides.add("sodium", prop);
            jo.add("overrides", overrides);

        }

        try (FileOutputStream stream = new FileOutputStream(file);
             Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {

            GSON.toJson(obj, writer);
        } catch (IOException ignored) {
        }
    }
    //I hate this. I've got to do what I've got to do. Cant stand random reports anymore
    //you were supposed to destroy the loader api nuking mods, not join them!
    public static void crashIfFabricRenderingAPIHasBeenNuked() {
        if (PlatHelper.isModLoaded("sodium") && !PlatHelper.isModLoaded("indium")) {
            Supplementaries.LOGGER.error("You seem to have installed Sodium which has been known to break fabric rendering API." +
                    "Things might not work well");
        }
    }

    public static void vibeCheckModels() {
        try {
            DummySprite textureAtlasSprite = DummySprite.INSTANCE;

            BakedQuadBuilder b = BakedQuadBuilder.create(textureAtlasSprite);
            b.setAutoDirection();
            VertexUtil.addQuad(b, new PoseStack(), 0, 0, 1, 1, 0, 0);
            var dummy = b.build();

            List<BakedQuad> l = List.of(dummy);
            Map<Direction, List<BakedQuad>> map = Maps.newEnumMap(Direction.class);
            map.putAll(Map.of(Direction.DOWN, l,
                    Direction.UP, l,
                    Direction.SOUTH, l,
                    Direction.NORTH, l,
                    Direction.WEST, l,
                    Direction.EAST, l));
            new WallLanternBakedModel(new SimpleBakedModel(l,
                    map,
                    false, false, false,
                    textureAtlasSprite,
                    ItemTransforms.NO_TRANSFORMS, ItemOverrides.EMPTY

            ),
                    new ModelState() {
                        @Override
                        public Transformation getRotation() {
                            return new Transformation(new Vector3f(1, 1, 1),
                                    null, null, null);
                        }
                    });
        } catch (Exception e) {
            throw new VibeChecker.BadModError("Some OTHER mod failed to load baked models. Refusing to proceed further to prevent in game issues. See logs for details", e);
        }
    }


}
