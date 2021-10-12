package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HangingFlowerPotBlockTile extends SwayingBlockTile implements IBlockHolder, IOwnerProtected {
    private UUID owner = null;

    private BlockState pot = Blocks.FLOWER_POT.defaultBlockState();

    public HangingFlowerPotBlockTile() {
        super(ModRegistry.HANGING_FLOWER_POT_TILE.get());
    }

    static {
        maxSwingAngle = 45f;
        minSwingAngle = 2f;
        maxPeriod = 35f;
        angleDamping = 80f;
        periodDamping = 70f;
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public BlockState getHeldBlock(int index) {
        return pot;
    }

    @Override
    public boolean setHeldBlock(BlockState state, int index) {
        if(state.getBlock() instanceof FlowerPotBlock){
            this.pot = state;
            this.setChanged();
            //TODO: optimize mark dirty and block update to send only what's needed
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
            return true;
        }
        return false;
    }


    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        //if(pot != Blocks.AIR.getDefaultState())
        compound.put("Pot", NBTUtil.writeBlockState(pot));
        this.saveOwner(compound);
        return compound;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        pot = NBTUtil.readBlockState(compound.getCompound("Pot"));
        this.loadOwner(compound);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.worldPosition);
    }

    @Override
    public double getViewDistance() {
        return 64;
    }
}
