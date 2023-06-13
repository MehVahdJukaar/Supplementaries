package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;

public class PancakeItem extends BlockItem {
    public PancakeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (!player.isSecondaryUseActive()) {
            Level level = context.getLevel();
            BlockPos blockpos = context.getClickedPos();
            var blockEntity = level.getBlockEntity(blockpos);
            if (blockEntity instanceof JukeboxBlockEntity jukeboxBlock && !blockEntity.getBlockState().getValue(JukeboxBlock.HAS_RECORD)) {
                ItemStack itemstack = context.getItemInHand();
                if (!level.isClientSide) {
                    ModRegistry.PANCAKE_DISC
                    level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(player, blockEntity.getBlockState()));
                    jukeboxBlock.setFirstItem(itemstack.split(1));

                    player.awardStat(Stats.PLAY_RECORD);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useOn(context);
    }
}
