package net.mehvahdjukaar.supplementaries.common.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.common.items.components.MobContainerView;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.BucketHelper;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.CapturedMobHandler;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.MobContainer;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractMobContainerItem extends BlockItem {

    private final float mobContainerHeight;
    private final float mobContainerWidth;
    //used for containers that like jars have custom renderer for fishies
    private final boolean isAquarium;

    protected AbstractMobContainerItem(Block block, Properties properties, float width, float height, boolean aquarium) {
        super(block, properties);
        this.mobContainerWidth = width;
        this.mobContainerHeight = height;
        this.isAquarium = aquarium;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    //if it can hold and display liquids
    public boolean isAquarium() {
        return isAquarium;
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

    @ForgeOverride
    public int getMaxStackSize(ItemStack stack) {
        return this.isFull(stack) ? 1 : 64;
    }

    public boolean isFull(ItemStack stack) {
        return stack.has(ModComponents.MOB_HOLDER_CONTENT.get());
    }

    //called from event for better compat
    /*
    @Override
    @PlatformOnly(PlatformOnly.FABRIC)
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (this.isFull(stack)) return InteractionResult.PASS;
        return this.doInteract(stack, player, entity, hand);
    }*/

    //@Override
    @ForgeOverride
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (this.isFull(stack)) return false;
        InteractionHand hand = player.getUsedItemHand();
        if (hand == InteractionHand.OFF_HAND) return false;

        return this.doInteract(stack, player, entity, player.getUsedItemHand()).consumesAction();
    }

    //1
    private <T extends Entity> boolean canCatch(T entity, Player player) {
        //immediately discards pets and not living entities as well as players
        if (!entity.isAlive() || entity instanceof Player) return false;

        String name = Utils.getID(entity.getType()).toString();
        if (CommonConfigs.Functional.CAGE_ALL_MOBS.get() || CapturedMobHandler.getInstance(entity.level()).isCommandMob(name)) {
            return true;
        }

        if (entity instanceof LivingEntity living) {
            if (living.isDeadOrDying()) return false;
        }

        if (entity.getType().is(ModTags.CAPTURE_BLACKLIST)) return false;

        // If people want to catch these, so be it. All hardcoded checks are below the global config
        if (ForgeHelper.isMultipartEntity(entity)) return false;
        ICatchableMob cap = CapturedMobHandler.INSTANCE.getCatchableMobCapOrDefault(entity);

        // this calls can ItemCatch for default or let's full control for custom ones
        return cap.canBeCaughtWithItem(entity, this, player);
    }

    /**
     * condition specific to the item. called from mob catchable mob cap
     */
    //3
    public abstract boolean canItemCatch(Entity e);

    /**
     * returns an item stack that contains the mob
     *
     * @param entity       mob
     * @param currentStack holder item
     * @param bucketStack  optional filled bucket item
     * @return new full item stack if it was successful, same otherwise
     */
    public ItemStack saveEntityInItem(Entity entity, ItemStack currentStack, ItemStack bucketStack) {
        ItemStack returnStack = currentStack.copy();
        MobContainer mobContainer = new MobContainer(this.getMobContainerWidth(), this.getMobContainerHeight(), this.isAquarium);
        boolean success = mobContainer.captureEntity(entity, bucketStack);
        if (success) {
            returnStack.set(ModComponents.MOB_HOLDER_CONTENT.get(), MobContainerView.of(mobContainer));
            return returnStack;
        }
        return currentStack;
    }

    //TODO: delegate to mobHolder
    //free mob
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        MobContainerView content = stack.get(ModComponents.MOB_HOLDER_CONTENT.get());
        Player player = context.getPlayer();
        if (!context.getPlayer().isShiftKeyDown() && content != null) {
            //TODO: add other case
            var data = content.getDataUnsafe();
            boolean success = false;
            Level world = context.getLevel();
            Vec3 v = context.getClickLocation();
            if (data instanceof MobContainer.MobData.Bucket b) {
                ItemStack bucketStack = b.getBucket();
                if (bucketStack.getItem() instanceof BucketItem bi) {
                    bi.checkExtraContent(player, world, bucketStack.copy(), context.getClickedPos());
                    success = true;
                }
            } else if (data instanceof MobContainer.MobData.Entity e) {
                Entity entity = EntityType.loadEntityRecursive(e.getTag(), world, o -> o);
                if (entity != null) {

                    success = true;
                    if (!world.isClientSide) {
                        //anger entity
                        if (!player.isCreative() && entity instanceof NeutralMob ang && !entity.getType().is(ModTags.NON_ANGERABLE)) {
                            ang.forgetCurrentTargetAndRefreshUniversalAnger();
                            ang.setPersistentAngerTarget(player.getUUID());
                            ang.setLastHurtByMob(player);
                        }
                        entity.absMoveTo(v.x(), v.y(), v.z(), context.getRotation(), 0);

                        if (CommonConfigs.Functional.CAGE_PERSISTENT_MOBS.get() && entity instanceof Mob mob) {
                            mob.setPersistenceRequired();
                        }

                        UUID temp = entity.getUUID();
                        UUID dataUUID = e.getUuid();
                        if (dataUUID != null) {
                            entity.setUUID(dataUUID);
                        }
                        if (!world.addFreshEntity(entity)) {
                            //spawn failed, reverting to old UUID
                            entity.setUUID(temp);
                            success = world.addFreshEntity(entity);
                            if (!success) Supplementaries.LOGGER.warn("Failed to release caged mob");
                        }
                        //TODO fix sound categories
                    }
                    //create new uuid for creative itemStack
                    if (player.isCreative() && e.getUuid() != null) {
                        stack.set(ModComponents.MOB_HOLDER_CONTENT.get(),
                                content.copyWithNewUUID(Mth.createInsecureUUID(world.random)));
                    }
                } else Supplementaries.LOGGER.error("Failed to load entity from itemstack");
            }
            if (success) {
                if (!world.isClientSide) {
                    this.playReleaseSound(world, v);
                    if (!player.hasInfiniteMaterials()) {
                        ItemStack returnItem = stack.copyWithCount(1);
                        returnItem.remove(ModComponents.MOB_HOLDER_CONTENT.get());
                        Utils.swapItemNBT(player, context.getHand(), stack, returnItem);
                    }
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return super.useOn(context);
    }

    public boolean blocksPlacement() {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        MobContainerView content = stack.get(ModComponents.MOB_HOLDER_CONTENT.get());
        if (content != null) {
            content.addToTooltip(context, tooltipComponents::add, tooltipFlag);
        }
        if (MiscUtils.showsHints(tooltipFlag)) {
            this.addPlacementTooltip(tooltipComponents);
        }
    }

    @Environment(EnvType.CLIENT)
    public void addPlacementTooltip(List<Component> tooltip) {
        tooltip.add(Component.translatable("message.supplementaries.cage.tooltip",
                        Minecraft.getInstance().options.keyShift.getTranslatedKeyMessage())
                .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
    }

    private void angerNearbyEntities(Entity entity, Player player) {
        //anger entities
        if (entity instanceof NeutralMob && entity instanceof Mob) {
            getEntitiesInRange((Mob) entity).stream()
                    .filter((mob) -> mob != entity).map(
                            NeutralMob.class::cast).forEach((mob) -> {
                        mob.forgetCurrentTargetAndRefreshUniversalAnger();
                        mob.setPersistentAngerTarget(player.getUUID());
                        mob.setLastHurtByMob(player);
                    });
        }
        Level level = entity.level();
//TODO rewrite
        //piglin workaround. don't know why they are IAngerable
        if (entity instanceof Piglin) {
            entity.hurt(level.damageSources().playerAttack(player), 0);
        }
        if (entity instanceof Villager villager && level instanceof ServerLevel serverLevel) {
            Optional<NearestVisibleLivingEntities> optional = villager.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
            optional.ifPresent(entities -> entities.findAll(ReputationEventHandler.class::isInstance).forEach((e) ->
                    serverLevel.onReputationEvent(ReputationEventType.VILLAGER_HURT, player, (ReputationEventHandler) e)));
        }
    }

    private static List<?> getEntitiesInRange(Mob e) {
        double d0 = e.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB aabb = AABB.unitCubeFromLowerCorner(e.position()).inflate(d0, 10.0D, d0);
        return e.level().getEntitiesOfClass(e.getClass(), aabb, EntitySelector.NO_SPECTATORS);
    }

    /**
     * interact with an entity to catch it
     */
    public InteractionResult doInteract(ItemStack stack, Player player, Entity entity, InteractionHand hand) {
        if (hand == null) {
            return InteractionResult.PASS;
        }
        boolean canCatch = this.canCatch(entity, player);
        MutableComponent failedMessage = Component.translatable("message.supplementaries.cage.fail");

        if (canCatch && entity instanceof LivingEntity le) {
            if (CommonConfigs.Functional.CAGE_TAMED.get() && entity instanceof TamableAnimal pet && (!pet.isTame() || !pet.isOwnedBy(player))) {
                failedMessage = Component.translatable("message.supplementaries.cage.fail_tamed");
                canCatch = false;
            }
            int percentage = CommonConfigs.Functional.CAGE_HEALTH_THRESHOLD.get();
            if (percentage != 100 && (le.getHealth() > le.getMaxHealth() * (percentage / 100f))) {
                failedMessage = Component.translatable("message.supplementaries.cage.fail_health", percentage);
                canCatch = false;
            }
        }


        if (canCatch) {
            ItemStack bucket = ItemStack.EMPTY;
            //try getting a filled bucket for any water mobs for aquariums and only catchable for others

            if (this.isAquarium) {
                bucket = BucketHelper.getBucketFromEntity(entity);
            }
            //not ideal here...
            if (CommonConfigs.Functional.CAGE_PERSISTENT_MOBS.get() && entity instanceof Mob mob) {
                mob.setPersistenceRequired();
            }

            ItemStack newItem = this.saveEntityInItem(entity, stack, bucket);
            if (newItem == stack) return InteractionResult.FAIL;

            this.playCatchSound(player);
            //return for client
            if (player.level().isClientSide) return InteractionResult.SUCCESS;

            Utils.swapItemNBT(player, hand, stack, newItem);

            this.angerNearbyEntities(entity, player);

            if (entity instanceof Mob mob) {
                mob.dropLeash(true, !player.getAbilities().instabuild);
            }

            entity.remove(Entity.RemovalReason.DISCARDED);
            return InteractionResult.CONSUME;
        } else if (!player.level().isClientSide && entity instanceof LivingEntity) {
            player.displayClientMessage(failedMessage, true);
        }
        this.playFailSound(player);
        return InteractionResult.PASS;
    }


    //cancel block placement when not shifting
    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if ((player != null && !player.isShiftKeyDown()) && this.blocksPlacement()) {
            return InteractionResult.PASS;
        }
        return super.place(context);
    }
}
