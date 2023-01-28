package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.events.forge.ClientEventsForge;
import net.mehvahdjukaar.supplementaries.common.events.forge.ServerEventsForge;
import net.mehvahdjukaar.supplementaries.common.items.forge.ShulkerShellItem;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
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
        bus.addListener(SupplementariesForge::registerOverrides);

        ServerEventsForge.init();
        ModLootModifiers.init();

        PlatformHelper.getEnv().ifClient(() -> {
            ClientRegistry.init();
            ClientEventsForge.init();
            ClientPlatformHelper.addClientSetup(ClientRegistry::setup);
        });
    }

    public static void registerOverrides(RegisterEvent event) {
        if (event.getRegistryKey() == ForgeRegistries.ITEMS.getRegistryKey()) {
            if (RegistryConfigs.SHULKER_HELMET_ENABLED.get()) {

                event.getForgeRegistry().register(new ResourceLocation("minecraft:shulker_shell"),
                        new ShulkerShellItem(new Item.Properties()
                                .stacksTo(64)
                                .tab(CreativeModeTab.TAB_MATERIALS)));
            }
        }
    }


    public static final ToolAction SOAP_CLEAN = ToolAction.get("soap_clean");

}
