package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.world.entity.Mob;

@Deprecated(forRemoval = true)
public class ManeuverAndShootCannonGoal extends UseCannonBoatGoal{

    public ManeuverAndShootCannonGoal(Mob mob, int a, int b, int minRange, int maxDuration) {
        super(mob, a, b, minRange, maxDuration);
    }
}
