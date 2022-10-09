package net.mehvahdjukaar.supplementaries.integration.fabric;

import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

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

    public static void registerTooltipComponent(ClientPlatformHelper.TooltipComponentEvent event) {

    }
}
