package net.mehvahdjukaar.supplementaries.integration;

import it.crystalnest.soul_fire_d.api.Fire;
import it.crystalnest.soul_fire_d.api.FireManager;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GlobeBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class SoulFiredCompat {
    public static final ResourceLocation LUMISENE_FIRE_TYPE = Supplementaries.res("lumisene");

    public static void setup() {

        //why arent these null by default? seems bad api
        var fire = FireManager.fireBuilder(LUMISENE_FIRE_TYPE)
                .setDamage(1)
                .setCanRainDouse(false)
              //  .setComponent(Fire.Component.SOURCE_BLOCK, ModFluids.LUMISENE_BLOCK.getId())
                .removeComponent(Fire.Component.SOURCE_BLOCK) //DUMB above doesnt use it
                .removeComponent(Fire.Component.WALL_TORCH_BLOCK)
                .removeComponent(Fire.Component.CAMPFIRE_BLOCK)
                .removeComponent(Fire.Component.FLAME_PARTICLE)
                .removeComponent(Fire.Component.CAMPFIRE_ITEM)
                .removeComponent(Fire.Component.LANTERN_BLOCK)
                .removeComponent(Fire.Component.LANTERN_BLOCK)
                .removeComponent(Fire.Component.TORCH_BLOCK)
                .removeComponent(Fire.Component.TORCH_ITEM);
        try {
            fire.setComponent(Fire.Component.SOURCE_BLOCK, ModFluids.LUMISENE_BLOCK.getId());
        } catch (Exception e) {
            Supplementaries.LOGGER.error("Some error from soul fired: ", e);
        }


        FireManager.registerFire(fire.build());
    }

    public static void setOnFire(Entity entity, int duration) {
        FireManager.setOnFire(entity, duration, LUMISENE_FIRE_TYPE);
    }
}
