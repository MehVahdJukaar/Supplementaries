package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
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
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;
import java.util.stream.IntStream;

public class FaucetBlockTile extends TileEntity implements ITickableTileEntity {
    private int transferCooldown = 0;
    protected final Random rand = new Random();
    public int watercolor = 0x423cf7;
    public FaucetBlockTile() {
        super(Registry.FAUCET_TILE);
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

    private boolean tryExtract() {
        BlockPos behind = this.pos.offset(this.getBlockState().get(FaucetBlock.FACING), -1);
        BlockState backstate = this.world.getBlockState(behind);
        // empty beehive
        if (backstate.getBlock() instanceof BeehiveBlock && backstate.get(BlockStateProperties.HONEY_LEVEL) > 0) {
            if (this.hasJar()) {
                if (this.addItemToJar(new ItemStack(Items.HONEY_BOTTLE))) {
                    this.world.setBlockState(behind,
                            backstate.with(BlockStateProperties.HONEY_LEVEL, backstate.get(BlockStateProperties.HONEY_LEVEL) - 1), 3);
                    return true;
                }
            }
            return false;
        }
        // empty cauldron
        else if (backstate.getBlock() instanceof CauldronBlock && backstate.get(BlockStateProperties.LEVEL_0_3) > 0 && world.getTileEntity(behind)==null) {
            if (this.hasJar()) {
                if (this.addItemToJar(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER))) {
                    this.world.setBlockState(behind,
                            backstate.with(BlockStateProperties.LEVEL_0_3, backstate.get(BlockStateProperties.LEVEL_0_3) - 1), 3);
                    return true;
                }
            }
            return false;
        }
        return this.pullItems();
    }

    public boolean isOpen() {
        return (this.getBlockState().get(BlockStateProperties.POWERED) ^ this.getBlockState().get(BlockStateProperties.ENABLED));
    }

    public boolean hasWater() {
        return this.getBlockState().get(FaucetBlock.HAS_WATER);
    }

    public boolean hasJar() {
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
                jartileentity.addItem(it, 1);
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
        if (this.hasJar()) {
            // can only transfer from jar to jar
            if (world.getBlockState(backpos).getBlock() instanceof JarBlock && !itemstack.isEmpty()) {
                if (this.addItemToJar(itemstack)) {
                    inventoryIn.markDirty();
                    return true;
                }
            }
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
        this.watercolor = compound.getInt("watercolor");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("watercolor", this.watercolor);
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

    @OnlyIn(Dist.CLIENT)
    public int updateClientWaterColor(){
        this.watercolor = BiomeColors.getWaterColor(this.world, this.pos);
        return this.watercolor;
    }
}