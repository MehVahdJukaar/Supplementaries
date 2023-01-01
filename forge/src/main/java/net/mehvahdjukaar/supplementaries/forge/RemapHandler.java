package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import vazkii.quark.content.world.module.GlimmeringWealdModule;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class RemapHandler {

    private static final Map<String, ResourceLocation> REMAP = new HashMap<>();

    @SubscribeEvent
    public static void onRemapBlocks(MissingMappingsEvent event) {
        event.getMappings(ForgeRegistries.BLOCKS.getRegistryKey(), Supplementaries.MOD_ID)
                .forEach(MissingMappingsEvent.Mapping::ignore);
        event.getMappings(ForgeRegistries.ITEMS.getRegistryKey(), Supplementaries.MOD_ID)
                .forEach(MissingMappingsEvent.Mapping::ignore);
    }

}
