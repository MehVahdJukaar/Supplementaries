package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.capabilities.mobholder.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.capabilities.mobholder.MobContainer;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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

    public void playCatchSound(Player player) {
    }

    public void playFailSound(Player player) {
    }

    public void playReleaseSound(Level world, Vec3 v) {
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return this.isFull(stack) ? 1 : super.getItemStackLimit(stack);
    }

    public boolean isFull(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("BlockEntityTag");
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (this.isFull(stack)) return InteractionResult.PASS;
        return this.doInteract(stack, player, entity, hand);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (this.isFull(stack)) return false;
        InteractionHand hand = player.getUsedItemHand();
        if (hand == InteractionHand.OFF_HAND) return false;

        return this.doInteract(stack, player, entity, player.getUsedItemHand()).consumesAction();
    }

    //TODO: merge
    //immediately discards pets and not alive entities
    protected final boolean isEntityValid(Entity e, Player player) {
        if (!e.isAlive() || (e instanceof LivingEntity && ((LivingEntity) e).isDeadOrDying())) return false;

        if (e instanceof TamableAnimal pet) {
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

        CompoundTag cmp = MobContainer.createMobHolderItemTag(entity, this.getMobContainerWidth(), this.getMobContainerHeight(),
                bucketStack, this.isAquarium);
        if (cmp != null) returnStack.addTagElement("BlockEntityTag", cmp);
        return returnStack;
    }

    //TODO: delegate to mobHolder
    //free mob
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        CompoundTag com = stack.getTagElement("BlockEntityTag");
        Player player = context.getPlayer();
        if (!context.getPlayer().isShiftKeyDown() && com != null) {
            //TODO: add other case
            boolean success = false;
            Level world = context.getLevel();
            Vec3 v = context.getClickLocation();
            if (com.contains("BucketHolder")) {
                ItemStack bucketStack = ItemStack.of(com.getCompound("BucketHolder").getCompound("Bucket"));
                if (bucketStack.getItem() instanceof BucketItem) {
                    ((BucketItem) bucketStack.getItem()).checkExtraContent(player, world, bucketStack, context.getClickedPos());
                    success = true;
                }
            } else if (com.contains("MobHolder")) {
                CompoundTag nbt = com.getCompound("MobHolder");
                Entity entity = EntityType.loadEntityRecursive(nbt.getCompound("EntityData"), world, o -> o);
                if (entity != null) {

                    success = true;
                    if (!world.isClientSide) {
                        //anger entity
                        if (!player.isCreative() && entity instanceof NeutralMob ang) {
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
                            nbt.putUUID("UUID", Mth.createInsecureUUID(world.random));
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
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        if (tag != null) {
            CompoundTag com = tag.getCompound("MobHolder");
            if (com.isEmpty()) com = tag.getCompound("BucketHolder");
            if (com.contains("Name")) {
                tooltip.add(new TextComponent(com.getString("Name")).withStyle(ChatFormatting.GRAY));
            }
        }
        tooltip.add(new TranslatableComponent("message.supplementaries.cage").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
    }

    private void angerNearbyEntities(Entity entity, Player player) {
        //anger entities
        if (entity instanceof NeutralMob && entity instanceof Mob) {
            getEntitiesInRange((Mob) entity).stream()
                    .filter((mob) -> mob != entity).map(
                            (mob) -> (NeutralMob) mob).forEach((mob) -> {
                        mob.forgetCurrentTargetAndRefreshUniversalAnger();
                        mob.setPersistentAngerTarget(player.getUUID());
                        mob.setLastHurtByMob(player);
                    });
        }
        //piglin workaround. don't know why they are IAngerable
        if (entity instanceof Piglin) {
            entity.hurt(DamageSource.playerAttack(player), 0);
        }
    }

    private static List<?> getEntitiesInRange(Mob e) {
        double d0 = e.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB aabb = AABB.unitCubeFromLowerCorner(e.position()).inflate(d0, 10.0D, d0);
        return e.level.getEntitiesOfClass(e.getClass(), aabb, EntitySelector.NO_SPECTATORS);
    }

    /**
     * interact with an entity to catch it
     */
    public InteractionResult doInteract(ItemStack stack, Player player, Entity entity, InteractionHand hand) {

        if (this.isEntityValid(entity, player)) {
            ItemStack bucket = ItemStack.EMPTY;
            //try getting a filled bucket for any water mobs for aquariums and only catchable for others
            if (entity instanceof WaterAnimal && (this.isAquarium || this.canCatch(entity))) {
                bucket = this.tryGettingFishBucket(player, entity, hand);
            }
            if (!bucket.isEmpty() || this.canCatch(entity)) {
                entity.revive();
                //return for client
                if (player.level.isClientSide) return InteractionResult.SUCCESS;

                this.playCatchSound(player);
                this.angerNearbyEntities(entity, player);

                Utils.swapItemNBT(player, hand, stack, this.captureEntityInItem(entity, stack, bucket));

                entity.remove(Entity.RemovalReason.DISCARDED);
                return InteractionResult.CONSUME;
            }
        }
        this.playFailSound(player);
        return InteractionResult.PASS;
    }

    /**
     * try catching a mob with a water or empty bucket to then store it in the mob holder
     *
     * @return filled bucket stack or empty stack
     */
    private ItemStack tryGettingFishBucket(Player player, Entity entity, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand).copy();

        ItemStack bucket = ItemStack.EMPTY;
        //hax incoming
        player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
        InteractionResult result = entity.interact(player, hand);
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
    public InteractionResult place(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            return super.place(context);
        }
        return InteractionResult.PASS;
    }
}
