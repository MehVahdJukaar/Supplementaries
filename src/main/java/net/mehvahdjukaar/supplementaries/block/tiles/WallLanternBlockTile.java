package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.block.util.ILightMimic;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;


public class WallLanternBlockTile extends EnhancedLanternBlockTile implements ITickableTileEntity, IBlockHolder, ILightMimic {

    public BlockState lanternBlock = Blocks.LANTERN.defaultBlockState();//Blocks.AIR.getDefaultState();
    public boolean isRedstoneLantern = false;

    static {
        maxSwingAngle = 45f;
        minSwingAngle = 1.9f;
        maxPeriod = 28f;
        angleDamping = 80f;
        periodDamping = 70f;
    }

    public WallLanternBlockTile() {
        super(Registry.WALL_LANTERN_TILE.get());
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.lanternBlock = NBTUtil.readBlockState(compound.getCompound("Lantern"));
        this.isRedstoneLantern = compound.getBoolean("IsRedstone");
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.put("Lantern", NBTUtil.writeBlockState(lanternBlock));
        compound.putBoolean("IsRedstone",this.isRedstoneLantern);
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
//maybe merge these two
    @Override
    public void setLight(int light) {
        boolean lit = true;
        if(this.lanternBlock.getBlock().getRegistryName().toString().equals("charm:redstone_lantern")) {
            this.isRedstoneLantern = true;
            light = 15;
            lit = false;
        }
        if(this.getBlockState().getValue(WallLanternBlock.LIGHT_LEVEL)!=light)
            this.getLevel().setBlock(this.worldPosition, this.getBlockState().setValue(WallLanternBlock.LIT,lit)
                    .setValue(WallLanternBlock.LIGHT_LEVEL,light),4|16);
    }
}