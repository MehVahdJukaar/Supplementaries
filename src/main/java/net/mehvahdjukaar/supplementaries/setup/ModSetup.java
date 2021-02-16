package net.mehvahdjukaar.supplementaries.setup;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.network.commands.ModCommands;
import net.mehvahdjukaar.supplementaries.plugins.create.SupplementariesCreatePlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.conditions.RandomChance;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraftforge.common.BasicTrade;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {

    public static void init(final FMLCommonSetupEvent event) {

        Spawns.registerSpawningStuff();
        //event.enqueueWork(Dispenser::registerBehaviors);
        Dispenser.registerBehaviors();

        if(ModList.get().isLoaded("create")){
            SupplementariesCreatePlugin.initialize();
        }

    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void registerWanderingTraderTrades(VillagerTradesEvent event) {
        event.getType();
        //TODO: add villager trades
    }

    @SubscribeEvent
    public static void registerWanderingTraderTrades(WandererTradesEvent event) {
        //adding twice cause it's showing up too rarely
        for(int i = 0; i<ServerConfigs.cached.GLOBE_TRADES; i++) {
            event.getRareTrades()
                    .add(new BasicTrade(10, new ItemStack(Registry.GLOBE_ITEM.get(), 1), 2, 20));
        }
    }

    //TODO: maybe move in /data json
    //globe to shipwrecks
    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent e) {
        if (e.getName().toString().equals("minecraft:chests/shipwreck_treasure")) {
            float chance = (float) ServerConfigs.cached.GLOBE_TREASURE_CHANCE;
            LootPool pool = LootPool.builder()
                    .name("supplementaries_injected")
                    .rolls(ConstantRange.of(1))
                    .acceptCondition(RandomChance.builder(chance))
                    .addEntry(ItemLootEntry.builder(Registry.GLOBE_ITEM.get()).weight(1))
                    .build();
            e.getTable().addPool(pool);
        }
    }

    public static void reflectionStuff(){
        Field[] methods = DispenserTileEntity.class.getDeclaredFields();
        // get the name of every method present in the list
        for (Field method : methods) {
            String MethodName = method.getName();
            System.out.println("Name of the method: "
                    + MethodName);
        }
    }



}
