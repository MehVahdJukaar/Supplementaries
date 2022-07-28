package net.mehvahdjukaar.supplementaries.client;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.JarBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WallLanternTexturesRegistry extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Map<Block, ResourceLocation> SPECIAL_TEXTURES = new HashMap<>();
    private static Set<Block> POSSIBLE_LANTERNS = null;

    public static final WallLanternTexturesRegistry RELOAD_INSTANCE = new WallLanternTexturesRegistry();

    private WallLanternTexturesRegistry() {
        super(GSON, "textures/blocks/wall_lanterns");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager manager, ProfilerFiller pProfiler) {
        reloadTextures(manager);
    }

    public static void reloadTextures(ResourceManager manager) {
        if (POSSIBLE_LANTERNS == null) initialize();
        SPECIAL_TEXTURES.clear();
        for (Block i : POSSIBLE_LANTERNS) {

            ResourceLocation reg = Utils.getID(i);
            String namespace = (reg.getNamespace().equals("minecraft") || reg.getNamespace().equals("supplementaries")) ? "" : reg.getNamespace() + "/";
            String s = "textures/blocks/wall_lanterns/" + namespace + reg.getPath() + ".json";
            ResourceLocation fullPath = Supplementaries.res(s);
            var resource = manager.getResource(fullPath);
            if (resource.isPresent()) {
                try (var stream = resource.get().open()) {
                    JsonElement bsElement = RPUtils.deserializeJson(stream);

                    String texture = RPUtils.findFirstResourceInJsonRecursive(bsElement);
                    if (!texture.isEmpty()) SPECIAL_TEXTURES.put(i, new ResourceLocation(texture));

                } catch (Exception ignored) {
                }
            }
        }

        //jar stuff
        //using this to also load jar model
        ResourceLocation fullPath = Supplementaries.res("textures/blocks/jar_fluid.json");
        var resource = manager.getResource(fullPath);
        if (resource.isPresent()) {
            try (var stream = resource.get().open()) {
                JsonObject bsElement = RPUtils.deserializeJson(stream);
                float width = GsonHelper.getAsFloat(bsElement, "width") / 16f;
                float height = GsonHelper.getAsFloat(bsElement, "height") / 16f;
                float y0 = GsonHelper.getAsFloat(bsElement, "y") / 16f;
                JarBlockTileRenderer.liquidParams.set(width, height, y0);
            } catch (Exception ignored) {
            }
        }
    }

    private static void initialize() {
        ImmutableSet.Builder<Block> builder = ImmutableSet.builder();
        for (Block i : Registry.BLOCK) {
            if (CommonUtil.isLanternBlock(i)) builder.add(i);
        }
        POSSIBLE_LANTERNS = builder.build();
    }

    @Nullable
    public static TextureAtlasSprite getTextureForLantern(Block block) {
        var res = SPECIAL_TEXTURES.get(block);
        if (res == null) return null;
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(res);
    }

}
