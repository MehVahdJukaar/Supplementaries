package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.level.block.BushBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.MissingMappingsEvent;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class RemapHandler {

    private static final Map<String, String> REMAP = new HashMap<>();

    static {
        REMAP.put("supplementaries:copper_lantern", "suppsquared:copper_lantern");
        REMAP.put("supplementaries:crimson_lantern", "suppsquared:crimson_lantern");
        REMAP.put("supplementaries:brass_lantern", "suppsquared:brass_lantern");
        REMAP.put("supplementaries:silver_lantern", "oreganized:silver_lantern");
        REMAP.put("supplementaries:silver_door", "oreganized:silver_door");
        REMAP.put("supplementaries:silver_trapdoor", "oreganized:silver_trapdoor");
        REMAP.put("supplementaries:lead_lantern", "oreganized:lead_lantern");
        REMAP.put("supplementaries:lead_door", "oreganized:lead_door");
        REMAP.put("supplementaries:lead_trapdoor", "oreganized:lead_trapdoor");
        REMAP.put("supplementaries:cabin_siding", "supplementaries:fine_wood");
        REMAP.put("supplementaries:cabin_siding_slab", "supplementaries:fine_wood_slab");
        REMAP.put("supplementaries:cabin_siding_stairs", "supplementaries:fine_wood_stairs");

    }

    @SubscribeEvent
    public static void onRemapBlocks(MissingMappingsEvent event) {
        remapAll(event, BuiltInRegistries.BLOCK);
        remapAll(event, BuiltInRegistries.ITEM);
    }


    private static <T> void remapAll(MissingMappingsEvent event, DefaultedRegistry<T> block) {
        for (var v : event.getMappings(block.key(), Supplementaries.MOD_ID)) {
            String rem = REMAP.get(v.getKey().toString());
            if (rem != null) {
                var b = block.getOptional(new ResourceLocation(rem));
                b.ifPresent(v::remap);
            } else v.ignore();
        }
    }


}
