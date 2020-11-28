package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.blocks.MobJarBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CollectionNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class MobJarItem extends BlockItem {
    public static Entity mob = null;

    public MobJarItem(Block blockIn, Properties properties) {
        super(blockIn, properties);
    }


    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return new StringTextComponent("this is a test");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getTag();

        if(compoundnbt==null)return;

        if (compoundnbt.contains("LootTable", 8)) {
            tooltip.add(new StringTextComponent("???????"));
        }

        if (compoundnbt.contains("CachedJarMobValues")) {
            CompoundNBT com = compoundnbt.getCompound("CachedJarMobValues");
            if(com.contains("Name")){
                tooltip.add(new StringTextComponent(com.getString("Name")));
            }
        }

    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        CompoundNBT com = context.getItem().getTag();
        if(!context.getPlayer().isSneaking() && com!=null && com.contains("JarMob")){
            World world = context.getWorld();
            Entity entity  = EntityType.loadEntityAndExecute(com.getCompound("JarMob"), world, o -> o);
            if(entity!=null) {
                if(!world.isRemote) {
                    Vector3d v = context.getHitVec();
                    entity.setPositionAndRotation(v.getX(), v.getY(), v.getZ(), context.getPlacementYaw(), 0);
                    world.addEntity(entity);
                }
            }
            return ActionResultType.SUCCESS;

        }
        return super.onItemUse(context);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {


        String name =  entity.getType().getRegistryName().toString();
        if(!ServerConfigs.cached.MOB_JAR_ALLOWED_MOBS.contains(name)){
            int aa = 1;
            return false;
        }

        if(entity instanceof SlimeEntity && ((SlimeEntity)entity).getSlimeSize()>1) return false;

        if(player.world.isRemote)return true;

        Entity e = entity;
        e.copyDataFromOld(entity);
        e.rotationYaw=0;
        e.prevRotationYaw=0;
        e.prevRotationPitch=0;
        e.rotationPitch=0;
        if(e instanceof LivingEntity){
            LivingEntity le = ((LivingEntity)e);
            le.prevRotationYawHead=0;
            le.rotationYawHead=0;
        }

        CommonUtil.saveJarMobItemNBT(stack,e);

        e.remove();
        return true;
    }

    @Override
    protected boolean onBlockPlaced(BlockPos pos, World worldIn, PlayerEntity player, ItemStack stack, BlockState state) {
        boolean ret = super.onBlockPlaced(pos, worldIn, player, stack, state);
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof MobJarBlockTile){
            MobJarBlockTile mobjar = ((MobJarBlockTile)te);
            CompoundNBT compound = stack.getTag();
            if(compound!=null&&compound.contains("JarMob")&&compound.contains("CachedJarMobValues")) {
                CompoundNBT com2 = compound.getCompound("CachedJarMobValues");
                CompoundNBT com = compound.getCompound("JarMob");

                mobjar.entityData = com;
                mobjar.yOffset = com2.getFloat("YOffset");
                mobjar.scale = com2.getFloat("Scale");
                mobjar.updateMob();

            }

        }
        return ret;
    }
}
