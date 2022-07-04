package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.supplementaries.SupplementariesNewInit;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Author: MehVahdJukaar
 */
@Mod(SupplementariesNewInit.MOD_ID)
public class SupplementariesForge {

    public static final String MOD_ID = SupplementariesNewInit.MOD_ID;

    public SupplementariesForge() {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();


        SupplementariesNewInit.commonInit();


        /**
         * Update stuff:
         * Configs
         * sand later
         * ash layer
         * leaf layer
         */

        //TODO: fix layers texture generation
        //TODO: fix grass growth replacing double plants and add tag


        bus.addListener(SupplementariesForge::init);
        bus.addListener(SupplementariesForge::registerAdditional);
    }


    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SupplementariesNewInit.commonSetup();
        });

    }


    public static void registerAdditional(RegisterEvent event) {
        if (!event.getRegistryKey().equals(ForgeRegistries.ITEMS.getRegistryKey())) return;
        SupplementariesNewInit.commonRegistration();
    }


}
