package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.ICustomDataHolder;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(UndeadHorseRenderer.class)
public abstract class UndeadHorseRenderMixin extends AbstractHorseRenderer<AbstractHorse, HorseModel<AbstractHorse>> {

    protected UndeadHorseRenderMixin(EntityRendererProvider.Context context, HorseModel<AbstractHorse> abstractHorseHorseModel, float v) {
        super(context, abstractHorseHorseModel, v);
    }

    @Override
    public boolean isShaking(AbstractHorse horse) {
        return super.isShaking(horse) || (horse instanceof ICustomDataHolder dataHolder && dataHolder.supplementaries$getVariable());
    }
}