package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

// yes this bs here could have been done with a data attachment instead...
public interface ISlimeable {


    int supp$getSlimedTicks();

    void supp$setSlimedTicks(int slimed, boolean sync);

    static float getAlpha(LivingEntity le, float partialTicks) {
        float slimeTicks = ((ISlimeable) le).supp$getSlimedTicks() - partialTicks;
        float maxFade = 70;
        return slimeTicks > maxFade ? 1 : Mth.clamp(slimeTicks / maxFade, 0, 1);
    }

    static void tickEntity(LivingEntity entity) {
        ISlimeable slimed = (ISlimeable) entity;
        int t = slimed.supp$getSlimedTicks();
        if (t > 0 && CommonConfigs.Tweaks.SLIME_OVERLAY.get()) {
            if (entity.isUnderWater()) {
                slimed.supp$setSlimedTicks(0, true);
            } else slimed.supp$setSlimedTicks(t - 1, false);
        }
    }

}
