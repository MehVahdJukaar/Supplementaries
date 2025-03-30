package net.mehvahdjukaar.supplementaries.common.block.placeable_book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public record BookType(Item item, float enchantPower, boolean isHorizontal,
                       ResourceLocation bookVisuals) {

    public static final Codec<BookType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(BookType::item),
            Codec.FLOAT.optionalFieldOf("enchant_power", 0f).forGetter(BookType::enchantPower),
            Codec.BOOL.optionalFieldOf("horizontal", false).forGetter(BookType::isHorizontal),
            ResourceLocation.CODEC.fieldOf("book_visuals").forGetter(BookType::bookVisuals)
    ).apply(instance, BookType::new));

    //client sync codec
    public static final StreamCodec<RegistryFriendlyByteBuf, BookType> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM), BookType::item,
            ByteBufCodecs.FLOAT, BookType::enchantPower,
            ByteBufCodecs.BOOL, BookType::isHorizontal,
            ResourceLocation.STREAM_CODEC, BookType::bookVisuals,
            BookType::new
    );

}
