package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import net.minecraft.world.level.block.Block;

//has tooltip. thats it
public class SuppAdditionalPlacement extends AdditionalItemPlacement {

    public SuppAdditionalPlacement(Block placeable) {
        super(placeable);
    }

}
