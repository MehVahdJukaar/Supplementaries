package net.mehvahdjukaar.supplementaries.client;

import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.model.RenderMaterial;

import java.util.HashMap;
import java.util.Map;

public class Materials {

    public static final Map<IWoodType, RenderMaterial> HANGING_SIGNS_MATERIAL = new HashMap<>();
    public static final Map<IWoodType,RenderMaterial> SIGN_POSTS_MATERIAL = new HashMap<>();
    static {
        for(IWoodType type : WoodTypes.TYPES.values()){
            HANGING_SIGNS_MATERIAL.put(type, new RenderMaterial(Atlases.SIGN_ATLAS, Textures.HANGING_SIGNS_TEXTURES.get(type)));
            SIGN_POSTS_MATERIAL.put(type, new RenderMaterial(Atlases.SIGN_ATLAS, Textures.SIGN_POSTS_TEXTURES.get(type)));
        }
    }
}
