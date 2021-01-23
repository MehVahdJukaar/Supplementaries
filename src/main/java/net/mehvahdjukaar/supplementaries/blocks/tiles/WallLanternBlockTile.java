package net.mehvahdjukaar.supplementaries.blocks.tiles;

import net.mehvahdjukaar.supplementaries.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.common.IBlockHolder;
import net.mehvahdjukaar.supplementaries.common.ILightMimic;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class WallLanternBlockTile extends SwayingBlockTile implements ITickableTileEntity, IBlockHolder, ILightMimic {

    public BlockState lanternBlock = Blocks.LANTERN.getDefaultState();

    static {
        maxSwingAngle = 45f;
        minSwingAngle = 1.9f;
        maxPeriod = 28f;
        angleDamping = 80f;
        periodDamping = 70f;
    }

    public WallLanternBlockTile() {
        super(Registry.WALL_LANTERN_TILE);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        this.lanternBlock = NBTUtil.readBlockState(compound.getCompound("Lantern"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.put("Lantern", NBTUtil.writeBlockState(lanternBlock));
        return compound;
    }

    @Override
    public BlockState getHeldBlock() {
        return this.lanternBlock;
    }

    @Override
    public boolean setHeldBlock(BlockState state) {
        this.lanternBlock = state;
        return true;
    }

    @Override
    public void setLight(int light) {
        if(this.getBlockState().get(WallLanternBlock.LIGHT_LEVEL)!=light)
            this.getWorld().setBlockState(this.pos, this.getBlockState().with(WallLanternBlock.LIGHT_LEVEL,light),4|16);
    }
}