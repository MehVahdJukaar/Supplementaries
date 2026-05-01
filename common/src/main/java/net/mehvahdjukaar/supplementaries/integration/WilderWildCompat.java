package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

;

public class WilderWildCompat {

    private static final Class<?> WIND_MANAGER;
    private static final Method GET_WIND_ANGLE;

    static {
        try {
            WIND_MANAGER = Class.forName("net.frozenblock.lib.wind.api.ClientWindManager");
            GET_WIND_ANGLE = WIND_MANAGER.getDeclaredMethod("getWindMovement", Level.class, BlockPos.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static float getWindAngle(BlockPos pos, Level level) {
        try {
            Vec3 wind = (Vec3) GET_WIND_ANGLE.invoke(null, level, pos);
            return (float) MthUtils.getYaw(wind) - 90;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Vec3 getParticleWindDirection(Vec3 pos, Level level, float scale) {
        return Vec3.ZERO;// ClientWindManager.getWindMovement(level, pos, scale, 7.0, 5.0).scale(AmbienceAndMiscConfig.getParticleWindIntensity());
    }
}
