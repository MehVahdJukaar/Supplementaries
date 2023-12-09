package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.integration.quark.TaterInAJarTileRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.violetmoon.quark.api.event.UsageTickerEvent;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.GeneralConfig;
import org.violetmoon.quark.content.client.module.ImprovedTooltipsModule;

public class QuarkClientCompat {

    @ExpectPlatform
    public static void initClient() {

    }

    public static void registerEntityRenderers(ClientHelper.BlockEntityRendererEvent event) {
        event.register(QuarkCompat.TATER_IN_A_JAR_TILE.get(), TaterInAJarTileRenderer::new);
    }

    public static void setupClient() {
        ClientHelper.registerRenderType(QuarkCompat.TATER_IN_A_JAR.get(), RenderType.cutout());
    }

    public static boolean shouldHaveButtonOnRight() {
        return !(GeneralConfig.qButtonOnRight && GeneralConfig.enableQButton);
    }

    public static boolean canRenderBlackboardTooltip() {
        return canRenderQuarkTooltip();
    }

    public static boolean canRenderQuarkTooltip() {
        return Quark.ZETA.modules.isEnabled(ImprovedTooltipsModule.class)
                && ImprovedTooltipsModule.shulkerTooltips &&
                (!ImprovedTooltipsModule.shulkerBoxRequireShift || Screen.hasShiftDown());
    }



}
