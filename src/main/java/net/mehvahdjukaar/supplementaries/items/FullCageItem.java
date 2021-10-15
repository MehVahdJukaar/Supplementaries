package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class FullCageItem extends BlockItem {
    public final Supplier<Item> empty;
    public FullCageItem(Block blockIn, Properties properties, Supplier<Item> empty) {
        super(blockIn, properties);
        this.empty = empty;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

        if(entityIn instanceof Player){
            ItemStack newStack = new ItemStack(empty.get(), stack.getCount());
            newStack.setTag(stack.getTag());
            ((Player)entityIn).inventory.setItem(itemSlot, newStack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(new TextComponent("pick me up").withStyle(ChatFormatting.DARK_PURPLE));
        CompoundTag compoundnbt = stack.getTagElement("BlockEntityTag");
        if (compoundnbt != null) {
            CompoundTag com = compoundnbt.getCompound("MobHolder");
            if (com == null || com.isEmpty()) com = compoundnbt.getCompound("BucketHolder");
            if (com != null) {
                if (com.contains("Name")) {
                    tooltip.add(new TextComponent(com.getString("Name")).withStyle(ChatFormatting.GRAY));
                    if (!ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().options.advancedItemTooltips)
                        return;
                }
            }
        } else {
            CompoundTag c = stack.getTag();
            if (c != null && (c.contains("JarMob") || c.contains("CachedJarMobValues")))
                tooltip.add(new TextComponent("try placing me down").withStyle(ChatFormatting.GRAY));
        }
    }

    //free mob
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        CompoundTag com = stack.getTagElement("BlockEntityTag");
        Player player = context.getPlayer();
        if(!context.getPlayer().isShiftKeyDown() && com!=null){
            //TODO: add other case
            boolean success = false;
            Level world = context.getLevel();
            Vec3 v = context.getClickLocation();
            if(com.contains("BucketHolder")){
                ItemStack bucketStack = ItemStack.of(com.getCompound("BucketHolder"));
                if(bucketStack.getItem() instanceof BucketItem){
                    ((BucketItem) bucketStack.getItem()).checkExtraContent(world,bucketStack,context.getClickedPos());
                    success = true;
                }
            }
            else if(com.contains("MobHolder")) {
                CompoundTag nbt = com.getCompound("MobHolder");
                Entity entity = EntityType.loadEntityRecursive(nbt.getCompound("EntityData"), world, o -> o);
                if (entity != null) {

                    success = true;
                    if (!world.isClientSide) {
                        //anger entity
                        if (!player.isCreative() && entity instanceof NeutralMob) {
                            NeutralMob ang = (NeutralMob) entity;
                            ang.forgetCurrentTargetAndRefreshUniversalAnger();
                            ang.setPersistentAngerTarget(player.getUUID());
                            ang.setLastHurtByMob(player);
                        }
                        entity.absMoveTo(v.x(), v.y(), v.z(), context.getRotation(), 0);

                        UUID temp = entity.getUUID();
                        if (nbt.contains("UUID")) {
                            UUID id = nbt.getUUID("UUID");
                            entity.setUUID(id);
                        }
                        if (!world.addFreshEntity(entity)) {
                            //spawn failed, reverting to old UUID
                            entity.setUUID(temp);
                            success = world.addFreshEntity(entity);
                            if (!success) Supplementaries.LOGGER.warn("Failed to release caged mob");
                        }
                        //TODO fix sound categories
                    }
                    //create new uuid for creative itemstack
                    if(player.isCreative()){
                        if (nbt.contains("UUID")) {
                            nbt.putUUID("UUID", Mth.createInsecureUUID(random));
                        }
                    }
                }
            }
            if(success) {
                world.playSound(null, v.x(), v.y(), v.z(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1, 0.05f);
                if (!player.isCreative()) {
                    ItemStack returnItem = new ItemStack(empty.get());
                    if (stack.hasCustomHoverName()) returnItem.setHoverName(stack.getHoverName());
                    Utils.swapItemNBT(player, context.getHand(), stack, new ItemStack(empty.get()));
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return super.useOn(context);
    }

}
