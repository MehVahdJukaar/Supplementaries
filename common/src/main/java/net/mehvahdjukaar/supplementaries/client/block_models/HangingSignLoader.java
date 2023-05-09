package net.mehvahdjukaar.supplementaries.client.block_models;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class HangingSignLoader implements CustomModelLoader {

    @Override
    public Geometry deserialize(JsonObject json, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        BlockModel stick = ClientHelper.parseBlockModel(json.get("stick"));
        BlockModel leftFence = ClientHelper.parseBlockModel(json.get("left_post"));
        BlockModel leftPalisade = ClientHelper.parseBlockModel(json.get("left_palisade"));
        BlockModel leftWall = ClientHelper.parseBlockModel(json.get("left_wall"));
        BlockModel leftBeam = ClientHelper.parseBlockModel(json.get("left_beam"));
        BlockModel rightFence = ClientHelper.parseBlockModel(json.get("right_post"));
        BlockModel rightPalisade = ClientHelper.parseBlockModel(json.get("right_palisade"));
        BlockModel rightWall = ClientHelper.parseBlockModel(json.get("right_wall"));
        BlockModel rightBeam = ClientHelper.parseBlockModel(json.get("right_beam"));
        BlockModel leftStick = ClientHelper.parseBlockModel(json.get("left_stick"));
        BlockModel rightStick = ClientHelper.parseBlockModel(json.get("right_stick"));
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
        public CustomBakedModel bake(ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ResourceLocation modelLocation) {
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
    }
}
