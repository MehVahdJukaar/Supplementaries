package net.mehvahdjukaar.supplementaries.reg;

import net.minecraft.world.damagesource.DamageSource;

public class ModDamageSources extends DamageSource {

    protected ModDamageSources(String string) {
        super(string);
    }
    public static final DamageSource SPIKE_DAMAGE = new ModDamageSources("supplementaries.bamboo_spikes");
    public static final DamageSource BOTTLING_DAMAGE = new ModDamageSources("supplementaries.xp_extracting");

}
