package net.mehvahdjukaar.supplementaries.client;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.WallLanternBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WallLanternBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

//handles wall lanterns and jar special models stuff. reloaded by dynamic pack early
public class ClientSpecialModelsManager {

    private static final Map<Block, ResourceLocation> SPECIAL_MOUNT_TEXTURES = new IdentityHashMap<>();
    private static final Map<Block, ResourceLocation> SPECIAL_LANTERN_MODELS = new IdentityHashMap<>();

    private static final Set<Block> POSSIBLE_LANTERNS = new HashSet<>();
    private static boolean initialized = false;

    //early reload so we can register these models
    public static void refreshModels(ResourceManager manager) {
        reloadTextures(manager);
        reloadModels(manager);
    }

    private static void reloadModels(ResourceManager manager) {
        if (!initialized) {
            initialize();
            initialized = true;
        }
        SPECIAL_LANTERN_MODELS.clear();
        for (Block l : POSSIBLE_LANTERNS) {

            ResourceLocation reg = Utils.getID(l);
            String namespace = (reg.getNamespace().equals("minecraft") || reg.getNamespace().equals("supplementaries")) ? "" : reg.getNamespace() + "/";
            String s = "block/custom_wall_lanterns/" + namespace + reg.getPath() ;
            ResourceLocation fullPath = Supplementaries.res("models/"+s+ ".json");
            var resource = manager.getResource(fullPath);
            if (resource.isPresent()) {
                SPECIAL_LANTERN_MODELS.put(l, Supplementaries.res(s));
            }
        }
    }

    private static void reloadTextures(ResourceManager manager) {
        if (!initialized) {
            initialize();
            initialized = true;
        }
        SPECIAL_MOUNT_TEXTURES.clear();
        for (Block l : POSSIBLE_LANTERNS) {

            ResourceLocation reg = Utils.getID(l);
            String namespace = (reg.getNamespace().equals("minecraft") || reg.getNamespace().equals("supplementaries")) ? "" : reg.getNamespace() + "/";
            String s = "textures/block/wall_lanterns/" + namespace + reg.getPath() + ".json";
            ResourceLocation fullPath = Supplementaries.res(s);
            var resource = manager.getResource(fullPath);
            if (resource.isPresent()) {
                try (var stream = resource.get().open()) {
                    JsonElement bsElement = RPUtils.deserializeJson(stream);

                    String texture = RPUtils.findFirstResourceInJsonRecursive(bsElement);
                    if (!texture.isEmpty()) SPECIAL_MOUNT_TEXTURES.put(l, new ResourceLocation(texture));

                } catch (Exception ignored) {
                }
            }
        }
    }

    private static void initialize() {
        ImmutableSet.Builder<Block> builder = ImmutableSet.builder();
        for (Block i : BuiltInRegistries.BLOCK) {
            if (WallLanternBlock.isValidBlock(i)) builder.add(i);
        }
        POSSIBLE_LANTERNS.clear();
        POSSIBLE_LANTERNS.addAll(builder.build());
    }

    @Nullable
    public static TextureAtlasSprite getTextureForLantern(Block block) {
        var res = SPECIAL_MOUNT_TEXTURES.get(block);
        if (res == null) return null;
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(res);
    }

    public static void registerSpecialModels(ClientHelper.SpecialModelEvent event) {
        SPECIAL_LANTERN_MODELS.values().forEach(event::register);
    }

    //returns the normal or custom wall lantern model
    public static BakedModel getWallLanternModel(BlockModelShaper blockModelShaper, BlockState lantern) {
        var special = SPECIAL_LANTERN_MODELS.get(lantern.getBlock());
        if (special != null) {
            return ClientHelper.getModel(Minecraft.getInstance().getModelManager(), special);
        }
        return blockModelShaper.getBlockModel(lantern);
    }
}
