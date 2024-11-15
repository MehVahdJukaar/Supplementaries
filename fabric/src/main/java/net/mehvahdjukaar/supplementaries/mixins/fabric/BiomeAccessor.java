package net.mehvahdjukaar.supplementaries.mixins.fabric;

import net.fabricmc.fabric.impl.resource.conditions.conditions.AllModsLoadedResourceCondition;
import net.fabricmc.fabric.impl.screenhandler.Networking;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Biome.class)
public interface BiomeAccessor {

    @Accessor("climateSettings")
    Biome.ClimateSettings getClimateSettings();

}
