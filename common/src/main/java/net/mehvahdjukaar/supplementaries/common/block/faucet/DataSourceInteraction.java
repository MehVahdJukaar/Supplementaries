package net.mehvahdjukaar.supplementaries.common.block.faucet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.Optional;

//Data defined fluid interaction
public record DataSourceInteraction(RuleTest target, ResourceLocation softFluid, int amount,
                                    Optional<BlockState> output) implements IFaucetBlockSource {

    public static final Codec<DataSourceInteraction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RuleTest.CODEC.fieldOf("target").forGetter(DataSourceInteraction::target),
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(DataSourceInteraction::softFluid),
            // SoftFluid.HOLDER_CODEC.fieldOf("fluid").forGetter(DataSourceInteraction::softFluid),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount", 1).forGetter(DataSourceInteraction::amount),
            BlockState.CODEC.optionalFieldOf("replace_with").forGetter(DataSourceInteraction::output)
    ).apply(instance, DataSourceInteraction::new));

    @Override
    public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state,
                                      FaucetBlockTile.FillAction fillAction) {
        if (target.test(state, level.random)) {
            var fluid = SoftFluidRegistry.getOptional(softFluid);
            if (fluid.isPresent()) {
                faucetTank.fill(fluid.get());
                faucetTank.setCount(amount);
                if (fillAction == null) return InteractionResult.SUCCESS;
                if (fillAction.tryExecute()) {
                    output.ifPresent(s -> level.setBlock(pos, s, 3));
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
        }
        return null;
    }

    @Override
    public int getTransferCooldown() {
        return IFaucetBlockSource.super.getTransferCooldown() * amount;
    }
}
