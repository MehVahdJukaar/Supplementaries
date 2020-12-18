package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.blocks.tiles.CageBlockTile;
import net.mehvahdjukaar.supplementaries.blocks.JarBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class CageItem extends BlockItem {
    public CageItem(Block blockIn, Properties properties) {
        super(blockIn, properties);
    }


    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        //mob jar
        CompoundNBT com = stack.getChildTag("CachedJarMobValues");
        if (com != null){
            if(com.contains("Name")){
                tooltip.add(new StringTextComponent(com.getString("Name")));
            }
        }
    }


    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getItem();
        CompoundNBT com = stack.getTag();
        if(!context.getPlayer().isSneaking() && com!=null && com.contains("JarMob")){
            World world = context.getWorld();
            Entity entity  = EntityType.loadEntityAndExecute(com.getCompound("JarMob"), world, o -> o);
            if(entity!=null) {
                if(!world.isRemote) {
                    Vector3d v = context.getHitVec();
                    entity.setPositionAndRotation(v.getX(), v.getY(), v.getZ(), context.getPlacementYaw(), 0);
                    world.addEntity(entity);
                }
                if(!context.getPlayer().isCreative()) {
                   ItemStack returnItem = new ItemStack(((BlockItem)stack.getItem()).getBlock().asItem());
                   if(stack.hasDisplayName())returnItem.setDisplayName(stack.getDisplayName());
                   context.getPlayer().setHeldItem(context.getHand(), returnItem);
                }

            }
            return ActionResultType.SUCCESS;

        }
        return super.onItemUse(context);
    }

    @Override
    public ActionResultType tryPlace(BlockItemUseContext context) {
        ActionResultType placeresult = super.tryPlace(context);
        if(placeresult.isSuccessOrConsume()) {
            World world = context.getWorld();
            BlockPos pos = context.getPos();
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof CageBlockTile){
                CageBlockTile mobjar = ((CageBlockTile)te);
                CompoundNBT compound = context.getItem().getTag();
                if(compound!=null&&compound.contains("JarMob")&&compound.contains("CachedJarMobValues")) {
                    CompoundNBT com2 = compound.getCompound("CachedJarMobValues");
                    CompoundNBT com = compound.getCompound("JarMob");

                    mobjar.entityData = com;
                    mobjar.yOffset = com2.getFloat("YOffset");
                    mobjar.scale = com2.getFloat("Scale");
                    if (!world.isRemote){
                        int light = 0;
                        //TODO move to enum
                        if(com.getString("id").equals("minecraft:endermite"))light = 5;
                        else if(com.getString("id").equals("iceandfire:pixie"))light = 10;
                        if(light!=0) {
                            BlockState state = world.getBlockState(pos);
                            world.setBlockState(pos, state.with(JarBlock.LIGHT_LEVEL, light));
                        }

                    }
                    mobjar.markDirty();
                    //mobjar.updateMob();

                }
            }
        }
        return placeresult;
    }

}
