package net.mehvahdjukaar.supplementaries.common.block.cauldron;

import net.mehvahdjukaar.moonlight.api.map.MapHelper;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LayeredCauldronBlock;

public class CauldronBehaviorsManager {

    public static void registerBehaviors() {
        for (var item : ModRegistry.FLAGS.values()) {
            CauldronInteraction.WATER.put(item.get().asItem(), CauldronInteraction.BANNER);
        }
        CauldronInteraction.WATER.put(ModRegistry.QUIVER_ITEM.get(), CauldronInteraction.DYED_ITEM);

        var atlas = Registry.ITEM.getOptional(new ResourceLocation("map_atlases:atlas"));
        atlas.ifPresent(item -> CauldronInteraction.WATER.put(item, MAP_INTERACTION));

        CauldronInteraction.WATER.put(Items.FILLED_MAP, MAP_INTERACTION);
    }

    private static final CauldronInteraction MAP_INTERACTION = (state, level, pos, player, hand, stack) -> {
        if (MapHelper.removeAllCustomMarkers(level, stack, player)) {

            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    };
}
