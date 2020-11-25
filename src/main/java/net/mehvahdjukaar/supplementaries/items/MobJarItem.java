package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.blocks.MobJarBlockTile;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CollectionNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MobJarItem extends BlockItem {
    public static Entity mob = null;

    public MobJarItem(Block blockIn, Properties properties) {
        super(blockIn, properties);
    }


    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        if(player.world.isRemote)return true;
        CompoundNBT compound = new CompoundNBT();
        Entity e = entity;
        e.copyDataFromOld(entity);
        e.rotationYaw=0;
        e.prevRotationYaw=0;
        e.prevRotationPitch=0;
        e.rotationPitch=0;

        e.tick();
        if(e instanceof LivingEntity){
            LivingEntity le = ((LivingEntity)e);
            le.prevRotationYawHead=0;
            le.rotationYawHead=0;

            le.livingTick();
        }
        e.writeUnlessPassenger(compound);
        e.remove();
        if (!compound.isEmpty()) {
            stack.setTagInfo("BlockEntityTag", compound);
        }
        return true;
    }

    @Override
    protected boolean onBlockPlaced(BlockPos pos, World worldIn, PlayerEntity player, ItemStack stack, BlockState state) {
        boolean ret = super.onBlockPlaced(pos, worldIn, player, stack, state);
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof MobJarBlockTile){
            MobJarBlockTile mobjar = ((MobJarBlockTile)te);
            CompoundNBT compound = stack.getTag();
            if(compound!=null&&compound.contains("BlockEntityTag")) {

                CompoundNBT com = compound.getCompound("BlockEntityTag");
                com.remove("Passengers");
                com.remove("Leash");
                com.remove("UUID");

                mobjar.entityData=com;
                mobjar.updateMob();

            }

        }
        return ret;
    }
}
