package net.mehvahdjukaar.supplementaries.common.events;


import com.google.common.base.Preconditions;
import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.client.renderers.CapturedMobCache;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AshLayerBlock;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimesManager;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.PlaceableBookManager;
import net.mehvahdjukaar.supplementaries.common.block.tiles.EndermanSkullBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.goals.EatFodderGoal;
import net.mehvahdjukaar.supplementaries.common.entities.goals.EvokerRedMerchantWololooSpellGoal;
import net.mehvahdjukaar.supplementaries.common.entities.goals.ManeuverAndShootCannonGoal;
import net.mehvahdjukaar.supplementaries.common.events.overrides.InteractEventsHandler;
import net.mehvahdjukaar.supplementaries.common.items.*;
import net.mehvahdjukaar.supplementaries.common.items.crafting.WeatheredMapRecipe;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.ColoredMapHandler;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.MapLightHandler;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.CapturedMobHandler;
import net.mehvahdjukaar.supplementaries.common.network.SyncEquippedQuiverPacket;
import net.mehvahdjukaar.supplementaries.common.utils.IQuiverPlayer;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.mehvahdjukaar.supplementaries.common.utils.fake_level.IEntityInterceptFakeLevel;
import net.mehvahdjukaar.supplementaries.common.worldgen.RoadSignStructure;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;


public class ServerEvents {

    @EventCalled
    public static void onFireConsume(IFireConsumeBlockEvent event) {
        if (event.getState().getBlock() instanceof IRopeConnection) {
            LevelAccessor level = event.getLevel();
            BlockPos pos = event.getPos();
            level.removeBlock(pos, false);
            if (BaseFireBlock.canBePlacedAt((Level) level, pos, Direction.DOWN)) {
                BlockState state = BaseFireBlock.getState(level, pos);
                if (state.hasProperty(FireBlock.AGE)) {
                    event.setFinalState(state.setValue(FireBlock.AGE, 8));
                }
                level.scheduleTick(pos, Blocks.FIRE, 2 + ((Level) level).random.nextInt(1));
            }//TODO: make faster
        } else AshLayerBlock.tryConvertToAsh(event);
    }

    //block placement should stay low in priority to allow other more important mod interaction that use the event
    @EventCalled
    public static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isSpectator()) { //is this check even needed?
            return InteractEventsHandler.onItemUsedOnBlock(player, level,
                    player.getItemInHand(hand), hand, hitResult);
        }
        return InteractionResult.PASS;
    }

    @EventCalled
    public static InteractionResult onRightClickBlockHP(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isSpectator()) {
            return InteractEventsHandler.onItemUsedOnBlockHP(player, level,
                    player.getItemInHand(hand), hand, hitResult);
        }
        return InteractionResult.PASS;
    }

    @EventCalled
    public static InteractionResultHolder<ItemStack> onUseItem(Player player, Level level, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isSpectator()) {
            return InteractEventsHandler.onItemUse(player, level, hand, stack);
        }
        return InteractionResultHolder.pass(stack);
    }


    @EventCalled
    public static void onPlayerLoggedIn(ServerPlayer player) {

        VibeChecker.checkVibe(player);
    }

    @EventCalled
    public static InteractionResult onRightClickEntity(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (player.isSpectator()) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        if (item instanceof FluteItem) {
            if (FluteItem.interactWithPet(stack, player, entity, hand)) {
                return InteractionResult.SUCCESS; // we need this for event to be actually cancelled
            }
        } else if (item instanceof AbstractMobContainerItem containerItem) {
            if (!containerItem.isFull(stack)) {
                var res = containerItem.doInteract(stack, player, entity, hand);
                if (res.consumesAction()) {
                    return InteractionResult.SUCCESS;
                }
            }
        } else if (item == ModRegistry.SOAP.get()) {
            if (SoapItem.interactWithEntity(stack, player, entity, hand)) {
                return InteractionResult.SUCCESS;
            }
        } else if (item == Items.CARROT && entity.getType() == EntityType.PUFFERFISH) {
            stack.shrink(1);
            entity.playSound(ModSounds.AEUGH.get());
            ((LivingEntity) entity).heal(2);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @EventCalled
    public static void onServerStopped() {
        if (PlatHelper.getPhysicalSide().isClient()) {
            CapturedMobCache.clear();
        }
        WeatheredMapRecipe.onWorldUnload();
        RoadSignStructure.clearCache();
        EndermanSkullBlockTile.clearCache();
        ColoredMapHandler.clearIdCache();
    }

    @EventCalled
    public static void onServerStart(MinecraftServer server) {
        FaucetBehaviorsManager.reloadWithLevel(server.overworld());
        //compute cache so it doesnt lag later.. because aparelty thats a thing.
        //TODO: figure out why starting this lags
        IEntityInterceptFakeLevel.get(server.overworld());
    }

    @EventCalled
    public static void onDataSyncToPlayer(ServerPlayer player, boolean joined) {
        CapturedMobHandler.sendDataToClient(player);
        HourglassTimesManager.sendDataToClient(player);
        MapLightHandler.sendDataToClient(player);
        PlaceableBookManager.onDataSync(player, joined);
    }

    @EventCalled
    public static void beforeServerStart(RegistryAccess ra) {
        PlaceableBookManager.registerBookPlacements(ra);
    }


    @EventCalled
    public static void onAddExtraGoals(Entity entity, ServerLevel serverLevel) {
        if (CommonConfigs.Functional.FODDER_ENABLED.get()) {
            if (entity instanceof Animal animal) {
                EntityType<?> type = entity.getType();
                if (type.is(ModTags.EATS_FODDER)) {
                    animal.goalSelector.addGoal(3,
                            new EatFodderGoal(animal, 1, 8, 2, 30));
                }
                return;
            }
        }
        if (entity.getType() == EntityType.EVOKER) {
            ((Evoker) entity).goalSelector.addGoal(6,
                    new EvokerRedMerchantWololooSpellGoal((Evoker) entity));
        }
        if (CommonConfigs.Functional.CANNON_BOAT_ENABLED.get()) {
            if (entity instanceof AbstractIllager pillager) {
                pillager.goalSelector.addGoal(2,
                        new ManeuverAndShootCannonGoal(pillager, 20, 40));
            }
        }
    }


    @EventCalled
    public static void onLivingDeath(LivingEntity entity, DamageSource source) {
        Entity sourceEntity = source.getEntity();
        if (sourceEntity instanceof Creeper creeper && creeper.canDropMobsSkull()) {
            if (entity instanceof EnderMan && CommonConfigs.Redstone.ENDERMAN_HEAD_DROP.get()) {
                creeper.increaseDroppedSkulls();
                entity.spawnAtLocation(ModRegistry.ENDERMAN_SKULL_ITEM.get());
            } else if (entity instanceof Spider && CommonConfigs.Building.SPIDER_HEAD_ENABLED.get()) {
                creeper.increaseDroppedSkulls();
                entity.spawnAtLocation(ModRegistry.SPIDER_SKULL_ITEM.get());
            }
        }
    }

    @EventCalled
    public static void serverPlayerTick(Player player) {
        CandyItem.checkSweetTooth(player);

        //refresh quiver for remote players
        if (player instanceof IQuiverPlayer q) {
            SlotReference oldSlot = q.supplementaries$getQuiverSlot();
            SlotReference newSlot = QuiverItem.findActiveQuiverSlot(player);
            if (!oldSlot.get(player).equals(newSlot.get(player))) {
                q.supplementaries$setQuiverSlot(newSlot);
                NetworkHelper.sendToAllClientPlayersTrackingEntity(player,
                        new SyncEquippedQuiverPacket(player, q));
            }
        }
    }

    //TODO: fabric
    @EventCalled
    public static boolean onItemPickup(ItemEntity itemEntity, Player player) {
        ItemStack stack = itemEntity.getItem();
        if (!itemEntity.hasPickUpDelay() && CommonConfigs.Tools.QUIVER_PICKUP.get() &&
                QuiverItem.canAcceptItem(stack) &&
                (itemEntity.getOwner() == null ||
                        SuppPlatformStuff.getItemLifeSpawn(itemEntity) - itemEntity.getAge() <= 200 ||
                        itemEntity.getOwner().equals(player))
        ) {
            ItemStack old = stack.copy();
            if (takeArrow(itemEntity, player, stack)) {
                SuppPlatformStuff.fireItemPickupPost(player, itemEntity, old);
                player.onItemPickup(itemEntity);
                player.awardStat(Stats.ITEM_PICKED_UP.get(stack.getItem()), old.getCount() - stack.getCount());
                return true;
            }
        }
        return false;
    }

    @EventCalled
    public static boolean onArrowPickup(AbstractArrow arrow, Player player, Supplier<ItemStack> pickup) {
        if (CommonConfigs.Tools.QUIVER_PICKUP.get()) {
            ItemStack stack = pickup.get();
            Preconditions.checkNotNull(stack, "Arrow pickup item was null! This is an issue from the mod that added this entity " + arrow);
            return takeArrow(arrow, player, stack);
        }
        return false;
    }

    private static boolean takeArrow(Entity itemEntity, Player player, ItemStack toPickUp) {
        ItemStack quiverItem = QuiverItem.findActiveQuiver(player);
        if (!quiverItem.isEmpty()) {
            var data = quiverItem.get(ModComponents.QUIVER_CONTENT.get());
            if (data != null) {
                var mutable = data.toMutable();
                ItemStack copy = toPickUp.copy();
                int count = copy.getCount();
                int newCount = mutable.tryAdding(copy, true).getCount();
                if (count != newCount) {
                    player.take(itemEntity, count);
                    toPickUp.setCount(newCount);
                    if (toPickUp.isEmpty()) {
                        itemEntity.discard();
                    }
                    quiverItem.set(ModComponents.QUIVER_CONTENT.get(), mutable.toImmutable());
                    return true;
                }
            }
        }
        return false;
    }

}
