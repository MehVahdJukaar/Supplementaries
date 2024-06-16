package net.mehvahdjukaar.supplementaries.integration.flywheel;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.hardcoded.ModelPart;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.CannonBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CannonInstance extends BlockEntityInstance<CannonBlockTile> implements DynamicInstance {

    private final TextureAtlasSprite texture;

    private final ModelData head;
    private final ModelData headOverlay;
    private final ModelData legs;
    private final ModelData base;
    private final PoseStack stack;

    public CannonInstance(MaterialManager materialManager, CannonBlockTile tile) {
        super(materialManager, tile);

        this.texture = ModMaterials.CANNON_MATERIAL.sprite();
        this.stack = new PoseStack();

        var p = this.getInstancePosition();
        this.stack.translate(p.getX(), p.getY(), p.getZ());
        this.stack.scale(0.9995F, 0.9995F, 0.9995F);
        this.stack.translate(2.5E-4D, 2.5E-4D, 2.5E-4D);
        this.stack.translate(0.5, 0.5, 0.5);

        this.legs = this.makeLegsInstance().setTransform(this.stack);
        this.base = this.makeBaseInstance().setTransform(this.stack);
        this.stack.pushPose();
        this.stack.translate(0, -1 / 16f, 0);
        this.head = this.makeHeadInstance().setTransform(this.stack);
        this.headOverlay = this.makeHeadOverlayInstance().setTransform(this.stack);
        this.stack.popPose();
    }

    @Override
    public void beginFrame() {
        float partialTick = AnimationTickHolder.getPartialTicks();

        this.stack.pushPose();

        Quaternionf rotation = blockEntity.getBlockState().getValue(CannonBlock.FACING).getOpposite().getRotation();
        this.stack.mulPose(rotation);
        this.base.setTransform(this.stack);


        float yawRad = blockEntity.getYaw(partialTick) * Mth.DEG_TO_RAD;
        float pitchRad = blockEntity.getPitch(partialTick) * Mth.DEG_TO_RAD;

        Vector3f forward = new Vector3f(0f, 0, 1);

        forward.rotateX(Mth.PI - pitchRad);

        forward.rotateY(Mth.PI - yawRad);
        forward.rotate(rotation.invert());

        yawRad = (float) Mth.atan2(forward.x, forward.z);

        pitchRad = (float) Mth.atan2(-forward.y, Mth.sqrt(forward.x * forward.x + forward.z * forward.z));
        //float rollRad = (float) Math.atan2(forward.y, forward.z);

        this.stack.mulPose(Axis.YP.rotation(yawRad));
        this.legs.setTransform(this.stack);

        this.stack.translate(0, -1 / 16f, 0);
        this.stack.mulPose(Axis.XP.rotation(pitchRad));


        // animation
        float cooldownCounter = blockEntity.getCooldownAnimation(partialTick);
        float fireCounter = blockEntity.getFiringAnimation(partialTick);

        //write equation of sawtooth wave with same period as that sine wave
        float squish = CannonBlockTileRenderer.triangle(1 - cooldownCounter, 0.01f, 0.15f) * 0.2f;

        float wobble = Mth.sin(fireCounter * 20f * (float) Math.PI) * 0.005f;
        float scale = wobble + 1f + squish * 0.7f;

        this.stack.translate(0,0,  (squish * 5.675f)/16f);
        this.stack.scale(scale, scale, 1 - squish);

        this.headOverlay.setTransform(this.stack);
        float overlayScale = 0.95f;
        this.stack.scale(overlayScale, overlayScale, overlayScale);
        this.head.setTransform(this.stack);

        this.stack.popPose();
    }

    @Override
    public void remove() {
        this.legs.delete();
        this.base.delete();
        this.head.delete();
        this.headOverlay.delete();
    }

    @Override
    public void updateLight() {
        this.relight(this.pos, this.legs, this.base, this.head, this.headOverlay);
    }

    private ModelData makeLegsInstance() {
        return this.materialManager.defaultCutout().material(Materials.TRANSFORMED)
                .model("legs_" + this.blockEntity.getType(), this::makeLegs).createInstance();
    }

    private ModelData makeBaseInstance() {
        return this.materialManager.defaultCutout().material(Materials.TRANSFORMED)
                .model("base_" + this.blockEntity.getType(), this::makeBase).createInstance();
    }

    private ModelData makeHeadInstance() {
        return this.materialManager.defaultCutout().material(Materials.TRANSFORMED)
                .model("head_" + this.blockEntity.getType(), this::makeHead).createInstance();
    }

    private ModelData makeHeadOverlayInstance() {
        return this.materialManager.defaultCutout().material(Materials.TRANSFORMED)
                .model("head_overlay_" + this.blockEntity.getType(), this::makeHeadOverlay).createInstance();
    }

    private ModelPart makeLegs() {
        return ModelPart.builder("legs", 64, 64)
                .sprite(this.texture)
                .cuboid()
                .textureOffset(0, 0)
                .start(6.0F, -4.0F, -3.0F)
                .size(2.0F, 10.0F, 6.0F)
                .endCuboid()
                .cuboid()
                .textureOffset(48, 0)
                .start(-8.0F, -4.0F, -3.0F)
                .size(2.0F, 10.0F, 6.0F)
                .endCuboid()
                .build();
    }

    private ModelPart makeBase() {
        return ModelPart.builder("base", 64, 64)
                .sprite(this.texture)
                .cuboid()
                .textureOffset(0, 0)
                .start(-8.0F, 6.0F, -8.0F)
                .size(16.0F, 2.0F, 16.0F)
                .endCuboid()
                .build();
    }

    private ModelPart makeHead() {
        return ModelPart.builder("head", 64, 64)
                .sprite(this.texture)
                .cuboid()
                .textureOffset(0, 18)
                .start(-6.0F, -6.0F, -6.5F)
                .size(12.0F, 12.0F, 13.0F)
                .endCuboid()
                .build();
    }

    private ModelPart makeHeadOverlay() {
        return ModelPart.builder("head_overlay", 64, 64)
                .sprite(this.texture)
                .cuboid()
                .textureOffset(0, 46)
                .start(-6.0F, -6.0F, -6.5F)
                .size(12.0F, 12.0F, 6.0F)
                .endCuboid()
                .build();
    }

}
