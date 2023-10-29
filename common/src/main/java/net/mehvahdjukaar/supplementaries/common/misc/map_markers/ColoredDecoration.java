package net.mehvahdjukaar.supplementaries.common.misc.map_markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ColoredDecoration extends CustomMapDecoration {
    private final DyeColor color;
    private final int value;

    public ColoredDecoration(MapDecorationType<?, ?> type, byte x, byte y, byte rot, @Nullable Component displayName, @NotNull DyeColor color) {
        super(type, x, y, rot, displayName);
        this.color = color;
        this.value = ColorHelper.pack(color.getTextureDiffuseColors());
    }

    public ColoredDecoration(MapDecorationType<?, ?> type, FriendlyByteBuf buffer) {
        this(type, buffer.readByte(), buffer.readByte(), (byte) (buffer.readByte() & 15), buffer.readBoolean() ? buffer.readComponent() : null,
                DyeColor.byId(buffer.readByte()));
    }

    @Override
    public void saveToBuffer(FriendlyByteBuf buffer) {
        super.saveToBuffer(buffer);
        buffer.writeByte(color.getId());
    }

    public DyeColor getColor() {
        return color;
    }

    public int getColorValue() {
        return value;
    }

}
