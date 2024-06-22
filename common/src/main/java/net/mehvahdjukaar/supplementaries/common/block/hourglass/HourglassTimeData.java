package net.mehvahdjukaar.supplementaries.common.block.hourglass;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.StrOpt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public record HourglassTimeData(HolderSet<Item> dusts, int duration, int light, Optional<ResourceLocation> texture, int ordering) {

    public static final HourglassTimeData EMPTY = new HourglassTimeData(HolderSet.direct(), 0, 0, Optional.empty(), 99);

    public static final Codec<HourglassTimeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(p -> p.dusts),
            ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(p -> p.duration),
            StrOpt.of(Codec.intRange(0, 15), "light_level", 0).forGetter(p -> p.light),
            StrOpt.of(ResourceLocation.CODEC, "texture").forGetter(p -> p.texture),
            StrOpt.of(ExtraCodecs.POSITIVE_INT, "ordering", 0).forGetter(p -> p.ordering)
    ).apply(instance, HourglassTimeData::new));

    public static final Codec<HourglassTimeData> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("items").forGetter(p -> p.dusts.stream()
                    .map(Holder::value)
                    .toList()),
            ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(p -> p.duration),
            StrOpt.of(Codec.intRange(0, 15), "light_level", 0).forGetter(p -> p.light),
            StrOpt.of(ResourceLocation.CODEC, "texture").forGetter(p -> p.texture),
            StrOpt.of(ExtraCodecs.POSITIVE_INT, "ordering", 0).forGetter(p -> p.ordering)
    ).apply(instance, HourglassTimeData::fromNetwork));

    private static HourglassTimeData fromNetwork(List<Item> items, Integer integer, Integer integer1, Optional<ResourceLocation> resourceLocation, Integer integer2) {
        return new HourglassTimeData(HolderSet.direct( items.stream().map(BuiltInRegistries.ITEM::wrapAsHolder).toList()),
                integer, integer1, resourceLocation, integer2);
    }

    public ResourceLocation computeTexture(ItemStack i, Level world) {
        Minecraft mc = Minecraft.getInstance();
        if (this.texture.isEmpty()) {
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
