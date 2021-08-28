package net.mehvahdjukaar.supplementaries.compat.flywheel.instances;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.core.model.ModelPart;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.blocks.BellowsBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.BellowsBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;

public class BellowsInstance extends TileEntityInstance<BellowsBlockTile> implements IDynamicInstance {

    private final TextureAtlasSprite texture;
    private final ModelData center;
    private final ModelData top;
    private final ModelData bottom;
    private final ModelData leather;
    private MatrixTransformStack stack;
    private float lastProgress = 0;

    public BellowsInstance(MaterialManager<?> materialManager, BellowsBlockTile tile) {
        super(materialManager, tile);

        this.texture = net.mehvahdjukaar.supplementaries.client.Materials.BELLOWS_MATERIAL.sprite();
        Quaternion rotation = this.getDirection().getRotation();
        this.stack = new MatrixTransformStack();
        this.stack.translate(this.getInstancePosition()).scale(0.9995F).translateAll(2.5E-4D).centre()
                .multiply(rotation).multiply(Const.X90);

        this.center = this.makeCenterInstance().setTransform(this.stack.unwrap());
        this.stack.unCentre();

        this.leather = this.makeLeatherInstance().setTransform(this.stack.unwrap());

        this.top = this.makeTopInstance().setTransform(this.stack.unwrap());
        //this.stack.translateY(-13/16f);
        this.bottom = this.makeBottomInstance().setTransform(this.stack.unwrap());

    }

    @Override
    public void beginFrame() {

        float dh = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), tile.prevHeight, tile.height);

        this.stack.push();

        this.stack.centre();

        this.stack.push();

        this.stack.translate(0, -1+(3/16d)-dh, 0);

        this.top.setTransform(this.stack.unwrap());

        this.stack.pop();

        this.stack.push();

        this.stack.translate(0, dh,0);

        this.bottom.setTransform(this.stack.unwrap());

        this.stack.pop();

        float j = 3.2f;

        MatrixTransformStack old = this.stack;
        MatrixStack s = this.stack.unwrap();
        s.scale(1,1+j*dh,1);
        this.stack = new MatrixTransformStack(s);

        //this.stack.scale(1+j*dh);

        this.leather.setTransform(this.stack.unwrap());

        this.stack = old;

        this.stack.pop();


        /*
        float progress = 2.5f;//this.tile.getProgress(AnimationTickHolder.getPartialTicks());
        if (progress != this.lastProgress) {
            this.lastProgress = progress;
            Quaternion spin = Vector3f.YP.rotationDegrees(270.0F * progress);
            this.stack.push().centre().multiply(spin).unCentre().translateY(progress * 0.5F);
            this.lid.setTransform(this.stack.unwrap());
            this.stack.pop();
        }*/
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
                .model("top_" + this.tile.getType(), this::makeLidModel).createInstance();
    }

    private ModelData makeBottomInstance() {
        return this.materialManager.defaultCutout().material(Materials.TRANSFORMED)
                .model("bottom_" + this.tile.getType(), this::makeLidModel).createInstance();
    }

    private ModelData makeLeatherInstance() {
        return this.materialManager.defaultCutout().material(Materials.TRANSFORMED)
                .model("leather_" + this.tile.getType(), this::makeLeatherModel).createInstance();
    }

    private ModelData makeCenterInstance() {
        return this.materialManager.defaultCutout().material(Materials.TRANSFORMED)
                .model("center_" + this.tile.getType(), this::makeCenterModel).createInstance();
    }

    private ModelPart makeLeatherModel() {
        return ModelPart.builder(64, 64)
                .sprite(this.texture)
                .cuboid()
                .textureOffset(0, 37)
                .start(-7.0F, -5.0F, -7.0F)
                .size(14.0F, 10.0F, 14.0F)
                .endCuboid()
                .build();
    }

    private ModelPart makeLidModel() {
        return ModelPart.builder(64, 64)
                .sprite(this.texture)
                .cuboid()
                .textureOffset(0, 0)
                .start(-8.0F, 5.0F, -8.0F)
                .size( 16.0F, 3.0F, 16.0F)
                .endCuboid()
                .build();
    }

    private ModelPart makeCenterModel() {
        return ModelPart.builder(64, 64)
                .sprite(this.texture)
                .cuboid()
                .textureOffset(0, 0)
                .start(-2.0F, -2.0F, -8.0F)
                .size( 4.0F, 1.0F, 1.0F)
                .endCuboid()
                .cuboid()
                .textureOffset(0, 2)
                .start(-2.0F, 1.0F, -8.0F)
                .size( 4.0F, 1.0F, 1.0F)
                .endCuboid()
                .cuboid()
                .textureOffset(0, 19)
                .start(-8.0F, -1.0F, -8.0F)
                .size( 16.0F, 2.0F, 16.0F)
                .endCuboid()
                .build();
    }

    private Direction getDirection() {
        return this.blockState.getValue(BellowsBlock.FACING);
    }
}
