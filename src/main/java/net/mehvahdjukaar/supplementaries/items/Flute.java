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

import net.minecraft.item.Item.Properties;

public class Flute extends Item {
    public Flute(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getTagElement("Enchantments");
        return compoundnbt != null && (compoundnbt.contains("Pet") || super.isFoil(stack));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        if(!(entity instanceof LivingEntity)||player.getUsedItemHand()==null)return false;
        return this.interactLivingEntity(stack,player, ((LivingEntity) entity),player.getUsedItemHand()).consumesAction();
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {

        ItemStack stack = playerIn.getItemInHand(handIn);
        if (!worldIn.isClientSide) {
            double x = playerIn.getX();
            double y = playerIn.getY();
            double z = playerIn.getZ();
            int r=ServerConfigs.cached.FLUTE_RADIUS;
            CompoundNBT com1 = stack.getTagElement("Enchantments");
            if(com1!=null&&com1.contains("Pet")){
                CompoundNBT com = com1.getCompound("Pet");
                Entity entity = worldIn.getEntity(com.getInt("ID"));
                int maxdistsq = ServerConfigs.cached.FLUTE_DISTANCE*ServerConfigs.cached.FLUTE_DISTANCE;
                LivingEntity pet1 = ((LivingEntity) entity);
                if (pet1.level==playerIn.level&&pet1.distanceToSqr(playerIn)<maxdistsq) {
                    if(pet1.randomTeleport(x, y, z, false)){
                        //pet1.setSleeping(false);
                    }
                }

            }
            else {
                AxisAlignedBB bb = new AxisAlignedBB(x - r, y - r, z - r, x + r, y + r, z + r);
                List<Entity> entities = worldIn.getEntities(playerIn, bb, (e) -> e instanceof TameableEntity);
                for (Entity e : entities) {
                    TameableEntity pet = ((TameableEntity) e);
                    if (pet.isTame() && !pet.isOrderedToSit() && pet.getOwnerUUID().equals(playerIn.getUUID())) {
                        pet.randomTeleport(x, y, z, false);
                    }
                }
            }


            playerIn.getCooldowns().addCooldown(this, 20);
            stack.hurtAndBreak(1, playerIn, (en) -> en.broadcastBreakEvent(EquipmentSlotType.MAINHAND));
            worldIn.playSound(null, playerIn.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE, SoundCategory.PLAYERS, 1f, 1.45f);

            return ActionResult.consume(stack);
        }
        //swings hand
        return ActionResult.success(stack);
    }

    @Override
    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
        CompoundNBT c = stack.getTagElement("Enchantments");
        String s  = target.getType().getRegistryName().toString();
        if((c==null||!c.contains("Pet")) && (target instanceof TameableEntity && ((TameableEntity) target).isTame() &&
                ((TameableEntity) target).getOwnerUUID().equals(playerIn.getUUID())) ||
                ServerConfigs.cached.FLUTE_EXTRA_MOBS.contains(target.getType().getRegistryName().toString())) {
            if(target instanceof AbstractHorseEntity && !((AbstractHorseEntity) target).isTamed())return ActionResultType.PASS;
            //if(target instanceof FoxEntity && ! ((FoxEntity)target).isTrustedUUID(p_213497_1_.getUniqueID())return ActionResultType.PASS;
            CompoundNBT com = new CompoundNBT();
            com.putString("Name", target.getName().getString());
            com.putUUID("UUID", target.getUUID());
            com.putInt("ID", target.getId());
            CompoundNBT com2 = new CompoundNBT();
            com2.put("Pet",com);

            stack.addTagElement("Enchantments",com2);
            playerIn.setItemInHand(hand, stack);
            playerIn.getCooldowns().addCooldown(this, 20);
            return ActionResultType.sidedSuccess(playerIn.level.isClientSide);
        }
        return super.interactLivingEntity(stack, playerIn, target, hand);
    }


    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getTagElement("Enchantments");
            if (compoundnbt !=null && compoundnbt.contains("Pet")){
                CompoundNBT com = compoundnbt.getCompound("Pet");
                tooltip.add(new StringTextComponent(com.getString("Name")).withStyle(TextFormatting.GRAY));
            }

    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 60;
    }


}
