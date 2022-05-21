package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.effects.StasisEnchantment;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.event.LootTableLoadEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class LootTableStuff {

    //TODO: find out how to register these so the dont throw errors or use glm

    private static final List<BiConsumer<LootTableLoadEvent, TableType>> LOOT_INJECTS = new ArrayList<>();

    //initialize so I don't have to constantly check configs for each loot table entry
    public static void init() {
        if (RegistryConfigs.Reg.GLOBE_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectGlobe);
        if (RegistryConfigs.Reg.ROPE_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectRope);
        if (RegistryConfigs.Reg.FLAX_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectFlax);
        if (RegistryConfigs.Reg.BOMB_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectBlueBomb);
        if (RegistryConfigs.Reg.BOMB_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectBomb);
        if (StasisEnchantment.ENABLED) LOOT_INJECTS.add(LootTableStuff::tryInjectStasis);
        if (RegistryConfigs.Reg.BAMBOO_SPIKES_ENABLED.get() &&
                RegistryConfigs.Reg.TIPPED_SPIKES_ENABLED.get()) LOOT_INJECTS.add(LootTableStuff::tryInjectSpikes);
    }

    public static void injectLootTables(LootTableLoadEvent event) {
        ResourceLocation res = event.getName();
        String nameSpace = res.getNamespace();
        if (nameSpace.equals("minecraft") || nameSpace.equals("repurposed_structures")) {
            TableType type = LootHelper.getType(res.toString());
            if (type != TableType.OTHER) {
                LOOT_INJECTS.forEach(i -> i.accept(event, type));
            }
        }
    }

    public enum TableType {
        OTHER,
        MINESHAFT,
        SHIPWRECK_TREASURE,
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
        RUIN,
        SHIPWRECK_STORAGE,
        END_CITY
    }

    private static class LootHelper {

        static boolean RS = CompatHandler.repurposed_structures;

        public static TableType getType(String name) {
            if (isShipwreck(name)) return TableType.SHIPWRECK_TREASURE;
            if (isShipwreckStorage(name)) return TableType.SHIPWRECK_STORAGE;
            if (isMineshaft(name)) return TableType.MINESHAFT;
            if (isDungeon(name)) return TableType.DUNGEON;
            if (isTemple(name)) return TableType.TEMPLE;
            if (isTempleDispenser(name)) return TableType.TEMPLE_DISPENSER;
            if (isOutpost(name)) return TableType.PILLAGER;
            if (isStronghold(name)) return TableType.STRONGHOLD;
            if (isFortress(name)) return TableType.FORTRESS;
            if (isEndCity(name)) return TableType.END_CITY;
            return TableType.OTHER;
        }

        private static final Pattern RS_SHIPWRECK = Pattern.compile("repurposed_structures:chests/shipwreck/\\w*/treasure_chest");

        private static boolean isShipwreck(String s) {
            return s.equals(BuiltInLootTables.SHIPWRECK_TREASURE.toString()) || RS && RS_SHIPWRECK.matcher(s).matches();
        }

        private static final Pattern RS_SHIPWRECK_STORAGE = Pattern.compile("repurposed_structures:chests/shipwreck/\\w*/supply_chest");

        private static boolean isShipwreckStorage(String s) {
            return s.equals(BuiltInLootTables.SHIPWRECK_SUPPLY.toString()) || RS && RS_SHIPWRECK_STORAGE.matcher(s).matches();
        }

        private static boolean isMineshaft(String s) {
            return s.equals(BuiltInLootTables.ABANDONED_MINESHAFT.toString()) || RS && s.contains("repurposed_structures:chests/mineshaft");
        }

        private static boolean isOutpost(String s) {
            return s.equals(BuiltInLootTables.PILLAGER_OUTPOST.toString()) || RS && s.contains("repurposed_structures:chests/outpost");
        }

        private static boolean isDungeon(String s) {
            return s.equals(BuiltInLootTables.SIMPLE_DUNGEON.toString()) || RS && s.contains("repurposed_structures:chests/dungeon");
        }

        private static final Pattern RS_TEMPLE = Pattern.compile("repurposed_structures:chests/temple/\\w*_chest");

        private static boolean isTemple(String s) {
            return s.equals(BuiltInLootTables.JUNGLE_TEMPLE.toString()) || RS && RS_TEMPLE.matcher(s).matches();
        }

        private static final Pattern RS_TEMPLE_DISPENSER = Pattern.compile("repurposed_structures:chests/temple/\\w*_dispenser");

        private static boolean isTempleDispenser(String s) {
            return s.equals(BuiltInLootTables.JUNGLE_TEMPLE.toString()) || RS && RS_TEMPLE_DISPENSER.matcher(s).matches();
        }

        private static boolean isStronghold(String s) {
            return s.equals(BuiltInLootTables.STRONGHOLD_CROSSING.toString()) || RS && s.contains("repurposed_structures:chests/stronghold/nether_storage_room");
        }

        private static boolean isFortress(String s) {
            return s.equals(BuiltInLootTables.NETHER_BRIDGE.toString()) || RS && s.contains("repurposed_structures:chests/fortress");
        }

        private static boolean isEndCity(String s) {
            return s.equals(BuiltInLootTables.END_CITY_TREASURE.toString());
        }
    }

    private static void injectLootPool(LootTableLoadEvent event, TableType type, String name) {
        String id = type.toString().toLowerCase(Locale.ROOT) + "_" + name;
        LootPool pool = LootPool.lootPool().add(
                        LootTableReference.lootTableReference(Supplementaries.res("inject/" + id)))
                .name("supp_" + name).build();
        event.getTable().addPool(pool);
    }

    public static void tryInjectGlobe(LootTableLoadEvent e, TableType type) {
        if (type == TableType.SHIPWRECK_TREASURE) {
            injectLootPool(e, type, "globe");
        }
    }

    public static void tryInjectRope(LootTableLoadEvent e, TableType type) {
        if (type == TableType.MINESHAFT) {
            injectLootPool(e, type, "rope");
        }
    }

    public static void tryInjectFlax(LootTableLoadEvent e, TableType type) {
        if (type == TableType.MINESHAFT || type == TableType.DUNGEON || type == TableType.SHIPWRECK_STORAGE || type == TableType.PILLAGER) {
            injectLootPool(e, type, "flax");
        }
    }

    public static void tryInjectBlueBomb(LootTableLoadEvent e, TableType type) {
        if (type == TableType.STRONGHOLD || type == TableType.MINESHAFT || type == TableType.TEMPLE
                || type == TableType.FORTRESS || type == TableType.DUNGEON) {
            injectLootPool(e, type, "blue_bomb");
        }
    }

    public static void tryInjectBomb(LootTableLoadEvent e, TableType type) {
        if (type == TableType.STRONGHOLD || type == TableType.MINESHAFT || type == TableType.TEMPLE
                || type == TableType.FORTRESS) {
            injectLootPool(e, type, "bomb");
        }
    }

    public static void tryInjectSpikes(LootTableLoadEvent e, TableType type) {
        if (type == TableType.TEMPLE) {
            injectLootPool(e, type, "spikes");
        }
    }

    public static void tryInjectStasis(LootTableLoadEvent e, TableType type) {
        if (type == TableType.END_CITY) {
            injectLootPool(e, type, "stasis");
        }
    }

}
