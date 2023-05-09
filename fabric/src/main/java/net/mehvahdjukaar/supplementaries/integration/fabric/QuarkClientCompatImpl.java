package net.mehvahdjukaar.supplementaries.integration.fabric;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.common.items.SoapItem;
import net.mehvahdjukaar.supplementaries.common.misc.SoapWashableHelper;

public class QuarkClientCompatImpl {

    public static boolean shouldHaveButtonOnRight() {
        return false;
    }

    public static boolean canRenderBlackboardTooltip() {
        return true;
    }

    public static boolean canRenderQuarkTooltip() {
        return true;
    }

    public static void registerTooltipComponent(ClientHelper.TooltipComponentEvent event) {

    }

    public static void init() {
    }
}
