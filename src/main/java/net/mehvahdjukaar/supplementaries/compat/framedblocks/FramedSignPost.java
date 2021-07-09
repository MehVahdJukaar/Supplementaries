package net.mehvahdjukaar.supplementaries.compat.framedblocks;

import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import xfacthd.framedblocks.client.util.FramedBlockData;
import xfacthd.framedblocks.common.FBContent;
import xfacthd.framedblocks.common.block.IFramedBlock;
import xfacthd.framedblocks.common.tileentity.FramedTileEntity;
import xfacthd.framedblocks.common.util.Utils;

import javax.annotation.Nullable;

public class FramedSignPost {
    public static final BlockState framedFence = FBContent.blockFramedFence.get().defaultBlockState();

    public static final Item framedHammer = FBContent.itemFramedHammer.get();

    @OnlyIn(Dist.CLIENT)
    public static IModelData getModelData(BlockState mimic){
        FramedBlockData date = new FramedBlockData();
        date.setCamoState(mimic);
        return date;
    }

    @Nullable
    public static Block tryGettingFramedBlock(Block block, World world, BlockPos pos){
        if(block.is(FBContent.blockFramedFence.get())){
            TileEntity tile = world.getBlockEntity(pos);
            if(tile instanceof FramedTileEntity){
                return ((FramedTileEntity) tile).getCamoState().getBlock();
            }
        }
        return null;
    }

    public static boolean handleInteraction(SignPostBlockTile te, PlayerEntity player, Hand hand, ItemStack stack, World world, BlockPos pos) {
        Item i = stack.getItem();
        boolean hasMimic = !te.mimic.isAir() && !te.mimic.is(framedFence.getBlock());
        if(hasMimic && i == FBContent.itemFramedHammer.get()){
            net.mehvahdjukaar.selene.util.Utils.swapItem(player,hand,stack,new ItemStack(te.mimic.getBlock().asItem()));
            te.setHeldBlock(framedFence);
            te.setChanged();
            te.requestModelDataUpdate();
            return true;
        }
        else if(!hasMimic && i instanceof BlockItem){
            BlockState state = ((BlockItem)stack.getItem()).getBlock().defaultBlockState();
            if (isValidBlock(state, player, world, pos)) {
                te.setHeldBlock(state);
                te.setChanged();
                te.requestModelDataUpdate();
                return true;
            }
        }
        return false;
    }

    private static boolean isValidBlock(BlockState state, PlayerEntity player, World world, BlockPos pos){
        Block block = state.getBlock();
        if (block instanceof IFramedBlock) {
            return false;
        } else if (state.is(Utils.BLACKLIST)) {
            player.displayClientMessage(FramedTileEntity.MSG_BLACKLISTED, true);
            return false;
        } else if (block.hasTileEntity(state)) {
            player.displayClientMessage(FramedTileEntity.MSG_TILE_ENTITY, true);
            return false;
        } else {
            return state.isSolidRender(world, pos) || state.is(Utils.FRAMEABLE);
        }
    }

}
