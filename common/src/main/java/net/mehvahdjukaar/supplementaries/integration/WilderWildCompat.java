package net.mehvahdjukaar.supplementaries.integration;

import net.frozenblock.lib.wind.api.ClientWindManager;
import net.mehvahdjukaar.moonlight.api.misc.TMethod;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WilderWildCompat {

    private static final TMethod<ClientWindManager, Vec3> GET_WIND_ANGLE =
            TMethod.of(ClientWindManager.class, "getWindMovement");

    public static float getWindAngle(BlockPos pos, Level level) {
        Vec3 wind = GET_WIND_ANGLE.invoke(null, level, pos);
        return (float) MthUtils.getYaw(wind) - 90;
    }

    public static Vec3 getParticleWindDirection(Vec3 pos, Level level, float scale) {
        return Vec3.ZERO;// ClientWindManager.getWindMovement(level, pos, scale, 7.0, 5.0).scale(AmbienceAndMiscConfig.getParticleWindIntensity());
    }
}
