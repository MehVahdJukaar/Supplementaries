package net.mehvahdjukaar.supplementaries.common.block.faucet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.StrOpt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.Optional;

public record DataItemInteraction(RuleTest target, ItemStack stack,
                                  Optional<BlockState> output) implements IFaucetItemSource {

    public static final Codec<DataItemInteraction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RuleTest.CODEC.fieldOf("target").forGetter(DataItemInteraction::target),
            ItemStack.CODEC.fieldOf("item").forGetter(DataItemInteraction::stack),
            StrOpt.of(BlockState.CODEC, "replace_with").forGetter(DataItemInteraction::output)
    ).apply(instance, DataItemInteraction::new));

    @Override
    public ItemStack tryExtractItem(Level level, BlockPos pos, BlockState state, Direction direction, BlockEntity tile) {
        if (target.test(state, level.random)) {
            output.ifPresent(s -> level.setBlock(pos, s, 3));
            return stack.copy();
        }
        return ItemStack.EMPTY;
    }
}
