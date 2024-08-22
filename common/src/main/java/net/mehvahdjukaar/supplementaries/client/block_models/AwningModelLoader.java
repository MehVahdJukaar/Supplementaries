package net.mehvahdjukaar.supplementaries.client.block_models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AwningBlock;
import net.mehvahdjukaar.supplementaries.common.entities.SlimeBallEntity;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.List;
import java.util.function.Function;

public class AwningModelLoader implements CustomModelLoader {


    private static final BlockModel.Deserializer DESERIALIZER = new BlockModel.Deserializer();

    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        BlockModel modelHack = DESERIALIZER.deserialize(json, BlockModel.class, context);

        Material top = modelHack.getMaterial("top");
        Material sides = modelHack.getMaterial("side");
        Material particle = modelHack.getMaterial("particle");
        float height = GsonHelper.getAsFloat(json, "height", 1f);
        return new CustomGeometry() {
            @Override
            public CustomBakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location) {
                return null;
            }

            @Override
            public BakedModel bakeModel(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter,
                                        ModelState rotation, ResourceLocation location) {

                var particleTexture = spriteGetter.apply(particle);
                var topTexture = spriteGetter.apply(top);
                var sidesTexture = spriteGetter.apply(sides);



                SimpleBakedModel.Builder modelBuilder = new SimpleBakedModel.Builder(modelHack, ItemOverrides.EMPTY, true);
                modelBuilder.particle(particleTexture);

                BakedQuadBuilder quadBuilder = BakedQuadBuilder.create(topTexture, rotation.getRotation());

                double angleRad = Math.toRadians(ClientConfigs.Blocks.AWNINGS_ANGLE.get());
                double cos = Math.cos(angleRad);
                double sin = Math.sin(angleRad);
                double tan = Math.tan(angleRad);
                float length = (float) (1 / Math.max(Math.abs(cos), Math.abs(sin)));
                PoseStack poseStack = new PoseStack();


                poseStack.translate(0, height, 1);

                poseStack.pushPose();

                poseStack.mulPose(Axis.XP.rotation((float) angleRad));
                poseStack.scale(1, length, 1);
                poseStack.translate(0, -1, 0);
                quadBuilder.setDirection(Direction.UP);
                VertexUtil.addQuad(quadBuilder, poseStack, 0, 0, 1, 1, 0, 0);
                modelBuilder.addUnculledFace(quadBuilder.build());
                quadBuilder.setDirection(Direction.DOWN);
                VertexUtil.addQuad(quadBuilder, poseStack, 1, 0, 0, 1, 0, 0);
                modelBuilder.addUnculledFace(quadBuilder.build());


                poseStack.popPose();

                float zFightOffset = 0.001f;
                float extraH = (float) (zFightOffset / tan);
                float stretch = 0.001f; // to get rid of little seams
                float sideH = 4 / 16f;

                quadBuilder = BakedQuadBuilder.create(sidesTexture, rotation.getRotation());

                poseStack.translate(0, -(float) (1 / tan), -1 + zFightOffset);
                quadBuilder.setDirection(Direction.NORTH);
                VertexUtil.addQuad(quadBuilder, poseStack, 0, -sideH-stretch, 1, extraH, 0, 0, 1, sideH,
                        255, 255, 255, 255, 0, 0);
                BakedQuad quad = quadBuilder.build();
                modelBuilder.addCulledFace(quad.getDirection(), quad);
                VertexUtil.addQuad(quadBuilder, poseStack, 1, -sideH-stretch, 0, extraH, 0, 0, 1, sideH,
                        255, 255, 255, 255, 0, 0);
                quad = quadBuilder.build();
                modelBuilder.addCulledFace(quad.getDirection(), quad);

                return modelBuilder.build();
            }
        };
    }

}
