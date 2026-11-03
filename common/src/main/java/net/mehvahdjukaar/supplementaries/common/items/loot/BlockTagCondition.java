package net.mehvahdjukaar.supplementaries.common.items.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Set;

public record BlockTagCondition(TagKey<Block> tag) implements LootItemCondition {
    public static final MapCodec<BlockTagCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(BlockTagCondition::tag)
    ).apply(instance, BlockTagCondition::new));

    @Override
    public LootItemConditionType getType() {
        return ModRegistry.BLOCK_TAG_CONDITION.get();
    }


    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.BLOCK_STATE);
    }

    @Override
    public boolean test(LootContext context) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);

        return state != null && state.is(tag);
    }
}
