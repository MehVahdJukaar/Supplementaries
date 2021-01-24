package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.blocks.tiles.CageBlockTile;
import net.mehvahdjukaar.supplementaries.common.MobHolder;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class CageItem extends BlockItem {
    public final Supplier<Item> empty;
    public CageItem(Block blockIn, Properties properties, Supplier<Item> empty) {
        super(blockIn, properties);
        this.empty = empty;
    }



    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getChildTag("BlockEntityTag");
        if (compoundnbt != null && compoundnbt.contains("MobHolder")) {
            CompoundNBT com = compoundnbt.getCompound("MobHolder");
            if (com != null) {
                if (com.contains("Name")) {
                    tooltip.add(new StringTextComponent(com.getString("Name")).mergeStyle(TextFormatting.GRAY));
                    tooltip.add(new TranslationTextComponent("message.supplementaries.cage").mergeStyle(TextFormatting.ITALIC).mergeStyle(TextFormatting.GRAY));
                }
            }
        }
        else{
            CompoundNBT c = stack.getTag();
            if(c!=null&&(c.contains("JarMob")||c.contains("CachedJarMobValues")))
                tooltip.add(new StringTextComponent("try placing me down").mergeStyle(TextFormatting.GRAY));
        }


    }

    //free mob
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getItem();
        CompoundNBT com = stack.getChildTag("BlockEntityTag");
        if(!context.getPlayer().isSneaking() && com!=null && com.contains("MobHolder")){
            CompoundNBT nbt = com.getCompound("MobHolder");
            World world = context.getWorld();
            Entity entity  = EntityType.loadEntityAndExecute(nbt.getCompound("EntityData"), world, o -> o);
            if(entity!=null) {
                if(!world.isRemote) {
                    Vector3d v = context.getHitVec();
                    entity.setPositionAndRotation(v.getX(), v.getY(), v.getZ(), context.getPlacementYaw(), 0);

                    UUID temp = entity.getUniqueID();
                    if(nbt.contains("UUID")) {
                        UUID id = nbt.getUniqueId("UUID");
                        entity.setUniqueId(id);
                    }
                    if(!world.addEntity(entity)){
                        //spawn failed, reverting to old UUID
                        entity.setUniqueId(temp);
                        world.addEntity(entity);
                    }
                    //TODO fix sound categories
                    world.playSound(null,v.getX(), v.getY(), v.getZ(), SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.PLAYERS,1,0.05f);

                }
                if(!context.getPlayer().isCreative()) {
                   ItemStack returnItem = new ItemStack(empty.get());
                   if(stack.hasDisplayName())returnItem.setDisplayName(stack.getDisplayName());
                   context.getPlayer().setHeldItem(context.getHand(), returnItem);
                }

            }
            return ActionResultType.SUCCESS;

        }
        return super.onItemUse(context);
    }



    //remove this in the future
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

                    mobjar.mobHolder.entityData = compound.getCompound("JarMob");
                    mobjar.mobHolder.yOffset = com2.getFloat("YOffset");
                    mobjar.mobHolder.scale = com2.getFloat("Scale");
                    mobjar.mobHolder.entityChanged = true;
                    mobjar.mobHolder.animationType= MobHolder.MobHolderType.DEFAULT;
                    mobjar.mobHolder.name="reload needed";

                    mobjar.markDirty();
                    //mobjar.updateMob();

                }
            }
        }
        return placeresult;
    }


}
