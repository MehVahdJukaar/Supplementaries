package net.mehvahdjukaar.supplementaries.common.entities.controllers;

import net.mehvahdjukaar.supplementaries.common.entities.ISpyglassMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

public class LookControlWithSpyglass<M extends Mob & ISpyglassMob> extends LookControl {
    private final M spyglassMob;

    public LookControlWithSpyglass(M mob) {
        super(mob);
        this.spyglassMob = mob;
    }

    @Override
    public void setLookAt(double x, double y, double z, float deltaYaw, float deltaPitch) {
        super.setLookAt(x, y, z, deltaYaw, deltaPitch);

        double dist = mob.distanceToSqr(x, y, z);
        double startUsingSpyglassDist = spyglassMob.getStartUsingSpyglassDistance();
        spyglassMob.setUsingSpyglass(dist >= startUsingSpyglassDist * startUsingSpyglassDist);
    }
}
