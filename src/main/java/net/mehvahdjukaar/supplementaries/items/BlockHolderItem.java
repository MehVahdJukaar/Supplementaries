package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.block.util.ILightMimic;
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
        ActionResultType result = super.place(context);
        if(result.consumesAction()){
            TileEntity te = context.getLevel().getBlockEntity(context.getClickedPos());
            if(te instanceof IBlockHolder){
                BlockState state = mimicBlock.defaultBlockState();
                ((IBlockHolder) te).setHeldBlock(state);
                if(te instanceof ILightMimic){
                    ((ILightMimic) te).setLight(state.getLightEmission());
               }
            }

        }
        return result;
    }
}
