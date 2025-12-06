package net.mehvahdjukaar.supplementaries.common.entities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public interface ISpyglassMob {

    default float getSpyglassMaxSeeDistance() {
        return 70;
    }

    default double getStartUsingSpyglassDistance() {
        return 18;// ((LivingEntity) this).getAttribute(Attributes.FOLLOW_RANGE).getValue();
    }

    boolean isUsingSpyglass();

    void setUsingSpyglass(boolean using);
}
