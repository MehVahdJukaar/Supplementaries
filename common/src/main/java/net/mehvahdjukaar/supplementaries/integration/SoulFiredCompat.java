package net.mehvahdjukaar.supplementaries.integration;

import it.crystalnest.soul_fire_d.api.FireManager;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class SoulFiredCompat {
    public static final ResourceLocation LUMISENE_FIRE_TYPE = Supplementaries.res("lumisene");

    public static void setup() {
        FireManager.registerFire(
                FireManager.fireBuilder(LUMISENE_FIRE_TYPE)
                        .setDamage(1)
                        .setCanRainDouse(false)
                        .build()
        );
    }

    public static void setOnFire(Entity entity, int duration) {
        FireManager.setOnFire(entity, duration, LUMISENE_FIRE_TYPE);
    }
}
