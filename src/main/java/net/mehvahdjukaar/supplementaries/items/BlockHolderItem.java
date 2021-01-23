package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.common.IBlockHolder;
import net.mehvahdjukaar.supplementaries.common.ILightMimic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;

public class BlockHolderItem extends BlockItem {
    public BlockHolderItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    public ActionResultType tryPlace(BlockItemUseContext context, Block mimicBlock) {
        ActionResultType result = super.tryPlace(context);
        if(result.isSuccessOrConsume() && !context.getWorld().isRemote){
            TileEntity te = context.getWorld().getTileEntity(context.getPos());
            if(te instanceof IBlockHolder){
                BlockState state = mimicBlock.getDefaultState();
                ((IBlockHolder) te).setHeldBlock(state);
                if(te instanceof ILightMimic){
                    ((ILightMimic) te).setLight(state.getLightValue());
               }
            }

        }
        return result;
    }
}
