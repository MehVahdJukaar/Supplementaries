package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PancakeItem extends BlockItem {
    public PancakeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getPlayer().isShiftKeyDown()) {
            Level world = context.getLevel();
            BlockPos blockpos = context.getClickedPos();
            BlockState blockstate = world.getBlockState(blockpos);
            if (blockstate.is(Blocks.JUKEBOX) && !blockstate.getValue(JukeboxBlock.HAS_RECORD)) {
                ItemStack itemstack = context.getItemInHand();
                if (!world.isClientSide) {
                    ((JukeboxBlock) Blocks.JUKEBOX).setRecord(world, blockpos, blockstate, itemstack.split(1));
                    world.levelEvent(null, 1010, blockpos, Item.getId(ModRegistry.PANCAKE_DISC.get()));
                    Player player = context.getPlayer();
                    if (player != null) {
                        player.awardStat(Stats.PLAY_RECORD);
                    }
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return super.useOn(context);
    }
}
