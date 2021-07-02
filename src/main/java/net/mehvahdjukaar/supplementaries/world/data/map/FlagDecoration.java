package net.mehvahdjukaar.supplementaries.world.data.map;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.CustomDecorationType;
import net.minecraft.item.DyeColor;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.Nullable;

public class FlagDecoration extends CustomDecoration {
    private final DyeColor color;
    public FlagDecoration(CustomDecorationType<?, ?> type, byte x, byte y, byte rot, @Nullable ITextComponent displayName, DyeColor color) {
        super(type, x, y, rot, displayName);
        this.color = color;
    }
    public FlagDecoration(CustomDecorationType<?,?> type, PacketBuffer buffer){
        this(type, buffer.readByte(), buffer.readByte(), (byte)(buffer.readByte() & 15), buffer.readBoolean() ? buffer.readComponent() : null,
                DyeColor.byId(buffer.readByte()));
    }
    public void saveToBuffer(PacketBuffer buffer){
        super.saveToBuffer(buffer);
        buffer.writeByte(color.getId());
    }

    public DyeColor getColor() {
        return color;
    }
}
