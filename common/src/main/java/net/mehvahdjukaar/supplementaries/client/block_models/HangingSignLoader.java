package net.mehvahdjukaar.supplementaries.client.block_models;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class HangingSignLoader implements CustomModelLoader {

    @Override
    public Geometry deserialize(JsonObject json, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        BlockModel stick = ClientPlatformHelper.parseBlockModel(json.get("stick"));
        BlockModel leftFence = ClientPlatformHelper.parseBlockModel(json.get("left_post"));
        BlockModel leftPalisade = ClientPlatformHelper.parseBlockModel(json.get("left_palisade"));
        BlockModel leftWall = ClientPlatformHelper.parseBlockModel(json.get("left_wall"));
        BlockModel leftBeam = ClientPlatformHelper.parseBlockModel(json.get("left_beam"));
        BlockModel rightFence = ClientPlatformHelper.parseBlockModel(json.get("right_post"));
        BlockModel rightPalisade = ClientPlatformHelper.parseBlockModel(json.get("right_palisade"));
        BlockModel rightWall = ClientPlatformHelper.parseBlockModel(json.get("right_wall"));
        BlockModel rightBeam = ClientPlatformHelper.parseBlockModel(json.get("right_beam"));
        BlockModel leftStick = ClientPlatformHelper.parseBlockModel(json.get("left_stick"));
        BlockModel rightStick = ClientPlatformHelper.parseBlockModel(json.get("right_stick"));
        return new Geometry(stick, leftFence, leftPalisade, leftWall, leftBeam, leftStick,
                rightFence, rightPalisade, rightWall, rightBeam, rightStick);
    }


    private record Geometry(BlockModel stick,
                            BlockModel leftFence,
                            BlockModel leftPalisade,
                            BlockModel leftWall,
                            BlockModel leftBeam,
                            BlockModel leftStick,
                            BlockModel rightFence,
                            BlockModel rightPalisade,
                            BlockModel rightWall,
                            BlockModel rightBeam,
                            BlockModel rightStick) implements CustomGeometry {


        @Override
        public CustomBakedModel bake(ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ResourceLocation modelLocation) {
            BakedModel bakedStick = this.stick.bake(bakery, stick, spriteGetter, modelTransform, modelLocation, true);
            BakedModel bakedLeftFence = this.leftFence.bake(bakery, leftFence, spriteGetter, modelTransform, modelLocation, true);
            BakedModel bakedLeftPalisade = this.leftPalisade.bake(bakery, leftPalisade, spriteGetter, modelTransform, modelLocation, true);
            BakedModel bakedLeftWall = this.leftWall.bake(bakery, leftWall, spriteGetter, modelTransform, modelLocation, true);
            BakedModel bakedLeftBeam = this.leftBeam.bake(bakery, leftBeam, spriteGetter, modelTransform, modelLocation, true);
            BakedModel bakedLeftStick = this.leftStick.bake(bakery, leftStick, spriteGetter, modelTransform, modelLocation, true);
            BakedModel bakedRightFence = this.rightFence.bake(bakery, rightFence, spriteGetter, modelTransform, modelLocation, true);
            BakedModel bakedRightPalisade = this.rightPalisade.bake(bakery, rightPalisade, spriteGetter, modelTransform, modelLocation, true);
            BakedModel bakedRightWall = this.rightWall.bake(bakery, rightWall, spriteGetter, modelTransform, modelLocation, true);
            BakedModel bakedRightBeam = this.rightBeam.bake(bakery, rightBeam, spriteGetter, modelTransform, modelLocation, true);
            BakedModel bakedRightStick = this.rightStick.bake(bakery, rightStick, spriteGetter, modelTransform, modelLocation, true);

            return new HangingSignBakedModel(bakedStick,
                    bakedLeftFence, bakedLeftPalisade, bakedLeftWall, bakedLeftBeam, bakedLeftStick,
                    bakedRightFence, bakedRightPalisade, bakedRightWall, bakedRightBeam, bakedRightStick);
        }

        @Override
        public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            List<Material> list = new ArrayList<>();
            list.addAll(stick.getMaterials(modelGetter, missingTextureErrors));
            list.addAll(leftFence.getMaterials(modelGetter, missingTextureErrors));
            list.addAll(leftPalisade.getMaterials(modelGetter, missingTextureErrors));
            list.addAll(leftBeam.getMaterials(modelGetter, missingTextureErrors));
            list.addAll(leftWall.getMaterials(modelGetter, missingTextureErrors));
            list.addAll(leftStick.getMaterials(modelGetter, missingTextureErrors));
            list.addAll(rightBeam.getMaterials(modelGetter, missingTextureErrors));
            list.addAll(rightPalisade.getMaterials(modelGetter, missingTextureErrors));
            list.addAll(rightWall.getMaterials(modelGetter, missingTextureErrors));
            list.addAll(rightFence.getMaterials(modelGetter, missingTextureErrors));
            list.addAll(rightStick.getMaterials(modelGetter, missingTextureErrors));
            return list;
        }
    }
}
