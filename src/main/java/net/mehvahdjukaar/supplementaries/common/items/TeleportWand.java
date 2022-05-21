package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TeleportWand extends Item {

    public TeleportWand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        if (!pLevel.isClientSide) {
            //this.handleInteraction(pPlayer, pState, pLevel, pPos, false, pPlayer.getItemInHand(InteractionHand.MAIN_HAND));
        }

        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand pUsedHand) {
        ItemStack stack = player.getItemInHand(pUsedHand);
        if (!level.isClientSide) {

            if (!player.canUseGameMasterBlocks()) {
                return InteractionResultHolder.pass(stack);
            }
            else{
                var trace = CommonUtil.rayTrace(player, level,  ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, 128);
                var v = trace.getLocation();
                boolean success = player.randomTeleport(v.x, v.y, v.z, true);
                if(success) return InteractionResultHolder.consume(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }

    private boolean teleport(Player player, Level level, double pX, double pY, double pZ) {





        return false;
    }
}
