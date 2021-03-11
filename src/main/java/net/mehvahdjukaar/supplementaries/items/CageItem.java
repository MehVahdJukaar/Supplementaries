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
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getChildTag("BlockEntityTag");
        if (compoundnbt != null) {
            CompoundNBT com = compoundnbt.getCompound("MobHolder");
            if(com==null||com.isEmpty()) com = compoundnbt.getCompound("BucketHolder");
            if (com != null) {
                if (com.contains("Name")) {
                    tooltip.add(new StringTextComponent(com.getString("Name")).mergeStyle(TextFormatting.GRAY));
                    if(!ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().gameSettings.advancedItemTooltips)return;
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
        PlayerEntity player = context.getPlayer();
        if(!context.getPlayer().isSneaking() && com!=null){
            //TODO: add other case
            boolean success = false;
            World world = context.getWorld();
            Vector3d v = context.getHitVec();
            if(com.contains("BucketHolder")){
                ItemStack bucketStack = ItemStack.read(com.getCompound("BucketHolder"));
                if(bucketStack.getItem() instanceof BucketItem){
                    ((BucketItem) bucketStack.getItem()).onLiquidPlaced(world,bucketStack,context.getPos());
                    success = true;
                }
            }
            else if(com.contains("MobHolder")) {
                CompoundNBT nbt = com.getCompound("MobHolder");
                Entity entity = EntityType.loadEntityAndExecute(nbt.getCompound("EntityData"), world, o -> o);
                if (entity != null) {
                    if (!world.isRemote) {
                        //anger entity
                        if (!player.isCreative() && entity instanceof IAngerable) {
                            IAngerable ang = (IAngerable) entity;
                            ang.func_241355_J__();
                            ang.setAngerTarget(player.getUniqueID());
                            ang.setRevengeTarget(player);
                        }
                        entity.setPositionAndRotation(v.getX(), v.getY(), v.getZ(), context.getPlacementYaw(), 0);

                        UUID temp = entity.getUniqueID();
                        if (nbt.contains("UUID")) {
                            UUID id = nbt.getUniqueId("UUID");
                            entity.setUniqueId(id);
                        }
                        if (!world.addEntity(entity)) {
                            //spawn failed, reverting to old UUID
                            entity.setUniqueId(temp);
                            boolean fail = world.addEntity(entity);
                            if (!fail) Supplementaries.LOGGER.warn("Failed to release caged mob");
                        }
                        //TODO fix sound categories
                    }
                    CagedMobHelper.addMob(entity);
                    success = true;
                }
            }
            if(success) {
                world.playSound(null, v.getX(), v.getY(), v.getZ(), SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.PLAYERS, 1, 0.05f);
                if (!player.isCreative()) {
                    ItemStack returnItem = new ItemStack(empty.get());
                    if (stack.hasDisplayName()) returnItem.setDisplayName(stack.getDisplayName());
                    context.getPlayer().setHeldItem(context.getHand(), returnItem);
                }
                return ActionResultType.func_233537_a_(world.isRemote);
            }
        }
        return super.onItemUse(context);
    }



    //remove this in the future. it's for backwards compat
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
                    mobjar.mobHolder.specialBehaviorType = MobHolder.SpecialBehaviorType.NONE;
                    mobjar.mobHolder.name="reload needed";

                    mobjar.markDirty();
                    //mobjar.updateMob();

                }
            }
        }
        return placeresult;
    }


}
