package net.mehvahdjukaar.supplementaries.plugins.quark;

import net.mehvahdjukaar.supplementaries.block.blocks.MagmaCreamBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import vazkii.quark.api.IConditionalSticky;

import javax.annotation.Nullable;
import java.util.List;

public class QuarkBlocks {

    public static MagmaCreamBlock createMagmaCreamBlock(){
        return new QuarkMagmaCreamBlock(AbstractBlock.Properties.from(Blocks.SLIME_BLOCK));
    }

    public static class QuarkMagmaCreamBlock extends MagmaCreamBlock implements IConditionalSticky {


        public QuarkMagmaCreamBlock(Properties properties) {
            super(properties);
        }

        @Override
        public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {}

        @Override
        public boolean canStickToBlock(World world, BlockPos pistonPos, BlockPos fromPos, BlockPos toPos, BlockState fromState, BlockState toState, Direction moveDir) {
            if(fromState.getBlock()==this) {
                Direction stickDir = fromState.get(FACING);
                if(fromPos.offset(stickDir).equals(toPos))return true;
                else if(fromPos.offset(stickDir.getOpposite()).equals(toPos))return false;
                else if (toState.getBlock()==this)return toState.get(FACING)==stickDir;
                else return toState.getBlock().isStickyBlock(toState);
            }
            return false;
        }
    }



}
