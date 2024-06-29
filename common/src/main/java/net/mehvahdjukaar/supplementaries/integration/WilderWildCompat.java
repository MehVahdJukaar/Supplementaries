package net.mehvahdjukaar.supplementaries.integration;

import net.frozenblock.lib.wind.api.ClientWindManager;
import net.frozenblock.wilderwild.config.AmbienceAndMiscConfig;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WilderWildCompat {

    public static float getWindAngle(BlockPos pos, Level level) {
        Vec3 wind = ClientWindManager.getWindMovement(level, pos);
        return (float) MthUtils.getYaw(wind) - 90;
    }

    public static Vec3 getParticleWindDirection(Vec3 pos, Level level, float scale) {
        return ClientWindManager.getWindMovement(level, pos,
                scale, 7.0, 5.0).scale(AmbienceAndMiscConfig.getParticleWindIntensity());
    }
}
