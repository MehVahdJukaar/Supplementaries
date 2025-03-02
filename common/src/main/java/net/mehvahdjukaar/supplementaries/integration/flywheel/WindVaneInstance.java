package net.mehvahdjukaar.supplementaries.integration.flywheel;

/*
import com.jozufozu.flywheel.api.Material;
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
import net.mehvahdjukaar.supplementaries.common.block.tiles.WindVaneBlockTile;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class WindVaneInstance extends BlockEntityInstance<WindVaneBlockTile> implements DynamicInstance {

    private final TextureAtlasSprite texture;

    private final ModelData chicken;
    private final PoseStack stack;

    public WindVaneInstance(MaterialManager materialManager, WindVaneBlockTile tile) {
        super(materialManager, tile);

        this.texture = ModMaterials.WIND_VANE_MATERIAL.sprite();
        this.stack = new PoseStack();

        var p = this.getInstancePosition();
        this.stack.translate(p.getX(), p.getY(), p.getZ());
        this.stack.scale(0.9995F, 0.9995F, 0.9995F);
        this.stack.translate(2.5E-4D, 2.5E-4D, 2.5E-4D);
        this.stack.translate(0.5, 0.5, 0.5);
        this.stack.scale(1,-1,-1);

        this.chicken = this.makeCenterInstance().setTransform(this.stack);
    }

    @Override
    public void beginFrame() {
        this.stack.pushPose();
        float yaw = blockEntity.getYaw(AnimationTickHolder.getPartialTicks());
        this.stack.mulPose(Axis.YP.rotationDegrees(yaw));
        this.chicken.setTransform(this.stack);
        this.stack.popPose();
    }

    @Override
    public void remove() {
        this.chicken.delete();
    }

    @Override
    public void updateLight() {
        this.relight(this.pos, this.chicken);
    }

    private ModelData makeCenterInstance() {
        Material<ModelData> material = this.materialManager.defaultCutout().material(Materials.TRANSFORMED);
        return material
                .model("chicken_" + this.blockEntity.getType(), this::makeModel).createInstance();
    }

    private ModelPart makeModel() {
        return ModelPart.builder("chicken", 32, 32)
                .sprite(this.texture)
                .cuboid()
                .textureOffset(0, -11)
                .start(0.0F, -8.0F, -5.5F)
                .size(0.0F, 11.0F, 11.0F)
                .endCuboid()
                .build();
    }
}
 */
