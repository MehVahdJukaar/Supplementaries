package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;

public class HangingFlowerPotBlockTile extends SwayingBlockTile implements IBlockHolder {
    public BlockState pot = Blocks.FLOWER_POT.getDefaultState();
    public HangingFlowerPotBlockTile() {
        super(Registry.HANGING_FLOWER_POT_TILE.get());
    }

    static {
        maxSwingAngle = 45f;
        minSwingAngle = 2f;
        maxPeriod = 35f;
        angleDamping = 80f;
        periodDamping = 70f;
    }

    @Override
    public BlockState getHeldBlock() {
        return pot;
    }

    @Override
    public boolean setHeldBlock(BlockState state) {
        if(state.getBlock() instanceof FlowerPotBlock){
            this.pot = state;
            this.markDirty();
            //TODO: optimize mark dirty and block update to send only what's needed
            this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
            return true;
        }
        return false;
    }


    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        //if(pot != Blocks.AIR.getDefaultState())
        compound.put("Pot", NBTUtil.writeBlockState(pot));
        return compound;
        //TODO: maybe write resource location instead
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        pot = NBTUtil.readBlockState(compound.getCompound("Pot"));
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.pos);
    }
}
