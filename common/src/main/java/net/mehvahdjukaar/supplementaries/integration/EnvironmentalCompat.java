package net.mehvahdjukaar.supplementaries.integration;

import com.teamabnormals.blueprint.common.world.storage.tracking.IDataManager;
import com.teamabnormals.environmental.core.other.EnvironmentalDataProcessors;
import net.minecraft.world.entity.animal.Pig;

public class EnvironmentalCompat {

    public static boolean maybeCleanMuddyPig(Pig pig) {
        IDataManager data = (IDataManager) pig;
        if (data.getValue(EnvironmentalDataProcessors.IS_MUDDY)) {
            data.setValue(EnvironmentalDataProcessors.IS_MUDDY, false);
            return true;
        }
        return false;
    }
}
