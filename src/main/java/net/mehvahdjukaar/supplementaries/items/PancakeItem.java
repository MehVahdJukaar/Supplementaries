package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PancakeItem extends BlockItem {
    public PancakeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        if(!context.getPlayer().isShiftKeyDown()) {
            World world = context.getLevel();
            BlockPos blockpos = context.getClickedPos();
            BlockState blockstate = world.getBlockState(blockpos);
            if (blockstate.is(Blocks.JUKEBOX) && !blockstate.getValue(JukeboxBlock.HAS_RECORD)) {
                ItemStack itemstack = context.getItemInHand();
                if (!world.isClientSide) {
                    ((JukeboxBlock) Blocks.JUKEBOX).setRecord(world, blockpos, blockstate, itemstack.split(1));
                    world.levelEvent(null, 1010, blockpos, Item.getId(ModRegistry.PANCAKE_DISC.get()));
                    PlayerEntity playerentity = context.getPlayer();
                    if (playerentity != null) {
                        playerentity.awardStat(Stats.PLAY_RECORD);
                    }
                }
                return ActionResultType.sidedSuccess(world.isClientSide);
            }
        }
        return super.useOn(context);
    }
}
