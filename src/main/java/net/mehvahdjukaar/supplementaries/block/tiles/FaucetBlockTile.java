package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.fluids.*;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.blocks.FaucetBlock;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.CompatObjects;
import net.mehvahdjukaar.supplementaries.compat.inspirations.CauldronPlugin;
import net.mehvahdjukaar.supplementaries.fluids.ModSoftFluids;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Random;
import java.util.stream.IntStream;

public class FaucetBlockTile extends BlockEntity implements TickableBlockEntity {
    private int transferCooldown = 0;
    protected final Random rand = new Random();
    public final SoftFluidHolder fluidHolder = new SoftFluidHolder(2);

    public FaucetBlockTile() {
        super(ModRegistry.FAUCET_TILE.get());
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
    public double getViewDistance() {
        return 80;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(0, -1, 0), getBlockPos().offset(1, 1, 1));
    }

    private boolean isOnTransferCooldown() {
        return this.transferCooldown > 0;
    }

    @Override
    public void tick() {
        if (this.level != null && !this.level.isClientSide) {
            if (this.isOnTransferCooldown()) {
                this.transferCooldown--;
            } else if (this.isOpen()) {
                boolean flag = this.tryExtract(true);
                if (flag) {
                    this.transferCooldown = 20;
                }
            }
        }
    }

    //------fluids------

    //TODO: fix drinking

    //TODO: make it connect with pipes
    //returns true if it has water
    public boolean updateContainedFluidVisuals(BlockState state) {
        //fluid stuff
        FluidState fluidState = level.getFluidState(this.worldPosition.relative(state.getValue(FaucetBlock.FACING).getOpposite()));
        if (!fluidState.isEmpty()) {
            this.fluidHolder.fill(SoftFluidRegistry.fromForgeFluid(fluidState.getType()));
            return true;
        }
        return this.tryExtract(false);
    }

    private boolean tryExtract(boolean doTransfer) {
        Direction dir = this.getBlockState().getValue(FaucetBlock.FACING);
        BlockPos behind = this.worldPosition.relative(dir.getOpposite());
        BlockState backState = this.level.getBlockState(behind);
        Block backBlock = backState.getBlock();

        if (backBlock instanceof ISoftFluidProvider) {
            ISoftFluidProvider provider = (ISoftFluidProvider) backBlock;
            Pair<SoftFluid, CompoundTag> stack = provider.getProvidedFluid(this.level, backState, behind);
            this.fluidHolder.fill(stack.getLeft(), stack.getRight());
            if (doTransfer && tryFillingBlockBelow()) {
                provider.consumeProvidedFluid(this.level, backState, behind, this.fluidHolder.getFluid(), this.fluidHolder.getNbt(), 1);
                return true;
            }
        }
        //beehive
        else if (backState.hasProperty(BlockStateProperties.LEVEL_HONEY)) {
            if (backState.getValue(BlockStateProperties.LEVEL_HONEY) == 5) {
                this.fluidHolder.fill(SoftFluidRegistry.HONEY);
                if (doTransfer && tryFillingBlockBelow()) {
                    this.level.setBlock(behind, backState.setValue(BlockStateProperties.LEVEL_HONEY,
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
                if (doTransfer && tryFillingBlockBelow()) {
                    this.level.setBlock(behind, backState.setValue(BlockProperties.HONEY_LEVEL_POT,
                            backState.getValue(BlockProperties.HONEY_LEVEL_POT) - 1), 3);
                    return true;
                }
            }
            return false;
        }
        //sap log
        else if (CompatHandler.autumnity && (backBlock == CompatObjects.SAPPY_MAPLE_LOG.get() || backBlock == CompatObjects.SAPPY_MAPLE_WOOD.get())) {
            this.fluidHolder.fill(ModSoftFluids.SAP);
            if (doTransfer && tryFillingBlockBelow()) {
                Block log = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(backBlock.getRegistryName().toString().replace("sappy", "stripped")));
                if (log != null) {
                    this.level.setBlock(behind, log.defaultBlockState().setValue(BlockStateProperties.AXIS,
                            backState.getValue(BlockStateProperties.AXIS)), 3);
                }
                return true;
            }
        }
        //cauldron
        else if (backBlock instanceof CauldronBlock) {
            if (backState.getValue(BlockStateProperties.LEVEL_CAULDRON) > 0) {
                BlockEntity cauldronTile = level.getBlockEntity(behind);
                if (cauldronTile == null) {
                    this.fluidHolder.fill(SoftFluidRegistry.WATER);
                    if (doTransfer && tryFillingBlockBelow()) {
                        this.level.setBlock(behind, backState.setValue(BlockStateProperties.LEVEL_CAULDRON,
                                backState.getValue(BlockStateProperties.LEVEL_CAULDRON) - 1), 3);
                        return true;
                    }
                } else if (CompatHandler.inspirations) {
                    return CauldronPlugin.doStuff(cauldronTile, this.fluidHolder, doTransfer, this::tryFillingBlockBelow);
                }
            }
            return false;
        } else {
            //soft fluid holders
            BlockEntity tileBack = level.getBlockEntity(behind);
            if (tileBack instanceof ISoftFluidHolder && ((ISoftFluidHolder) tileBack).canInteractWithFluidHolder()) {
                SoftFluidHolder fluidHolder = ((ISoftFluidHolder) tileBack).getSoftFluidHolder();
                this.fluidHolder.copy(fluidHolder);
                if (doTransfer && tryFillingBlockBelow()) {
                    fluidHolder.shrink(1);
                    tileBack.setChanged();
                    return true;
                }
            }
            //forge tanks
            else {
                IFluidHandler handlerBack = FluidUtil.getFluidHandler(this.level, behind, dir).orElse(null);
                if (handlerBack != null) {
                    this.fluidHolder.copy(handlerBack);
                    if (doTransfer && tryFillingBlockBelow()) {
                        handlerBack.drain(250, IFluidHandler.FluidAction.EXECUTE);
                        tileBack.setChanged();
                        return true;
                    }
                } else if (level.getFluidState(behind).getType() == Fluids.WATER) {
                    this.fluidHolder.fill(SoftFluidRegistry.WATER);
                    return true;
                }
            }
        }
        if (!doTransfer) return !this.fluidHolder.isEmpty();
        //pull other items
        return this.pullItems();
    }


    //sf->ff/sf
    private boolean tryFillingBlockBelow() {
        BlockPos below = worldPosition.below();
        BlockState belowState = level.getBlockState(below);
        Block belowBlock = belowState.getBlock();
        SoftFluid softFluid = this.fluidHolder.getFluid();

        //consumer
        if (belowBlock instanceof ISoftFluidConsumer) {
            ISoftFluidConsumer consumer = (ISoftFluidConsumer) belowBlock;
            return consumer.tryAcceptingFluid(this.level, belowState, below, softFluid, this.fluidHolder.getNbt(), 1);
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
            this.dropXP();
            return true;
        } else if (belowState.getBlock() instanceof CauldronBlock) {
            int levels = belowState.getValue(BlockStateProperties.LEVEL_CAULDRON);
            if (levels < 3) {
                BlockEntity cauldronTile = level.getBlockEntity(below);
                if (cauldronTile == null) {
                    level.setBlock(below, belowState.setValue(BlockStateProperties.LEVEL_CAULDRON, levels + 1), 3);
                    return true;
                }
                //if any other mod adds a cauldron tile this will crash
                else if (CompatHandler.inspirations) {
                    return CauldronPlugin.tryAddFluid(cauldronTile, this.fluidHolder);
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
        IFluidHandler handlerDown = FluidUtil.getFluidHandler(this.level, below, Direction.UP).orElse(null);
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

    private void dropXP() {
        int i = 3 + this.level.random.nextInt(5) + this.level.random.nextInt(5);
        while (i > 0) {
            int xp = ExperienceOrb.getExperienceValue(i);
            i -= xp;
            ExperienceOrb orb = new ExperienceOrb(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() - 0.125f, this.worldPosition.getZ() + 0.5, xp);
            orb.setDeltaMovement(new Vec3(0, 0, 0));
            this.level.addFreshEntity(orb);
        }
        float f = (this.rand.nextFloat() - 0.5f) / 4f;
        this.level.playSound(null, this.worldPosition, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.3F, 0.5f + f);
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

    //-----hopper-code-----
    private static boolean canExtractItemFromSlot(Container inventoryIn, ItemStack stack, int index, Direction side) {
        return !(inventoryIn instanceof WorldlyContainer) || ((WorldlyContainer) inventoryIn).canTakeItemThroughFace(index, stack, side);
    }

    private boolean pullItemFromSlot(Container inventoryIn, int index, Direction direction) {
        ItemStack itemstack = inventoryIn.getItem(index);
        // special case for jars. has to be done to prevent other hoppers
        // frominteracting with them cause canextractitems is always false
        if (this.isConnectedBelow()) {
            return false;
        } else if (!itemstack.isEmpty() && canExtractItemFromSlot(inventoryIn, itemstack, index, direction)) {
            ItemStack it = itemstack.copy();
            itemstack.shrink(1);
            inventoryIn.setChanged();
            it.setCount(1);
            ItemEntity drop = new ItemEntity(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY(), this.worldPosition.getZ() + 0.5, it);
            drop.setDeltaMovement(new Vec3(0, 0, 0));
            this.level.addFreshEntity(drop);
            float f = (this.rand.nextFloat() - 0.5f) / 4f;
            this.level.playSound(null, this.worldPosition, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.3F, 0.5f + f);
            return true;
        }
        return false;
    }

    public boolean pullItems() {
        Container iinventory = getSourceInventory();
        if (iinventory != null) {
            Direction direction = this.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
            return getSlots(iinventory, direction).anyMatch((p_213971_3_)
                    -> pullItemFromSlot(iinventory, p_213971_3_, direction));
        }
        return false;
    }

    public Container getSourceInventory() {
        BlockPos behind = this.worldPosition.relative(this.getBlockState().getValue(HorizontalDirectionalBlock.FACING), -1);
        Container firstinv = HopperBlockEntity.getContainerAt(this.getLevel(), behind);
        if (firstinv != null) {
            return firstinv;
        } else if (this.level.getBlockState(behind).isRedstoneConductor(this.level, this.worldPosition)) {
            return HopperBlockEntity.getContainerAt(this.getLevel(),
                    this.worldPosition.relative(this.getBlockState().getValue(HorizontalDirectionalBlock.FACING), -2));
        } else
            return null;
    }

    private static IntStream getSlots(Container inv, Direction dir) {
        return inv instanceof WorldlyContainer
                ? IntStream.of(((WorldlyContainer) inv).getSlotsForFace(dir))
                : IntStream.range(0, inv.getContainerSize());
    }

    //------end-hopper-code------

    @Override
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);
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
        this.load(this.getBlockState(), pkt.getTag());
    }
}