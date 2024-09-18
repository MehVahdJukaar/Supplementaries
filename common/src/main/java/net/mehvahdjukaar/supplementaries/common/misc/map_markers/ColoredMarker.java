package net.mehvahdjukaar.supplementaries.common.misc.map_markers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class ColoredMarker extends MLMapMarker<ColoredDecoration> {

    public static final MapCodec<ColoredMarker> DIRECT_CODEC = RecordCodecBuilder.mapCodec(i ->
            baseCodecGroup(i).and(DyeColor.CODEC.fieldOf("Color").forGetter(m -> m.color)
            ).apply(i, ColoredMarker::new));

    private final DyeColor color;

    public ColoredMarker(Holder<MLMapDecorationType<?, ?>> type, BlockPos pos, float rotation, Optional<Component> component,
                         Optional<Boolean> shouldRefresh, Optional<Boolean> shouldSave, boolean preventsExtending,
                         DyeColor color) {
        super(type, pos, rotation, component, shouldRefresh, shouldSave, preventsExtending);
        this.color = color;
    }

    public ColoredMarker(Holder<MLMapDecorationType<?, ?>> type, BlockPos pos, DyeColor color) {
        this(type, pos, null, color);
    }

    public ColoredMarker(Holder<MLMapDecorationType<?, ?>> type, BlockPos pos, @Nullable Component name, DyeColor color) {
        this(type, pos, 0f, Optional.ofNullable(name), Optional.empty(),
                Optional.empty(), false, color);
    }

    @Nullable
    @Override
    public ColoredDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new ColoredDecoration(this.getType(), mapX, mapY, rot, this.name, this.color);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && o instanceof ColoredMarker marker && this.color == marker.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getType(), this.pos, this.name, this.color);
    }

}
