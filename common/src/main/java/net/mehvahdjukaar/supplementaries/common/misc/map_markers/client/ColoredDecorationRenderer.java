package net.mehvahdjukaar.supplementaries.common.misc.map_markers.client;

import net.mehvahdjukaar.moonlight.api.map.client.MapDecorationRenderer;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ColoredDecoration;
import net.minecraft.resources.ResourceLocation;

public class ColoredDecorationRenderer extends MapDecorationRenderer<ColoredDecoration> {

    public ColoredDecorationRenderer(ResourceLocation texture) {
        super(texture);
    }

    @Override
    public int getColor(ColoredDecoration decoration) {
        return decoration.getColor().getTextureDiffuseColor();
    }

}
