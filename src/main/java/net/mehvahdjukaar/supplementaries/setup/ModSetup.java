package net.mehvahdjukaar.supplementaries.setup;


import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.capabilities.mobholder.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.common.entities.trades.VillagerTradesHandler;
import net.mehvahdjukaar.supplementaries.common.events.ItemsOverrideHandler;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.commands.ModCommands;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.mehvahdjukaar.supplementaries.common.world.data.map.WeatheredMap;
import net.mehvahdjukaar.supplementaries.common.world.generation.WorldGenHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
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

                Stopwatch watch = Stopwatch.createStarted();

                CompatHandler.init();
                setupStage++;

                CMDreg.init(event);
                setupStage++;

                WeatheredMap.init();
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

                registerCompostables();
                setupStage++;

                registerMobFoods();
                setupStage++;

                CauldronRegistry.registerInteractions();
                setupStage++;

                PresentRegistry.registerBehaviors();
                setupStage++;

                hasFinishedSetup = true;

                Supplementaries.LOGGER.info("Finished mod setup in: {} seconds", watch.elapsed().toSeconds());

            } catch (Exception e) {
                terminateWhenSetupFails();
            }

        });
    }

    private static void terminateWhenSetupFails() {
        //if setup fails crash the game. idk why it doesn't do that on its own wtf
        //Supplementaries.LOGGER.throwing(e);
        throw new IllegalStateException("Mod setup has failed to complete (" + setupStage + ").\n" +
                " This might be due to some mod incompatibility or outdated dependencies (check if everything is up to date).\n" +
                " Refusing to continue loading with a broken modstate. Next step: crashing this game, no survivors");
    }

    private static void registerMobFoods() {
        List<ItemStack> chickenFood = new ArrayList<>(List.of(Chicken.FOOD_ITEMS.getItems()));
        chickenFood.add(new ItemStack(ModRegistry.FLAX_SEEDS_ITEM.get()));
        Chicken.FOOD_ITEMS = Ingredient.of(chickenFood.stream());

        List<ItemStack> horseFood = new ArrayList<>(List.of(new ItemStack(ModRegistry.FLAX_ITEM.get()), new ItemStack(ModRegistry.FLAX_BLOCK_ITEM.get())));
        horseFood.addAll(List.of(AbstractHorse.FOOD_ITEMS.getItems()));
        AbstractHorse.FOOD_ITEMS = Ingredient.of(horseFood.stream());
    }

    private static void registerCompostables() {
        ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_SEEDS_ITEM.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_ITEM.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_WILD_ITEM.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_BLOCK_ITEM.get(), 1);
    }


    //damn I hate this. If setup fails forge doesn't do anything and it keeps on going quietly
    private static boolean hasFinishedSetup = false;
    private static int setupStage = 0;
    public static boolean firstTagLoad = false;

    //events on setup
    @SubscribeEvent
    public static void onTagLoad(TagsUpdatedEvent event) {
        if (!firstTagLoad) {
            firstTagLoad = true;
            if (!hasFinishedSetup) {
                terminateWhenSetupFails();
            }
            //using this as a post setup event that can access tags
            Stopwatch watch = Stopwatch.createStarted();

            DispenserRegistry.registerBehaviors();
            ItemsOverrideHandler.registerOverrides();

            Supplementaries.LOGGER.info("Finished additional setup in {} seconds", watch.elapsed().toSeconds());
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


}
