package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.supplementaries.block.util.MobHolder;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFrameItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;
import java.util.function.Supplier;

public class EmptyCageItem extends BlockItem {
    public final Supplier<Item> full;
    public final CageWhitelist cageType;
    public EmptyCageItem(Block blockIn, Properties properties, Supplier<Item> full, CageWhitelist whitelist) {
        super(blockIn, properties);
        this.full = full;
        this.cageType = whitelist;
    }

    private static List<MobEntity> getEntitiesInRange(MobEntity e) {
        double d0 = e.getAttributeValue(Attributes.FOLLOW_RANGE);
        AxisAlignedBB axisalignedbb = AxisAlignedBB.fromVector(e.getPositionVec()).grow(d0, 10.0D, d0);
        return e.world.getLoadedEntitiesWithinAABB(e.getClass(), axisalignedbb);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        if(player.getActiveHand()==null)return false;

        return this.doInteract(stack,player, entity,player.getActiveHand()).isSuccessOrConsume();

    }

    public ActionResultType doInteract(ItemStack stack, PlayerEntity player, Entity entity, Hand hand) {
        ResourceLocation n = entity.getType().getRegistryName();
        if(n==null)return ActionResultType.PASS;
        String name = n.toString();

        boolean isFirefly = false;
        boolean canBeCaught = false;
        switch (this.cageType){
            case CAGE:
                canBeCaught = (ServerConfigs.cached.CAGE_ALL_MOBS ||
                        (entity instanceof LivingEntity && ServerConfigs.cached.CAGE_ALL_BABIES && ((LivingEntity) entity).isChild()) ||
                        ServerConfigs.cached.CAGE_ALLOWED_MOBS.contains(name) ||
                        (entity instanceof LivingEntity && ServerConfigs.cached.CAGE_ALLOWED_BABY_MOBS.contains(name)&&((LivingEntity) entity).isChild()));
                break;
            case JAR:
                isFirefly = entity.getType().getRegistryName().getPath().toLowerCase().contains("firefl");
                canBeCaught = ServerConfigs.cached.CAGE_ALL_MOBS || isFirefly || ServerConfigs.cached.MOB_JAR_ALLOWED_MOBS.contains(name);
                break;
            case TINTED_JAR:
                canBeCaught = ServerConfigs.cached.CAGE_ALL_MOBS || ServerConfigs.cached.MOB_JAR_TINTED_ALLOWED_MOBS.contains(name);
                break;
        }
        if(!canBeCaught)return ActionResultType.PASS;

        if(!entity.isAlive() || (entity instanceof LivingEntity && ((LivingEntity) entity).getShouldBeDead()))return ActionResultType.PASS;
        if(entity instanceof SlimeEntity && ((SlimeEntity)entity).getSlimeSize()>1) return ActionResultType.PASS;

        if(player.world.isRemote)return ActionResultType.SUCCESS;

        ItemStack returnStack = new ItemStack(isFirefly?  Registry.FIREFLY_JAR_ITEM.get() : this.full.get());

        if(!isFirefly) {

            if (stack.hasDisplayName()) returnStack.setDisplayName(stack.getDisplayName());

            CompoundNBT cmp = MobHolder.createMobHolderItemNBT(entity, this.cageType.height, this.cageType.width);
            if(cmp!=null) returnStack.setTagInfo("BlockEntityTag", cmp);
        }

        CommonUtil.swapItem(player,hand,stack,returnStack);
        //TODO: cage sound here
        player.world.playSound(null, player.getPosition(),  SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.BLOCKS,1,1);

        //anger entities
        if(entity instanceof IAngerable && entity instanceof MobEntity){
            getEntitiesInRange((MobEntity) entity).stream()
                    .filter((mob) -> mob != entity).map(
                    (mob) -> (IAngerable)mob).forEach((mob)->{
                mob.func_241355_J__();
                mob.setAngerTarget(player.getUniqueID());
                mob.setRevengeTarget(player);
            });
        }
        //piglin workaround. don't know why they are IAngerable
        if(entity instanceof PiglinEntity){
            entity.attackEntityFrom(DamageSource.causePlayerDamage(player), 0);
        }

        entity.remove();
        return ActionResultType.CONSUME;
    }

    @Override
    public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        return this.doInteract(stack, player, entity, hand);
    }

    //TODO: add whitelist reference in here or in item constructor. maybe ad return item here too
    public enum CageWhitelist{
        CAGE( 1f, 0.875f),
        JAR( 0.875f, 0.625f),
        TINTED_JAR( 0.875f, 0.625f);

        public final float width;
        public final float height;

        CageWhitelist(float blockH, float blockW){
            this.width = blockW;
            this.height = blockH;
        }

        public boolean isJar(){
            return this!=CAGE;
        }
    }

}
