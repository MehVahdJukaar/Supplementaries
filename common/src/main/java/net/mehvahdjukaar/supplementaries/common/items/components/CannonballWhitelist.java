package net.mehvahdjukaar.supplementaries.common.items.components;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class CannonballWhitelist {

    public static final CannonballWhitelist EMPTY = new CannonballWhitelist(Set.of());

    private CannonballWhitelist(Set<Block> blocks) {
        this.blocks = blocks;
    }

    public static final Codec<CannonballWhitelist> CODEC = BuiltInRegistries.BLOCK.byNameCodec().listOf()
            .xmap(l -> new CannonballWhitelist(Set.copyOf(l)), c -> List.copyOf(c.blocks));

    public static final StreamCodec<RegistryFriendlyByteBuf, CannonballWhitelist> STREAM_CODEC =
            ByteBufCodecs.registry(Registries.BLOCK).apply(ByteBufCodecs.list())
                    .map(l -> new CannonballWhitelist(Set.copyOf(l)), c -> List.copyOf(c.blocks));
    private final Set<Block> blocks;


    public boolean contains(Block block) {
        return this.blocks.contains(block);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CannonballWhitelist) obj;
        return Objects.equals(this.blocks, that.blocks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blocks);
    }

    @Override
    public String toString() {
        return "CannonballWhitelist[" + "blocks=" + blocks + ']';
    }


}
