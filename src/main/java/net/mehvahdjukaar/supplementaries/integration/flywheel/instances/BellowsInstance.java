package net.mehvahdjukaar.supplementaries.integration.flywheel.instances;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.hardcoded.ModelPart;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.mehvahdjukaar.supplementaries.client.renderers.RotHlpr;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BellowsBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BellowsBlockTile;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class BellowsInstance extends BlockEntityInstance<BellowsBlockTile> implements DynamicInstance {
    //TODO: add more instances
    private final TextureAtlasSprite texture;
    private final ModelData center;
    private final ModelData top;
    private final ModelData bottom;
    private final ModelData leather;
    private final PoseStack stack;
    private float lastProgress = 0;

    public BellowsInstance(MaterialManager materialManager, BellowsBlockTile tile) {
        super(materialManager, tile);

        this.texture = net.mehvahdjukaar.supplementaries.client.Materials.BELLOWS_MATERIAL.sprite();
        Quaternion rotation = this.getDirection().getRotation();
        this.stack = new PoseStack();

        var p = this.getInstancePosition();
        this.stack.translate(p.getX(), p.getY(), p.getZ());
        this.stack.scale(0.9995F, 0.9995F, 0.9995F);
        this.stack.translate(2.5E-4D, 2.5E-4D, 2.5E-4D);
        this.stack.translate(0.5, 0.5, 0.5);
        this.stack.mulPose(rotation);
        this.stack.mulPose(RotHlpr.X90);

        this.center = this.makeCenterInstance().setTransform(this.stack);
        this.stack.translate(-0.5, -0.5, -0.5);

        this.leather = this.makeLeatherInstance().setTransform(this.stack);

        this.top = this.makeTopInstance().setTransform(this.stack);
        //this.stack.translateY(-13/16f);
        this.bottom = this.makeBottomInstance().setTransform(this.stack);

    }

    @Override
    public void beginFrame() {

        float dh = Mth.lerp(AnimationTickHolder.getPartialTicks(), blockEntity.prevHeight, blockEntity.height);

        this.stack.pushPose();

        this.stack.translate(0.5, 0.5, 0.5);

        this.stack.pushPose();

        this.stack.translate(0, -1 + (3 / 16d) - dh, 0);

        this.top.setTransform(this.stack);

        this.stack.popPose();

        this.stack.pushPose();

        this.stack.translate(0, dh, 0);

        this.bottom.setTransform(this.stack);

        this.stack.popPose();

        float j = 3.2f;

        this.stack.scale(1, 1 + j * dh, 1);

        this.leather.setTransform(this.stack);

        this.stack.popPose();

    }

    @Override
    public void remove() {
        this.top.delete();
        this.leather.delete();
        this.center.delete();
        this.bottom.delete();
    }

    @Override
    public void updateLight() {
        this.relight(this.pos, this.top, this.center, this.leather, this.bottom);
    }

    private ModelData makeTopInstance() {
        return this.materialManager.defaultCutout().material(Materials.TRANSFORMED)
                .model("top_" + this.blockEntity.getType(), this::makeLidModel).createInstance();
    }

    private ModelData makeBottomInstance() {
        return this.materialManager.defaultCutout().material(Materials.TRANSFORMED)
                .model("bottom_" + this.blockEntity.getType(), this::makeLidModel).createInstance();
    }

    private ModelData makeLeatherInstance() {
        return this.materialManager.defaultCutout().material(Materials.TRANSFORMED)
                .model("leather_" + this.blockEntity.getType(), this::makeLeatherModel).createInstance();
    }

    private ModelData makeCenterInstance() {
        return this.materialManager.defaultCutout().material(Materials.TRANSFORMED)
                .model("center_" + this.blockEntity.getType(), this::makeCenterModel).createInstance();
    }

    private ModelPart makeLeatherModel() {
        return ModelPart.builder("bellows_leather", 64, 64)
                .sprite(this.texture)
                .cuboid()
                .textureOffset(0, 37)
                .start(-7.0F, -5.0F, -7.0F)
                .size(14.0F, 10.0F, 14.0F)
                .endCuboid()
                .build();
    }

    private ModelPart makeLidModel() {
        return ModelPart.builder("bellows_lid", 64, 64)
                .sprite(this.texture)
                .cuboid()
                .textureOffset(0, 0)
                .start(-8.0F, 5.0F, -8.0F)
                .size(16.0F, 3.0F, 16.0F)
                .endCuboid()
                .build();
    }

    private ModelPart makeCenterModel() {
        return ModelPart.builder("bellows_center", 64, 64)
                .sprite(this.texture)
                .cuboid()
                .textureOffset(0, 0)
                .start(-2.0F, -2.0F, -8.0F)
                .size(4.0F, 1.0F, 1.0F)
                .endCuboid()
                .cuboid()
                .textureOffset(0, 2)
                .start(-2.0F, 1.0F, -8.0F)
                .size(4.0F, 1.0F, 1.0F)
                .endCuboid()
                .cuboid()
                .textureOffset(0, 19)
                .start(-8.0F, -1.0F, -8.0F)
                .size(16.0F, 2.0F, 16.0F)
                .endCuboid()
                .build();
    }

    private Direction getDirection() {
        return this.blockState.getValue(BellowsBlock.FACING);
    }
}