package net.mehvahdjukaar.supplementaries.common.block.hourglass;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public record HourglassTimeData(HolderSet<Item> dusts, int duration, int light, Optional<ResourceLocation> texture,
                                int ordering) {

    public static final HourglassTimeData EMPTY = new HourglassTimeData(HolderSet.direct(), 0, 0, Optional.empty(), 99);

    public static final Codec<HourglassTimeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(HourglassTimeData::dusts),
            ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(HourglassTimeData::duration),
            Codec.intRange(0, 15).optionalFieldOf("light_level", 0).forGetter(HourglassTimeData::light),
            ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(HourglassTimeData::texture),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("ordering", 0).forGetter(HourglassTimeData::ordering)
    ).apply(instance, HourglassTimeData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HourglassTimeData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM).apply(ByteBufCodecs.list()), h -> h.dusts.stream().toList(),
            ByteBufCodecs.INT, HourglassTimeData::duration,
            ByteBufCodecs.INT, HourglassTimeData::light,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), HourglassTimeData::texture,
            ByteBufCodecs.INT, HourglassTimeData::ordering,
            HourglassTimeData::fromNetwork
    );

    private static HourglassTimeData fromNetwork(List<Holder<Item>> items, Integer integer, Integer integer1, Optional<ResourceLocation> resourceLocation, Integer integer2) {
        return new HourglassTimeData(HolderSet.direct(items),
                integer, integer1, resourceLocation, integer2);
    }

    @Environment(EnvType.CLIENT)
    public ResourceLocation computeTexture(ItemStack i, Level world) {
        if (this.texture.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            ItemRenderer itemRenderer = mc.getItemRenderer();
            BakedModel model = itemRenderer.getModel(i, world, null, 0);
            return model.getParticleIcon().contents().name();

        }
        return this.texture.get();
    }

    public Stream<Holder<Item>> getItems() {
        return dusts.stream();
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public float getIncrement() {
        return 1f / duration;
    }
}
