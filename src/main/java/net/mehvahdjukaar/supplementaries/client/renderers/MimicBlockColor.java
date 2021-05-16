package net.mehvahdjukaar.supplementaries.client.renderers;

import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import org.jetbrains.annotations.Nullable;

public class MimicBlockColor implements IBlockColor {

    @Override
    public int getColor(BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tint) {
        if(world!=null&&pos!=null) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof IBlockHolder) {
                BlockState mimic = ((IBlockHolder) te).getHeldBlock();
                if(mimic!=null){
                    return Minecraft.getInstance().getBlockColors().getColor(mimic, world, pos, tint);
                }
            }
        }
        return -1;
    }
}

