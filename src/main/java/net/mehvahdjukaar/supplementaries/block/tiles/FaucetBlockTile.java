package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.fluids.*;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.blocks.FaucetBlock;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.CompatObjects;
import net.mehvahdjukaar.supplementaries.compat.inspirations.CauldronPlugin;
import net.mehvahdjukaar.supplementaries.fluids.ModSoftFluids;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Random;
import java.util.stream.IntStream;

public class FaucetBlockTile extends BlockEntity {
    private int transferCooldown = 0;
    protected final Random rand = new Random();
    public final SoftFluidHolder fluidHolder = new SoftFluidHolder(2);

    public FaucetBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.FAUCET_TILE.get(), pos, state);
    }

    @Override
    public void setChanged() {
        if (this.level == null) return;
        int light = this.fluidHolder.getFluid().getLuminosity();
        if (light != this.getBlockState().getValue(BlockProperties.LIGHT_LEVEL_0_15)) {
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(BlockProperties.LIGHT_LEVEL_0_15, light), 2);
        }
        super.setChanged();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(0, -1, 0), getBlockPos().offset(1, 1, 1));
    }

    private boolean isOnTransferCooldown() {
        return this.transferCooldown > 0;
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, FaucetBlockTile tile) {
        if (tile.isOnTransferCooldown()) {
            tile.transferCooldown--;
        } else if (tile.isOpen()) {
            boolean flag = tile.tryExtract(pLevel, pPos, pState, true);
            if (flag) {
                tile.transferCooldown = 20;
            }
        }
    }

    //------fluids------

    //TODO: fix drinking

    //TODO: make it connect with pipes
    //returns true if it has water
    public boolean updateContainedFluidVisuals(Level level, BlockPos pos, BlockState state) {
        //fluid stuff
        FluidState fluidState = level.getFluidState(pos.relative(state.getValue(FaucetBlock.FACING).getOpposite()));
        if (!fluidState.isEmpty()) {
            this.fluidHolder.fill(SoftFluidRegistry.fromForgeFluid(fluidState.getType()));
            return true;
        }
        return this.tryExtract(level, pos, state, false);
    }

    @SuppressWarnings("ConstantConditions")
    private boolean tryExtract(Level level, BlockPos pos, BlockState state, boolean doTransfer) {
        Direction dir = state.getValue(FaucetBlock.FACING);
        BlockPos behind = pos.relative(dir.getOpposite());
        BlockState backState = level.getBlockState(behind);
        Block backBlock = backState.getBlock();

        if (backBlock instanceof ISoftFluidProvider provider) {
            Pair<SoftFluid, CompoundTag> stack = provider.getProvidedFluid(level, backState, behind);
            this.fluidHolder.fill(stack.getLeft(), stack.getRight());
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                provider.consumeProvidedFluid(level, backState, behind, this.fluidHolder.getFluid(), this.fluidHolder.getNbt(), 1);
                return true;
            }
        }
        //beehive
        else if (backState.hasProperty(BlockStateProperties.LEVEL_HONEY)) {
            if (backState.getValue(BlockStateProperties.LEVEL_HONEY) == 5) {
                this.fluidHolder.fill(SoftFluidRegistry.HONEY);
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    level.setBlock(behind, backState.setValue(BlockStateProperties.LEVEL_HONEY,
                            backState.getValue(BlockStateProperties.LEVEL_HONEY) - 1), 3);
                    return true;
                }
            }
            return false;
        }
        //honey pot
        else if (CompatHandler.buzzier_bees && backState.hasProperty(BlockProperties.HONEY_LEVEL_POT)) {
            if (backState.getValue(BlockProperties.HONEY_LEVEL_POT) > 0) {
                this.fluidHolder.fill(SoftFluidRegistry.HONEY);
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    level.setBlock(behind, backState.setValue(BlockProperties.HONEY_LEVEL_POT,
                            backState.getValue(BlockProperties.HONEY_LEVEL_POT) - 1), 3);
                    return true;
                }
            }
            return false;
        }
        //sap log
        else if (CompatHandler.autumnity && (backBlock == CompatObjects.SAPPY_MAPLE_LOG.get() || backBlock == CompatObjects.SAPPY_MAPLE_WOOD.get())) {
            this.fluidHolder.fill(ModSoftFluids.SAP);
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                Block log = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(backBlock.getRegistryName().toString().replace("sappy", "stripped")));
                if (log != null) {
                    level.setBlock(behind, log.defaultBlockState().setValue(BlockStateProperties.AXIS,
                            backState.getValue(BlockStateProperties.AXIS)), 3);
                }
                return true;
            }
        }
        //cauldron
        else if (backBlock == Blocks.WATER_CAULDRON) {
            if (backState.getValue(BlockStateProperties.LEVEL_CAULDRON) > 0) {
                if (CompatHandler.inspirations) {
                    return CauldronPlugin.doStuff(level.getBlockEntity(behind), this.fluidHolder, doTransfer, () -> this.tryFillingBlockBelow(level, pos));
                }

                this.fluidHolder.fill(SoftFluidRegistry.WATER);
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    level.setBlock(behind, backState.setValue(BlockStateProperties.LEVEL_CAULDRON,
                            backState.getValue(BlockStateProperties.LEVEL_CAULDRON) - 1), 3);
                    return true;
                }

            }
            return false;
            //tile stuff
        }
        //soft fluid holders
        BlockEntity tileBack = level.getBlockEntity(behind);
        if(tileBack != null) {
            if (tileBack instanceof ISoftFluidHolder && ((ISoftFluidHolder) tileBack).canInteractWithFluidHolder()) {
                SoftFluidHolder fluidHolder = ((ISoftFluidHolder) tileBack).getSoftFluidHolder();
                this.fluidHolder.copy(fluidHolder);
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    fluidHolder.shrink(1);
                    tileBack.setChanged();
                    return true;
                }
            }
            //forge tanks
            else {
                IFluidHandler handlerBack = tileBack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir).orElse(null);
                if (handlerBack != null) {
                    //only works in 250 increment
                    if (handlerBack.getFluidInTank(0).getAmount() < 250) return false;
                    this.fluidHolder.copy(handlerBack);
                    if (doTransfer && tryFillingBlockBelow(level, pos)) {
                        handlerBack.drain(250, IFluidHandler.FluidAction.EXECUTE);
                        tileBack.setChanged();
                        return true;
                    }
                } else if (level.getFluidState(behind).getType() == Fluids.WATER) {
                    this.fluidHolder.fill(SoftFluidRegistry.WATER);
                    return true;
                }
            }
            if (!doTransfer) return !this.fluidHolder.isEmpty();
            //pull other items from containers
            return this.spillItemsFromInventory(level, pos, dir, tileBack);
        }

        if (!doTransfer) return !this.fluidHolder.isEmpty();
        return false;
    }


    //sf->ff/sf
    @SuppressWarnings("ConstantConditions")
    private boolean tryFillingBlockBelow(Level level, BlockPos pos) {
        SoftFluid softFluid = this.fluidHolder.getFluid();
        //can't full below if empty
        if (softFluid.isEmpty()) return false;

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        Block belowBlock = belowState.getBlock();

        //consumer
        if (belowBlock instanceof ISoftFluidConsumer consumer) {
            return consumer.tryAcceptingFluid(level, belowState, below, softFluid, this.fluidHolder.getNbt(), 1);
        }
        //beehive
        else if (softFluid == SoftFluidRegistry.HONEY) {

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
            else if (CompatHandler.buzzier_bees && belowState.hasProperty(BlockProperties.HONEY_LEVEL_POT)) {
                int h = belowState.getValue(BlockProperties.HONEY_LEVEL_POT);
                if (h < 4) {
                    level.setBlock(below, belowState.setValue(BlockProperties.HONEY_LEVEL_POT, h + 1), 3);
                    return true;
                }
                return false;
            }
        } else if (softFluid == SoftFluidRegistry.XP && belowState.isAir()) {
            this.dropXP(level, pos);
            return true;
        } else if (belowBlock instanceof AbstractCauldronBlock) {
            //if any other mod adds a cauldron tile this will crash
            if (CompatHandler.inspirations) {
                return CauldronPlugin.tryAddFluid(level.getBlockEntity(below), this.fluidHolder);
            } else if (softFluid == SoftFluidRegistry.WATER) {
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
            }

            return false;
        }


        //default behavior
        boolean result;
        //soft fluid holders
        BlockEntity tileBelow = level.getBlockEntity(below);
        if (tileBelow instanceof ISoftFluidHolder) {
            SoftFluidHolder fluidHolder = ((ISoftFluidHolder) tileBelow).getSoftFluidHolder();
            result = this.fluidHolder.tryTransferFluid(fluidHolder);
            if (result) {
                tileBelow.setChanged();
                this.fluidHolder.fillCount();
            }
            return result;
        }
        //forge tanks
        IFluidHandler handlerDown = tileBelow.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.UP).orElse(null);
        if (handlerDown != null) {
            result = this.fluidHolder.tryTransferToFluidTank(handlerDown);
            if (result) {
                tileBelow.setChanged();
                this.fluidHolder.fillCount();
            }
            return result;
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
        float f = (this.rand.nextFloat() - 0.5f) / 4f;
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

    @SuppressWarnings("ConstantConditions")
    public boolean spillItemsFromInventory(Level level, BlockPos pos, Direction dir, BlockEntity tile) {
        //TODO: maybe add here insertion in containers below
        if(this.isConnectedBelow()) return false;
        IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).orElse(null);
        if (itemHandler != null) {
            for(int slot = 0; slot<itemHandler.getSlots(); slot++) {
                ItemStack itemstack = itemHandler.getStackInSlot(slot);
                if (!itemstack.isEmpty()) {
                    ItemStack extracted = itemHandler.extractItem(slot,1, false);
                    //non empty stack can't be extracted. trying next
                    //if(extracted.isEmpty())continue;
                    tile.setChanged();
                    ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, extracted);
                    drop.setDeltaMovement(new Vec3(0, 0, 0));
                    level.addFreshEntity(drop);
                    float f = (this.rand.nextFloat() - 0.5f) / 4f;
                    level.playSound(null, pos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.3F, 0.5f + f);
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.transferCooldown = compound.getInt("TransferCooldown");
        this.fluidHolder.load(compound);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        compound.putInt("TransferCooldown", this.transferCooldown);
        this.fluidHolder.save(compound);
        return compound;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }
}