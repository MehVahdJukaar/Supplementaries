package net.mehvahdjukaar.supplementaries.common.misc.map_markers;

import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

import java.util.Optional;

public class ColoredDecoration extends MLMapDecoration {


    public static final StreamCodec<RegistryFriendlyByteBuf, ColoredDecoration> DIRECT_CODEC = StreamCodec.composite(
            MLMapDecorationType.STREAM_CODEC, ColoredDecoration::getType,
            ByteBufCodecs.BYTE, ColoredDecoration::getX,
            ByteBufCodecs.BYTE, ColoredDecoration::getY,
            ByteBufCodecs.BYTE, ColoredDecoration::getRot,
            ComponentSerialization.OPTIONAL_STREAM_CODEC, m -> Optional.ofNullable(m.getDisplayName()),
            DyeColor.STREAM_CODEC, ColoredDecoration::getColor,
            ColoredDecoration::new
    );

    private final DyeColor color;

    public ColoredDecoration(Holder<MLMapDecorationType<?, ?>> type, byte x, byte y, byte rot, Optional<Component> displayName,
                             DyeColor color) {
        super(type, x, y, rot, displayName);
        this.color = color;
    }

    public DyeColor getColor() {
        return color;
    }
}
