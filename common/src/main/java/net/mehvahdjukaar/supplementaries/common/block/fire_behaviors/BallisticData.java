package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BallisticData(float drag, float gravity, float initialSpeed) {
    public static final BallisticData LINE = new BallisticData(1, 0, 1);

    public static final Codec<BallisticData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("drag").forGetter(BallisticData::drag),
            Codec.FLOAT.fieldOf("gravity").forGetter(BallisticData::gravity),
            Codec.FLOAT.fieldOf("initialSpeed").forGetter(BallisticData::initialSpeed)
    ).apply(instance, BallisticData::new));

    public static final StreamCodec<FriendlyByteBuf, BallisticData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, BallisticData::drag,
            ByteBufCodecs.FLOAT, BallisticData::gravity,
            ByteBufCodecs.FLOAT, BallisticData::initialSpeed,
            BallisticData::new
    );

}
