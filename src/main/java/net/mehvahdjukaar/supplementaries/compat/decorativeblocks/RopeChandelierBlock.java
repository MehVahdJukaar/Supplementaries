package net.mehvahdjukaar.supplementaries.compat.decorativeblocks;

import com.lilypuree.decorative_blocks.blocks.ChandelierBlock;
import com.lilypuree.decorative_blocks.setup.Registration;
import net.mehvahdjukaar.supplementaries.block.blocks.RopeBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.Lazy;

import java.util.List;
import java.util.function.Supplier;

public class RopeChandelierBlock extends ChandelierBlock {
    private final Supplier<? extends Block> mimic;
    private final Lazy<BlockState> defMimic;
    public RopeChandelierBlock(Properties properties, boolean isSoul) {
        super(properties, isSoul);
        mimic = isSoul? Registration.SOUL_CHANDELIER:Registration.CHANDELIER;
        defMimic = Lazy.of(()->this.mimic.get().defaultBlockState());
    }

    @Override
    public IFormattableTextComponent getName() {
        return mimic.get().getName();
    }

    @Override
    public ItemStack getCloneItemStack(IBlockReader reader, BlockPos pos, BlockState state) {
        return mimic.get().getCloneItemStack(reader,pos,defMimic.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return mimic.get().getDrops(defMimic.get(), builder);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if(facing==Direction.UP && !(facingState.getBlock() instanceof RopeBlock)){
            return defMimic.get();
        }
        return stateIn;
    }

    //TODO: maybe make conversion map
    public static void tryConverting(BlockState state, IWorld world, BlockPos pos){
        if(state.getBlock() == Registration.CHANDELIER.get()){
            world.setBlock(pos,DecoBlocksCompatRegistry.CHANDELIER_ROPE.defaultBlockState(),3);
        }
        else if(state.getBlock() == Registration.SOUL_CHANDELIER.get()){
            world.setBlock(pos,DecoBlocksCompatRegistry.SOUL_CHANDELIER_ROPE.defaultBlockState(),3);
        }
    }
}
