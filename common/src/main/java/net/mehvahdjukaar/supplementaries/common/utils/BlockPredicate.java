package net.mehvahdjukaar.supplementaries.common.utils;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface BlockPredicate extends Predicate<BlockState> {

    Codec<BlockPredicate> CODEC = Codec.STRING.xmap(
            BlockPredicate::create,
            blockStatePredicate -> {
                if (blockStatePredicate instanceof Tag tp) {
                    return "#" + tp.tag.location();
                } else if (blockStatePredicate instanceof Block bp) {
                    return bp.id.toString();
                }
                throw new IllegalArgumentException("Must either be Tag or Block predicate");
            }
    );

    @NotNull
    static BlockPredicate create(String s) {
        if (s.startsWith("#")) {
            var tag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(s.replace("#", "")));
            return new Tag(tag);
        }
        ResourceLocation id = ResourceLocation.parse(s);
        return new Block(id);
    }


    record Tag(TagKey<net.minecraft.world.level.block.Block> tag) implements BlockPredicate {

        @Override
        public boolean test(BlockState state) {
            return state.is(tag);
        }
    }

    record Block(ResourceLocation id) implements BlockPredicate {

        @Override
        public boolean test(BlockState state) {
            return BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(id);
        }
    }
}
