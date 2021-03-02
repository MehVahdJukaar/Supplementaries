package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.FaucetBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluid;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluidHolder;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluidList;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.*;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Random;
import java.util.stream.IntStream;

public class FaucetBlockTile extends TileEntity implements ITickableTileEntity {
    private int transferCooldown = 0;
    protected final Random rand = new Random();
    public final SoftFluidHolder fluidHolder = new SoftFluidHolder(1);

    public FaucetBlockTile() {
        super(Registry.FAUCET_TILE.get());
    }

    @Override
    public void onLoad() {
        this.fluidHolder.setWorldAndPos(this.world, this.pos);
    }

    //returns true if it has water
    public boolean updateDisplayedFluid(BlockState state){
        Direction dir = state.get(FaucetBlock.FACING).getOpposite();
        BlockPos backPos = pos.offset(dir);
        BlockState backState = world.getBlockState(backPos);

        Block b = backState.getBlock();

        if(b instanceof JarBlock){
            TileEntity te = world.getTileEntity(backPos);
            if(te instanceof JarBlockTile){
                if(!((JarBlockTile) te).fluidHolder.isEmpty()){
                    this.fluidHolder.copy(((JarBlockTile) te).fluidHolder);
                    return true;
                }
            }
            return false;
        }
        else if(b instanceof BeehiveBlock){
            if(backState.get(BeehiveBlock.HONEY_LEVEL) > 0){
                this.fluidHolder.fill(SoftFluidList.HONEY);
                return true;
            }
            return false;
        }
        else if(b instanceof CauldronBlock){
            if(world instanceof World && backState.getComparatorInputOverride(world,backPos)>0){
                this.fluidHolder.fill(SoftFluidList.WATER);
                return true;
            }
            return false;
        }
        //fluid stuff
        FluidState fluidState = world.getFluidState(backPos);
        if(!fluidState.isEmpty()){
            this.fluidHolder.fill(SoftFluidList.fromFluid(fluidState.getFluid()));
            return true;
        }
        IFluidHandler handler = FluidUtil.getFluidHandler(world,backPos,dir.getOpposite()).orElse(null);
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
    public double getMaxRenderDistanceSquared() {
        return 80;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().add(0, -1, 0), getPos().add(1, 1, 1));
    }

    private boolean isOnTransferCooldown() {
        return this.transferCooldown > 0;
    }

    @Override
    public void tick() {
        if (this.world != null && !this.world.isRemote) {
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
        TileEntity tileDown = world.getTileEntity(pos.down());
        if (tileDown instanceof JarBlockTile) {
            if(((JarBlockTile) tileDown).fluidHolder.tryAddingFluid(softFluid)){
                tileDown.markDirty();
                return true;
            }
            return false;
        }
        IFluidHandler handlerDown = FluidUtil.getFluidHandler(this.world, pos.down(), Direction.UP).orElse(null);
        if(handlerDown!=null){
            FluidStack honeyStack = new FluidStack(softFluid.getFluid(),250);
            if(!honeyStack.isEmpty() && handlerDown.fill(honeyStack, IFluidHandler.FluidAction.SIMULATE)>0){
                handlerDown.fill(honeyStack, IFluidHandler.FluidAction.EXECUTE);
                return true;
            }
        }
        return false;
    }

    //optimize this
    private boolean tryExtract() {
        Direction dir = this.getBlockState().get(FaucetBlock.FACING);
        BlockPos behind = this.pos.offset(dir.getOpposite());
        BlockState backState = this.world.getBlockState(behind);
        Block backBlock = backState.getBlock();

        if (this.hasFluidTankBelow()){
            if (backBlock instanceof BeehiveBlock && backState.get(BlockStateProperties.HONEY_LEVEL) > 0) {
                if(tryFillingBlockBelow(SoftFluidList.HONEY)) {
                    this.world.setBlockState(behind, backState.with(BlockStateProperties.HONEY_LEVEL,
                            backState.get(BlockStateProperties.HONEY_LEVEL) - 1), 3);
                    return true;
                }
                return false;
            }
            else if(backBlock instanceof CauldronBlock && backState.get(BlockStateProperties.LEVEL_0_3) > 0) {
                if(world.getTileEntity(behind)==null && tryFillingBlockBelow(SoftFluidList.WATER)) {
                    this.world.setBlockState(behind, backState.with(BlockStateProperties.LEVEL_0_3,
                            backState.get(BlockStateProperties.LEVEL_0_3) - 1), 3);
                    return true;
                }
                //TODO: add inspirations cauldron support
                return false;
            }
            //TODO: optimize this
            IFluidHandler handlerDown = FluidUtil.getFluidHandler(this.world, pos.down(), Direction.UP).orElse(null);
            //sf->sf
            if(backBlock instanceof JarBlock){
                TileEntity tileBack = world.getTileEntity(behind);
                if(tileBack instanceof JarBlockTile) {
                    if(handlerDown!=null){
                        if(((JarBlockTile) tileBack).fluidHolder.fillFluidTank(handlerDown)){
                            tileBack.markDirty();
                            return true;
                        }
                    }
                    TileEntity tileDown = world.getTileEntity(pos.down());
                    if (tileDown instanceof JarBlockTile) {
                        if(((JarBlockTile) tileBack).fluidHolder.tryTransferFluid(((JarBlockTile) tileDown).fluidHolder)){
                            tileBack.markDirty();
                            tileDown.markDirty();
                            return true;
                        }
                    }
                }
                return false;
            }
            //ff->ff
            IFluidHandler handlerBack = FluidUtil.getFluidHandler(this.world,behind,dir).orElse(null);
            if(handlerBack!=null){
                if(handlerDown!=null) {
                    return FluidUtil.tryFluidTransfer(handlerDown, handlerBack, 250, true) != null;
                }
                TileEntity tileDown = world.getTileEntity(pos.down());
                if(tileDown instanceof JarBlockTile) {
                    if(((JarBlockTile) tileDown).fluidHolder.drainFluidTank(handlerBack)){
                        tileDown.markDirty();
                        return true;
                    }
                }
            }

        }

        //pull other items
        return this.pullItems();
    }

    public boolean isOpen() {
        return (this.getBlockState().get(BlockStateProperties.POWERED) ^ this.getBlockState().get(BlockStateProperties.ENABLED));
    }

    public boolean hasWater() {
        return this.getBlockState().get(FaucetBlock.HAS_WATER);
    }

    public boolean hasFluidTankBelow() {
        return this.getBlockState().get(FaucetBlock.HAS_JAR);
    }

    // hopper code
    private static boolean canExtractItemFromSlot(IInventory inventoryIn, ItemStack stack, int index, Direction side) {
        return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory) inventoryIn).canExtractItem(index, stack, side);
    }

    private boolean addItemToJar(ItemStack itemstack) {
        TileEntity tileentity = world.getTileEntity(this.pos.down());
        if (tileentity instanceof JarBlockTile) {
            JarBlockTile jartileentity = (JarBlockTile) tileentity;
            if (jartileentity.isItemValidForSlot(0, itemstack)) {
                ItemStack it = itemstack.copy();
                itemstack.shrink(1);
                //jartileentity.addItem(it, 1);
                jartileentity.markDirty();
                return true;
            }
        }
        return false;
    }

    private boolean pullItemFromSlot(IInventory inventoryIn, int index, Direction direction) {
        ItemStack itemstack = inventoryIn.getStackInSlot(index);
        BlockPos backpos = this.pos.offset(this.getBlockState().get(HorizontalBlock.HORIZONTAL_FACING), -1);
        // special case for jars. has to be done to prevent other hoppers
        // frominteracting with them cause canextractitems is always false
        if (this.hasFluidTankBelow()) {
            // can only transfer from jar to jar
            if (world.getBlockState(backpos).getBlock() instanceof JarBlock && !itemstack.isEmpty()) {
                if (this.addItemToJar(itemstack)) {
                    inventoryIn.markDirty();
                    return true;
                }
            }
            //TODO: add xp, pancakes & others
            return false;
        } else if (!itemstack.isEmpty() && canExtractItemFromSlot(inventoryIn, itemstack, index, direction)) {
            ItemStack it = itemstack.copy();
            itemstack.shrink(1);
            inventoryIn.markDirty();
            it.setCount(1);
            ItemEntity drop = new ItemEntity(this.world, this.pos.getX() + 0.5, this.pos.getY(), this.pos.getZ() + 0.5, it);
            drop.setMotion(new Vector3d(0, 0, 0));
            this.world.addEntity(drop);
            float f = (this.rand.nextFloat() - 0.5f) / 4f;
            this.world.playSound(null, this.pos, SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.BLOCKS, 0.3F, 0.5f + f);
            return true;
        }
        return false;
    }

    public boolean pullItems() {
        IInventory iinventory = getSourceInventory();
        if (iinventory != null) {
            Direction direction = this.getBlockState().get(HorizontalBlock.HORIZONTAL_FACING);
            return func_213972_a(iinventory, direction).anyMatch((p_213971_3_)
                    -> pullItemFromSlot(iinventory, p_213971_3_, direction));
        }
        return false;
    }

    public IInventory getSourceInventory() {
        BlockPos behind = this.pos.offset(this.getBlockState().get(HorizontalBlock.HORIZONTAL_FACING), -1);
        IInventory firstinv = HopperTileEntity.getInventoryAtPosition(this.getWorld(), behind);
        if (firstinv != null) {
            return firstinv;
        } else if (this.world.getBlockState(behind).isNormalCube(this.world, this.pos)) {
            return HopperTileEntity.getInventoryAtPosition(this.getWorld(),
                    this.pos.offset(this.getBlockState().get(HorizontalBlock.HORIZONTAL_FACING), -2));
        } else
            return null;
    }

    private static IntStream func_213972_a(IInventory inv, Direction dir) {
        return inv instanceof ISidedInventory
                ? IntStream.of(((ISidedInventory) inv).getSlotsForFace(dir))
                : IntStream.range(0, inv.getSizeInventory());
    }

    // end hopper code

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        this.transferCooldown = compound.getInt("TransferCooldown");
        this.fluidHolder.read(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("TransferCooldown", this.transferCooldown);
        this.fluidHolder.write(compound);
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(), pkt.getNbtCompound());
    }
}