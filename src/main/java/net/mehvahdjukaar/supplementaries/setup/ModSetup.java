package net.mehvahdjukaar.supplementaries.setup;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobs;
import net.mehvahdjukaar.supplementaries.common.FlowerPotHelper;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.datagen.RecipeCondition;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluidList;
import net.mehvahdjukaar.supplementaries.mixins.ChickenEntityAccessor;
import net.mehvahdjukaar.supplementaries.mixins.HorseEntityAccessor;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.commands.ModCommands;
import net.mehvahdjukaar.supplementaries.plugins.create.SupplementariesCreatePlugin;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.conditions.RandomChance;
import net.minecraft.loot.functions.SetCount;
import net.minecraftforge.common.BasicTrade;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {


    public static void init(final FMLCommonSetupEvent event) {

        CraftingHelper.register(new RecipeCondition.Serializer(RecipeCondition.MY_FLAG));

        NetworkHandler.registerMessages();

        //order matters here
        Spawns.registerSpawningStuff();

        ComposterBlock.CHANCES.put(Registry.FLAX_SEEDS_ITEM.get().asItem(),0.3F);
        ComposterBlock.CHANCES.put(Registry.FLAX_ITEM.get().asItem(),0.65F);
        ComposterBlock.CHANCES.put(Registry.FLAX_BLOCK.get().asItem(),1);


        List<ItemStack> chickenFood = new ArrayList<>();
        Collections.addAll(chickenFood, ChickenEntityAccessor.getTemptationItems().getMatchingStacks());
        chickenFood.add(new ItemStack(Registry.FLAX_SEEDS_ITEM.get()));
        ChickenEntityAccessor.setTemptationItems(Ingredient.fromStacks(chickenFood.stream()));

        List<ItemStack> horseFood = new ArrayList<>();
        Collections.addAll(horseFood, HorseEntityAccessor.getfield_234235_bE_().getMatchingStacks());
        horseFood.add(new ItemStack(Registry.FLAX_ITEM.get()));
        horseFood.add(new ItemStack(Registry.FLAX_BLOCK_ITEM.get()));
        HorseEntityAccessor.setfield_234235_bE_(Ingredient.fromStacks(horseFood.stream()));



        if (ModList.get().isLoaded("create")) {
            SupplementariesCreatePlugin.initialize();
        }

        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(Registry.FLAX_ITEM.get().getRegistryName(), Registry.FLAX_POT);

        FlowerPotHelper.init();

        SoftFluidList.init();

        CapturedMobs.refresh();

        Dispenser.registerBehaviors();
        //event.enqueueWork(Dispenser::registerBehaviors);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void villagerTradesEvent(VillagerTradesEvent ev) {
        if(RegistryConfigs.reg.FLAX_ENABLED.get()){
            if(ev.getType().equals(VillagerProfession.FARMER)){
                ev.getTrades().get(3).add(new BasicTrade(new ItemStack(Registry.FLAX_SEEDS_ITEM.get(), 15), new ItemStack(net.minecraft.item.Items.EMERALD), 16, 2, 0.05f));
            }
        }
    }

    @SubscribeEvent
    public static void registerWanderingTraderTrades(WandererTradesEvent event) {
        //adding twice cause it's showing up too rarely
        if(RegistryConfigs.reg.GLOBE_ENABLED.get()) {
            for(int i = 0; i<ServerConfigs.cached.GLOBE_TRADES; i++) {
                event.getRareTrades()
                        .add(new BasicTrade(10, new ItemStack(Registry.GLOBE_ITEM.get(), 1), 3, 20));
            }
        }
        if(RegistryConfigs.reg.FLAX_ENABLED.get()) {
            for (int i = 0; i < 3; i++) {
                event.getGenericTrades()
                        .add(new BasicTrade(6, new ItemStack(Registry.FLAX_SEEDS_ITEM.get(), 1), 5, 10));
            }
        }
    }


    //TODO: maybe move in /data json
    //globe to shipwrecks
    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent e) {
        String name = e.getName().toString();
        switch (name) {
            case "minecraft:chests/shipwreck_treasure": {
                if (!RegistryConfigs.reg.GLOBE_ENABLED.get()) return;
                float chance = (float) ServerConfigs.cached.GLOBE_TREASURE_CHANCE;
                LootPool pool = LootPool.builder()
                        .name("supplementaries_injected_globe")
                        .rolls(ConstantRange.of(1))
                        .acceptCondition(RandomChance.builder(chance))
                        .addEntry(ItemLootEntry.builder(Registry.GLOBE_ITEM.get()).weight(1))
                        .build();
                e.getTable().addPool(pool);
                break;
            }
            case "minecraft:chests/abandoned_mineshaft": {
                if (RegistryConfigs.reg.ROPE_ENABLED.get()) {
                    float chance = 0.35f;
                    LootPool pool = LootPool.builder()
                            .name("supplementaries_injected_rope")
                            .acceptFunction(SetCount.builder(RandomValueRange.of(4.0F, 8.0F)))
                            .rolls(new RandomValueRange(1, 2))
                            .acceptCondition(RandomChance.builder(chance))
                            .addEntry(ItemLootEntry.builder(Registry.ROPE_ITEM.get()).weight(1))
                            .build();
                    e.getTable().addPool(pool);
                }

                if (RegistryConfigs.reg.FLAX_ENABLED.get()) {
                    float chance2 = 0.10f;
                    LootPool pool2 = LootPool.builder()
                            .name("supplementaries_injected_flax")
                            .rolls(new RandomValueRange(1, 3))
                            .acceptCondition(RandomChance.builder(chance2))
                            .addEntry(ItemLootEntry.builder(Registry.FLAX_SEEDS_ITEM.get()).weight(1))
                            .build();
                    e.getTable().addPool(pool2);
                }
                break;
            }
            case "minecraft:chests/pillager_outpost": {
                if (!RegistryConfigs.reg.FLAX_ENABLED.get()) return;
                float chance = 0.95f;
                LootPool pool = LootPool.builder()
                        .name("supplementaries_injected_flax")
                        .acceptFunction(SetCount.builder(RandomValueRange.of(2F,5.0F)))
                        .acceptCondition(RandomChance.builder(chance))
                        .addEntry(ItemLootEntry.builder(Registry.FLAX_SEEDS_ITEM.get()).weight(1))
                        .build();
                e.getTable().addPool(pool);
                break;
            }
            case "minecraft:chests/simple_dungeon": {
                if (!RegistryConfigs.reg.FLAX_ENABLED.get()) return;
                float chance = 0.2f;
                LootPool pool = LootPool.builder()
                        .name("supplementaries_injected_flax")
                        .rolls(new RandomValueRange(1, 3))
                        .acceptCondition(RandomChance.builder(chance))
                        .addEntry(ItemLootEntry.builder(Registry.FLAX_SEEDS_ITEM.get()).weight(1))
                        .build();
                e.getTable().addPool(pool);
                break;
            }
        }
    }


}
