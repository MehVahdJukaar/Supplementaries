package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

//TODO: simplify /use glm
public class LootTablesInjects {

    public static void init(){
        RegHelper.addLootTableInjects(LootTablesInjects::injectLootPools);
    }

    private static ResourceLocation injectLootPools(RegHelper.LootInjectEvent event) {
        String nameSpace = event.getTable().getNamespace();
        if (nameSpace.equals("minecraft") || nameSpace.equals("repurposed_structures")) {
            String location = event.getTable().toString();
            TableType type = getType(location);
            if (type != TableType.OTHER) {
                LOOT_INJECTS.forEach(i -> i.accept(event::addTableReference, type));
            }
        }
        return null;
    }

    //TODO: find out how to register these so the don't throw errors or use glm

    private static final List<BiConsumer<Consumer<ResourceLocation>, TableType>> LOOT_INJECTS = new ArrayList<>();

    //initialize so I don't have to constantly check configs for each loot table entry
    public static void setup() {
        if (CommonConfigs.Building.GLOBE_ENABLED.get()) LOOT_INJECTS.add(LootTablesInjects::tryInjectGlobe);
        if (CommonConfigs.Tools.QUIVER_ENABLED.get()) LOOT_INJECTS.add(LootTablesInjects::tryInjectQuiver);
        if (CommonConfigs.Functional.ROPE_ENABLED.get()) LOOT_INJECTS.add(LootTablesInjects::tryInjectRope);
        if (CommonConfigs.Functional.FLAX_ENABLED.get()) LOOT_INJECTS.add(LootTablesInjects::tryInjectFlax);
        if (CommonConfigs.Tools.BOMB_ENABLED.get()) LOOT_INJECTS.add(LootTablesInjects::tryInjectBlueBomb);
        if (CommonConfigs.Tools.BOMB_ENABLED.get()) LOOT_INJECTS.add(LootTablesInjects::tryInjectBomb);
        if (CommonConfigs.stasisEnabled()) LOOT_INJECTS.add(LootTablesInjects::tryInjectStasis);
        if (CommonConfigs.Functional.BAMBOO_SPIKES_ENABLED.get() &&
                CommonConfigs.Functional.TIPPED_SPIKES_ENABLED.get())
            LOOT_INJECTS.add(LootTablesInjects::tryInjectSpikes);
    }


    private enum TableType {
        OTHER,
        MINESHAFT,
        SHIPWRECK_TREASURE,
        PILLAGER,
        DUNGEON,
        PYRAMID,
        STRONGHOLD,
        TEMPLE,
        IGLOO,
        MANSION,
        FORTRESS,
        BASTION,
        RUIN,
        SHIPWRECK_STORAGE,
        END_CITY,
        FISHING_TREASURE
    }


    private static final boolean RS = CompatHandler.REPURPOSED_STRUCTURES;

    private static TableType getType(String name) {
        if (isShipwreck(name)) return TableType.SHIPWRECK_TREASURE;
        if (isShipwreckStorage(name)) return TableType.SHIPWRECK_STORAGE;
        if (isMineshaft(name)) return TableType.MINESHAFT;
        if (isDungeon(name)) return TableType.DUNGEON;
        if (isTemple(name)) return TableType.TEMPLE;
        if (isOutpost(name)) return TableType.PILLAGER;
        if (isStronghold(name)) return TableType.STRONGHOLD;
        if (isFortress(name)) return TableType.FORTRESS;
        if (isEndCity(name)) return TableType.END_CITY;
        if (isMansion(name)) return TableType.MANSION;
        if (isFishTreasure(name)) return TableType.FISHING_TREASURE;
        return TableType.OTHER;
    }

    private static final String FISHING_TREASURE = BuiltInLootTables.FISHING_TREASURE.location().toString();
    private static boolean isFishTreasure(String name) {
        return name.equals(FISHING_TREASURE);
    }

    private static final String MANSION = BuiltInLootTables.WOODLAND_MANSION.location().toString();
    private static boolean isMansion(String name) {
        return name.equals(MANSION) || RS && name.contains("repurposed_structures:chests/mansion");
    }

    private static final Pattern RS_SHIPWRECK = Pattern.compile("repurposed_structures:chests/shipwreck/\\w*/treasure_chest");
    private static final String SHIPWRECK_TREASURE = BuiltInLootTables.SHIPWRECK_TREASURE.location().toString();
    private static boolean isShipwreck(String s) {
        return s.equals(SHIPWRECK_TREASURE) || RS && RS_SHIPWRECK.matcher(s).matches();
    }

    private static final Pattern RS_SHIPWRECK_STORAGE = Pattern.compile("repurposed_structures:chests/shipwreck/\\w*/supply_chest");
    private static final String SHIPWRECK_SUPPLY = BuiltInLootTables.SHIPWRECK_SUPPLY.location().toString();
    private static boolean isShipwreckStorage(String s) {
        return s.equals(SHIPWRECK_SUPPLY) || RS && RS_SHIPWRECK_STORAGE.matcher(s).matches();
    }

    private static final String ABANDONED_MINESHAFT = BuiltInLootTables.ABANDONED_MINESHAFT.location().toString();
    private static boolean isMineshaft(String s) {
        return s.equals(ABANDONED_MINESHAFT) || RS && s.contains("repurposed_structures:chests/mineshaft");
    }

    private static final String PILLAGER_OUTPOST = BuiltInLootTables.PILLAGER_OUTPOST.location().toString();
    private static boolean isOutpost(String s) {
        return s.equals(PILLAGER_OUTPOST) || RS && s.contains("repurposed_structures:chests/outpost");
    }

    private static final String DUNGEON = BuiltInLootTables.SIMPLE_DUNGEON.location().toString();
    private static boolean isDungeon(String s) {
        return s.equals(DUNGEON) || RS && s.contains("repurposed_structures:chests/dungeon");
    }

    private static final Pattern RS_TEMPLE = Pattern.compile("repurposed_structures:chests/temple/\\w*_chest");
    private static final String JUNGLE_TEMPLE = BuiltInLootTables.JUNGLE_TEMPLE.location().toString();
    private static boolean isTemple(String s) {
        return s.equals(JUNGLE_TEMPLE) || RS && RS_TEMPLE.matcher(s).matches();
    }

    private static final Pattern RS_TEMPLE_DISPENSER = Pattern.compile("repurposed_structures:chests/temple/\\w*_dispenser");
    private static final String JUNGLE_TEMPLE_DISPENSER = BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER.location().toString();
    private static boolean isTempleDispenser(String s) {
        return s.equals(JUNGLE_TEMPLE_DISPENSER) || RS && RS_TEMPLE_DISPENSER.matcher(s).matches();
    }

    private static final String STRONGHOLD_CROSSING = BuiltInLootTables.STRONGHOLD_CROSSING.location().toString();
    private static boolean isStronghold(String s) {
        return s.equals(STRONGHOLD_CROSSING) || RS && s.contains("repurposed_structures:chests/stronghold/nether_storage_room");
    }

    private static final String NETHER_BRIDGE = BuiltInLootTables.NETHER_BRIDGE.location().toString();
    private static boolean isFortress(String s) {
        return s.equals(NETHER_BRIDGE) || RS && s.contains("repurposed_structures:chests/fortress");
    }

    private static final String END_CITY_TREASURE = BuiltInLootTables.END_CITY_TREASURE.location().toString();
    private static boolean isEndCity(String s) {
        return s.equals(END_CITY_TREASURE);
    }

    private static void injectPool(Consumer<ResourceLocation> consumer, TableType type, String name) {
        String id = type.toString().toLowerCase(Locale.ROOT) + "_" + name;
        consumer.accept(Supplementaries.res("inject/" + id));
    }


    private static void tryInjectGlobe(Consumer<ResourceLocation> e, TableType type) {
        if (type == TableType.SHIPWRECK_TREASURE) {
            injectPool(e, type, "globe");
        }
    }

    private static void tryInjectQuiver(Consumer<ResourceLocation> e, TableType type) {
        if (type == TableType.DUNGEON || type == TableType.MANSION || type == TableType.PILLAGER) {
            injectPool(e, type, "quiver");
        }
    }

    private static void tryInjectRope(Consumer<ResourceLocation> e, TableType type) {
        if (type == TableType.MINESHAFT) {
            injectPool(e, type, "rope");
        }
    }

    private static void tryInjectFlax(Consumer<ResourceLocation> e, TableType type) {
        if (type == TableType.MINESHAFT || type == TableType.DUNGEON || type == TableType.SHIPWRECK_STORAGE || type == TableType.PILLAGER) {
            injectPool(e, type, "flax");
        }
    }

    private static void tryInjectBlueBomb(Consumer<ResourceLocation> e, TableType type) {
        if (type == TableType.STRONGHOLD || type == TableType.MINESHAFT || type == TableType.TEMPLE
                || type == TableType.FORTRESS || type == TableType.DUNGEON) {
            injectPool(e, type, "blue_bomb");
        }
    }

    private static void tryInjectBomb(Consumer<ResourceLocation> e, TableType type) {
        if (type == TableType.STRONGHOLD || type == TableType.MINESHAFT || type == TableType.TEMPLE
                || type == TableType.FORTRESS) {
            injectPool(e, type, "bomb");
        }
    }

    private static void tryInjectSpikes(Consumer<ResourceLocation> e, TableType type) {
        if (type == TableType.TEMPLE) {
            injectPool(e, type, "spikes");
        }
    }

    private static void tryInjectStasis(Consumer<ResourceLocation> e, TableType type) {
        if (type == TableType.END_CITY) {
            injectPool(e, type, "stasis");
        }
    }

}
