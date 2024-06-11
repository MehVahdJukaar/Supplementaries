package net.mehvahdjukaar.supplementaries.integration.flywheel;

/*
public class CannonInstance extends BlockEntityInstance<CannonBlockTile> implements DynamicInstance {

    private final TextureAtlasSprite texture;

    private final ModelData head;
    private final ModelData legs;
    private final ModelData pivot;
    private final ModelData model;
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
        this.stack.scale(1,-1,-1);
        RenderUtil
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
        return this.materialManager.defaultCutout().material(Materials.TRANSFORMED)
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