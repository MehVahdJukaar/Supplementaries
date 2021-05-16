package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.blocks.FaucetBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluid;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluidHolder;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluidList;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.*;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;
import java.util.stream.IntStream;

public class FaucetBlockTile extends TileEntity implements ITickableTileEntity {
    private int transferCooldown = 0;
    protected final Random rand = new Random();
    public final SoftFluidHolder fluidHolder = new SoftFluidHolder(2);

    public FaucetBlockTile() {
        super(Registry.FAUCET_TILE.get());
    }

    @Override
    public void onLoad() {
        this.fluidHolder.setWorldAndPos(this.level, this.worldPosition);
    }

    @Override
    public void setChanged() {
        int light = this.fluidHolder.getFluid().getLuminosity();
        if(light!=this.getBlockState().getValue(BlockProperties.LIGHT_LEVEL_0_15)){
            this.level.setBlock(this.worldPosition,this.getBlockState().setValue(BlockProperties.LIGHT_LEVEL_0_15,light),2);
        }
        super.setChanged();
    }

    //TODO: rework all of this
    //TODO: make it connect with pipes
    //returns true if it has water
    public boolean updateContainedFluid(BlockState state){
        Direction backDir = state.getValue(FaucetBlock.FACING).getOpposite();
        BlockPos backPos = worldPosition.relative(backDir);
        BlockState backState = level.getBlockState(backPos);

        Block b = backState.getBlock();

        if(b instanceof JarBlock){
            TileEntity te = level.getBlockEntity(backPos);
            if(te instanceof JarBlockTile){
                if(!((JarBlockTile) te).fluidHolder.isEmpty()){
                    this.fluidHolder.copy(((JarBlockTile) te).fluidHolder);
                    return true;
                }
            }
            return false;
        }
        else if(backState.hasProperty(BeehiveBlock.HONEY_LEVEL)){
            if(backState.getValue(BeehiveBlock.HONEY_LEVEL) > 0){
                this.fluidHolder.fill(SoftFluidList.HONEY);
                return true;
            }
            return false;
        }
        else if(b instanceof CauldronBlock){
            if(level instanceof World && backState.getAnalogOutputSignal(level,backPos)>0){
                this.fluidHolder.fill(SoftFluidList.WATER);
                return true;
            }
            return false;
        }
        //fluid stuff
        FluidState fluidState = level.getFluidState(backPos);
        if(!fluidState.isEmpty()){
            this.fluidHolder.fill(SoftFluidList.fromFluid(fluidState.getType()));
            return true;
        }
        IFluidHandler handler = FluidUtil.getFluidHandler(level,backPos,backDir.getOpposite()).orElse(null);
        if(handler!=null){
            FluidStack fluid = handler.getFluidInTank(0);
            if(!fluid.isEmpty()){
                this.fluidHolder.fill(fluid);
                return true;
            }
        }
        return false;
    }

    @Override
    public double getViewDistance() {
        return 80;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getBlockPos().offset(0, -1, 0), getBlockPos().offset(1, 1, 1));
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
                boolean flag = this.tryExtract();
                if (flag) {
                    this.transferCooldown = 20;
                }
            }
        }
    }
    //sf->ff/sf
    private boolean tryFillingBlockBelow(SoftFluid softFluid){
        if(softFluid==SoftFluidList.HONEY && level.getBlockState(worldPosition.below()).hasProperty(BlockStateProperties.LEVEL_HONEY)){
            BlockState state = level.getBlockState(worldPosition.below());
            int h = state.getValue(BlockStateProperties.LEVEL_HONEY);
            if(h<5){
                level.setBlock(worldPosition.below(),state.setValue(BlockStateProperties.LEVEL_HONEY,h+1),3);
                return true;
            }
            return false;
        }

        TileEntity tileDown = level.getBlockEntity(worldPosition.below());
        if (tileDown instanceof JarBlockTile) {
            if(((JarBlockTile) tileDown).fluidHolder.tryAddingFluid(softFluid)){
                tileDown.setChanged();
                return true;
            }
            return false;
        }
        IFluidHandler handlerDown = FluidUtil.getFluidHandler(this.level, worldPosition.below(), Direction.UP).orElse(null);
        if(handlerDown!=null){
            FluidStack honeyStack = new FluidStack(softFluid.getFluid(),250);
            if(!honeyStack.isEmpty() && handlerDown.fill(honeyStack, IFluidHandler.FluidAction.SIMULATE)>0){
                handlerDown.fill(honeyStack, IFluidHandler.FluidAction.EXECUTE);
                return true;
            }
        }
        return false;
    }

    private static boolean isSapLog(Block block){
        String name = block.getRegistryName().toString();
        return name.equals("autumnity:sappy_maple_log")||name.equals("autumnity:sappy_maple_wood");
    }

    //optimize this
    private boolean tryExtract() {
        Direction dir = this.getBlockState().getValue(FaucetBlock.FACING);
        BlockPos behind = this.worldPosition.relative(dir.getOpposite());
        BlockState backState = this.level.getBlockState(behind);
        Block backBlock = backState.getBlock();
        //TODO: optimize thiis

        if (this.isConnectedBelow()){
            if (backState.hasProperty(BlockStateProperties.LEVEL_HONEY) && backState.getValue(BlockStateProperties.LEVEL_HONEY) > 0) {
                if(tryFillingBlockBelow(SoftFluidList.HONEY)) {
                    //TODO: support fulling beehives and honeypots
                    this.level.setBlock(behind, backState.setValue(BlockStateProperties.LEVEL_HONEY,
                            backState.getValue(BlockStateProperties.LEVEL_HONEY) - 1), 3);
                    return true;
                }
                return false;
            }
            if (isSapLog(backBlock)) {
                if(tryFillingBlockBelow(SoftFluidList.SAP)) {
                    Block log = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(backBlock.getRegistryName().toString().replace("sappy","stripped")));
                    if(log!=null) {
                        this.level.setBlock(behind, log.defaultBlockState().setValue(BlockStateProperties.AXIS,
                                backState.getValue(BlockStateProperties.AXIS)), 3);
                    }
                    return true;
                }
                return false;
            }
            else if(backBlock instanceof CauldronBlock && backState.getValue(BlockStateProperties.LEVEL_CAULDRON) > 0) {
                TileEntity cauldronTile = level.getBlockEntity(behind);
                if(cauldronTile==null && tryFillingBlockBelow(SoftFluidList.WATER)) {
                    this.level.setBlock(behind, backState.setValue(BlockStateProperties.LEVEL_CAULDRON,
                            backState.getValue(BlockStateProperties.LEVEL_CAULDRON) - 1), 3);
                    return true;
                }
                //else if(ModList.get().isLoaded("inspirations")){
                    //ItemStack s = CauldronPlugin.tryExtractFluid(cauldronTile);
                //}
                //TODO: add inspirations cauldron support
                return false;
            }
            //TODO: optimize this
            IFluidHandler handlerDown = FluidUtil.getFluidHandler(this.level, worldPosition.below(), Direction.UP).orElse(null);
            //sf->sf
            if(backBlock instanceof JarBlock){
                TileEntity tileBack = level.getBlockEntity(behind);
                if(tileBack instanceof JarBlockTile) {
                    if(handlerDown!=null){
                        if(((JarBlockTile) tileBack).fluidHolder.fillFluidTank(handlerDown)){
                            tileBack.setChanged();
                            return true;
                        }
                    }
                    TileEntity tileDown = level.getBlockEntity(worldPosition.below());
                    if (tileDown instanceof JarBlockTile && ((IInventory) tileBack).isEmpty()) {
                        if(((JarBlockTile) tileBack).fluidHolder.tryTransferFluid(((JarBlockTile) tileDown).fluidHolder)){
                            tileBack.setChanged();
                            tileDown.setChanged();
                            return true;
                        }
                    }
                }
                return false;
            }
            //ff->ff
            IFluidHandler handlerBack = FluidUtil.getFluidHandler(this.level,behind,dir).orElse(null);
            if(handlerBack!=null){
                if(handlerDown!=null) {
                    return FluidUtil.tryFluidTransfer(handlerDown, handlerBack, 250, true) != null;
                }
                TileEntity tileDown = level.getBlockEntity(worldPosition.below());
                if(tileDown instanceof JarBlockTile) {
                    if(((JarBlockTile) tileDown).fluidHolder.drainFluidTank(handlerBack)){
                        tileDown.setChanged();
                        return true;
                    }
                }
            }

        }

        else if(backBlock instanceof JarBlock){
            TileEntity te = this.level.getBlockEntity(behind);
            if(te instanceof JarBlockTile){
                if(((IInventory) te).isEmpty()) {
                    SoftFluidHolder holder = ((JarBlockTile) te).fluidHolder;
                    if (holder.canRemove(1)) {
                        if (holder.getFluid() == SoftFluidList.XP) {
                            ((JarBlockTile) te).fluidHolder.shrink(1);
                            this.dropXP();
                            te.setChanged();
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        //pull other items
        return this.pullItems();
    }

    private void dropXP(){
        int i = 3 + this.level.random.nextInt(5) + this.level.random.nextInt(5);

        while(i > 0) {
            int xp = ExperienceOrbEntity.getExperienceValue(i);
            i -= xp;
            ExperienceOrbEntity orb = new ExperienceOrbEntity(this.level,this.worldPosition.getX() + 0.5, this.worldPosition.getY()-0.125f, this.worldPosition.getZ() + 0.5, xp);
            orb.setDeltaMovement(new Vector3d(0, 0, 0));
            this.level.addFreshEntity(orb);
        }
        //average xp bottle xp
        //int xp = 7;
        //this.world.addEntity(new ExperienceOrbEntity(this.world,this.pos.getX() + 0.5, this.pos.getY(), this.pos.getZ() + 0.5, xp));
        float f = (this.rand.nextFloat() - 0.5f) / 4f;
        this.level.playSound(null, this.worldPosition, SoundEvents.CHICKEN_EGG, SoundCategory.BLOCKS, 0.3F, 0.5f + f);
    }

    public boolean isOpen() {
        return (this.getBlockState().getValue(BlockStateProperties.POWERED) ^ this.getBlockState().getValue(BlockStateProperties.ENABLED));
    }

    public boolean hasWater() {
        return this.getBlockState().getValue(FaucetBlock.HAS_WATER);
    }

    public boolean isConnectedBelow() {
        return this.getBlockState().getValue(FaucetBlock.HAS_JAR);
    }

    // hopper code
    private static boolean canExtractItemFromSlot(IInventory inventoryIn, ItemStack stack, int index, Direction side) {
        return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory) inventoryIn).canTakeItemThroughFace(index, stack, side);
    }

    private boolean pullItemFromSlot(IInventory inventoryIn, int index, Direction direction) {
        ItemStack itemstack = inventoryIn.getItem(index);
        // special case for jars. has to be done to prevent other hoppers
        // frominteracting with them cause canextractitems is always false
        if (this.isConnectedBelow()) {
            return false;
        }
        else if (!itemstack.isEmpty() && canExtractItemFromSlot(inventoryIn, itemstack, index, direction)) {
            ItemStack it = itemstack.copy();
            itemstack.shrink(1);
            inventoryIn.setChanged();
            it.setCount(1);
            ItemEntity drop = new ItemEntity(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY(), this.worldPosition.getZ() + 0.5, it);
            drop.setDeltaMovement(new Vector3d(0, 0, 0));
            this.level.addFreshEntity(drop);
            float f = (this.rand.nextFloat() - 0.5f) / 4f;
            this.level.playSound(null, this.worldPosition, SoundEvents.CHICKEN_EGG, SoundCategory.BLOCKS, 0.3F, 0.5f + f);
            return true;
        }
        return false;
    }

    public boolean pullItems() {
        IInventory iinventory = getSourceInventory();
        if (iinventory != null) {
            Direction direction = this.getBlockState().getValue(HorizontalBlock.FACING);
            return getSlots(iinventory, direction).anyMatch((p_213971_3_)
                    -> pullItemFromSlot(iinventory, p_213971_3_, direction));
        }
        return false;
    }

    public IInventory getSourceInventory() {
        BlockPos behind = this.worldPosition.relative(this.getBlockState().getValue(HorizontalBlock.FACING), -1);
        IInventory firstinv = HopperTileEntity.getContainerAt(this.getLevel(), behind);
        if (firstinv != null) {
            return firstinv;
        } else if (this.level.getBlockState(behind).isRedstoneConductor(this.level, this.worldPosition)) {
            return HopperTileEntity.getContainerAt(this.getLevel(),
                    this.worldPosition.relative(this.getBlockState().getValue(HorizontalBlock.FACING), -2));
        } else
            return null;
    }

    private static IntStream getSlots(IInventory inv, Direction dir) {
        return inv instanceof ISidedInventory
                ? IntStream.of(((ISidedInventory) inv).getSlotsForFace(dir))
                : IntStream.range(0, inv.getContainerSize());
    }

    // end hopper code

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.transferCooldown = compound.getInt("TransferCooldown");
        this.fluidHolder.read(compound);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.putInt("TransferCooldown", this.transferCooldown);
        this.fluidHolder.write(compound);
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }
}