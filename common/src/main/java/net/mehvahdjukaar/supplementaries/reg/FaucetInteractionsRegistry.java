package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.block.ISoftFluidConsumer;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidProvider;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.VanillaSoftFluids;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile.*;
import net.mehvahdjukaar.supplementaries.common.utils.FluidsUtil;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.InspirationCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class FaucetInteractionsRegistry {

    public static void registerBehaviors() {
        FaucetBlockTile.registerInteraction(new SoftFluidProviderInteraction());
        FaucetBlockTile.registerInteraction(new WaterCauldronInteraction());
        FaucetBlockTile.registerInteraction(new LavaCauldronInteraction());
        FaucetBlockTile.registerInteraction(new PowderSnowCauldronInteraction());
        FaucetBlockTile.registerInteraction(new BeehiveInteraction());
        FaucetBlockTile.registerInteraction(new SoftFluidTankInteraction());
        FaucetBlockTile.registerInteraction(new ForgeFluidTankInteraction());
        FaucetBlockTile.registerInteraction(new WaterBlockInteraction());
        FaucetBlockTile.registerInteraction(new SpongeInteraction());
        FaucetBlockTile.registerInteraction(new XPDroppingInteraction());
        if (CompatHandler.BUZZIER_BEES) FaucetBlockTile.registerInteraction(new HoneyPotInteraction());
        if (CompatHandler.AUTUMNITY) FaucetBlockTile.registerInteraction(new SappyLogInteraction());

    }

    private static class WaterBlockInteraction implements IFluidSourceInteraction {

        @Override
        public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                          BlockPos pos, FluidState fluidState, FillAction fillAction) {

            if (fluidState.getType() == Fluids.WATER) {
                //Unlimited water!!
                prepareToTransferBottle(faucetTank, VanillaSoftFluids.WATER.get());
                if (fillAction.tryExecute()) {
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        }

    }

    //consume to finish current group
    private static class SoftFluidTankInteraction implements
            ITileSourceInteraction, ITileTargetInteraction {

        @Override
        public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                          BlockPos pos, BlockEntity tile, Direction dir, FillAction fillAction) {
            if (tile instanceof ISoftFluidTankProvider holder && holder.canInteractWithSoftFluidTank()) {
                SoftFluidTank fluidHolder = holder.getSoftFluidTank();
                faucetTank.copy(fluidHolder);
                faucetTank.setCount(2);
                if (fillAction.tryExecute()) {
                    fluidHolder.shrink(1);
                    tile.setChanged();
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }

        @Override
        public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockEntity tile) {
            if (tile instanceof ISoftFluidTankProvider holder) {
                SoftFluidTank tank = holder.getSoftFluidTank();
                boolean result = faucetTank.tryTransferFluid(tank, faucetTank.getCount() - 1);
                if (result) {
                    tile.setChanged();
                    faucetTank.fillCount();
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }
    }

    private static class ForgeFluidTankInteraction implements
            ITileSourceInteraction, ITileTargetInteraction {

        @Override
        public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                          BlockPos pos, BlockEntity tile, Direction dir, FillAction fillAction) {
            if (FluidsUtil.tryExtractFromFluidHandler(tile, tile.getBlockState().getBlock(), dir, faucetTank, fillAction)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        @Override
        public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockEntity tile) {
            return FluidsUtil.tryFillFluidTank(tile, faucetTank) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
    }

    private static class SoftFluidProviderInteraction implements
            IBlockSourceInteraction, ITileSourceInteraction, ITileTargetInteraction, IBlockTargetInteraction {

        @Override
        public int getTransferCooldown() {
            return IBlockSourceInteraction.super.getTransferCooldown();
        }

        @Override
        public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                          BlockPos pos, BlockState state, FillAction fillAction) {
            return drainGeneric(level, faucetTank, pos, state, fillAction, state.getBlock());
        }

        @Override
        public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockEntity tile, Direction dir, FillAction fillAction) {
            return drainGeneric(level, faucetTank, pos, tile.getBlockState(), fillAction, tile);
        }

        private static InteractionResult drainGeneric(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state, FillAction fillAction, Object backBlock) {
            if (backBlock instanceof ISoftFluidProvider provider) {
                var stack = provider.getProvidedFluid(level, state, pos);
                prepareToTransferBottle(faucetTank, stack.getFirst(), stack.getSecond());
                if (fillAction.tryExecute()) {
                    provider.consumeProvidedFluid(level, state, pos, faucetTank.getFluid(), faucetTank.getNbt(), 1);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        @Override
        public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
            return tryFillGeneric(level, faucetTank, pos, state, state.getBlock());
        }


        @Override
        public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockEntity tile) {
            return tryFillGeneric(level, faucetTank, pos, tile.getBlockState(), tile);
        }

        public InteractionResult tryFillGeneric(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state, Object object) {
            if (object instanceof ISoftFluidConsumer consumer) {
                return consumer.tryAcceptingFluid(level, state, pos, faucetTank.getFluid(), faucetTank.getNbt(), 1)
                        ? InteractionResult.SUCCESS : InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }
    }

    private static class SpongeInteraction implements IBlockTargetInteraction {

        @Override
        public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
            if (state.getBlock() == Blocks.SPONGE) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
    }

    private static class BeehiveInteraction implements IBlockSourceInteraction, IBlockTargetInteraction {

        @Override
        public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                          BlockPos pos, BlockState state, FillAction fillAction) {

            if (state.hasProperty(BlockStateProperties.LEVEL_HONEY)) {
                if (state.getValue(BlockStateProperties.LEVEL_HONEY) == 5) {
                    prepareToTransferBottle(faucetTank, VanillaSoftFluids.HONEY.get());
                    if (fillAction.tryExecute()) {
                        level.setBlock(pos, state.setValue(BlockStateProperties.LEVEL_HONEY,
                                state.getValue(BlockStateProperties.LEVEL_HONEY) - 1), 3);
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }

        @Override
        public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
            var fluid = faucetTank.getFluid();
            if (fluid == VanillaSoftFluids.HONEY.get() && state.hasProperty(BlockStateProperties.LEVEL_HONEY)) {
                int h = state.getValue(BlockStateProperties.LEVEL_HONEY);
                if (h == 0) {
                    level.setBlock(pos, state.setValue(BlockStateProperties.LEVEL_HONEY, 5), 3);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }
    }

    private static class XPDroppingInteraction implements IBlockTargetInteraction {

        @Override
        public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
            var fluid = faucetTank.getFluid();
            if (state.isAir()) {
                if (fluid == VanillaSoftFluids.XP.get()) {
                    this.dropXP(level, pos);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }

        private void dropXP(Level level, BlockPos pos) {
            int i = 3 + level.random.nextInt(5) + level.random.nextInt(5);
            while (i > 0) {
                int xp = ExperienceOrb.getExperienceValue(i);
                i -= xp;
                ExperienceOrb orb = new ExperienceOrb(level, pos.getX() + 0.5, pos.getY() - 0.125f, pos.getZ() + 0.5, xp);
                orb.setDeltaMovement(new Vec3(0, 0, 0));
                level.addFreshEntity(orb);
            }
            float f = (level.random.nextFloat() - 0.5f) / 4f;
            level.playSound(null, pos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.3F, 0.5f + f);
        }

    }

    private static class HoneyPotInteraction implements IBlockSourceInteraction, IBlockTargetInteraction {

        @Override
        public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                          BlockPos pos, BlockState state, FillAction fillAction) {

            if (state.hasProperty(ModBlockProperties.HONEY_LEVEL_POT)) {
                if (state.getValue(ModBlockProperties.HONEY_LEVEL_POT) > 0) {
                    prepareToTransferBottle(faucetTank, VanillaSoftFluids.HONEY.get());
                    if (fillAction.tryExecute()) {
                        level.setBlock(pos, state.setValue(ModBlockProperties.HONEY_LEVEL_POT,
                                state.getValue(ModBlockProperties.HONEY_LEVEL_POT) - 1), 3);
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }

        @Override
        public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
            var fluid = faucetTank.getFluid();

            if (fluid == VanillaSoftFluids.HONEY.get() && state.hasProperty(ModBlockProperties.HONEY_LEVEL_POT)) {
                int h = state.getValue(ModBlockProperties.HONEY_LEVEL_POT);
                if (h < 4) {
                    level.setBlock(pos, state.setValue(ModBlockProperties.HONEY_LEVEL_POT, h + 1), 3);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }
    }

    private static class SappyLogInteraction implements IBlockSourceInteraction {

        @Override
        public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                          BlockPos pos, BlockState state, FillAction fillAction) {
            Block backBlock = state.getBlock();
            if (backBlock == CompatObjects.SAPPY_MAPLE_LOG.get() || backBlock == CompatObjects.SAPPY_MAPLE_WOOD.get()) {
                prepareToTransferBottle(faucetTank, ModSoftFluids.SAP.get());
                if (fillAction.tryExecute()) {
                    Optional<Block> log = Registry.BLOCK.getOptional(new ResourceLocation(Utils.getID(backBlock).toString().replace("sappy", "stripped")));
                    log.ifPresent(block -> level.setBlock(pos, block.withPropertiesOf(state), 3));
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }
    }

    private static class MalumInteraction implements IBlockSourceInteraction {

        @Override
        public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                          BlockPos pos, BlockState state, FillAction fillAction) {
  /* else if (CompatHandler.malum && MalumPlugin.isSappyLog(backBlock)) {
            this.prepareToTransferBottle(MalumPlugin.getSap(backBlock));
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                MalumPlugin.extractSap(level, backState, behind);
                return true;
            }
        }*/
            return InteractionResult.PASS;
        }
    }

    private static class WaterCauldronInteraction implements IBlockSourceInteraction, IBlockTargetInteraction {

        @Override
        public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                          BlockPos pos, BlockState state, FillAction fillAction) {
            if (state.is(Blocks.WATER_CAULDRON)) {
                int waterLevel = state.getValue(BlockStateProperties.LEVEL_CAULDRON);
                if (waterLevel > 0) {
                    if (CompatHandler.INSPIRATIONS) {
                        return InspirationCompat.doCauldronStuff(level.getBlockEntity(pos), faucetTank, fillAction);
                    }

                    prepareToTransferBottle(faucetTank, VanillaSoftFluids.WATER.get());
                    if (fillAction.tryExecute()) {
                        if (waterLevel > 1) {
                            level.setBlock(pos, state.setValue(BlockStateProperties.LEVEL_CAULDRON,
                                    waterLevel - 1), 3);
                        } else level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }

        @Override
        public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
            if (state.getBlock() instanceof AbstractCauldronBlock) {
                SoftFluid softFluid = faucetTank.getFluid();
                if (CompatHandler.INSPIRATIONS) {
                    return InspirationCompat.tryAddFluid(level.getBlockEntity(pos), faucetTank);
                } else if (softFluid == VanillaSoftFluids.WATER.get()) {
                    if (state.is(Blocks.WATER_CAULDRON)) {
                        int levels = state.getValue(BlockStateProperties.LEVEL_CAULDRON);
                        if (levels < 3) {
                            level.setBlock(pos, state.setValue(BlockStateProperties.LEVEL_CAULDRON, levels + 1), 3);
                            return InteractionResult.SUCCESS;
                        }
                        return InteractionResult.FAIL;
                    } else if (state.is(Blocks.CAULDRON)) {
                        level.setBlock(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(BlockStateProperties.LEVEL_CAULDRON, 1), 3);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.PASS;
        }
    }

    private static class LavaCauldronInteraction implements IBlockSourceInteraction, IBlockTargetInteraction {

        @Override
        public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                          BlockPos pos, BlockState state, FillAction fillAction) {
            if (state.is(Blocks.LAVA_CAULDRON)) {
                prepareToTransferBucket(faucetTank, VanillaSoftFluids.LAVA.get());
                if (fillAction.tryExecute()) {
                    level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }

        @Override
        public int getTransferCooldown() {
            return IBlockSourceInteraction.super.getTransferCooldown() * 3;
        }


        @Override
        public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
            if (state.is(Blocks.CAULDRON) && faucetTank.getFluid() == VanillaSoftFluids.LAVA.get()) {
                if (faucetTank.getCount() == 5) {
                    level.setBlock(pos, Blocks.LAVA_CAULDRON.defaultBlockState(), 3);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }
    }

    private static class PowderSnowCauldronInteraction implements IBlockSourceInteraction, IBlockTargetInteraction {

        @Override
        public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                          BlockPos pos, BlockState state, FillAction fillAction) {
            if (state.is(Blocks.POWDER_SNOW_CAULDRON)) {
                int waterLevel = state.getValue(BlockStateProperties.LEVEL_CAULDRON);
                if (waterLevel == 3) {
                    prepareToTransferBucket(faucetTank, VanillaSoftFluids.POWDERED_SNOW.get());
                    if (fillAction.tryExecute()) {
                        level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.PASS;
        }

        @Override
        public int getTransferCooldown() {
            return IBlockSourceInteraction.super.getTransferCooldown() * 3;
        }

        @Override
        public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
            if (state.is(Blocks.CAULDRON) && faucetTank.getFluid() == VanillaSoftFluids.POWDERED_SNOW.get()) {
                if (faucetTank.getCount() == 5) {
                    level.setBlock(pos, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState()
                            .setValue(LayeredCauldronBlock.LEVEL, 3), 3);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }
    }


    private static void prepareToTransferBottle(SoftFluidTank tempFluidHolder, SoftFluid softFluid) {
        tempFluidHolder.fill(softFluid);
        tempFluidHolder.setCount(2);
    }

    private static void prepareToTransferBottle(SoftFluidTank tempFluidHolder, SoftFluid softFluid, CompoundTag tag) {
        tempFluidHolder.fill(softFluid, tag);
        tempFluidHolder.setCount(2);
    }

    private static void prepareToTransferBucket(SoftFluidTank tempFluidHolder, SoftFluid softFluid) {
        tempFluidHolder.fill(softFluid);
    }

}



