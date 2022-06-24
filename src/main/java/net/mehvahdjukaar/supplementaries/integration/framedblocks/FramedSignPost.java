package net.mehvahdjukaar.supplementaries.integration.framedblocks;

/*
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import xfacthd.framedblocks.api.block.FramedBlockEntity;
import xfacthd.framedblocks.api.block.IFramedBlock;
import xfacthd.framedblocks.api.util.FramedBlockData;
import xfacthd.framedblocks.api.util.Utils;
import xfacthd.framedblocks.common.FBContent;

import javax.annotation.Nullable;

import static xfacthd.framedblocks.api.block.FramedBlockEntity.MSG_BLACKLISTED;
import static xfacthd.framedblocks.api.block.FramedBlockEntity.MSG_BLOCK_ENTITY;

public class FramedSignPost {
    public static final BlockState framedFence = FBContent.blockFramedFence.get().defaultBlockState();

    public static final Item framedHammer = FBContent.itemFramedHammer.get();

    public static IModelData getModelData(BlockState mimic) {
        FramedBlockData date = new FramedBlockData(false);
        date.setCamoState(mimic);

        return date;
    }

    @Nullable
    public static Block tryGettingFramedBlock(Block block, Level world, BlockPos pos) {
        if (block == FBContent.blockFramedFence.get()) {
            if (world.getBlockEntity(pos) instanceof FramedBlockEntity tile) {
                return tile.getCamoState().getBlock();
            }
        }
        return null;
    }

    public static boolean handleInteraction(SignPostBlockTile te, Player player, InteractionHand hand, ItemStack stack, Level world, BlockPos pos) {
        Item i = stack.getItem();
        boolean hasMimic = !te.mimic.isAir() && !te.mimic.is(framedFence.getBlock());
        if (hasMimic && i == FBContent.itemFramedHammer.get()) {
            net.mehvahdjukaar.moonlight.util.Utils.swapItem(player, hand, stack, new ItemStack(te.mimic.getBlock().asItem()));
            te.setHeldBlock(framedFence);
            te.setChanged();
            te.requestModelDataUpdate();
            return true;
        } else if (!hasMimic && i instanceof BlockItem blockItem) {
            BlockState state = blockItem.getBlock().defaultBlockState();
            if (isValidBlock(state, player, world, pos)) {
                te.setHeldBlock(state);
                te.setChanged();
                te.requestModelDataUpdate();
                return true;
            }
        }
        return false;
    }

    protected static boolean isValidBlock(BlockState state, Player player, Level level, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof IFramedBlock) {
            return false;
        } else if (state.is(Utils.BLACKLIST)) {
            player.displayClientMessage(MSG_BLACKLISTED, true);
            return false;
        } else if (state.hasBlockEntity()) {
            player.displayClientMessage(MSG_BLOCK_ENTITY, true);
            return false;
        } else {
            return state.isSolidRender(level, pos) || state.is(Utils.FRAMEABLE);
        }
    }

}
*/

