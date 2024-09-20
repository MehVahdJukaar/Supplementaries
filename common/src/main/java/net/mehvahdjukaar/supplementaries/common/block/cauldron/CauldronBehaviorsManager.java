package net.mehvahdjukaar.supplementaries.common.block.cauldron;

import net.mehvahdjukaar.moonlight.api.map.MapHelper;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LayeredCauldronBlock;

public class CauldronBehaviorsManager {

    public static void registerBehaviors() {
        for (var item : ModRegistry.FLAGS.values()) {
            CauldronInteraction.WATER.map().put(item.get().asItem(), CauldronInteraction.BANNER);
        }
        CauldronInteraction.WATER.map().put(ModRegistry.QUIVER_ITEM.get(), CauldronInteraction.DYED_ITEM);

        CompatObjects.ATLAS.asOptionalValue()
                .ifPresent(item -> CauldronInteraction.WATER.map().put(item, MAP_INTERACTION));

        CauldronInteraction.WATER.map().put(Items.FILLED_MAP, MAP_INTERACTION);
    }

    private static final CauldronInteraction MAP_INTERACTION = (state, level, pos, player, hand, stack) -> {
        if (MapHelper.removeAllCustomMarkers(level, stack, player)) {

            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    };
}
