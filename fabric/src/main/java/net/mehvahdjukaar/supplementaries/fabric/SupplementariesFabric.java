package net.mehvahdjukaar.supplementaries.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.impl.content.registry.FireBlockHooks;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.fabric.FabricSetupCallbacks;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.events.fabric.ClientEventsFabric;
import net.mehvahdjukaar.supplementaries.common.events.fabric.ServerEventsFabric;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.level.block.Block;

public class SupplementariesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Supplementaries.commonInit();

        ServerEventsFabric.init();
        FabricSetupCallbacks.COMMON_SETUP.add(Supplementaries::commonSetup);

        if (PlatformHelper.getEnv().isClient()) {
            ClientEventsFabric.init();
            FabricSetupCallbacks.CLIENT_SETUP.add(SupplementariesFabricClient::clientSetup);
            throwIfFabricRenderingAPIHasBeenNuked();
        }
        RegHelper.registerBlockFlammability(ModRegistry.ROPE.get(),60,100);
    }

    //I hate this. I've got to do what I've got to do. Cant stand random reports anymore
    public static void throwIfFabricRenderingAPIHasBeenNuked() {
        if (PlatformHelper.isModLoaded("sodium") && !PlatformHelper.isModLoaded("indium")) {
            throw new IllegalStateException("You seem to have installed Sodium which breaks fabric rendering API." +
                    "To fix you must install Indium as Supplementaries, as many other mods, rely on said API");
        }
    }
}
