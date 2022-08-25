package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Contract;

import java.util.List;

public class QuarkClientCompat {

    @Contract
    @ExpectPlatform
    public static boolean shouldHaveButtonOnRight() {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean canRenderBlackboardTooltip() {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean canRenderQuarkTooltip() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerTooltipComponent(ClientPlatformHelper.TooltipComponentEvent event) {
        throw new AssertionError();
    }

}
