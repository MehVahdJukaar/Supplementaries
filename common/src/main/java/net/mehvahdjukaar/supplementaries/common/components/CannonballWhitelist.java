package net.mehvahdjukaar.supplementaries.common.components;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

public record CannonballWhitelist(Set<Block> blocks) {

    public static final CannonballWhitelist EMPTY = new CannonballWhitelist(Set.of());

    public CannonballWhitelist(Set<Block> blocks) {
        this.blocks = Set.copyOf(blocks);
    }

    public static final Codec<CannonballWhitelist> CODEC = BuiltInRegistries.BLOCK.byNameCodec().listOf()
            .xmap(l -> new CannonballWhitelist(Set.copyOf(l)), c -> List.copyOf(c.blocks));

    public static final StreamCodec<RegistryFriendlyByteBuf, CannonballWhitelist> STREAM_CODEC =
            ByteBufCodecs.registry(Registries.BLOCK).apply(ByteBufCodecs.list())
                    .map(l -> new CannonballWhitelist(Set.copyOf(l)), c -> List.copyOf(c.blocks));

    public boolean contains(Block block) {
        return this.blocks.contains(block);
    }

    public Set<Block> blocks() {
        return Set.copyOf(this.blocks);
    }

}
