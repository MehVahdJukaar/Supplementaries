package net.mehvahdjukaar.supplementaries.integration.platform;

import com.teamabnormals.caverns_and_chasms.common.entity.animal.rat.Rat;
import net.minecraft.world.entity.Entity;

public class CavernsAndChasmsCompatImpl {

    public static boolean maybeCleanDirtyRat(Entity entity) {
        if (entity instanceof Rat rat && rat.isDirty()) {
            rat.setDirty(false);
            return true;
        }
        return false;
    }
}
