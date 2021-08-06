package net.mehvahdjukaar.supplementaries.compat.decorativeblocks;

import com.lilypuree.decorative_blocks.blocks.ChandelierBlock;
import com.lilypuree.decorative_blocks.setup.Registration;
import net.mehvahdjukaar.supplementaries.block.blocks.RopeBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class RopeChandelierBlock extends ChandelierBlock {
    private final Block mimic;
    private final Lazy<BlockState> defMimic;
    protected final Lazy<BasicParticleType> particleData;
    public RopeChandelierBlock(Properties properties, Block chandelier, Supplier<BasicParticleType> particleData) {
        super(properties, chandelier==Registration.SOUL_CHANDELIER.get());
        mimic = chandelier;
        defMimic = Lazy.of(()->this.mimic.defaultBlockState());

        this.particleData = Lazy.of(()->{
            BasicParticleType data = particleData.get();
            if(data==null)data = ParticleTypes.FLAME;
            return data;
        });
    }

    @Override
    public IFormattableTextComponent getName() {
        return mimic.getName();
    }

    @Override
    public ItemStack getCloneItemStack(IBlockReader reader, BlockPos pos, BlockState state) {
        return mimic.getCloneItemStack(reader,pos,defMimic.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return mimic.getDrops(defMimic.get(), builder);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if(facing==Direction.UP && !(facingState.getBlock() instanceof RopeBlock)){
            return defMimic.get();
        }
        return stateIn;
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        double d0 = (double) pos.getX() + 0.5D;
        double d1 = (double) pos.getY() + 0.7D;
        double d2 = (double) pos.getZ() + 0.5D;
        double off1 = 0.1875D;
        double off2 = 0.3125D;
        double off3 = 0.0625D;
        worldIn.addParticle(ParticleTypes.SMOKE, d0 - off1, d1, d2 - off2, 0.0D, 0.0D, 0.0D);
        worldIn.addParticle(ParticleTypes.SMOKE, d0 - off2 - off3, d1, d2 + off1 - off3, 0.0D, 0.0D, 0.0D);
        worldIn.addParticle(ParticleTypes.SMOKE, d0 + off1 - off3, d1, d2 + off2 + off3, 0.0D, 0.0D, 0.0D);
        worldIn.addParticle(ParticleTypes.SMOKE, d0 + off2, d1, d2 - off1, 0.0D, 0.0D, 0.0D);

        worldIn.addParticle(particleData.get(), d0 - off1, d1, d2 - off2, 0.0D, 0.0D, 0.0D);
        worldIn.addParticle(particleData.get(), d0 - off2 - off3, d1, d2 + off1 - off3, 0.0D, 0.0D, 0.0D);
        worldIn.addParticle(particleData.get(), d0 + off1 - off3, d1, d2 + off2 + off3, 0.0D, 0.0D, 0.0D);
        worldIn.addParticle(particleData.get(), d0 + off2, d1, d2 - off1, 0.0D, 0.0D, 0.0D);
    }

    public static void tryConverting(BlockState state, IWorld world, BlockPos pos){
        String name = state.getBlock().getRegistryName().toString();
        switch (name) {
            case "decorative_blocks:chandelier":
                world.setBlock(pos, DecoBlocksCompatRegistry.CHANDELIER_ROPE.defaultBlockState(), 3);
                break;
            case "decorative_blocks:soul_chandelier":
                world.setBlock(pos, DecoBlocksCompatRegistry.SOUL_CHANDELIER_ROPE.defaultBlockState(), 3);
                break;
            case "decorative_blocks_abnormals:ender_chandelier":
                world.setBlock(pos,DecoBlocksCompatRegistry.getEnderRopeChandelier()
                        .defaultBlockState(), 3);
                break;
        }
    }
}
