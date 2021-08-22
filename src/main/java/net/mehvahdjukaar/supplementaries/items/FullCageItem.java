package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
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

public class FullCageItem extends BlockItem {
    public final Supplier<Item> empty;
    public FullCageItem(Block blockIn, Properties properties, Supplier<Item> empty) {
        super(blockIn, properties);
        this.empty = empty;
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

        if(entityIn instanceof PlayerEntity){
            ItemStack newStack = new ItemStack(empty.get(), stack.getCount());
            newStack.setTag(stack.getTag());
            ((PlayerEntity)entityIn).inventory.setItem(itemSlot, newStack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");
        if (compoundnbt != null) {
            CompoundNBT com = compoundnbt.getCompound("MobHolder");
            if (com == null || com.isEmpty()) com = compoundnbt.getCompound("BucketHolder");
            if (com != null) {
                if (com.contains("Name")) {
                    tooltip.add(new StringTextComponent(com.getString("Name")).withStyle(TextFormatting.GRAY));
                    if (!ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().options.advancedItemTooltips)
                        return;
                    tooltip.add(new TranslationTextComponent("message.supplementaries.cage").withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
                }
            }
        } else {
            CompoundNBT c = stack.getTag();
            if (c != null && (c.contains("JarMob") || c.contains("CachedJarMobValues")))
                tooltip.add(new StringTextComponent("try placing me down").withStyle(TextFormatting.GRAY));
        }
    }

    //free mob
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        ItemStack stack = context.getItemInHand();
        CompoundNBT com = stack.getTagElement("BlockEntityTag");
        PlayerEntity player = context.getPlayer();
        if(!context.getPlayer().isShiftKeyDown() && com!=null){
            //TODO: add other case
            boolean success = false;
            World world = context.getLevel();
            Vector3d v = context.getClickLocation();
            if(com.contains("BucketHolder")){
                ItemStack bucketStack = ItemStack.of(com.getCompound("BucketHolder"));
                if(bucketStack.getItem() instanceof BucketItem){
                    ((BucketItem) bucketStack.getItem()).checkExtraContent(world,bucketStack,context.getClickedPos());
                    success = true;
                }
            }
            else if(com.contains("MobHolder")) {
                CompoundNBT nbt = com.getCompound("MobHolder");
                Entity entity = EntityType.loadEntityRecursive(nbt.getCompound("EntityData"), world, o -> o);
                if (entity != null) {

                    success = true;
                    if (!world.isClientSide) {
                        //anger entity
                        if (!player.isCreative() && entity instanceof IAngerable) {
                            IAngerable ang = (IAngerable) entity;
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
                            nbt.putUUID("UUID", MathHelper.createInsecureUUID(random));
                        }
                    }
                }
            }
            if(success) {
                world.playSound(null, v.x(), v.y(), v.z(), SoundEvents.CHICKEN_EGG, SoundCategory.PLAYERS, 1, 0.05f);
                if (!player.isCreative()) {
                    ItemStack returnItem = new ItemStack(empty.get());
                    if (stack.hasCustomHoverName()) returnItem.setHoverName(stack.getHoverName());
                    Utils.swapItemNBT(player, context.getHand(), stack, new ItemStack(empty.get()));
                }
                return ActionResultType.sidedSuccess(world.isClientSide);
            }
        }
        return super.useOn(context);
    }

}
