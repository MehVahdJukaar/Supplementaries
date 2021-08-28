package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.client.renderer.entity.model.IHeadToggle;
import net.minecraft.client.renderer.entity.model.SegmentedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.util.math.MathHelper;

public class OrangeTraderModel <T extends Entity> extends SegmentedModel<T> implements IHasHead, IHeadToggle {
    protected ModelRenderer head;
    protected ModelRenderer hat;
    protected final ModelRenderer hatRim;
    protected final ModelRenderer body;
    protected final ModelRenderer jacket;
    protected final ModelRenderer arms;
    protected final ModelRenderer leg0;
    protected final ModelRenderer leg1;
    protected final ModelRenderer nose;

    public OrangeTraderModel(float p_i1163_1_) {
        this(p_i1163_1_, 64, 64);
    }

    public OrangeTraderModel(float p_i51059_1_, int p_i51059_2_, int p_i51059_3_) {
        float f = 0.5F;
        this.head = (new ModelRenderer(this)).setTexSize(p_i51059_2_, p_i51059_3_);
        this.head.setPos(0.0F, 0.0F, 0.0F);
        this.head.texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, p_i51059_1_);
        this.hat = (new ModelRenderer(this)).setTexSize(p_i51059_2_, p_i51059_3_);
        this.hat.setPos(0.0F, -1.0F, 0.0F);
        this.hat.texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, p_i51059_1_ + 0.5F);
        this.head.addChild(this.hat);
        this.hatRim = (new ModelRenderer(this)).setTexSize(p_i51059_2_, p_i51059_3_);
        this.hatRim.setPos(0.0F, 0.0F, 0.0F);
        this.hatRim.texOffs(30, 47).addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F, p_i51059_1_);
        this.hatRim.xRot = (-(float)Math.PI / 2F);
        this.hat.addChild(this.hatRim);
        this.nose = (new ModelRenderer(this)).setTexSize(p_i51059_2_, p_i51059_3_);
        this.nose.setPos(0.0F, -2.0F, 0.0F);
        this.nose.texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, p_i51059_1_);
        this.head.addChild(this.nose);
        this.body = (new ModelRenderer(this)).setTexSize(p_i51059_2_, p_i51059_3_);
        this.body.setPos(0.0F, 0.0F, 0.0F);
        this.body.texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, p_i51059_1_);
        this.jacket = (new ModelRenderer(this)).setTexSize(p_i51059_2_, p_i51059_3_);
        this.jacket.setPos(0.0F, 0.0F, 0.0F);
        this.jacket.texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, p_i51059_1_ + 0.5F);
        this.body.addChild(this.jacket);
        this.arms = (new ModelRenderer(this)).setTexSize(p_i51059_2_, p_i51059_3_);
        this.arms.setPos(0.0F, 2.0F, 0.0F);
        this.arms.texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, p_i51059_1_);
        this.arms.texOffs(44, 22).addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, p_i51059_1_, true);
        this.arms.texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, p_i51059_1_);
        this.leg0 = (new ModelRenderer(this, 0, 22)).setTexSize(p_i51059_2_, p_i51059_3_);
        this.leg0.setPos(-2.0F, 12.0F, 0.0F);
        this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_i51059_1_);
        this.leg1 = (new ModelRenderer(this, 0, 22)).setTexSize(p_i51059_2_, p_i51059_3_);
        this.leg1.mirror = true;
        this.leg1.setPos(2.0F, 12.0F, 0.0F);
        this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_i51059_1_);
    }

    public Iterable<ModelRenderer> parts() {
        return ImmutableList.of(this.head, this.body, this.leg0, this.leg1, this.arms);
    }

    public void setupAnim(T p_225597_1_, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {
        boolean flag = false;
        if (p_225597_1_ instanceof AbstractVillagerEntity) {
            flag = ((AbstractVillagerEntity)p_225597_1_).getUnhappyCounter() > 0;
        }

        this.head.yRot = p_225597_5_ * ((float)Math.PI / 180F);
        this.head.xRot = p_225597_6_ * ((float)Math.PI / 180F);
        if (flag) {
            this.head.zRot = 0.3F * MathHelper.sin(0.45F * p_225597_4_);
            this.head.xRot = 0.4F;
        } else {
            this.head.zRot = 0.0F;
        }

        this.arms.y = 3.0F;
        this.arms.z = -1.0F;
        this.arms.xRot = -0.75F;
        this.leg0.xRot = MathHelper.cos(p_225597_2_ * 0.6662F) * 1.4F * p_225597_3_ * 0.5F;
        this.leg1.xRot = MathHelper.cos(p_225597_2_ * 0.6662F + (float)Math.PI) * 1.4F * p_225597_3_ * 0.5F;
        this.leg0.yRot = 0.0F;
        this.leg1.yRot = 0.0F;
    }

    public ModelRenderer getHead() {
        return this.head;
    }

    public void hatVisible(boolean p_217146_1_) {
        this.head.visible = p_217146_1_;
        this.hat.visible = p_217146_1_;
        this.hatRim.visible = p_217146_1_;
    }
}