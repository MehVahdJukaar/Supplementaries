package net.mehvahdjukaar.supplementaries.common.block.faucet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.Optional;

//Data defined fluid interaction
public record DataFluidInteraction(RuleTest target, ResourceLocation softFluid, int amount,
                                   Optional<BlockState> output) implements FaucetSource.BlState {

    //TODO: use softfluidstack codec
    public static final Codec<DataFluidInteraction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RuleTest.CODEC.fieldOf("target").forGetter(DataFluidInteraction::target),
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(DataFluidInteraction::softFluid),
            // SoftFluid.HOLDER_CODEC.fieldOf("fluid").forGetter(DataSourceInteraction::softFluid),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount", 1).forGetter(DataFluidInteraction::amount),
            BlockState.CODEC.optionalFieldOf("replace_with").forGetter(DataFluidInteraction::output)
    ).apply(instance, DataFluidInteraction::new));

    @Override
    public SoftFluidStack getProvidedFluid(Level level, BlockPos pos, Direction dir, BlockState state) {
        if (target.test(state, level.random)) {
            return SoftFluidRegistry.getOptionalHolder(softFluid)
                    .map(fluid -> new SoftFluidStack(fluid, amount)).orElse(null);
        }
        return null;
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, BlockState source, int amount) {
        output.ifPresent(s -> level.setBlock(pos, s, 3));
    }
}
