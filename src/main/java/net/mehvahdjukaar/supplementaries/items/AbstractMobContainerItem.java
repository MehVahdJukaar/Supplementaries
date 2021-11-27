package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.common.mobholder.MobContainer;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
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

public abstract class AbstractMobContainerItem extends BlockItem {

    private final float mobContainerHeight;
    private final float mobContainerWidth;
    //used for containers that like jars have custom renderer for fishies
    private final boolean isAquarium;

    public AbstractMobContainerItem(Block block, Properties properties, float width, float height, boolean aquarium) {
        super(block, properties);
        this.mobContainerWidth = width;
        this.mobContainerHeight = height;
        this.isAquarium = aquarium;
    }

    public float getMobContainerHeight() {
        return mobContainerHeight;
    }

    public float getMobContainerWidth() {
        return mobContainerWidth;
    }

    protected boolean canFitEntity(Entity e) {
        float margin = 0.125f;
        float h = e.getBbHeight() - margin;
        float w = e.getBbWidth() - margin;
        return w < this.mobContainerWidth && h < mobContainerHeight;
    }

    public void playCatchSound(PlayerEntity player) {
    }

    public void playFailSound(PlayerEntity player) {
    }

    public void playReleaseSound(World world, Vector3d v) {
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return this.isFull(stack) ? 1 : super.getItemStackLimit(stack);
    }

    public boolean isFull(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.contains("BlockEntityTag");
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


    //TODO: merge
    //immediately discards pets and not alive entities
    protected final boolean isEntityValid(Entity e, PlayerEntity player) {
        if (!e.isAlive() || (e instanceof LivingEntity && ((LivingEntity) e).isDeadOrDying())) return false;

        if (e instanceof TameableEntity) {
            TameableEntity pet = ((TameableEntity) e);
            return !pet.isTame() || pet.isOwnedBy(player);
        }
        return true;
    }

    //2
    private <T extends Entity> boolean canCatch(T e) {
        String name = e.getType().getRegistryName().toString();

        if (ServerConfigs.cached.CAGE_ALL_MOBS || CapturedMobsHelper.COMMAND_MOBS.contains(name)) {
            return true;
        }
        ICatchableMob cap = MobContainer.getCap(e);
        return cap.canBeCaughtWithItem(this);
    }

    /**
     * condition specific to the item. called from mob holder cap
     */
    //4
    public abstract boolean canItemCatch(Entity e);

    /**
     * returns an item stack that contains the mob
     *
     * @param entity       mob
     * @param currentStack holder item
     * @param bucketStack  optional filled bucket item
     * @return full item stack
     */
    public ItemStack captureEntityInItem(Entity entity, ItemStack currentStack, ItemStack bucketStack) {
        ItemStack returnStack = new ItemStack(this);
        if (currentStack.hasCustomHoverName()) returnStack.setHoverName(currentStack.getHoverName());

        CompoundNBT cmp = MobContainer.createMobHolderItemTag(entity, this.getMobContainerWidth(), this.getMobContainerHeight(),
                bucketStack, this.isAquarium);
        if (cmp != null) returnStack.addTagElement("BlockEntityTag", cmp);
        return returnStack;
    }

    //TODO: delegate to mobHolder
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
                ItemStack bucketStack = ItemStack.of(com.getCompound("BucketHolder").getCompound("Bucket"));
                if (bucketStack.getItem() instanceof BucketItem) {
                    ((BucketItem) bucketStack.getItem()).checkExtraContent(world, bucketStack, context.getClickedPos());
                    success = true;
                }
            } else if (com.contains("MobHolder")) {
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
                if (!world.isClientSide) {
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
                }
            }
        }
        tooltip.add(new TranslationTextComponent("message.supplementaries.cage").withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
    }

    private void angerNearbyEntities(Entity entity, PlayerEntity player) {
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

    //1
    public ActionResultType doInteract(ItemStack stack, PlayerEntity player, Entity entity, Hand hand) {

        if (this.isEntityValid(entity, player)) {
            ItemStack bucket = ItemStack.EMPTY;
            //try getting a filled bucket for any water mobs for aquariums and only catchable for others
            if (entity instanceof WaterMobEntity && (this.isAquarium || this.canCatch(entity))) {
                bucket = this.tryGettingFishBucket(player, entity, hand);
            }
            if (!bucket.isEmpty() || this.canCatch(entity)) {
                entity.revive();
                //return for client
                if (player.level.isClientSide) return ActionResultType.SUCCESS;

                this.playCatchSound(player);
                this.angerNearbyEntities(entity, player);



                if (entity instanceof MobEntity) {
                    ((MobEntity)entity).setPersistenceRequired();
                    ((MobEntity)entity).dropLeash(true, !player.abilities.instabuild);
                }


                Utils.swapItemNBT(player, hand, stack, this.captureEntityInItem(entity, stack, bucket));

                entity.remove();
                return ActionResultType.CONSUME;
            }
        }
        this.playFailSound(player);
        return ActionResultType.PASS;
    }

    /**
     * try catching a mob with a water or empty bucket to then store it in the mob holder
     *
     * @return filled bucket stack or empty stack
     */
    private ItemStack tryGettingFishBucket(PlayerEntity player, Entity entity, Hand hand) {
        ItemStack heldItem = player.getItemInHand(hand).copy();

        ItemStack bucket = ItemStack.EMPTY;
        //hax incoming
        player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
        ActionResultType result = entity.interact(player, hand);
        if (!result.consumesAction()) {
            player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            result = entity.interact(player, hand);
        }

        if (result.consumesAction()) {
            ItemStack filledBucket = player.getItemInHand(hand);
            if (filledBucket != heldItem && !entity.isAlive()) {
                bucket = filledBucket;
            }
        }
        //hax
        player.setItemInHand(hand, heldItem);
        player.startUsingItem(hand);
        return bucket;
    }


    //cancel block placement when not shifting
    @Override
    public ActionResultType place(BlockItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            return super.place(context);
        }
        return ActionResultType.PASS;
    }


}
