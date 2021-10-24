package net.mehvahdjukaar.supplementaries.world.data.map.client;

import net.mehvahdjukaar.selene.map.client.DecorationRenderer;
import net.mehvahdjukaar.supplementaries.world.data.map.FlagDecoration;
import net.minecraft.resources.ResourceLocation;

public class FlagDecorationRenderer extends DecorationRenderer<FlagDecoration> {

    public FlagDecorationRenderer(ResourceLocation texture) {
        super(texture);
    }

    @Override
    public int getMapColor(FlagDecoration decoration) {
        return decoration.getColorValue();
    }
}
