package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.selene.fluids.*;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FaucetBlock;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.inspirations.CauldronPlugin;
import net.mehvahdjukaar.supplementaries.integration.malum.MalumPlugin;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSoftFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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

public class FaucetBlockTile extends BlockEntity {
    private static final int COOLDOWN = 20;

    private int transferCooldown = 0;
    public final SoftFluidHolder tempFluidHolder = new SoftFluidHolder(5);

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

    @Override
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

    //TODO: fix drinking

    //TODO: make it connect with pipes
    //returns true if it has water
    public boolean updateContainedFluidVisuals(Level level, BlockPos pos, BlockState state) {
        //fluid stuff
        FluidState fluidState = level.getFluidState(pos.relative(state.getValue(FaucetBlock.FACING).getOpposite()));
        if (!fluidState.isEmpty()) {
            this.tempFluidHolder.fill(SoftFluidRegistry.fromForgeFluid(fluidState.getType()));
            this.updateLight();
            return true;
        }
        boolean r = this.tryExtract(level, pos, state, false);
        this.updateLight();
        return r;
    }
//TODO: fix trasnfer to cauldrons
    @SuppressWarnings("ConstantConditions")
    private boolean tryExtract(Level level, BlockPos pos, BlockState state, boolean doTransfer) {
        Direction dir = state.getValue(FaucetBlock.FACING);
        BlockPos behind = pos.relative(dir.getOpposite());
        BlockState backState = level.getBlockState(behind);
        Block backBlock = backState.getBlock();
        this.tempFluidHolder.clear();
        if (backState.isAir()) {
            return false;
        } else if (backBlock instanceof ISoftFluidProvider provider) {
            Pair<SoftFluid, CompoundTag> stack = provider.getProvidedFluid(level, backState, behind);
            this.prepareToTransferBottle(stack.getLeft(), stack.getRight());
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                provider.consumeProvidedFluid(level, backState, behind, this.tempFluidHolder.getFluid(), this.tempFluidHolder.getNbt(), 1);
                return true;
            }
        }
        //beehive
        else if (backState.hasProperty(BlockStateProperties.LEVEL_HONEY)) {
            if (backState.getValue(BlockStateProperties.LEVEL_HONEY) == 5) {
                this.prepareToTransferBottle(SoftFluidRegistry.HONEY);
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
        else if (CompatHandler.buzzier_bees && backState.hasProperty(BlockProperties.HONEY_LEVEL_POT)) {
            if (backState.getValue(BlockProperties.HONEY_LEVEL_POT) > 0) {
                this.prepareToTransferBottle(SoftFluidRegistry.HONEY);
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
            this.prepareToTransferBottle(ModSoftFluids.SAP);
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                Block log = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(backBlock.getRegistryName().toString().replace("sappy", "stripped")));
                if (log != null) {

                    level.setBlock(behind, log.withPropertiesOf(backState), 3);
                }
                return true;
            }
        } else if (CompatHandler.malum && MalumPlugin.isSappyLog(backBlock)) {
            this.prepareToTransferBottle(MalumPlugin.getSap(backBlock));
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                MalumPlugin.extractSap(level, backState, behind);
                return true;
            }
        }
        //cauldron
        else if (backBlock == Blocks.WATER_CAULDRON) {
            int waterLevel = backState.getValue(BlockStateProperties.LEVEL_CAULDRON);
            if (waterLevel > 0) {
                if (CompatHandler.inspirations) {
                    return CauldronPlugin.doStuff(level.getBlockEntity(behind), this.tempFluidHolder, doTransfer, () -> this.tryFillingBlockBelow(level, pos));
                }

                this.prepareToTransferBottle(SoftFluidRegistry.WATER);
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    if (waterLevel > 1) {
                        level.setBlock(behind, backState.setValue(BlockStateProperties.LEVEL_CAULDRON,
                                waterLevel - 1), 3);
                    } else level.setBlock(behind, Blocks.CAULDRON.defaultBlockState(), 3);
                    return true;
                }
            }
        } else if (backBlock == Blocks.LAVA_CAULDRON) {
            this.prepareToTransferBucket(SoftFluidRegistry.LAVA);
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                level.setBlock(behind, Blocks.CAULDRON.defaultBlockState(), 3);
                this.transferCooldown += COOLDOWN * 3;
                return true;
            }
        } else if (backBlock == Blocks.POWDER_SNOW_CAULDRON) {
            int waterLevel = backState.getValue(BlockStateProperties.LEVEL_CAULDRON);
            if (waterLevel == 3) {
                this.prepareToTransferBucket(ModSoftFluids.POWDERED_SNOW);
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
            if (tileBack instanceof ISoftFluidHolder holder && holder.canInteractWithFluidHolder()) {
                SoftFluidHolder fluidHolder = holder.getSoftFluidHolder();
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
                IFluidHandler handlerBack = tileBack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir).orElse(null);
                //TODO: fix create fluid int bug
                if (handlerBack != null && !backBlock.getRegistryName().getPath().equals("fluid_interface")) {
                    //only works in 250 increment
                    if (handlerBack.getFluidInTank(0).getAmount() < 250) return false;
                    this.tempFluidHolder.copy(handlerBack);
                    this.tempFluidHolder.setCount(2);
                    if (doTransfer && tryFillingBlockBelow(level, pos)) {
                        handlerBack.drain(250, IFluidHandler.FluidAction.EXECUTE);
                        tileBack.setChanged();
                        return true;
                    }
                }
            }
            if (!doTransfer) return !this.tempFluidHolder.isEmpty();
            //pull other items from containers
            return this.spillItemsFromInventory(level, pos, dir, tileBack);
        } else if (level.getFluidState(behind).getType() == Fluids.WATER) {
            this.prepareToTransferBottle(SoftFluidRegistry.WATER);
            return true;
        }

        if (!doTransfer) return !this.tempFluidHolder.isEmpty();
        return false;
    }
    //TODO: maybe add registry block-> interaction

    private void prepareToTransferBottle(SoftFluid softFluid){
        this.tempFluidHolder.fill(softFluid);
        this.tempFluidHolder.setCount(2);
    }
    private void prepareToTransferBottle(SoftFluid softFluid, CompoundTag tag){
        this.tempFluidHolder.fill(softFluid, tag);
        this.tempFluidHolder.setCount(2);
    }
    private void prepareToTransferBucket(SoftFluid softFluid){
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
                return CauldronPlugin.tryAddFluid(level.getBlockEntity(below), this.tempFluidHolder);
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
            else if(softFluid == SoftFluidRegistry.LAVA){
                if (belowBlock instanceof CauldronBlock && this.tempFluidHolder.getCount() == 5) {
                    level.setBlock(below, Blocks.LAVA_CAULDRON.defaultBlockState(),3);
                    return true;
                }
            }
            else if(softFluid == ModSoftFluids.POWDERED_SNOW){
                if (belowBlock instanceof CauldronBlock && this.tempFluidHolder.getCount() == 5) {
                    level.setBlock(below, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState()
                            .setValue(PowderSnowCauldronBlock.LEVEL, 3),3);
                    return true;
                }
            }
            return false;
        }


        //default behavior
        boolean result;
        //soft fluid holders
        BlockEntity tileBelow = level.getBlockEntity(below);
        if (tileBelow instanceof ISoftFluidHolder holder) {
            SoftFluidHolder fluidHolder = holder.getSoftFluidHolder();
            result = this.tempFluidHolder.tryTransferFluid(fluidHolder, this.tempFluidHolder.getCount()-1);
            if (result) {
                tileBelow.setChanged();
                this.tempFluidHolder.fillCount();
            }
            return result;
        }
        if (tileBelow != null) {
            //forge tanks
            IFluidHandler handlerDown = tileBelow.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.UP).orElse(null);
            if (handlerDown != null) {
                result = this.tempFluidHolder.tryTransferToFluidTank(handlerDown, this.tempFluidHolder.getCount()-1);
                if (result) {
                    tileBelow.setChanged();
                    this.tempFluidHolder.fillCount();
                }
                return result;
            }
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

    @SuppressWarnings("ConstantConditions")
    public boolean spillItemsFromInventory(Level level, BlockPos pos, Direction dir, BlockEntity tile) {
        //TODO: maybe add here insertion in containers below
        if (this.isConnectedBelow()) return false;
        IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).orElse(null);
        if (itemHandler != null) {
            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                ItemStack itemstack = itemHandler.getStackInSlot(slot);
                if (!itemstack.isEmpty()) {
                    ItemStack extracted = itemHandler.extractItem(slot, 1, false);
                    //empty stack means it can't extract from inventory
                    if (!extracted.isEmpty()) {
                        tile.setChanged();
                        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, extracted);
                        drop.setDeltaMovement(new Vec3(0, 0, 0));
                        level.addFreshEntity(drop);
                        float f = (level.random.nextFloat() - 0.5f) / 4f;
                        level.playSound(null, pos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.3F, 0.5f + f);
                        return true;
                    }
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

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }
}