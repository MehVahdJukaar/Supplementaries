package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.events.forge.ClientEventsForge;
import net.mehvahdjukaar.supplementaries.common.events.forge.ServerEventsForge;

import net.mehvahdjukaar.supplementaries.common.items.forge.ShulkerShellItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSetup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Author: MehVahdJukaar
 */
@Mod(Supplementaries.MOD_ID)
public class SupplementariesForge {


    public SupplementariesForge() {
        Supplementaries.commonInit();


        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);

        ServerEventsForge.init();

        PlatHelper.getPhysicalSide().ifClient(() -> {
            ClientRegistry.init();
            ClientEventsForge.init();
            ClientHelper.addClientSetup(ClientRegistry::setup);
        });
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        CapabilityHandler.register(event);
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModSetup::setup);
        ModSetup.asyncSetup();
    }

    @SubscribeEvent
    public void registerOverrides(RegisterEvent event) {
        if (event.getRegistryKey() == ForgeRegistries.ITEMS.getRegistryKey()) {
            if (CommonConfigs.Tweaks.SHULKER_HELMET_ENABLED.get()) {

                event.getForgeRegistry().register(new ResourceLocation("minecraft:shulker_shell"),
                        new ShulkerShellItem(new Item.Properties()
                                .stacksTo(64)
                                ));
            }
        }
    }


    public static final ToolAction SOAP_CLEAN = ToolAction.get("soap_clean");



}
