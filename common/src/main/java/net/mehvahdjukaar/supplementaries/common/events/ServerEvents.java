package net.mehvahdjukaar.supplementaries.common.events;


import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AshLayerBlock;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimesManager;
import net.mehvahdjukaar.supplementaries.common.entities.goals.EatFodderGoal;
import net.mehvahdjukaar.supplementaries.common.entities.goals.EvokerRedMerchantWololooSpellGoal;
import net.mehvahdjukaar.supplementaries.common.events.overrides.InteractEventOverrideHandler;
import net.mehvahdjukaar.supplementaries.common.items.AbstractMobContainerItem;
import net.mehvahdjukaar.supplementaries.common.items.FluteItem;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.common.misc.globe.GlobeData;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.CapturedMobHandler;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.LootTablesInjects;
import net.mehvahdjukaar.supplementaries.reg.ModSetup;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class ServerEvents {

    @EventCalled
    public static void onFireConsume(IFireConsumeBlockEvent event) {
        if (event.getState().getBlock() instanceof IRopeConnection) {
            LevelAccessor level = event.getLevel();
            BlockPos pos = event.getPos();
            level.removeBlock(pos, false);
            if (BaseFireBlock.canBePlacedAt((Level) level, pos, Direction.DOWN)) {
                event.setFinalState(BaseFireBlock.getState(level, pos).setValue(FireBlock.AGE, 8));
                level.scheduleTick(pos, Blocks.FIRE, 2 + ((Level) level).random.nextInt(1));
            }//TODO: make faster
        } else AshLayerBlock.tryConvertToAsh(event);
    }

    //block placement should stay low in priority to allow other more important mod interaction that use the event
    @EventCalled
    public static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isSpectator()) { //is this check even needed?
            return InteractEventOverrideHandler.onItemUsedOnBlock(player, level,
                    player.getItemInHand(hand), hand, hitResult);
        }
        return InteractionResult.PASS;
    }

    @EventCalled
    public static InteractionResult onRightClickBlockHP(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isSpectator()) {
            return InteractEventOverrideHandler.onItemUsedOnBlockHP(player, level,
                    player.getItemInHand(hand), hand, hitResult);
        }
        return InteractionResult.PASS;
    }

    @EventCalled
    public static InteractionResultHolder<ItemStack> onUseItem(Player player, Level level, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isSpectator()) {
            return InteractEventOverrideHandler.onItemUse(player, level, hand, stack);
        }
        return InteractionResultHolder.pass(stack);
    }


    @EventCalled
    public static void onPlayerLoggedIn(ServerPlayer player) {
    }

    @EventCalled
    public static InteractionResult onRightClickEntity(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (player.isSpectator()) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof FluteItem) {
            if (FluteItem.interactWithPet(stack, player, entity, hand)) {
                return InteractionResult.SUCCESS; // we need this for event to be actually cancelled
            }
        } else if (stack.getItem() instanceof AbstractMobContainerItem containerItem) {
            if (!containerItem.isFull(stack)) {
                var res = containerItem.doInteract(stack, player, entity, hand);
                if (res.consumesAction()) {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @EventCalled
    public static void onDataSyncToPlayer(ServerPlayer player, boolean joined) {
        SongsManager.sendDataToClient(player);
        CapturedMobHandler.sendDataToClient(player);
        GlobeData.sendDataToClient(player);
        HourglassTimesManager.sendDataToClient(player);
    }

    @EventCalled
    public static void onCommonTagUpdate(RegistryAccess registryAccess, boolean client) {
        ModSetup.tagDependantSetup(registryAccess);
    }

    private static final boolean FODDER_ENABLED = CommonConfigs.Functional.FODDER_ENABLED.get();

    @EventCalled
    public static void onEntityLoad(Entity entity, ServerLevel serverLevel) {
        if (FODDER_ENABLED) {
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
    }

    @EventCalled
    public static void injectLootTables(LootTables lootManager, ResourceLocation name, Consumer<LootPool.Builder> builder) {
        LootTablesInjects.injectLootTables(name, builder);
    }


    //TODO: fabric
    @EventCalled
    public static boolean onItemPickup(ItemEntity itemEntity, Player player) {
        ItemStack stack = itemEntity.getItem();
        if (!itemEntity.hasPickUpDelay() && CommonConfigs.Tools.QUIVER_PICKUP.get() &&
                stack.getItem() instanceof ArrowItem &&
                (itemEntity.getOwner() == null ||
                        SuppPlatformStuff.getItemLifeSpawn(itemEntity) - itemEntity.getAge() <= 200 ||
                        itemEntity.getOwner().equals(player.getUUID()))
        ) {
            ItemStack old = stack.copy();
            if (takeArrow(itemEntity, player, stack)){
                SuppPlatformStuff.onItemPickup(player, itemEntity, old);
                player.onItemPickup(itemEntity);
                player.awardStat(Stats.ITEM_PICKED_UP.get(stack.getItem()), old.getCount() - stack.getCount());
                return true;
            }
        }
        return false;
    }

    @EventCalled
    public static boolean onArrowPickup(AbstractArrow arrow, Player player, Supplier<ItemStack> pickup) {
        if (CommonConfigs.Tools.QUIVER_PICKUP.get()){
            ItemStack stack = pickup.get();
            return takeArrow(arrow, player, stack);
        }
        return false;
    }

    private static boolean takeArrow(Entity itemEntity, Player player, ItemStack stack) {
        ItemStack quiverItem = QuiverItem.getQuiver(player);
        if (quiverItem != null) {
            var data = QuiverItem.getQuiverData(quiverItem);
            if (data != null) {
                ItemStack copy = stack.copy();
                int count = copy.getCount();
                int newCount = data.tryAdding(copy, true).getCount();
                if (count != newCount) {
                    player.take(itemEntity, count);
                    stack.setCount(newCount);
                    if (stack.isEmpty()) {
                        itemEntity.discard();
                    }
                    return true;
                }
            }
        }
        return false;
    }

}
