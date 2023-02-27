package net.mehvahdjukaar.supplementaries.common.block.hourglass;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.stream.Stream;


public class HourglassTimeData {

    public static final HourglassTimeData EMPTY = new HourglassTimeData(HolderSet.direct(), 0,0, Optional.empty(), 99);

    public static final Codec<HourglassTimeData> CODEC = RecordCodecBuilder.<HourglassTimeData>create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registry.ITEM_REGISTRY).fieldOf("items").forGetter(p -> p.dusts),
            ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(p->p.duration),
            Codec.intRange(0, 15).optionalFieldOf("light_level",0).forGetter(p->p.light),
            ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(p-> p.texture),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("ordering",0).forGetter(p->p.ordering)
    ).apply(instance, HourglassTimeData::new));

    private final HolderSet<Item> dusts;
    private final int duration;
    private final int light;
    private final Optional<ResourceLocation> texture;
    private final int ordering;

    public HourglassTimeData(HolderSet<Item> owner, int duration, int light, Optional<ResourceLocation> texture, int priority) {
        this.dusts = owner;
        this.duration = duration;
        this.light = light;
        this.texture = texture;
        this.ordering = priority;
    }

    @Environment(EnvType.CLIENT)
    public TextureAtlasSprite computeSprite(ItemStack i, Level world) {
        Minecraft mc = Minecraft.getInstance();
        if (this.texture.isEmpty()) {
            ItemRenderer itemRenderer = mc.getItemRenderer();
            BakedModel model = itemRenderer.getModel(i, world, null, 0);
            TextureAtlasSprite sprite = model.getParticleIcon();
           // if (sprite instanceof MissingTextureAtlasSprite)
               // sprite = mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(this.texture);
            return sprite;

        }
        return mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(this.texture.get());
    }

    public Stream<Holder<Item>> getItems() {
        return dusts.stream();
    }

    public int getLight() {
        return light;
    }

    public int getOrdering() {
        return ordering;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public float getIncrement() {
        return 1f/duration;
    }
}
