package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.world.entity.Mob;

import static net.mehvahdjukaar.supplementaries.common.entities.goals.PlundererAICommon.*;

@Deprecated(forRemoval = true)
public class ManeuverAndShootCannonGoal extends UseCannonBoatGoal {

    public ManeuverAndShootCannonGoal(Mob mob, int a, int b, int minRange, int maxDuration) {
        this(mob, minRange, maxDuration);
    }

    public ManeuverAndShootCannonGoal(Mob mob, int attackIntervalMin, int attackIntervalMax) {
        super(mob, MAX_USE_CANNON_BOAT, MIN_CANNON_RANGE, attackIntervalMin, attackIntervalMax, MAX_TIME_WITHOUT_SHOOTING);
    }
}
