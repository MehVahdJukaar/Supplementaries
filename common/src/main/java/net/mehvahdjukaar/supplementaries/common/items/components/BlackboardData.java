package net.mehvahdjukaar.supplementaries.common.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.LongStream;

public class BlackboardData implements TooltipComponent, TooltipProvider {

    private static final Component WAXED_TOOLTIP = Component.translatable("message.supplementaries.blackboard").withStyle(ChatFormatting.GRAY);
    private static final Component GLOW_TOOLTIP = Component.translatable("message.supplementaries.glowing").withStyle(ChatFormatting.GRAY);
    private static final int SIZE = 16;


    private static Codec<byte[][]> byteMatrix(int size) {
        return Codec.BYTE_BUFFER.xmap(buffer -> {
            byte[][] matrix = new byte[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    matrix[i][j] = buffer.get();
                }
            }
            return matrix;
        }, bytes -> {
            ByteBuffer buffer = ByteBuffer.allocate(size * size * 4);
            for (byte[] row : bytes) {
                buffer.put(row);
            }
            return buffer;
        });
    }

    private static final Codec<byte[][]> MATRIX_CODEC_OR_LEGACY = Codec.withAlternative(
            byteMatrix(SIZE),
            Codec.LONG_STREAM.xmap(LongStream::toArray, Arrays::stream)
                    .xmap(BlackboardData::unpackPixels, BlackboardData::packPixels)
    );

    public static final Codec<BlackboardData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MATRIX_CODEC_OR_LEGACY.fieldOf("values").forGetter(v -> v.pixels),
            Codec.BOOL.fieldOf("glow").forGetter(v -> v.glow),
            Codec.BOOL.fieldOf("waxed").forGetter(v -> v.waxed)
    ).apply(instance, BlackboardData::new));


    private static StreamCodec<ByteBuf, byte[][]> byteMatrixStream(int size) {
        return ByteBufCodecs.BYTE_ARRAY.map(buffer -> {
            byte[][] matrix = new byte[size][size];
            for (int i = 0; i < size; i++) {
                System.arraycopy(buffer, i * size, matrix[i], 0, size);
            }
            return matrix;
        }, bytes -> {
            var flattened = new byte[size * size];
            for (int i = 0; i < size; i++) {
                System.arraycopy(bytes[i], 0, flattened, i * size, size);
            }
            return flattened;
        });
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, BlackboardData> STREAM_CODEC = StreamCodec.composite(
            byteMatrixStream(SIZE), data -> data.pixels,
            ByteBufCodecs.BOOL, data -> data.glow,
            ByteBufCodecs.BOOL, data -> data.waxed,
            BlackboardData::new
    );

    public static final BlackboardData EMPTY = new BlackboardData(new byte[SIZE][SIZE], false, false);

    private final byte[][] pixels;
    private final boolean glow;
    private final boolean waxed;

    private final int cachedHashCode;

    public BlackboardData(byte[][] pixels, boolean glowing, boolean waxed) {
        this.pixels = pixels;
        this.glow = glowing;
        this.waxed = waxed;
        this.cachedHashCode = Objects.hash(Arrays.deepHashCode(pixels), glow, waxed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlackboardData that)) return false;
        return glow == that.glow && waxed == that.waxed && Objects.deepEquals(pixels, that.pixels);
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        if (waxed) {
            tooltipAdder.accept(WAXED_TOOLTIP);
        }
        if (glow) {
            tooltipAdder.accept(GLOW_TOOLTIP);
        }
    }

    public boolean isWaxed() {
        return waxed;
    }

    public boolean isGlow() {
        return glow;
    }

    public boolean hasSamePixels(byte[][] pixels) {
        return Arrays.deepEquals(this.pixels, pixels);
    }

    public boolean isEmpty() {
        for (byte[] row : pixels) {
            for (byte value : row) {
                if (value != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public byte getPixel(int xx, int yy) {
        return pixels[xx][yy];
    }

    public byte[][] getPixelsUnsafe() {
        return pixels;
    }

    public BlackboardData makeCleared() {
        return new BlackboardData(new byte[SIZE][SIZE], this.glow, this.waxed);
    }

    public BlackboardData withPixel(int x, int y, byte b) {
        byte[][] newPixels = new byte[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(pixels[i], 0, newPixels[i], 0, SIZE);
        }
        newPixels[x][y] = b;
        return new BlackboardData(newPixels, this.glow, this.waxed);
    }

    public BlackboardData withWaxed(boolean b) {
        return new BlackboardData(pixels, this.glow, b);
    }

    public BlackboardData withGlow(boolean b) {
        return new BlackboardData(pixels, b, this.waxed);
    }

    public BlackboardData withPixels(byte[][] pixels) {
        return new BlackboardData(pixels, this.glow, this.waxed);
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

    //string length = 16*4+64 = 128
    public static String packPixelsToString(long[] packed) {
        StringBuilder builder = new StringBuilder();
        for (var l : packed) {
            char c = 0;
            for (int k = 0; k < 4; k++) {
                byte h = 0;
                for (int j = 0; j < 4; j++) {
                    h = (byte) (h | ((l >> (j + (4 * k))) & 1));
                }
                c = (char) (c | (h << k));
            }
            char c1 = 0;
            for (int k = 0; k < 4; k++) {
                byte h = 0;
                for (int j = 0; j < 4; j++) {
                    h = (byte) (h | ((l >> (j + 16 + (4 * k))) & 1));
                }
                c1 = (char) (c1 | (h << k));
            }
            char c2 = 0;
            for (int k = 0; k < 4; k++) {
                byte h = 0;
                for (int j = 0; j < 4; j++) {
                    h = (byte) (h | ((l >> (j + 32 + (4 * k))) & 1));
                }
                c2 = (char) (c2 | (h << k));
            }
            char c3 = 0;
            for (int k = 0; k < 4; k++) {
                byte h = 0;
                for (int j = 0; j < 4; j++) {
                    h = (byte) (h | ((l >> (j + 48 + (4 * k))) & 1));
                }
                c3 = (char) (c3 | (h << k));
            }
            builder.append(c).append(c1).append(c2).append(c3);
        }
        for (var l : packed) {
            char a = (char) (l & Character.MAX_VALUE);
            char b = (char) (l >> 16 & Character.MAX_VALUE);
            char c = (char) (l >> 32 & Character.MAX_VALUE);
            char d = (char) (l >> 48 & Character.MAX_VALUE);
            builder.append(a).append(b).append(c).append(d);
        }
        return builder.toString();
    }

    public static long[] unpackPixelsFromString(String packed) {
        long[] unpacked = unpackPixelsFromStringWhiteOnly(packed);
        if (packed.length() <= 64)
            return unpacked;
        var chars = packed.substring(64).toCharArray();
        int j = 0;
        for (int i = 0; i + 3 < chars.length && j < 16; i += 4) {
            unpacked[j] = (unpacked[j] << 3) | (unpacked[j] << 2) | (unpacked[j] << 1);
            unpacked[j] = unpacked[j] & ((long) chars[i + 3] << 48 | (long) chars[i + 2] << 32 | (long) chars[i + 1] << 16 | chars[i]);
            j++;
        }
        return unpacked;
    }

    public static long[] unpackPixelsFromStringWhiteOnly(String packed) {
        long[] unpacked = new long[16];
        var chars = packed.toCharArray();
        int j = 0;
        for (int i = 0; i + 3 < chars.length && j < 16; i += 4) {
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

}
