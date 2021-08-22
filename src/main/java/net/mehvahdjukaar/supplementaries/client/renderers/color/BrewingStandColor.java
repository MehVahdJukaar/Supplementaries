package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import org.jetbrains.annotations.Nullable;

public class BrewingStandColor implements IBlockColor {

    @Override
    public int getColor(BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tint) {
        if(world!=null&&pos!=null) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof BrewingStandTileEntity) {
                ItemStack item = ((IInventory) te).getItem(tint);
                if(!item.isEmpty()){
                    if(!ClientConfigs.cached.COLORED_BWERING_STAND)return 0xff3434;
                    return PotionUtils.getColor(item);
                }
            }
        }
        return 0xffffff;
    }
}

