package net.mehvahdjukaar.supplementaries.client;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import net.mehvahdjukaar.selene.resourcepack.RPUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.LightableLanternBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WallLanternStuff {

    private static Set<Block> POSSIBLE_LANTERNS = null;

    public static final Map<Block, ResourceLocation> SPECIAL_TEXTURES = new HashMap<>();

    private static void init() {
        ImmutableSet.Builder<Block> builder = ImmutableSet.builder();
        for (Block i : ForgeRegistries.BLOCKS) {
            if (isLanternBlock(i)) builder.add(i);
        }
        POSSIBLE_LANTERNS = builder.build();
    }

    public static boolean isLanternBlock(Block b) {
        String namespace = b.getRegistryName().getNamespace();
        if (namespace.equals("skinnedlanterns")) return true;
        if (b instanceof LanternBlock) {
            return !b.defaultBlockState().hasBlockEntity() || b instanceof LightableLanternBlock;
        }
        return false;
    }

    public static void onResourceReload(ResourceManager manager) {
        if (POSSIBLE_LANTERNS == null) init();
        SPECIAL_TEXTURES.clear();
        for (Block i : POSSIBLE_LANTERNS) {
            try {
                ResourceLocation reg = i.getRegistryName();
                String namespace = (reg.getNamespace().equals("minecraft")||reg.getNamespace().equals("supplementaries")) ? "" : reg.getNamespace() + "/";
                String s = "wall_lanterns/" + namespace + reg.getPath();
                ResourceLocation fullPath = RPUtils.resPath(Supplementaries.res(s), RPUtils.ResType.BLOCK_MODELS);
                var resource = manager.getResource(fullPath);
                JsonElement bsElement = RPUtils.deserializeJson(resource.getInputStream());

                String texture = RPUtils.findFirstResourceInJsonRecursive(bsElement);
                if(!texture.isEmpty()) SPECIAL_TEXTURES.put(i, new ResourceLocation(texture));

            }catch (Exception ignored){}
        }
    }

    @Nullable
    public static TextureAtlasSprite getTextureForLantern(Block block) {
        var res = SPECIAL_TEXTURES.get(block);
        if (res == null) return null;
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(res);
    }
}
