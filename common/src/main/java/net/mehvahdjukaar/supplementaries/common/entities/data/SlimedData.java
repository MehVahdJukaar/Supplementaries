package net.mehvahdjukaar.supplementaries.common.entities.data;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class SlimedData {

    public static final Codec<SlimedData> CODEC = Codec.INT
            .xmap(SlimedData::new, d -> d.slimedTicks);

    public static final StreamCodec<RegistryFriendlyByteBuf, SlimedData> STREAM_CODEC = (StreamCodec) ByteBufCodecs.VAR_INT
            .map(SlimedData::new, d -> d.slimedTicks);

    private int slimedTicks;

    public SlimedData(int slimedTicks) {
        this.slimedTicks = slimedTicks;
    }

    public SlimedData() {
        this(0);
    }

    public void tick(LivingEntity entity) {
        if (slimedTicks > 0) {
            if (entity.isUnderWater()) {
                slimedTicks = 0;
                ModEntities.SLIMED_DATA.sync(entity);
            } else {
                slimedTicks--;
            }
        }
    }

    public void setSlimedTicks(LivingEntity entity, int newSlimedTicks) {
        int old = this.slimedTicks;
        this.slimedTicks = newSlimedTicks;
        if (!entity.level().isClientSide) {
            ModEntities.SLIMED_DATA.sync(entity);
            if (newSlimedTicks > old) {
                //send packet
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        ModSounds.SLIME_SPLAT.get(), entity.getSoundSource(), 1, 1);
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

    public static float getAlpha(LivingEntity le, float partialTicks) {
        if (!ClientConfigs.Tweaks.SLIME_OVERLAY.get()) return 0;
        SlimedData data = ModEntities.SLIMED_DATA.getOrCreate(le);
        float slimeTicks = data.slimedTicks - partialTicks;
        float maxFade = 70;
        return slimeTicks > maxFade ? 1 : Mth.clamp(slimeTicks / maxFade, 0, 1);
    }

}
