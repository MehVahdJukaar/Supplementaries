package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class RemapHandler {
    private static final Map<String, ResourceLocation> reMap = new HashMap<>();

    static {
        reMap.put("jar_full", ModRegistry.JAR_ITEM.getId());
        reMap.put("jar_full_tinted", ModRegistry.JAR_ITEM_TINTED.getId());
        reMap.put("cage_full", ModRegistry.CAGE_ITEM.getId());
        reMap.put(ModRegistry.WALL_LANTERN_NAME, Items.LANTERN.getRegistryName());
        reMap.put(ModRegistry.STICK_NAME, Items.STICK.getRegistryName());
        reMap.put(ModRegistry.BLAZE_ROD_NAME, Items.BLAZE_ROD.getRegistryName());
        reMap.put(ModRegistry.HANGING_FLOWER_POT_NAME, Items.FLOWER_POT.getRegistryName());
        reMap.put(ModRegistry.GUNPOWDER_BLOCK_NAME, Items.GUNPOWDER.getRegistryName());
        for(RegistryObject<Block> banner : ModRegistry.CEILING_BANNERS.values()){
            reMap.put(banner.getId().getPath(), new ResourceLocation("minecraft",
                    banner.getId().getPath().replace("ceiling_banner_","")+"_banner"));
        }
        reMap.put(ModRegistry.DIRECTIONAL_CAKE_NAME, Items.CAKE.getRegistryName());

    }

    //@SubscribeEvent
    public static void onRemapBlocks(RegistryEvent.MissingMappings<Block> event) {
        for (RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getMappings(Supplementaries.MOD_ID)) {
            if (reMap.containsKey(mapping.key.getPath())) {
                try {
                    Supplementaries.LOGGER.warn("Remapping block '{}' to '{}'", mapping.key, reMap.get(mapping.key.getPath()));
                    mapping.remap(ForgeRegistries.BLOCKS.getValue(reMap.get(mapping.key.getPath())));
                } catch (Throwable t) {
                    Supplementaries.LOGGER.warn("Remapping block '{}' to '{}' failed: {}", mapping.key,
                            reMap.get(mapping.key.getPath()), t);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRemapItems(RegistryEvent.MissingMappings<Item> event) {
        for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getMappings(Supplementaries.MOD_ID)) {
            if (reMap.containsKey(mapping.key.getPath())) {
                try {
                    Supplementaries.LOGGER.warn("Remapping item '{}' to '{}'", mapping.key, reMap.get(mapping.key.getPath()));
                    mapping.remap(ForgeRegistries.ITEMS.getValue(reMap.get(mapping.key.getPath())));
                } catch (Throwable t) {
                    Supplementaries.LOGGER.warn("Remapping item '{}' to '{}' failed: {}", mapping.key,
                            reMap.get(mapping.key.getPath()), t);
                }
            }
        }
    }
}
