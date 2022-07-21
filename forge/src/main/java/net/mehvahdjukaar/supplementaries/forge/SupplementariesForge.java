package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.WallLanternTexturesRegistry;
import net.mehvahdjukaar.supplementaries.common.capabilities.forge.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.items.crafting.forge.OptionalRecipeCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Author: MehVahdJukaar
 */
@Mod(Supplementaries.MOD_ID)
public class SupplementariesForge {

    public static final String MOD_ID = Supplementaries.MOD_ID;

    public SupplementariesForge() {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        Supplementaries.commonInit();

        CraftingHelper.register(new OptionalRecipeCondition.Serializer());

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

        MinecraftForge.EVENT_BUS.register(this);

        //wall lantern and jar jsons
        if (!FMLLoader.getLaunchHandler().isData()) {
            ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager())
                    .registerReloadListener(new WallLanternTexturesRegistry());
        }
    }


    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Supplementaries.commonSetup();
        });

    }


    public static void registerAdditional(RegisterEvent event) {
        if (!event.getRegistryKey().equals(ForgeRegistries.ITEMS.getRegistryKey())) return;
        Supplementaries.commonRegistration();
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        CapabilityHandler.register(event);
    }



}
