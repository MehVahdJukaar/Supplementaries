package net.mehvahdjukaar.supplementaries.common.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.LongStream;

public class BlackboardData implements TooltipComponent, TooltipProvider {

    private static final Component WAXED_TOOLTIP = Component.translatable("message.supplementaries.blackboard").withStyle(ChatFormatting.GRAY);


    public static final Codec<BlackboardData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG_STREAM.fieldOf("values")
                    .xmap(LongStream::toArray, Arrays::stream)
                    .forGetter(v -> v.values),
            Codec.BOOL.fieldOf("glow").forGetter(v -> v.glow),
            Codec.BOOL.fieldOf("waxed").forGetter(v -> v.waxed)
    ).apply(instance, BlackboardData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, long[]> LONG_ARRAY = new StreamCodec<>() {
        @Override
        public long[] decode(RegistryFriendlyByteBuf buffer) {
            int size = buffer.readByte();
            long[] values = new long[size];
            for (int i = 0; i < size; i++) {
                values[i] = buffer.readLong();
            }
            return values;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, long[] value) {
            buffer.writeByte(value.length);
            for (long l : value) {
                buffer.writeLong(l);
            }
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, BlackboardData> STREAM_CODEC = StreamCodec.composite(
            LONG_ARRAY, data -> data.values,
            ByteBufCodecs.BOOL, data -> data.glow,
            ByteBufCodecs.BOOL, data -> data.waxed,
            BlackboardData::new
    );

    public static final BlackboardData EMPTY = new BlackboardData(new long[16], false, false);

    private final long[] values;

    private final boolean glow;
    private final boolean waxed;

    BlackboardData(long[] packed, boolean glowing, boolean waxed) {
        this.values = packed;
        this.glow = glowing;
        this.waxed = waxed;
    }

    public static BlackboardData pack(byte[][] pixels, boolean glowing, boolean waxed){
        return new BlackboardData(packPixels(pixels), glowing, waxed);
    }

    public static BlackboardData of(long[] packPixels, boolean glowing, boolean waxed) {
        return new BlackboardData(packPixels, glowing, waxed);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlackboardData that)) return false;
        return glow == that.glow && waxed == that.waxed && Objects.deepEquals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(values), glow, waxed);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        if (waxed) {
            tooltipAdder.accept(WAXED_TOOLTIP);
        }
    }

    public byte[][] unpackPixels() {
        return unpackPixels(values);
    }

    public boolean waxed() {
        return waxed;
    }

    public boolean isGlow() {
        return glow;
    }

    public long[] packedPixels() {
        return values;
    }

    public static long[] packPixels(byte[][] pixels) {
        long[] packed = new long[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            long l = 0;
            for (int j = 0; j < pixels[i].length; j++) {
                l = l | (((long) (pixels[i][j] & 15)) << j * 4);
            }
            packed[i] = l;
        }
        return packed;
    }


    public static byte[][] unpackPixels(long[] packed) {
        byte[][] bytes = new byte[16][16];
        for (int i = 0; i < packed.length; i++) {
            for (int j = 0; j < 16; j++) {
                bytes[i][j] = (byte) ((packed[i] >> j * 4) & 15);
            }
        }
        return bytes;
    }

    public static long[] unpackPixelsFromStringWhiteOnly(String packed) {
        long[] unpacked = new long[16];
        var chars = packed.toCharArray();
        int j = 0;
        for (int i = 0; i + 3 < chars.length; i += 4) {
            long l = 0;
            char c = chars[i];
            for (int k = 0; k < 4; k++) {
                l = l | (((c >> k) & 1) << 4 * k);
            }
            char c2 = chars[i + 1];
            for (int k = 0; k < 4; k++) {
                l = l | ((long) ((c2 >> k) & 1) << (16 + (4 * k)));
            }
            char c3 = chars[i + 2];
            for (int k = 0; k < 4; k++) {
                l = l | ((long) ((c3 >> k) & 1) << (32 + (4 * k)));
            }
            char c4 = chars[i + 3];
            for (int k = 0; k < 4; k++) {
                l = l | ((long) ((c4 >> k) & 1) << (48 + (4 * k)));
            }
            unpacked[j] = l;
            j++;
        }
        return unpacked;
    }

    public static String packPixelsToStringWhiteOnly(long[] packed) {
        StringBuilder builder = new StringBuilder();
        for (var l : packed) {
            char c = 0;
            for (int k = 0; k < 4; k++) {
                byte h = (byte) ((l >> 4 * k) & 1);
                c = (char) (c | (h << k));
            }
            char c1 = 0;
            for (int k = 0; k < 4; k++) {
                byte h = (byte) ((l >> (16 + (4 * k))) & 1);
                c1 = (char) (c1 | (h << k));
            }
            char c2 = 0;
            for (int k = 0; k < 4; k++) {
                byte h = (byte) ((l >> (32 + (4 * k))) & 1);
                c2 = (char) (c2 | (h << k));
            }
            char c3 = 0;
            for (int k = 0; k < 4; k++) {
                byte h = (byte) ((l >> (48 + (4 * k))) & 1);
                c3 = (char) (c3 | (h << k));
            }
            builder.append(c).append(c1).append(c2).append(c3);
        }
        return builder.toString();
    }

    public boolean isEmpty() {
        return false;
    }

    public BlackboardData makeCleared() {
        return new BlackboardData(new long[16], this.glow, this.waxed);
    }

    public BlackboardData withPixel(int x, int y, byte b) {
        long[] newValues = Arrays.copyOf(values, values.length);
        long l = values[x];
        l = l & ~(15L << y * 4);
        l = l | ((long) (b & 15) << y * 4);
        newValues[x] = l;
        return new BlackboardData(newValues, this.glow, this.waxed);
    }

    public byte getPixel(int xx, int yy) {
        return (byte) ((values[xx] >> yy * 4) & 15);
    }

    public BlackboardData withWaxed(boolean b) {
        return new BlackboardData(values, this.glow, b);
    }

    public BlackboardData withGlow(boolean b) {
        return new BlackboardData(values, b, this.waxed);
    }
}
