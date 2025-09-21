package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.item.WoodBasedItem;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBoatEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class CannonBoatItem extends WoodBasedItem {
    public CannonBoatItem(Properties properties, WoodType wood) {
        super(properties, wood);
    }

    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        } else {
            Vec3 vec3 = player.getViewVector(1.0F);
            double d = 5.0;
            List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(vec3.scale(5.0)).inflate(1.0), ENTITY_PREDICATE);
            if (!list.isEmpty()) {
                Vec3 vec32 = player.getEyePosition();

                for (Entity entity : list) {
                    AABB aABB = entity.getBoundingBox().inflate(entity.getPickRadius());
                    if (aABB.contains(vec32)) {
                        return InteractionResultHolder.pass(itemStack);
                    }
                }
            }

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                Boat boat = this.getBoat(level, hitResult, itemStack, player);
                boat.setYRot(player.getYRot());
                if (!level.noCollision(boat, boat.getBoundingBox())) {
                    return InteractionResultHolder.fail(itemStack);
                } else {
                    if (!level.isClientSide) {
                        level.addFreshEntity(boat);
                        level.gameEvent(player, GameEvent.ENTITY_PLACE, hitResult.getLocation());
                        itemStack.consume(1, player);
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                }
            } else {
                return InteractionResultHolder.pass(itemStack);
            }
        }
    }

    private Boat getBoat(Level level, HitResult hitResult, ItemStack stack, Player player) {
        Vec3 vec3 = hitResult.getLocation();
        Boat boat = new CannonBoatEntity(level, vec3.x, vec3.y, vec3.z, this.getBlockType());
        if (level instanceof ServerLevel serverLevel) {
            EntityType.createDefaultStackConfig(serverLevel, stack, player).accept(boat);
        }

        return boat;
    }
}
