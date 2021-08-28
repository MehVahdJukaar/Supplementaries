package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.block.util.MobHolder;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
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

public class CageItem extends BlockItem {

    private final float mobContainerHeight;
    private final float mobContainerWidth;

    public CageItem(Block blockIn, Properties properties) {
        this(blockIn, properties, 0.875f, 1f);
    }

    public CageItem(Block blockIn, Properties properties, float width, float height) {
        super(blockIn, properties);
        this.mobContainerWidth = width;
        this.mobContainerHeight = height;
    }

    public float getMobContainerHeight() {
        return mobContainerHeight;
    }

    public float getMobContainerWidth() {
        return mobContainerWidth;
    }

    private boolean canFitEntity(Entity e){
        float margin = 0.125f;
        float h = e.getBbHeight() - margin;
        float w = e.getBbWidth() - margin;
        return w <  this.mobContainerWidth && h < mobContainerHeight;
    }

    public void playCatchSound(PlayerEntity player){
        player.level.playSound(null, player.blockPosition(), SoundEvents.CHAIN_FALL, SoundCategory.BLOCKS, 1, 0.7f);
    }

    public void playFailSound(PlayerEntity player){

    }

    public void playReleaseSound(World world, Vector3d v){
        world.playSound(null, v.x(), v.y(), v.z(), SoundEvents.CHICKEN_EGG, SoundCategory.PLAYERS, 1, 0.05f);
    }

    public ItemStack getFullItemStack(Entity entity, ItemStack currentStack) {
        ItemStack returnStack = new ItemStack(this);
        if (currentStack.hasCustomHoverName()) returnStack.setHoverName(currentStack.getHoverName());

        CompoundNBT cmp = MobHolder.createMobHolderItemNBT(entity, this.getMobContainerWidth(), this.getMobContainerHeight());
        if (cmp != null) returnStack.addTagElement("BlockEntityTag", cmp);
        return returnStack;
    }

    public boolean isFull(ItemStack stack){
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.contains("BlockEntityTag");
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
         return this.isFull(stack) ? 1 : super.getItemStackLimit(stack);
    }

    @Override
    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (this.isFull(stack)) return ActionResultType.PASS;
        return this.doInteract(stack, player, entity, hand);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        if (this.isFull(stack)) return false;
        Hand hand = player.getUsedItemHand();
        if (hand == null || hand == Hand.OFF_HAND) return false;

        return this.doInteract(stack, player, entity, player.getUsedItemHand()).consumesAction();
    }

    //immediately discards pets and not alive entities
    protected final boolean isEntityValid(Entity e, PlayerEntity player){
        if(!e.isAlive() || (e instanceof LivingEntity && ((LivingEntity) e).isDeadOrDying())) return false;

        if(e instanceof TameableEntity){
            TameableEntity pet = ((TameableEntity) e);
            return !pet.isTame() || pet.isOwnedBy(player);
        }
        return true;
    }

    public ActionResultType doInteract(ItemStack stack, PlayerEntity player, Entity entity, Hand hand) {

        if (this.isEntityValid(entity, player)) {

            if (this.canCatch(entity)) {

                if(entity.isPassenger()){
                    entity.getVehicle().ejectPassengers();
                }

                //return for client
                if (player.level.isClientSide) return ActionResultType.SUCCESS;

                this.playCatchSound(player);

                this.angerNearbyEntities(entity, player);

                Utils.swapItemNBT(player, hand, stack, this.getFullItemStack(entity, stack));

                entity.remove();
                return ActionResultType.CONSUME;
            }
            this.playFailSound(player);
        }
        return ActionResultType.PASS;
    }

    private boolean canCatch(Entity e){
        String name = e.getType().getRegistryName().toString();

        if (ServerConfigs.cached.CAGE_ALL_MOBS) {
            return true;
        }
        if (e instanceof ICatchableMob && ((ICatchableMob) e).canBeCaughtWithItem(this)) {
            return true;
        }
        //only allows small slimes
        if (e instanceof SlimeEntity && ((SlimeEntity) e).getSize() > 1){
            return false;
        }

        if(CapturedMobsHelper.COMMAND_MOBS.contains(name)){
            return true;
        }
        //hardcoding bees to work with resourceful bees
        if (e instanceof BeeEntity) {
            return true;
        }

        return canItemCatch(e);
    }

    /**
     * specific to item. Override
     */
    public boolean canItemCatch(Entity e){
        if(ServerConfigs.cached.CAGE_AUTO_DETECT && this.canFitEntity(e)) return true;

        EntityType<?> type = e.getType();

        boolean isBaby = e instanceof LivingEntity && ((LivingEntity) e).isBaby();
        return ((ServerConfigs.cached.CAGE_ALL_BABIES && isBaby) ||
                type.is(ModTags.CAGE_CATCHABLE) ||
                (type.is(ModTags.CAGE_BABY_CATCHABLE) && isBaby));
    }

    private void angerNearbyEntities(Entity entity, PlayerEntity player){
        //anger entities
        if (entity instanceof IAngerable && entity instanceof MobEntity) {
            getEntitiesInRange((MobEntity) entity).stream()
                    .filter((mob) -> mob != entity).map(
                            (mob) -> (IAngerable) mob).forEach((mob) -> {
                        mob.forgetCurrentTargetAndRefreshUniversalAnger();
                        mob.setPersistentAngerTarget(player.getUUID());
                        mob.setLastHurtByMob(player);
                    });
        }
        //piglin workaround. don't know why they are IAngerable
        if (entity instanceof PiglinEntity) {
            entity.hurt(DamageSource.playerAttack(player), 0);
        }
    }

    private static List<MobEntity> getEntitiesInRange(MobEntity e) {
        double d0 = e.getAttributeValue(Attributes.FOLLOW_RANGE);
        AxisAlignedBB axisalignedbb = AxisAlignedBB.unitCubeFromLowerCorner(e.position()).inflate(d0, 10.0D, d0);
        return e.level.getLoadedEntitiesOfClass(e.getClass(), axisalignedbb);
    }

    //full cage stuff

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
                    if (!ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().options.advancedItemTooltips) return;
                    tooltip.add(new TranslationTextComponent("message.supplementaries.cage").withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
                }
            }
        }
    }

    //free mob
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        ItemStack stack = context.getItemInHand();
        CompoundNBT com = stack.getTagElement("BlockEntityTag");
        PlayerEntity player = context.getPlayer();
        if (!context.getPlayer().isShiftKeyDown() && com != null) {
            //TODO: add other case
            boolean success = false;
            World world = context.getLevel();
            Vector3d v = context.getClickLocation();
            if (com.contains("BucketHolder")) {
                ItemStack bucketStack = ItemStack.of(com.getCompound("BucketHolder"));
                if (bucketStack.getItem() instanceof BucketItem) {
                    ((BucketItem) bucketStack.getItem()).checkExtraContent(world, bucketStack, context.getClickedPos());
                    success = true;
                }
            }
            else if (com.contains("MobHolder")) {
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
                    if (player.isCreative()) {
                        if (nbt.contains("UUID")) {
                            nbt.putUUID("UUID", MathHelper.createInsecureUUID(random));
                        }
                    }
                }
            }
            if (success) {
                if(!world.isClientSide) {
                    this.playReleaseSound(world, v);
                    if (!player.isCreative()) {
                        ItemStack returnItem = new ItemStack(this);
                        if (stack.hasCustomHoverName()) returnItem.setHoverName(stack.getHoverName());
                        Utils.swapItemNBT(player, context.getHand(), stack, returnItem);
                    }
                }
                return ActionResultType.sidedSuccess(world.isClientSide);
            }
        }
        return super.useOn(context);
    }

}
