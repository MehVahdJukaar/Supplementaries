package net.mehvahdjukaar.supplementaries.world.data.map.sup.client;

import net.mehvahdjukaar.supplementaries.world.data.map.lib.client.DecorationRenderer;
import net.mehvahdjukaar.supplementaries.world.data.map.sup.FlagDecoration;
import net.minecraft.util.ResourceLocation;

public class FlagDecorationRenderer extends DecorationRenderer<FlagDecoration> {

    public FlagDecorationRenderer(ResourceLocation texture) {
        super(texture);
    }

    @Override
    public int getMapColor(FlagDecoration decoration) {

        return decoration.getColor().getColorValue();
    }
}
