package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.common.block.dispenser.DispenserBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimesManager;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.PlaceableBookManager;
import net.mehvahdjukaar.supplementaries.common.entities.trades.ModVillagerTrades;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.mehvahdjukaar.supplementaries.common.events.overrides.InteractEventsHandler;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.ColoredMapHandler;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.DepthDataHandler;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.MapLightHandler;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.WeatheredHandler;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.CapturedMobHandler;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.common.utils.Credits;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.dynamicpack.ModClientDynamicResources;
import net.mehvahdjukaar.supplementaries.dynamicpack.ModServerDynamicResources;
import net.mehvahdjukaar.supplementaries.reg.*;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Supplementaries {

    public static final String MOD_ID = "supplementaries";

    public static final Logger LOGGER = LogManager.getLogger("Supplementaries");

    public static ResourceLocation res(String n) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, n);
    }

    public static ResourceLocation parseRes(String n) {
        if (n.contains(":")) return ResourceLocation.parse(n);
        else return res(n);
    }

    public static String str(String n) {
        return MOD_ID + ":" + n;
    }

    //called on mod creation
    public static void commonInit() {
        Credits.fetchFromServer();
        CommonConfigs.init();

        PlatHelper.getPhysicalSide().ifClient(ClientConfigs::init);

        RegHelper.registerSimpleRecipeCondition(res("flag"), CommonConfigs::isEnabled);

        MoonlightEventsHelper.addListener(ServerEvents::onFireConsume, IFireConsumeBlockEvent.class);

        ModSetup.init();
        ModNetwork.init();
        ModSounds.init();
        ModFluids.init();
        ModRegistry.init();
        ModComponents.init();
        ModRecipes.init();
        ModMenuTypes.init();
        ModEntities.init();
        ModParticles.init();
        ModEnchantments.init();
        ModCommands.init();
        ModVillagerTrades.init();
        ModWorldgen.init();
        ModMapMarkers.init();
        ModCreativeTabs.init();
        LootTablesInjects.init();
        InteractEventsHandler.init();
        DepthDataHandler.init();
        WeatheredHandler.init();
        ColoredMapHandler.init();
        MapLightHandler.init();
        DispenserBehaviorsManager.init();
        PlaceableBookManager.init();

        //init
        var k = SlotReference.TYPE_REGISTRY_KEY;
        RegHelper.registerDynamicResourceProvider(new ModServerDynamicResources());

        PlatHelper.addServerReloadListener(SongsManager::new, res("flute_songs"));
        PlatHelper.addServerReloadListener(HourglassTimesManager::new, res("hourglass_data"));
        PlatHelper.addServerReloadListener(FaucetBehaviorsManager::new, res("faucet_interactions"));
        PlatHelper.addServerReloadListener(CapturedMobHandler::new, res("catchable_mobs_properties"));

        if (PlatHelper.getPhysicalSide().isClient()) {
            RegHelper.registerDynamicResourceProvider(new ModClientDynamicResources());
            try {
                ClientHelper.registerOptionalTexturePack(res("darker_ropes"), false);
            } catch (Exception e) {
                Supplementaries.LOGGER.error(e);
            }
        }

    }

    public static void error() {
        if (PlatHelper.isDev()) {
            LOGGER.error("This should not happen");
        }
    }

    public static void error(String message, Object... params) {
        error();
        LOGGER.error(message, params);
    }


}
