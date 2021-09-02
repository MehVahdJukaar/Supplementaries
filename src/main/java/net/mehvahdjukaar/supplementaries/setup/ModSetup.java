package net.mehvahdjukaar.supplementaries.setup;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.common.AdventurerMapsHandler;
import net.mehvahdjukaar.supplementaries.common.FlowerPotHelper;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.entities.VillagerTradesHandler;
import net.mehvahdjukaar.supplementaries.events.ItemsOverrideHandler;
import net.mehvahdjukaar.supplementaries.fluids.ModSoftFluids;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.commands.ModCommands;
import net.mehvahdjukaar.supplementaries.world.data.map.CMDreg;
import net.mehvahdjukaar.supplementaries.world.structures.StructureLocator;
import net.mehvahdjukaar.supplementaries.world.structures.StructureRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.BasicTrade;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {


    public static void init(final FMLCommonSetupEvent event) {

        event.enqueueWork(()-> {
            try {

                StructureRegistry.setup();

                StructureLocator.init();

                CompatHandler.init();

                CMDreg.init(event);

                Spawns.registerSpawningStuff();

                ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_SEEDS_ITEM.get(), 0.3F);
                ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_ITEM.get(), 0.65F);
                ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_BLOCK_ITEM.get(), 1);

                /*
                List<ItemStack> chickenFood = new ArrayList<>();
                Collections.addAll(chickenFood, ChickenEntityAccessor.getFoodItems().getItems());
                chickenFood.add(new ItemStack(Registry.FLAX_SEEDS_ITEM.get()));
                ChickenEntityAccessor.setFoodItems(Ingredient.of(chickenFood.stream()));

                List<ItemStack> horseFood = new ArrayList<>();
                Collections.addAll(horseFood, HorseEntityAccessor.getFoodItems().getItems());
                horseFood.add(new ItemStack(Registry.FLAX_ITEM.get()));
                horseFood.add(new ItemStack(Registry.FLAX_BLOCK_ITEM.get()));
                HorseEntityAccessor.setFoodItems(Ingredient.of(horseFood.stream()));
                */

                ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModRegistry.FLAX_ITEM.get().getRegistryName(), ModRegistry.FLAX_POT);

                FlowerPotHelper.init();

                CapturedMobsHelper.refresh();

                ModSoftFluids.init();

                NetworkHandler.registerMessages();

                ItemsOverrideHandler.registerOverrides();

                LootTableStuff.init();


                //if(CompatHandler.quark) QuarkPlugin.addMissingDispenserBlockPlacingBehaviors();
            }catch(Exception e){
                Supplementaries.LOGGER.throwing(new Exception("Mod setup failed: "+e+". This is a big bug"));
            }

        });
    }

    //events on setup

    public static boolean firstTagLoad = false;

    @SubscribeEvent
    public static void onTagLoad(TagsUpdatedEvent event){
        if(!firstTagLoad) {
            firstTagLoad = true;
            DispenserStuff.registerBehaviors();
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void villagerTradesEvent(VillagerTradesEvent ev) {
        if(RegistryConfigs.reg.FLAX_ENABLED.get()){
            if(ev.getType().equals(VillagerProfession.FARMER)){
                ev.getTrades().get(3).add(new BasicTrade(new ItemStack(ModRegistry.FLAX_SEEDS_ITEM.get(), 15), new ItemStack(net.minecraft.item.Items.EMERALD), 16, 2, 0.05f));
            }
        }
        AdventurerMapsHandler.loadCustomTrades();
        AdventurerMapsHandler.addTrades(ev);
    }

    @SubscribeEvent
    public static void registerWanderingTraderTrades(WandererTradesEvent event) {
        //adding twice cause it's showing up too rarely
        VillagerTradesHandler.registerWanderingTraderTrades(event);
    }


    //TODO: maybe move in /data json
    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent e) {
        LootTableStuff.injectLootTables(e);
    }


}
