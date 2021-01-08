package net.mehvahdjukaar.supplementaries.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class Flute extends Item {
    public Flute(Properties properties) {
        super(properties);
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {

        ItemStack stack = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote) {
            BlockPos pos = playerIn.getPosition();
            double x = playerIn.getPosX();
            double y = playerIn.getPosY();
            double z = playerIn.getPosZ();
            int r = 20;
            AxisAlignedBB bb = new AxisAlignedBB(x - r, y - r, z - r, x + r, y + r, z + r);
            List<Entity> entities = worldIn.getEntitiesInAABBexcluding(playerIn, bb, (e) -> e instanceof TameableEntity);
            for (Entity e : entities) {
                TameableEntity pet = ((TameableEntity) e);
                if (pet.isTamed() && !pet.isSitting() && pet.getOwnerId().equals(playerIn.getUniqueID())) {
                    pet.attemptTeleport(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, false);
                    playerIn.getCooldownTracker().setCooldown(this, 20);
                }
            }
            stack.damageItem(1, playerIn, (en) -> en.sendBreakAnimation(EquipmentSlotType.MAINHAND));
            return ActionResult.resultConsume(stack);
        }
        //swings hand
        return ActionResult.resultSuccess(stack);
    }

    @Override
    public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
        if(stack.getChildTag("Pet")==null && target instanceof TameableEntity && ((TameableEntity) target).isTamed() &&
                ((TameableEntity) target).getOwnerId().equals(playerIn.getUniqueID())) {
            CompoundNBT com = new CompoundNBT();
            com.putString("Name", target.getName().getString());
            com.putUniqueId("Id", target.getUniqueID());
            stack.setTagInfo("Pet",com);
            playerIn.setHeldItem(hand, stack);
            playerIn.getCooldownTracker().setCooldown(this, 20);
            return ActionResultType.func_233537_a_(playerIn.world.isRemote);
        }
        return super.itemInteractionForEntity(stack, playerIn, target, hand);
    }

    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getChildTag("Pet");
            if (compoundnbt !=null && compoundnbt.contains("Name")){
                tooltip.add(new StringTextComponent(compoundnbt.getString("Name")).mergeStyle(TextFormatting.GRAY));
            }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 60;
    }


}
