package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item.Properties;

public class Flute extends Item {
    public Flute(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag compoundnbt = stack.getTagElement("Enchantments");
        return compoundnbt != null && (compoundnbt.contains("Pet") || super.isFoil(stack));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if(!(entity instanceof LivingEntity)||player.getUsedItemHand()==null)return false;
        return this.interactLivingEntity(stack,player, ((LivingEntity) entity),player.getUsedItemHand()).consumesAction();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (!worldIn.isClientSide) {
            double x = playerIn.getX();
            double y = playerIn.getY();
            double z = playerIn.getZ();
            int r=ServerConfigs.cached.FLUTE_RADIUS;
            CompoundTag com1 = stack.getTagElement("Enchantments");
            if(com1!=null&&com1.contains("Pet")){
                CompoundTag com = com1.getCompound("Pet");
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
                AABB bb = new AABB(x - r, y - r, z - r, x + r, y + r, z + r);
                List<Entity> entities = worldIn.getEntities(playerIn, bb, (e) -> e instanceof TamableAnimal);
                for (Entity e : entities) {
                    TamableAnimal pet = ((TamableAnimal) e);
                    if (pet.isTame() && !pet.isOrderedToSit() && pet.getOwnerUUID().equals(playerIn.getUUID())) {
                        pet.randomTeleport(x, y, z, false);
                    }
                }
            }


            playerIn.getCooldowns().addCooldown(this, 20);
            stack.hurtAndBreak(1, playerIn, (en) -> en.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            worldIn.playSound(null, playerIn.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE, SoundSource.PLAYERS, 1f, 1.30f+random.nextFloat()*0.3f);

            return InteractionResultHolder.consume(stack);
        }
        //swings hand
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
        CompoundTag c = stack.getTagElement("Enchantments");
        String s  = target.getType().getRegistryName().toString();
        if((c==null||!c.contains("Pet")) && (target instanceof TamableAnimal && ((TamableAnimal) target).isTame() &&
                ((TamableAnimal) target).getOwnerUUID().equals(playerIn.getUUID())) ||
                target.getType().is(ModTags.FLUTE_PET)) {
            if(target instanceof AbstractHorse && !((AbstractHorse) target).isTamed())return InteractionResult.PASS;
            //if(target instanceof FoxEntity && ! ((FoxEntity)target).isTrustedUUID(p_213497_1_.getUniqueID())return ActionResultType.PASS;
            CompoundTag com = new CompoundTag();
            com.putString("Name", target.getName().getString());
            com.putUUID("UUID", target.getUUID());
            com.putInt("ID", target.getId());
            CompoundTag com2 = new CompoundTag();
            com2.put("Pet",com);

            stack.addTagElement("Enchantments",com2);
            playerIn.setItemInHand(hand, stack);
            playerIn.getCooldowns().addCooldown(this, 20);
            return InteractionResult.sidedSuccess(playerIn.level.isClientSide);
        }
        return super.interactLivingEntity(stack, playerIn, target, hand);
    }


    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CompoundTag compoundnbt = stack.getTagElement("Enchantments");
            if (compoundnbt !=null && compoundnbt.contains("Pet")){
                CompoundTag com = compoundnbt.getCompound("Pet");
                tooltip.add(new TextComponent(com.getString("Name")).withStyle(ChatFormatting.GRAY));
            }

    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 60;
    }


}
