package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.entities.LabelEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class LabelItem extends Item {

    public LabelItem(Item.Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        BlockPos blockpos = pContext.getClickedPos();
        Direction direction = pContext.getClickedFace();
        BlockPos relative = blockpos.relative(direction);
        Player player = pContext.getPlayer();
        ItemStack itemstack = pContext.getItemInHand();
        if (player != null && !this.mayPlace(player, direction, itemstack, relative)) {
            return InteractionResult.FAIL;
        } else {
            Level level = pContext.getLevel();
            BlockState state = level.getBlockState(blockpos);
            var type = LabelEntity.AttachType.get(state);

            LabelEntity label = new LabelEntity(level, relative, direction);
            label.setAttachmentType(type);

            CompoundTag compoundtag = itemstack.getTag();
            if (compoundtag != null) {
                EntityType.updateCustomEntityTag(level, player, label, compoundtag);
            }

            if (label.survives()) {
                if (!level.isClientSide) {
                    label.playPlacementSound();
                    level.gameEvent(player, GameEvent.ENTITY_PLACE, blockpos);
                    level.addFreshEntity(label);
                }

                itemstack.shrink(1);
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return InteractionResult.CONSUME;
            }
        }
    }

    protected boolean mayPlace(Player pPlayer, Direction pDirection, ItemStack pHangingEntityStack, BlockPos pPos) {
        return !pDirection.getAxis().isVertical() && pPlayer.mayUseItemAt(pPos, pDirection, pHangingEntityStack);
    }
}
