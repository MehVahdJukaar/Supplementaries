package net.mehvahdjukaar.supplementaries.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class FallingBlockTileEntity extends FallingBlockEntity {
    public FallingBlockTileEntity(EntityType<? extends FallingBlockEntity> type, World world) {
        super(type, world);
    }

    public FallingBlockTileEntity(World worldIn, double x, double y, double z, BlockState fallingBlockState) {
        super(worldIn,x,y,z,fallingBlockState);
        TileEntity te = worldIn.getTileEntity(new BlockPos(x,y,z));
        if(fallingBlockState.hasTileEntity() && te!=null){
            CompoundNBT com = new CompoundNBT();
            te.write(com);
            this.tileEntityData = com;
        }
    }


    //public FallingBlockTileEntity(FMLPlayMessages.SpawnEntity packet, World world) { super((EntityType<? extends FallingBlockEntity>) Registry.FALLING_BLOCK_TILE_ENTITY, world); }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }



    @Nullable
    public ItemEntity entityDropItem(IItemProvider itemIn, int offset) {
        ItemStack stack = new ItemStack(itemIn);
        if(itemIn instanceof Block && ((Block) itemIn).getDefaultState().hasTileEntity()){
            stack.setTagInfo("BlockEntityTag", this.tileEntityData);
        }
        return this.entityDropItem(stack, (float)offset);
    }

}
