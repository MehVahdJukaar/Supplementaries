package net.mehvahdjukaar.supplementaries.neoforge;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.events.neoforge.ClientEventsForge;
import net.mehvahdjukaar.supplementaries.common.events.neoforge.ServerEventsForge;
import net.mehvahdjukaar.supplementaries.common.items.neoforge.ShulkerShellItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSetup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Author: MehVahdJukaar
 */
@Mod(Supplementaries.MOD_ID)
public class SupplementariesForge {


    public SupplementariesForge(IEventBus bus) {
        Supplementaries.commonInit();

        bus.register(this);

        ServerEventsForge.init();
        VillagerScareStuff.init();

        PlatHelper.getPhysicalSide().ifClient(() -> {
            ClientRegistry.init();
            ClientEventsForge.init();
        });
    }


    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        CapabilityHandler.register(event);
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent event) {
        VillagerScareStuff.setup();
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
