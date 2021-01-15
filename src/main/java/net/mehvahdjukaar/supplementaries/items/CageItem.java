package net.mehvahdjukaar.supplementaries.items;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class CageItem extends BlockItem {
    public CageItem(Block blockIn, Properties properties) {
        super(blockIn, properties);
    }


    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getChildTag("BlockEntityTag");
        if (compoundnbt != null && compoundnbt.contains("MobHolder")) {
            CompoundNBT com = compoundnbt.getCompound("MobHolder");
            if (com != null) {
                if (com.contains("Name")) {
                    tooltip.add(new StringTextComponent(com.getString("Name")).mergeStyle(TextFormatting.GRAY));
                }
            }
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
                   ItemStack returnItem = new ItemStack(((BlockItem)stack.getItem()).getBlock().asItem());
                   if(stack.hasDisplayName())returnItem.setDisplayName(stack.getDisplayName());
                   context.getPlayer().setHeldItem(context.getHand(), returnItem);
                }

            }
            return ActionResultType.SUCCESS;

        }
        return super.onItemUse(context);
    }



}
