package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.entities.HatStandEntity;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class HatStandItem extends Item {
    public HatStandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction direction = context.getClickedFace();
        if (direction == Direction.DOWN) {
            return InteractionResult.FAIL;
        } else {
            Level level = context.getLevel();
            BlockPlaceContext placeContext = new BlockPlaceContext(context);
            BlockPos blockpos = placeContext.getClickedPos();
            BlockPos above = blockpos.above();
            if (placeContext.canPlace() && level.getBlockState(above).canBeReplaced(placeContext)) {
                var type = ModEntities.HAT_STAND.get();
                Vec3 vec3 = Vec3.atBottomCenterOf(blockpos);
                AABB aABB = type.getDimensions().makeBoundingBox(vec3.x(), vec3.y(), vec3.z());
                if (level.noCollision(null, aABB) && level.getEntities(null, aABB).isEmpty()) {
                    ItemStack itemstack = context.getItemInHand();
                    if (level instanceof ServerLevel serverLevel) {
                        level.removeBlock(blockpos, false);
                        level.removeBlock(above, false);
                        Consumer<HatStandEntity> consumer = EntityType.createDefaultStackConfig(serverLevel, itemstack, context.getPlayer());
                        HatStandEntity dummy = type.create(serverLevel, itemstack.getTag(), consumer, blockpos, MobSpawnType.SPAWN_EGG, false, false);
                        if (dummy == null) {
                            return InteractionResult.FAIL;
                        }
                        float rotation = Mth.floor((Mth.wrapDegrees(context.getRotation() - 180.0F) + 11.25) / 22.5F) * 22.5F;
                        dummy.moveTo(vec3.x, vec3.y, vec3.z, rotation, 0.0F);

                        level.addFreshEntity(dummy);
                        level.playSound(null, dummy.getX(), dummy.getY(), dummy.getZ(), SoundEvents.ARMOR_STAND_PLACE,
                                SoundSource.BLOCKS, 0.75F, 0.8F);
                        dummy.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
                    }
                    itemstack.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.FAIL;

        }
    }
}
