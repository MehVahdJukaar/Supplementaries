package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.entities.MashlingEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class MashlingModel extends EntityModel<MashlingEntity> {

    private final ModelRenderer body;
    private final ModelRenderer leg_right;
    private final ModelRenderer leg_left;

    public MashlingModel() {
        texWidth = 32;
        texHeight = 32;

        body = new ModelRenderer(this);
        body.setPos(0.0F, 14.0F, 0.0F);

        ModelRenderer head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(head);

        // addBox = addCuboid




        leg_right = new ModelRenderer(this);
        leg_right.setPos(-2.0F, 4.0F, 0.0F);
        body.addChild(leg_right);
        leg_right.addBox(null, -1.0F, 2.0F, -1.0F, 2, 4, 2, 0.0F, 16, 14);

        leg_left = new ModelRenderer(this);
        leg_left.setPos(1.0F, 4.0F, 0.0F);
        body.addChild(leg_left);
        leg_left.addBox(null, 0.0F, 2.0F, -1.0F, 2, 4, 2, 0.0F, 24, 14);
    }

    @Override
    public void setupAnim(MashlingEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        leg_right.xRot = MathHelper.cos(limbSwing * 0.6662F) * limbSwingAmount;
        leg_left.xRot = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;



    }


    @Override
    public void renderToBuffer(MatrixStack matrix, IVertexBuilder vb, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_) {
        body.render(matrix, vb, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
