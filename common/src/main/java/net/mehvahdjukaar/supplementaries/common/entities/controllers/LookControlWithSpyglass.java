package net.mehvahdjukaar.supplementaries.common.entities.controllers;

import net.mehvahdjukaar.supplementaries.common.entities.ISpyglassMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

public class LookControlWithSpyglass<M extends Mob & ISpyglassMob> extends LookControl {
    private final M spyglassMob;

    private int useSpyglassDuration = 0;

    public LookControlWithSpyglass(M mob) {
        super(mob);
        this.spyglassMob = mob;
    }

    @Override
    public void setLookAt(double x, double y, double z, float deltaYaw, float deltaPitch) {
        super.setLookAt(x, y, z, deltaYaw, deltaPitch);

        double dist = mob.distanceToSqr(x, y, z);
        double startUsingSpyglassDist = spyglassMob.getStartUsingSpyglassDistance();
        boolean shouldUseSpyglass = dist >= startUsingSpyglassDist * startUsingSpyglassDist;
        spyglassMob.setUsingSpyglass(shouldUseSpyglass);
        if (shouldUseSpyglass) {
            useSpyglassDuration = 10; //after 10 ticks pull out spyglass
        } else {
            useSpyglassDuration = 0;
        }
    }


    @Override
    public void tick() {
        super.tick();
        if (useSpyglassDuration > 0) {
            useSpyglassDuration--;
            if (useSpyglassDuration == 0) {
                spyglassMob.setUsingSpyglass(false);
            }
        }
    }
}
