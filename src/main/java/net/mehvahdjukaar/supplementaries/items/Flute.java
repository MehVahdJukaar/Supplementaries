package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class Flute extends Item {
    public Flute(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getChildTag("Enchantments");
        return compoundnbt != null && (compoundnbt.contains("Pet") || super.hasEffect(stack));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        if(!(entity instanceof LivingEntity)||player.getActiveHand()==null)return false;
        return this.itemInteractionForEntity(stack,player, ((LivingEntity) entity),player.getActiveHand()).isSuccessOrConsume();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {

        ItemStack stack = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote) {
            double x = playerIn.getPosX();
            double y = playerIn.getPosY();
            double z = playerIn.getPosZ();
            int r=ServerConfigs.cached.FLUTE_RADIUS;
            CompoundNBT com1 = stack.getChildTag("Enchantments");
            if(com1!=null&&com1.contains("Pet")){
                CompoundNBT com = com1.getCompound("Pet");
                Entity entity = worldIn.getEntityByID(com.getInt("ID"));
                int maxdistsq = ServerConfigs.cached.FLUTE_DISTANCE*ServerConfigs.cached.FLUTE_DISTANCE;
                LivingEntity pet1 = ((LivingEntity) entity);
                if (pet1.world==playerIn.world&&pet1.getDistanceSq(playerIn)<maxdistsq) {
                    if(pet1.attemptTeleport(x, y, z, false)){
                        //pet1.setSleeping(false);
                    }
                }

            }
            else {
                AxisAlignedBB bb = new AxisAlignedBB(x - r, y - r, z - r, x + r, y + r, z + r);
                List<Entity> entities = worldIn.getEntitiesInAABBexcluding(playerIn, bb, (e) -> e instanceof TameableEntity);
                for (Entity e : entities) {
                    TameableEntity pet = ((TameableEntity) e);
                    if (pet.isTamed() && !pet.isSitting() && pet.getOwnerId().equals(playerIn.getUniqueID())) {
                        pet.attemptTeleport(x, y, z, false);
                    }
                }
            }


            playerIn.getCooldownTracker().setCooldown(this, 20);
            stack.damageItem(1, playerIn, (en) -> en.sendBreakAnimation(EquipmentSlotType.MAINHAND));
            worldIn.playSound(null, playerIn.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, SoundCategory.PLAYERS, 1f, 1.45f);

            return ActionResult.resultConsume(stack);
        }
        //swings hand
        return ActionResult.resultSuccess(stack);
    }

    @Override
    public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
        CompoundNBT c = stack.getChildTag("Enchantments");
        String s  = target.getType().getRegistryName().toString();
        if((c==null||!c.contains("Pet")) && (target instanceof TameableEntity && ((TameableEntity) target).isTamed() &&
                ((TameableEntity) target).getOwnerId().equals(playerIn.getUniqueID())) ||
                ServerConfigs.cached.FLUTE_EXTRA_MOBS.contains(target.getType().getRegistryName().toString())) {
            if(target instanceof AbstractHorseEntity && !((AbstractHorseEntity) target).isTame())return ActionResultType.PASS;
            //if(target instanceof FoxEntity && ! ((FoxEntity)target).isTrustedUUID(p_213497_1_.getUniqueID())return ActionResultType.PASS;
            CompoundNBT com = new CompoundNBT();
            com.putString("Name", target.getName().getString());
            com.putUniqueId("UUID", target.getUniqueID());
            com.putInt("ID", target.getEntityId());
            CompoundNBT com2 = new CompoundNBT();
            com2.put("Pet",com);

            stack.setTagInfo("Enchantments",com2);
            playerIn.setHeldItem(hand, stack);
            playerIn.getCooldownTracker().setCooldown(this, 20);
            return ActionResultType.func_233537_a_(playerIn.world.isRemote);
        }
        return super.itemInteractionForEntity(stack, playerIn, target, hand);
    }


    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getChildTag("Enchantments");
            if (compoundnbt !=null && compoundnbt.contains("Pet")){
                CompoundNBT com = compoundnbt.getCompound("Pet");
                tooltip.add(new StringTextComponent(com.getString("Name")).mergeStyle(TextFormatting.GRAY));
            }

    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 60;
    }


}
