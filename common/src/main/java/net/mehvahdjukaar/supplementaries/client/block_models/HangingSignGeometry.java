package net.mehvahdjukaar.supplementaries.client.block_models;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public record HangingSignGeometry(BlockModel stick,
                                  BlockModel leftFence,
                                  BlockModel leftPalisade,
                                  BlockModel leftWall,
                                  BlockModel leftBeam,
                                  BlockModel leftStick,
                                  BlockModel rightFence,
                                  BlockModel rightPalisade,
                                  BlockModel rightWall,
                                  BlockModel rightBeam,
                                  BlockModel rightStick) implements IModelGeometry<HangingSignGeometry> {


    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
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
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
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