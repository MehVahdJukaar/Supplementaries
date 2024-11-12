package net.mehvahdjukaar.supplementaries.neoforge;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.events.neoforge.ClientEventsForge;
import net.mehvahdjukaar.supplementaries.common.events.neoforge.ServerEventsForge;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

/**
 * Author: MehVahdJukaar
 */
@Mod(Supplementaries.MOD_ID)
public class SupplementariesForge {

    public static WeakReference<IEventBus> modBus;

    public SupplementariesForge(IEventBus bus) {
        modBus = new WeakReference<>(bus);
        RegHelper.startRegisteringFor(bus);
        Supplementaries.commonInit();

        bus.register(this);
        CapabilityHandler.init(bus);

        ServerEventsForge.init();
        VillagerScareStuff.init();

        PlatHelper.getPhysicalSide().ifClient(() -> {
            ClientRegistry.init();
            ClientEventsForge.init();
        });

        LOOT_MODIFIERS.register(bus);
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        CapabilityHandler.register(event);
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent event) {
        VillagerScareStuff.setup();
    }

    public static final ItemAbility SOAP_CLEAN = ItemAbility.get("soap_clean");


    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(
            NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Supplementaries.MOD_ID);

    public static final Supplier<MapCodec<ReplaceRopeByConfigModifier>> REPLACE_ROPE =
            LOOT_MODIFIERS.register("replace_rope", ReplaceRopeByConfigModifier.CODEC);


}
