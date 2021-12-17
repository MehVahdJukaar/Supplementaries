package net.mehvahdjukaar.supplementaries.setup;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.capabilities.mobholder.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.common.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.entities.trades.VillagerTradesHandler;
import net.mehvahdjukaar.supplementaries.events.ItemsOverrideHandler;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.commands.ModCommands;
import net.mehvahdjukaar.supplementaries.world.data.map.CMDreg;
import net.mehvahdjukaar.supplementaries.world.data.map.WeatheredMap;
import net.mehvahdjukaar.supplementaries.world.generation.FeaturesRegistry;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {

    public static void init(final FMLCommonSetupEvent event) {

        event.enqueueWork(() -> {
            try {


                // StructureRegistry.setup();
                FeaturesRegistry.setup();
                setupStage++;

                //  StructureLocator.init();
                setupStage++;

                CompatHandler.init();
                setupStage++;

                CMDreg.init(event);
                setupStage++;

                WeatheredMap.init();
                setupStage++;

                WorldGenSetup.registerMobSpawns();
                setupStage++;

                ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_SEEDS_ITEM.get(), 0.3F);
                ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_ITEM.get(), 0.65F);
                ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_BLOCK_ITEM.get(), 1);
                setupStage++;

                FlowerPotHandler.init();
                setupStage++;

                CapturedMobsHelper.refresh();
                setupStage++;

                ModSoftFluids.init();
                setupStage++;

                NetworkHandler.registerMessages();
                setupStage++;

                LootTableStuff.init();
                setupStage++;
                registerMobFoods();

                hasFinishedSetup = true;

            } catch (Exception e) {
                Supplementaries.LOGGER.throwing(new Exception("Exception during mod setup:" + e + ". This is a big bug"));
                terminateWhenSetupFails();
            }

        });
    }

    private static void terminateWhenSetupFails() {
        //if setup fails crash the game. idk why it doesn't do that on its own wtf
        IllegalStateException e = new IllegalStateException("Mod setup has failed to complete (stage = " + setupStage + "). This might be due to some mod incompatibility. Refusing to continue loading with a broken modstate. Next step: crashing this game, no survivors. Executing 69/0");
        Supplementaries.LOGGER.throwing(e);
        throw e;
    }

    private static void registerMobFoods() {
        List<ItemStack> chickenFood = new ArrayList<>(List.of(Chicken.FOOD_ITEMS.getItems()));
        chickenFood.add(new ItemStack(ModRegistry.FLAX_SEEDS_ITEM.get()));
        Chicken.FOOD_ITEMS = Ingredient.of(chickenFood.stream());

        List<ItemStack> horseFood = new ArrayList<>(List.of(new ItemStack(ModRegistry.FLAX_ITEM.get()), new ItemStack(ModRegistry.FLAX_BLOCK_ITEM.get())));
        horseFood.addAll(List.of(AbstractHorse.FOOD_ITEMS.getItems()));
        AbstractHorse.FOOD_ITEMS = Ingredient.of(horseFood.stream());
    }

    //damn I hate this. If setup fails forge doesn't do anything and it keeps on going quietly
    private static boolean hasFinishedSetup = false;
    private static int setupStage = 0;
    public static boolean firstTagLoad = false;

    //events on setup
    @SubscribeEvent
    public static void onTagLoad(TagsUpdatedEvent event) {
        if (!firstTagLoad) {

            //using this as a post setup event
            if (!hasFinishedSetup) {
                terminateWhenSetupFails();
            }

            firstTagLoad = true;
            DispenserRegistry.registerBehaviors();
            ItemsOverrideHandler.registerOverrides();
        }
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        CapabilityHandler.register(event);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void villagerTradesEvent(VillagerTradesEvent event) {
        VillagerTradesHandler.registerVillagerTrades(event);
    }

    @SubscribeEvent
    public static void registerWanderingTraderTrades(WandererTradesEvent event) {
        VillagerTradesHandler.registerWanderingTraderTrades(event);
    }

    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent e) {
        LootTableStuff.injectLootTables(e);
    }

    @SubscribeEvent
    public static void serverAboutToStart(final ServerAboutToStartEvent event) {

    }

    //TODO: move to ModSetup
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerStuffToBiomes(BiomeLoadingEvent event) {
        WorldGenSetup.registerStuffToBiomes(event);
    }

}
