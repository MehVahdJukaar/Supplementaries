package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.RandomChance;
import net.minecraft.loot.functions.SetCount;
import net.minecraftforge.event.LootTableLoadEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class LootTableStuff {

    private static final List<BiConsumer<LootTableLoadEvent, TableType>> LOOT_INJECTS = new ArrayList<>();

    //initialize so I don't have to constantly check configs for each loot table entry
    public static void init() {
        if (RegistryConfigs.reg.GLOBE_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectGlobe);
        if (RegistryConfigs.reg.ROPE_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectRope);
        if (RegistryConfigs.reg.FLAX_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectFlax);
    }

    public static void injectLootTables(LootTableLoadEvent event) {
        String name = event.getName().toString();
        TableType type = LootHelper.getType(name);
        LOOT_INJECTS.forEach(i -> i.accept(event, type));

        LOOT_INJECTS.clear();
    }

    public enum TableType {
        OTHER,
        MINESHAFT,
        SHIPWRECK,
        PILLAGER,
        DUNGEON,
        PYRAMID,
        STRONGHOLD,
        TEMPLE,
        TEMPLE_DISPENSER,
        IGLOO,
        MANSION,
        FORTRESS,
        BASTION,
        RUIN
    }

    private static class LootHelper {

        static boolean RS = CompatHandler.repurposed_structures;

        public static TableType getType(String name) {
            if (isShipwreck(name)) return TableType.SHIPWRECK;
            else if (isMineshaft(name)) return TableType.MINESHAFT;
            else if (isDungeon(name)) return TableType.DUNGEON;
            else if (isTemple(name)) return TableType.TEMPLE;
            else if (isTempleDispenser(name)) return TableType.TEMPLE_DISPENSER;
            else if (isOutpost(name)) return TableType.PILLAGER;
            return TableType.OTHER;
        }

        private static final Pattern RS_SHIPWRECK = Pattern.compile("repurposed_structures:chests/shipwreck/\\w*/treasure_chest");

        private static boolean isShipwreck(String s) {
            return s.equals(LootTables.SHIPWRECK_TREASURE.toString()) || RS && RS_SHIPWRECK.matcher(s).matches();
        }
        private static boolean isMineshaft(String s) {
            return s.equals(LootTables.ABANDONED_MINESHAFT.toString()) || RS && s.contains("repurposed_structures:chests/mineshaft");
        }
        private static boolean isOutpost(String s) {
            return s.equals(LootTables.PILLAGER_OUTPOST.toString()) || RS && s.contains("repurposed_structures:chests/outpost");
        }
        private static boolean isDungeon(String s) {
            return s.equals(LootTables.SIMPLE_DUNGEON.toString()) || RS && s.contains("repurposed_structures:chests/dungeon");
        }
        private static final Pattern RS_TEMPLE = Pattern.compile("repurposed_structures:chests/temple/\\w*_chest");

        private static boolean isTemple(String s) {
            return s.equals(LootTables.JUNGLE_TEMPLE.toString()) || RS && RS_TEMPLE.matcher(s).matches();
        }
        private static final Pattern RS_TEMPLE_DISPENSER = Pattern.compile("repurposed_structures:chests/temple/\\w*_dispenser");

        private static boolean isTempleDispenser(String s) {
            return s.equals(LootTables.JUNGLE_TEMPLE.toString()) || RS && RS_TEMPLE_DISPENSER.matcher(s).matches();
        }
    }


    public static void tryInjectGlobe(LootTableLoadEvent e, TableType type) {
        if (type == TableType.SHIPWRECK) {
            float chance = (float) ServerConfigs.cached.GLOBE_TREASURE_CHANCE;
            LootPool pool = LootPool.lootPool()
                    .name("supplementaries_injected_globe")
                    .setRolls(ConstantRange.exactly(1))
                    .when(RandomChance.randomChance(chance))
                    .add(ItemLootEntry.lootTableItem(Registry.GLOBE_ITEM.get()).setWeight(1))
                    .build();
            e.getTable().addPool(pool);
        }
    }

    public static void tryInjectRope(LootTableLoadEvent e, TableType type) {

        if (type == TableType.MINESHAFT) {
            float chance = 0.35f;
            LootPool pool = LootPool.lootPool()
                    .name("supplementaries_injected_rope")
                    .apply(SetCount.setCount(RandomValueRange.between(5.0F, 17.0F)))
                    .setRolls(ConstantRange.exactly(1))
                    .when(RandomChance.randomChance(chance))
                    .add(ItemLootEntry.lootTableItem(Registry.ROPE_ITEM.get()).setWeight(1))
                    .build();
            e.getTable().addPool(pool);
        }
    }

    public static void tryInjectFlax(LootTableLoadEvent e, TableType type) {
        float chance;
        float min = 1;
        float max = 3;
        if (type == TableType.MINESHAFT) {
            chance = 0.10f;
        }
        else if(type == TableType.DUNGEON){
            chance = 0.2f;
        }
        else if(type == TableType.PILLAGER){
            chance = 0.95f;
            min = 2;
            max = 5;
        }
        else return;

        LootPool pool = LootPool.lootPool()
                .name("supplementaries_injected_flax")
                .apply(SetCount.setCount(RandomValueRange.between(min,max)))
                .setRolls(ConstantRange.exactly(1))
                .when(RandomChance.randomChance(chance))
                .add(ItemLootEntry.lootTableItem(Registry.FLAX_SEEDS_ITEM.get()).setWeight(1))
                .build();
        e.getTable().addPool(pool);
    }

    public static void tryInjectBlueBomb(LootTableLoadEvent e, TableType type) {
        if (type == TableType.TEMPLE) {
            float chance = 0.08f;
            LootPool pool = LootPool.lootPool()
                    .name("supplementaries_injected_blue_bomb")
                    .setRolls(ConstantRange.exactly(1))
                    .when(RandomChance.randomChance(chance))
                    .add(ItemLootEntry.lootTableItem(Registry.BOMB_BLUE_ITEM.get()).setWeight(1))
                    .build();
            e.getTable().addPool(pool);
        }
    }


}
