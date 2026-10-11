package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.block.blocks.PignataAnchorBlock;
import net.mehvahdjukaar.supplementaries.common.entities.PignataEntity;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PignataItem extends Item {

    public PignataItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Direction face = context.getClickedFace();
        BlockPos pos = context.getClickedPos().relative(face);

        BlockState stateAt = level.getBlockState(pos);
        if (!stateAt.canBeReplaced()) return InteractionResult.FAIL;

        EntityType<PignataEntity> type = ModEntities.PIGNATA.get();
        Vec3 bottom = Vec3.atBottomCenterOf(pos);
        AABB box = type.getDimensions().makeBoundingBox(bottom);
        boolean spaceTaken = !level.noCollision(null, box) || !level.getEntities((Entity) null, box, e -> e.blocksBuilding).isEmpty();
        if (spaceTaken) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        if (level instanceof ServerLevel serverLevel) {
            PignataEntity pignata = type.create(serverLevel, EntityType.createDefaultStackConfig(serverLevel, stack, context.getPlayer()),
                    pos, MobSpawnType.SPAWN_EGG, false, false);
            if (pignata == null) return InteractionResult.FAIL;
            pignata.moveTo(bottom.x, bottom.y, bottom.z, Mth.wrapDegrees(context.getRotation() + 180), 0);
            boolean placedOnFloor = face == Direction.UP;
            if (!placedOnFloor && PignataAnchorBlock.canHangBelow(level, pos.above())) {
                pignata.hookTo(pos);
            }
            level.addFreshEntity(pignata);
            level.playSound(null, pignata.getX(), pignata.getY(), pignata.getZ(), ModSounds.PRESENT_PLACE.get(), SoundSource.BLOCKS, 1, 1);
            pignata.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
        }
        stack.consume(1, context.getPlayer());
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
