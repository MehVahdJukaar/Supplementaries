package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.block.util.ICustomDataHolder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.client.model.HorseModel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(UndeadHorseRenderer.class)
public abstract class UndeadHorseRenderMixin extends AbstractHorseRenderer<AbstractHorse, HorseModel<AbstractHorse>> {


    public UndeadHorseRenderMixin(EntityRendererProvider.Context p_173906_, HorseModel<AbstractHorse> p_173907_, float p_173908_) {
        super(p_173906_, p_173907_, p_173908_);
    }

    @Override
    public void render(AbstractHorse p_225623_1_, float p_225623_2_, float p_225623_3_, PoseStack p_225623_4_, MultiBufferSource p_225623_5_, int p_225623_6_) {
        super.render(p_225623_1_, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
    }

    @Override
    public void setupRotations(AbstractHorse p_225621_1_, PoseStack p_225621_2_, float p_225621_3_, float p_225621_4_, float p_225621_5_) {
        if (this.isShaking(p_225621_1_)) {
            p_225621_4_ += (float)(Math.cos((double)p_225621_1_.tickCount * 3.25D) * Math.PI * (double)0.4F);
        }
        super.setupRotations(p_225621_1_, p_225621_2_, p_225621_3_, p_225621_4_, p_225621_5_);

    }

    @Override
    public boolean isShaking(AbstractHorse horse) {
        return (horse instanceof ICustomDataHolder && ((ICustomDataHolder) horse).getVariable());
    }
}