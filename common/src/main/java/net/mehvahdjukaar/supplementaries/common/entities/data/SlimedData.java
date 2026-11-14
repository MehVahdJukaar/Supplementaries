package net.mehvahdjukaar.supplementaries.common.entities.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.util.codec.CodecUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModData;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.Locale;

public class SlimedData {

    public static final Codec<SlimedData> CODEC = Codec.withAlternative(RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("ticks").forGetter(d -> d.slimedTicks),
            Type.CODEC.optionalFieldOf("type", Type.SLIME).forGetter(d -> d.type)
    ).apply(i, SlimedData::new)), Codec.INT.xmap(SlimedData::new, d -> d.slimedTicks));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlimedData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, d -> d.slimedTicks,
            Type.STREAM_CODEC, d -> d.type,
            SlimedData::new);

    private int slimedTicks;
    private Type type = Type.SLIME;

    public SlimedData(int slimedTicks) {
        this.slimedTicks = slimedTicks;
    }

    public SlimedData(int slimedTicks, Type type) {
        this.slimedTicks = slimedTicks;
        this.type = type;
    }

    public SlimedData() {
        this(0);
    }

    public float getAlpha(float partialTicks) {
        if (!ClientConfigs.Tweaks.SLIME_OVERLAY.get()) return 0;
        float slimeTicks = slimedTicks - partialTicks;
        float maxFade = 70;
        return slimeTicks > maxFade ? 1 : Mth.clamp(slimeTicks / maxFade, 0, 1);
    }

    public void tick(LivingEntity entity) {
        if (slimedTicks > 0) {
            if (entity.isUnderWater()) {
                slimedTicks = 0;
                ModData.SLIMED_DATA.sync(entity);
            } else {
                slimedTicks--;
            }
        }
    }

    public Type getType() {
        return type;
    }

    public void setSlimedTicks(LivingEntity entity, int newSlimedTicks) {
        setSlimedTicks(entity, newSlimedTicks, Type.SLIME);
    }

    public void setSlimedTicks(LivingEntity entity, int newSlimedTicks, Type type) {
        int old = this.slimedTicks;
        this.slimedTicks = newSlimedTicks;
        this.type = type;
        if (!entity.level().isClientSide) {
            ModData.SLIMED_DATA.sync(entity);
            if (newSlimedTicks > old) {
                //send packet
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        type.getHitSound(), entity.getSoundSource(), 1, 1);
            }
        }
    }

    public void clear(LivingEntity entity) {
        setSlimedTicks(entity, 0);
    }

    public boolean isSlimed() {
        return slimedTicks > 0;
    }

    public int getSlimedTicks() {
        return slimedTicks;
    }

    public enum Type implements StringRepresentable {
        SLIME,
        MUD;

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
        public static final StreamCodec<FriendlyByteBuf, Type> STREAM_CODEC = CodecUtils.enumStreamCodec(Type.class);

        public SoundEvent getHitSound() {
            return switch (this) {
                case SLIME -> ModSounds.SLIME_SPLAT.get();
                case MUD -> SoundEvents.MUD_HIT;
            };
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

}
