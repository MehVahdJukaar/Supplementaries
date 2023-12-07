package net.mehvahdjukaar.supplementaries.forge;

import forge.net.raphimc.immediatelyfast.injection.mixins.map_atlas_generation.MixinMapRenderer_MapTexture;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.mehvahdjukaar.supplementaries.mixins.PaintingItemMixin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
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
    }

    @SubscribeEvent
    public static void onRemapBlocks(MissingMappingsEvent event) {
        for (var v : event.getMappings(ForgeRegistries.BLOCKS.getRegistryKey(), Supplementaries.MOD_ID)) {
            String rem = REMAP.get(v.getKey().toString());
            if (rem != null) {
                var b = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation(rem));
                b.ifPresent(v::remap);
            } else v.ignore();
        }
        for (var v : event.getMappings(ForgeRegistries.ITEMS.getRegistryKey(), Supplementaries.MOD_ID)) {
            String rem = REMAP.get(v.getKey().toString());
            if (rem != null) {
                var b = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(rem));
                b.ifPresent(v::remap);
            } else v.ignore();
        }


    }


}
