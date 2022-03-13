package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.selene.map.ExpandedMapData;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.registries.ForgeRegistries;

public class CauldronRegistry {

    public static void registerInteractions() {
        for (var item : ModRegistry.FLAGS_ITEMS.values()) {
            CauldronInteraction.WATER.put(item.get(), CauldronInteraction.BANNER);
        }

        var atlas = ForgeRegistries.ITEMS.getValue(new ResourceLocation("map_atlases:atlas"));
        if (atlas != Items.AIR) {
            CauldronInteraction.WATER.put(atlas, MAP_INTERACTION);
        }

        CauldronInteraction.WATER.put(Items.FILLED_MAP, MAP_INTERACTION);
    }

    private static final CauldronInteraction MAP_INTERACTION = (state, level, pos, player, hand, stack) -> {
        Item item = stack.getItem();
        if (item instanceof MapItem) {

            if (!level.isClientSide) {
                MapItemSavedData data = MapItem.getSavedData(stack, level);
                if (data instanceof ExpandedMapData expandedMapData) {
                    expandedMapData.resetCustomDecoration();
                }
            }

            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } /*else if (CompatHandler.mapatlas && MapAtlasPlugin.isAtlas(item)) {
            if (!level.isClientSide) {
                MapItemSavedData data = MapAtlasPlugin.getSavedDataFromAtlas(stack, level, player);
                if (data instanceof ExpandedMapData expandedMapData) {
                    expandedMapData.resetCustomDecoration();
                }
            }

            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }*/

        return InteractionResult.PASS;

    };
}
