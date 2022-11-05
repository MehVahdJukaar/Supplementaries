package net.mehvahdjukaar.supplementaries.common.block.tiles;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidConsumer;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidProvider;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.VanillaSoftFluids;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FaucetBlock;
import net.mehvahdjukaar.supplementaries.common.utils.FluidsUtil;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.InspirationCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSoftFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class FaucetBlockTile extends BlockEntity {
    private static final int COOLDOWN = 20;

    private int transferCooldown = 0;
    public final SoftFluidTank tempFluidHolder = SoftFluidTank.create(5);

    public FaucetBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.FAUCET_TILE.get(), pos, state);
    }


    public void updateLight() {
        if (this.level == null) return;
        int light = this.tempFluidHolder.getFluid().getLuminosity();
        if (light != 0) light = (int) Mth.clamp(light / 2f, 1, 7);
        if (light != this.getBlockState().getValue(FaucetBlock.LIGHT_LEVEL)) {
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(FaucetBlock.LIGHT_LEVEL, light), 2);
        }
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(0, -1, 0), getBlockPos().offset(1, 1, 1));
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, FaucetBlockTile tile) {
        if (tile.transferCooldown > 0) {
            tile.transferCooldown--;
        } else if (tile.isOpen()) {
            boolean flag = tile.tryExtract(pLevel, pPos, pState, true);
            if (flag) {
                tile.transferCooldown += COOLDOWN;
            }
        }
    }

    //------fluids------

    //TODO: make it connect with pipes
    //returns true if it has water
    public boolean updateContainedFluidVisuals(Level level, BlockPos pos, BlockState state) {
        //fluid stuff
        FluidState fluidState = level.getFluidState(pos.relative(state.getValue(FaucetBlock.FACING).getOpposite()));
        if (!fluidState.isEmpty() && fluidState.isSource()) {
            var f = SoftFluidRegistry.fromForgeFluid(fluidState.getType());
            if (f != null) { //just to be sure
                this.tempFluidHolder.fill(f);
                this.updateLight();
                return true;
            } else {
                int aa = 1;//error
            }
        }
        boolean r = this.tryExtract(level, pos, state, false);
        this.updateLight();
        return r;
    }

    //TODO: fix transfer to cauldrons
    private boolean tryExtract(Level level, BlockPos pos, BlockState state, boolean doTransfer) {
        Direction dir = state.getValue(FaucetBlock.FACING);
        BlockPos behind = pos.relative(dir.getOpposite());
        BlockState backState = level.getBlockState(behind);
        Block backBlock = backState.getBlock();
        this.tempFluidHolder.clear();
        if (backState.isAir()) {
            return false;
        } else if (backBlock instanceof ISoftFluidProvider provider) {
            var stack = provider.getProvidedFluid(level, backState, behind);
            this.prepareToTransferBottle(stack.getFirst(), stack.getSecond());
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                provider.consumeProvidedFluid(level, backState, behind, this.tempFluidHolder.getFluid(), this.tempFluidHolder.getNbt(), 1);
                return true;
            }
        }
        //beehive
        else if (backState.hasProperty(BlockStateProperties.LEVEL_HONEY)) {
            if (backState.getValue(BlockStateProperties.LEVEL_HONEY) == 5) {
                this.prepareToTransferBottle(VanillaSoftFluids.HONEY.get());
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    level.setBlock(behind, backState.setValue(BlockStateProperties.LEVEL_HONEY,
                            backState.getValue(BlockStateProperties.LEVEL_HONEY) - 1), 3);
                    return true;
                }
            }
            return false;
        }
        //TODO: move in compat class
        //honey pot
        else if (CompatHandler.BUZZIER_BEES && backState.hasProperty(ModBlockProperties.HONEY_LEVEL_POT)) {
            if (backState.getValue(ModBlockProperties.HONEY_LEVEL_POT) > 0) {
                this.prepareToTransferBottle(VanillaSoftFluids.HONEY.get());
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    level.setBlock(behind, backState.setValue(ModBlockProperties.HONEY_LEVEL_POT,
                            backState.getValue(ModBlockProperties.HONEY_LEVEL_POT) - 1), 3);
                    return true;
                }
            }
            return false;
        }
        //sap log
        else if (CompatHandler.AUTUMNITY && (backBlock == CompatObjects.SAPPY_MAPLE_LOG.get() || backBlock == CompatObjects.SAPPY_MAPLE_WOOD.get())) {
            this.prepareToTransferBottle(ModSoftFluids.SAP.get());
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                Optional<Block> log = Registry.BLOCK.getOptional(new ResourceLocation(Utils.getID(backBlock).toString().replace("sappy", "stripped")));
                log.ifPresent(block -> level.setBlock(behind, block.withPropertiesOf(backState), 3));
                return true;
            }
        }/* else if (CompatHandler.malum && MalumPlugin.isSappyLog(backBlock)) {
            this.prepareToTransferBottle(MalumPlugin.getSap(backBlock));
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                MalumPlugin.extractSap(level, backState, behind);
                return true;
            }
        }*/
        //cauldron
        else if (backBlock == Blocks.WATER_CAULDRON) {
            int waterLevel = backState.getValue(BlockStateProperties.LEVEL_CAULDRON);
            if (waterLevel > 0) {
                if (CompatHandler.INSPIRATIONS) {
                    return InspirationCompat.doCauldronStuff(level.getBlockEntity(behind), this.tempFluidHolder, doTransfer, () -> this.tryFillingBlockBelow(level, pos));
                }

                this.prepareToTransferBottle(VanillaSoftFluids.WATER.get());
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    if (waterLevel > 1) {
                        level.setBlock(behind, backState.setValue(BlockStateProperties.LEVEL_CAULDRON,
                                waterLevel - 1), 3);
                    } else level.setBlock(behind, Blocks.CAULDRON.defaultBlockState(), 3);
                    return true;
                }
            }
            //TODO: this doesnt seem to work
        } else if (backBlock == Blocks.LAVA_CAULDRON) {
            this.prepareToTransferBucket(VanillaSoftFluids.LAVA.get());
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                level.setBlock(behind, Blocks.CAULDRON.defaultBlockState(), 3);
                this.transferCooldown += COOLDOWN * 3;
                return true;
            }
        } else if (backBlock == Blocks.POWDER_SNOW_CAULDRON) {
            int waterLevel = backState.getValue(BlockStateProperties.LEVEL_CAULDRON);
            if (waterLevel == 3) {
                this.prepareToTransferBucket(VanillaSoftFluids.POWDERED_SNOW.get());
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    level.setBlock(behind, Blocks.CAULDRON.defaultBlockState(), 3);
                    this.transferCooldown += COOLDOWN * 3;
                    return true;
                }
            }
        }

        //soft fluid holders
        BlockEntity tileBack = level.getBlockEntity(behind);
        if (tileBack != null) {
            if (tileBack instanceof ISoftFluidTankProvider holder && holder.canInteractWithSoftFluidTank()) {
                SoftFluidTank fluidHolder = holder.getSoftFluidTank();
                this.tempFluidHolder.copy(fluidHolder);
                this.tempFluidHolder.setCount(2);
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    fluidHolder.shrink(1);
                    tileBack.setChanged();
                    return true;
                }
            }
            //forge tanks
            else {
                if (FluidsUtil.tryExtractFromFluidHandler(tileBack, backBlock, dir, tempFluidHolder, doTransfer, () -> this.tryFillingBlockBelow(level, pos))) {
                    return true;
                }
            }
            if (!doTransfer) return !this.tempFluidHolder.isEmpty();
            //pull other items from containers
            return this.spillItemsFromInventory(level, pos, dir, tileBack);
        } else if (level.getFluidState(behind).getType() == Fluids.WATER) {
            //Unlimited water!!
            this.prepareToTransferBottle(VanillaSoftFluids.WATER.get());
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                return true;
            }
            return true;
        }

        if (!doTransfer) return !this.tempFluidHolder.isEmpty();

        if (backBlock instanceof WorldlyContainerHolder wc) {
            //TODO: add
            //container = wc.getContainer(backBlock, level, behind);
            //return this.spillItemsFromInventory(level, pos, dir, tileBack);

        }
        return false;
    }
    //TODO: maybe add a registry for block -> interaction like dispenser one

    private void prepareToTransferBottle(SoftFluid softFluid) {
        this.tempFluidHolder.fill(softFluid);
        this.tempFluidHolder.setCount(2);
    }

    private void prepareToTransferBottle(SoftFluid softFluid, CompoundTag tag) {
        this.tempFluidHolder.fill(softFluid, tag);
        this.tempFluidHolder.setCount(2);
    }

    private void prepareToTransferBucket(SoftFluid softFluid) {
        this.tempFluidHolder.fill(softFluid);
    }

    //sf->ff/sf
    @SuppressWarnings("ConstantConditions")
    private boolean tryFillingBlockBelow(Level level, BlockPos pos) {
        SoftFluid softFluid = this.tempFluidHolder.getFluid();
        //can't full below if empty
        if (softFluid.isEmpty()) return false;

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        Block belowBlock = belowState.getBlock();


        //consumer
        if (belowBlock instanceof ISoftFluidConsumer consumer) {
            return consumer.tryAcceptingFluid(level, belowState, below, softFluid, this.tempFluidHolder.getNbt(), 1);
        }
        //sponge voiding
        if (belowBlock == Blocks.SPONGE) {
            return true;
        }
        //beehive
        else if (softFluid == VanillaSoftFluids.HONEY.get()) {

            //beehives
            if (belowState.hasProperty(BlockStateProperties.LEVEL_HONEY)) {
                int h = belowState.getValue(BlockStateProperties.LEVEL_HONEY);
                if (h == 0) {
                    level.setBlock(below, belowState.setValue(BlockStateProperties.LEVEL_HONEY, 5), 3);
                    return true;
                }
                return false;
            }
            //honey pot
            else if (CompatHandler.BUZZIER_BEES && belowState.hasProperty(ModBlockProperties.HONEY_LEVEL_POT)) {
                int h = belowState.getValue(ModBlockProperties.HONEY_LEVEL_POT);
                if (h < 4) {
                    level.setBlock(below, belowState.setValue(ModBlockProperties.HONEY_LEVEL_POT, h + 1), 3);
                    return true;
                }
                return false;
            }
        } else if (softFluid == VanillaSoftFluids.XP.get() && belowState.isAir()) {
            this.dropXP(level, pos);
            return true;
        } else if (belowBlock instanceof AbstractCauldronBlock) {
            //if any other mod adds a cauldron tile this will crash
            if (CompatHandler.INSPIRATIONS) {
                return InspirationCompat.tryAddFluid(level.getBlockEntity(below), this.tempFluidHolder);
            } else if (softFluid == VanillaSoftFluids.WATER.get()) {
                //TODO: finish
                if (belowBlock == Blocks.WATER_CAULDRON) {
                    int levels = belowState.getValue(BlockStateProperties.LEVEL_CAULDRON);
                    if (levels < 3) {
                        level.setBlock(below, belowState.setValue(BlockStateProperties.LEVEL_CAULDRON, levels + 1), 3);
                        return true;
                    }
                    return false;
                } else if (belowBlock instanceof CauldronBlock) {
                    level.setBlock(below, Blocks.WATER_CAULDRON.defaultBlockState().setValue(BlockStateProperties.LEVEL_CAULDRON, 1), 3);
                    return true;
                }
            } else if (softFluid == VanillaSoftFluids.LAVA.get()) {
                if (belowBlock instanceof CauldronBlock && this.tempFluidHolder.getCount() == 5) {
                    level.setBlock(below, Blocks.LAVA_CAULDRON.defaultBlockState(), 3);
                    return true;
                }
            } else if (softFluid == VanillaSoftFluids.POWDERED_SNOW.get()) {
                if (belowBlock instanceof CauldronBlock && this.tempFluidHolder.getCount() == 5) {
                    level.setBlock(below, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState()
                            .setValue(PowderSnowCauldronBlock.LEVEL, 3), 3);
                    return true;
                }
            }
            return false;
        }


        //default behavior
        boolean result;
        //soft fluid holders
        BlockEntity tileBelow = level.getBlockEntity(below);
        if (tileBelow instanceof ISoftFluidTankProvider holder) {
            SoftFluidTank fluidHolder = holder.getSoftFluidTank();
            result = this.tempFluidHolder.tryTransferFluid(fluidHolder, this.tempFluidHolder.getCount() - 1);
            if (result) {
                tileBelow.setChanged();
                this.tempFluidHolder.fillCount();
            }
            return result;
        }
        if (tileBelow != null) {
            //forge tanks
            return FluidsUtil.tryFillFluidTank(tileBelow, tempFluidHolder);
        }
        return false;
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


    //------end-fluids------

    public boolean isOpen() {
        return (this.getBlockState().getValue(BlockStateProperties.POWERED) ^ this.getBlockState().getValue(BlockStateProperties.ENABLED));
    }

    public boolean hasWater() {
        return this.getBlockState().getValue(FaucetBlock.HAS_WATER);
    }

    public boolean isConnectedBelow() {
        return this.getBlockState().getValue(FaucetBlock.HAS_JAR);
    }

    //------items------

    public boolean spillItemsFromInventory(Level level, BlockPos pos, Direction dir, BlockEntity tile) {
        //TODO: maybe add here insertion in containers below
        if (this.isConnectedBelow()) return false;
        return ItemsUtil.faucetSpillItems(level, pos, dir, tile);
    }


    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.transferCooldown = compound.getInt("TransferCooldown");
        this.tempFluidHolder.load(compound);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("TransferCooldown", this.transferCooldown);
        this.tempFluidHolder.save(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

}