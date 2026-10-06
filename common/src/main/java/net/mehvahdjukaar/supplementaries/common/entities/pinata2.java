// Made with Blockbench 5.2.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class pinata2<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "pinata2"), "main");
	private final ModelPart bone;
	private final ModelPart bb_main;

	public pinata2(ModelPart root) {
		this.bone = root.getChild("bone");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 35).addBox(-5.0F, -17.0F, -4.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(32, 27).mirror().addBox(-11.0F, -17.0F, -4.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 27).mirror().addBox(-11.0F, -17.0F, -11.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(16, 27).addBox(-5.0F, -17.0F, -11.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 37.0F, 7.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -16.0F, -7.0F, 12.0F, 12.0F, 15.0F, new CubeDeformation(0.0F))
		.texOffs(16, 35).addBox(-3.0F, -10.0F, -9.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition ear_r1 = bb_main.addOrReplaceChild("ear_r1", CubeListBuilder.create().texOffs(40, 35).addBox(0.0F, 0.0F, -2.0F, 0.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -15.0F, -4.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition ear_r2 = bb_main.addOrReplaceChild("ear_r2", CubeListBuilder.create().texOffs(32, 35).addBox(0.0F, 0.0F, -2.0F, 0.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -15.0F, -4.0F, 0.0F, 0.0F, -0.2618F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}