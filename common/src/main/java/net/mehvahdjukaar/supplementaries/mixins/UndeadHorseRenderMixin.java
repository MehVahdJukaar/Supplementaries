package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.util.ICustomDataHolder;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(UndeadHorseRenderer.class)
public abstract class UndeadHorseRenderMixin extends AbstractHorseRenderer<AbstractHorse, HorseModel<AbstractHorse>> {

    public UndeadHorseRenderMixin(EntityRendererProvider.Context p_173906_, HorseModel<AbstractHorse> p_173907_, float p_173908_) {
        super(p_173906_, p_173907_, p_173908_);
    }

    @Override
    public boolean isShaking(AbstractHorse horse) {
        return super.isShaking(horse) || (horse instanceof ICustomDataHolder && ((ICustomDataHolder) horse).getVariable());
    }
}