package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.util.ICustomDataHolder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.client.renderer.entity.model.HorseModel;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(UndeadHorseRenderer.class)
public abstract class UndeadHorseRendererMixin extends AbstractHorseRenderer<AbstractHorseEntity, HorseModel<AbstractHorseEntity>> {


    public UndeadHorseRendererMixin(EntityRendererManager p_i50961_1_, HorseModel<AbstractHorseEntity> p_i50961_2_, float p_i50961_3_) {
        super(p_i50961_1_, p_i50961_2_, p_i50961_3_);
    }


    @Override
    public void render(AbstractHorseEntity p_225623_1_, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, IRenderTypeBuffer p_225623_5_, int p_225623_6_) {
        super.render(p_225623_1_, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
    }

    @Override
    public void setupRotations(AbstractHorseEntity p_225621_1_, MatrixStack p_225621_2_, float p_225621_3_, float p_225621_4_, float p_225621_5_) {
        if (this.isShaking(p_225621_1_)) {
            p_225621_4_ += (float)(Math.cos((double)p_225621_1_.tickCount * 3.25D) * Math.PI * (double)0.4F);
        }
        super.setupRotations(p_225621_1_, p_225621_2_, p_225621_3_, p_225621_4_, p_225621_5_);

    }

    @Override
    public boolean isShaking(AbstractHorseEntity horse) {
        return (horse instanceof ICustomDataHolder && ((ICustomDataHolder) horse).getVariable());
    }
}