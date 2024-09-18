package net.mehvahdjukaar.supplementaries.common.utils;

import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.minecraft.core.Holder;

public interface IExplorationFunctionExtension {

    Holder<MLMapDecorationType<?, ?>> supplementaries$getCustomDecoration();

    void supplementaries$setCustomDecoration(Holder<MLMapDecorationType<?, ?>> deco);

}
