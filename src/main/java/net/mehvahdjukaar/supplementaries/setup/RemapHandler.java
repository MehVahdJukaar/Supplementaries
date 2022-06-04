package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class RemapHandler {

    private static final Map<String, ResourceLocation> itemReMap = new HashMap<>();

    private static final Map<String, ResourceLocation> fullReMap = new HashMap<>();

    static{
        fullReMap.put("jar_tinted",Supplementaries.res(RegistryConstants.JAR_NAME));
    }

    @SubscribeEvent
    public static void onRemapBlocks(RegistryEvent.MissingMappings<Block> event) {
        for (RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getMappings(Supplementaries.MOD_ID)) {
            String k = mapping.key.getPath();
            if (fullReMap.containsKey(k)) {
                var i = fullReMap.get(k);
                try {
                    Supplementaries.LOGGER.warn("Remapping block '{}' to '{}'", mapping.key, i);
                    mapping.remap(ForgeRegistries.BLOCKS.getValue(i));
                } catch (Throwable t) {
                    Supplementaries.LOGGER.warn("Remapping block '{}' to '{}' failed: {}", mapping.key, i, t);
                }
            } else if (k.contains("hanging_sign")) {
                try {
                    Block newBlock = getNewBlock(ModRegistry.HANGING_SIGNS, k);
                    if (newBlock == null) {
                        newBlock = ModRegistry.HANGING_SIGNS.get(WoodType.OAK_WOOD_TYPE);
                    }
                    mapping.remap(newBlock);
                } catch (Exception ex) {
                    Supplementaries.LOGGER.warn("Remapping block '{}' failed: {}", mapping.key, ex);
                }
            }
        }
    }


    //@SubscribeEvent
    public static void onRemapTiles(RegistryEvent.MissingMappings<BlockEntityType<?>> event) {
        for (RegistryEvent.MissingMappings.Mapping<BlockEntityType<?>> mapping : event.getMappings(Supplementaries.MOD_ID)) {
            String k = mapping.key.getPath();
            if (fullReMap.containsKey(k)) {
                var i = fullReMap.get(k);
                try {
                    Supplementaries.LOGGER.warn("Remapping tile entity '{}' to '{}'", mapping.key, i);
                    mapping.remap(ForgeRegistries.BLOCK_ENTITIES.getValue(i));
                } catch (Throwable t) {
                    Supplementaries.LOGGER.warn("Remapping  tile entity '{}' to '{}' failed: {}", mapping.key, i, t);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRemapItems(RegistryEvent.MissingMappings<Item> event) {
        for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getMappings(Supplementaries.MOD_ID)) {
            String k = mapping.key.getPath();
            if (itemReMap.containsKey(k)) {
                var i = itemReMap.get(k);
                try {
                    Supplementaries.LOGGER.warn("Remapping item '{}' to '{}'", mapping.key, i);
                    mapping.remap(ForgeRegistries.ITEMS.getValue(i));
                } catch (Throwable t) {
                    Supplementaries.LOGGER.warn("Remapping item '{}' to '{}' failed: {}", mapping.key, i, t);
                }
            } else if (k.contains("hanging_sign")) {
                try {
                    Item newBlock = getNewBlock(ModRegistry.HANGING_SIGNS_ITEMS, k);
                    if (newBlock == null) {
                        newBlock = ModRegistry.HANGING_SIGNS_ITEMS.get(WoodType.OAK_WOOD_TYPE);
                    }
                    mapping.remap(newBlock);
                } catch (Exception ex) {
                    Supplementaries.LOGGER.warn("Remapping block '{}' failed: {}", mapping.key, ex);
                }
            } else if (k.contains("sign_post")) {
                try {
                    Item newBlock = getNewBlock(ModRegistry.SIGN_POST_ITEMS, k);
                    if (newBlock == null) {
                        newBlock = ModRegistry.SIGN_POST_ITEMS.get(WoodType.OAK_WOOD_TYPE);
                    }
                    mapping.remap(newBlock);
                } catch (Exception ex) {
                    Supplementaries.LOGGER.warn("Remapping block '{}' failed: {}", mapping.key, ex);
                }
            }
        }
    }

    //@SubscribeEvent
    public static void onRemapEntityTypes(RegistryEvent.MissingMappings<EntityType<?>> event) {
        for (RegistryEvent.MissingMappings.Mapping<EntityType<?>> mapping : event.getMappings(Supplementaries.MOD_ID)) {
            if (fullReMap.containsKey(mapping.key.getPath())) {
                try {
                    Supplementaries.LOGGER.warn("Remapping entity '{}' to '{}'", mapping.key, fullReMap.get(mapping.key.getPath()));
                    mapping.remap(ForgeRegistries.ENTITIES.getValue(fullReMap.get(mapping.key.getPath())));
                } catch (Throwable t) {
                    Supplementaries.LOGGER.warn("Remapping entity '{}' to '{}' failed: {}", mapping.key,
                            fullReMap.get(mapping.key.getPath()), t);
                }
            }
        }
    }

    @Deprecated
    public static String getLegacyWoodTypeAppendableID(WoodType wood) {
        String l = getLegacyAbbreviation(wood.getNamespace());
        return l != null ? "_" + wood.getTypeName() + l : "_" + wood.getAppendableId();
    }

    //T_T
    @Nullable
    public static String getLegacyAbbreviation(String modName) {
        return switch (modName) {
            default -> null;
            case "minecraft", "tofucraft", "betterendforge", "malum", "mowziesmobs", "ars_nouveau", "the_bumblezone",
                    "undergarden", "endergetic", "omni", "byg", "mysticalworld", "bamboo_blocks", "good_nights_sleep",
                    "pokecube", "simplytea", "outer_end", "upgrade_aquatic", "atmospheric", "domum_ornamentum",
                    "architects_palette", "botania", "enhanced_mushrooms", "druidcraft", "silentgear", "eidolon",
                    "greekfantasy", "forbidden_arcanus", "pokecube_legends" -> "";
            case "habitat" -> "_hbt";
            case "abundance" -> "_ab";
            case "biomesoplenty" -> "_bop";
            case "biomemakeover" -> "_bm";
            case "terraincognita" -> "_te";
            case "bayou_blues" -> "_bb";
            case "extendedmushrooms" -> "_em";
            case "rediscovered" -> "_red";
            case "autumnity" -> "_aut";
            case "unnamedanimalmod" -> "_un";
            case "premium_wood" -> "_pw";
            case "environmental" -> "_env";
            case "desolation" -> "_de";
            case "morecraft" -> "_mc";
            case "atum" -> "_atum";
            case "traverse" -> "_tr";
            case "lotr" -> "_lotr";
            case "terraqueous" -> "_ter";
            case "twilightforest" -> "_tf";
        };
    }

    @Nullable
    private static <T extends ForgeRegistryEntry<?>> T getNewBlock(Map<WoodType, T> newEntries, String oldPath) {

        for (var b : newEntries.values()) {
            String path = b.getRegistryName().getPath();
            String[] modId = path.split("/");
            if (modId.length == 2) {
                String abb = getLegacyAbbreviation(modId[0]);
                String match = modId[1] + abb;
                if (oldPath.equals(match)) {
                    return b;
                }
            }
        }
        return null;
    }

}
