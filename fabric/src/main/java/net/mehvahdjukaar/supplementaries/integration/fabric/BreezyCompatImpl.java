package net.mehvahdjukaar.supplementaries.integration.fabric;

import net.mehvahdjukaar.moonlight.core.mixins.fabric.ShaderInstanceMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BreezyCompatImpl {
    public static float getWindAngle(BlockPos pos, Level level) {
        return 90;
    }

}
