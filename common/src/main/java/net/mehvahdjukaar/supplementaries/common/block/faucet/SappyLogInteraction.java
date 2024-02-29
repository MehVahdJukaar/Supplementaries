package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.reg.ModSoftFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;


class SappyLogInteraction implements FaucetSource.BlState {

    @Override
    public FluidOffer getProvidedFluid(Level level, BlockPos pos, Direction dir, BlockState state) {
        Block backBlock = state.getBlock();

        if (backBlock == CompatObjects.SAPPY_MAPLE_LOG.get() || backBlock == CompatObjects.SAPPY_MAPLE_WOOD.get()) {
            return FluidOffer.of(ModSoftFluids.SAP.getHolder());
        }
        return null;
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, BlockState state, int amount) {
        Optional<Block> log = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation(Utils.getID(state.getBlock())
                .toString().replace("sappy", "stripped")));
        log.ifPresent(block -> level.setBlock(pos, block.withPropertiesOf(state), 3));
    }
}