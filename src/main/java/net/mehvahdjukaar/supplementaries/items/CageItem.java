package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.tiles.CageBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.MobHolder;
import net.mehvahdjukaar.supplementaries.common.CagedMobHelper;
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


    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");
        if (compoundnbt != null) {
            CompoundNBT com = compoundnbt.getCompound("MobHolder");
            if(com==null||com.isEmpty()) com = compoundnbt.getCompound("BucketHolder");
            if (com != null) {
                if (com.contains("Name")) {
                    tooltip.add(new StringTextComponent(com.getString("Name")).withStyle(TextFormatting.GRAY));
                    if(!ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().options.advancedItemTooltips)return;
                    tooltip.add(new TranslationTextComponent("message.supplementaries.cage").withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
                }
            }
        }
        else{
            CompoundNBT c = stack.getTag();
            if(c!=null&&(c.contains("JarMob")||c.contains("CachedJarMobValues")))
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
                            boolean fail = world.addFreshEntity(entity);
                            if (!fail) Supplementaries.LOGGER.warn("Failed to release caged mob");
                        }
                        //TODO fix sound categories
                    }
                    CagedMobHelper.addMob(entity);
                    success = true;
                }
            }
            if(success) {
                world.playSound(null, v.x(), v.y(), v.z(), SoundEvents.CHICKEN_EGG, SoundCategory.PLAYERS, 1, 0.05f);
                if (!player.isCreative()) {
                    ItemStack returnItem = new ItemStack(empty.get());
                    if (stack.hasCustomHoverName()) returnItem.setHoverName(stack.getHoverName());
                    context.getPlayer().setItemInHand(context.getHand(), returnItem);
                }
                return ActionResultType.sidedSuccess(world.isClientSide);
            }
        }
        return super.useOn(context);
    }



    //remove this in the future. it's for backwards compat
    @Override
    public ActionResultType place(BlockItemUseContext context) {
        ActionResultType placeresult = super.place(context);
        if(placeresult.consumesAction()) {
            World world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            TileEntity te = world.getBlockEntity(pos);
            if(te instanceof CageBlockTile){
                CageBlockTile mobjar = ((CageBlockTile)te);
                CompoundNBT compound = context.getItemInHand().getTag();
                if(compound!=null&&compound.contains("JarMob")&&compound.contains("CachedJarMobValues")) {
                    CompoundNBT com2 = compound.getCompound("CachedJarMobValues");

                    mobjar.mobHolder.entityData = compound.getCompound("JarMob");
                    mobjar.mobHolder.yOffset = com2.getFloat("YOffset");
                    mobjar.mobHolder.scale = com2.getFloat("Scale");
                    mobjar.mobHolder.specialBehaviorType = MobHolder.SpecialBehaviorType.NONE;
                    mobjar.mobHolder.name="reload needed";

                    mobjar.setChanged();
                    //mobjar.updateMob();

                }
            }
        }
        return placeresult;
    }


}
