//package net.mehvahdjukaar.supplementaries.compat.decorativeblocks;
//
//import com.lilypuree.decorative_blocks.blocks.ChandelierBlock;
//import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
//import net.mehvahdjukaar.supplementaries.compat.CompatObjects;
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.ItemStack;
//import net.minecraft.loot.LootContext;
//import net.minecraft.particles.BasicParticleType;
//import net.minecraft.particles.ParticleType;
//import net.minecraft.particles.ParticleTypes;
//import net.minecraft.util.Direction;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.RayTraceResult;
//import net.minecraft.util.text.IFormattableTextComponent;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.World;
//import net.minecraftforge.common.util.Lazy;
//
//import java.util.List;
//import java.util.Random;
//import java.util.function.Supplier;
//
//public class RopeChandelierBlock extends ChandelierBlock {
//    private final Supplier<Block> mimic;
//    private final Lazy<BlockState> defMimic;
//    protected final Lazy<BasicParticleType> particleData;
//    public <T extends ParticleType<?>> RopeChandelierBlock(Properties properties,  Supplier<Block> chandelier, Supplier<T> particleData) {
//        super(properties, chandelier==CompatObjects.SOUL_CHANDELIER);
//        /*
//        this.mimic = Lazy.of(()->{
//            Block data = chandelier.get();
//            if(data==null)data = Registration.CHANDELIER.get();
//            return data;
//        });*/
//        this.mimic = chandelier;
//        defMimic = Lazy.of(()->this.mimic.get().defaultBlockState());
//
//        this.particleData = Lazy.of(()->{
//            BasicParticleType data = (BasicParticleType) particleData.get();
//            if(data==null)data = ParticleTypes.FLAME;
//            return data;
//        });
//    }
//
//    @Override
//    public IFormattableTextComponent getName() {
//        return mimic.get().getName();
//    }
//
//    @Override
//    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
//        return mimic.get().getPickBlock(defMimic.get(), target, world,pos,player);
//    }
//
//    @Override
//    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
//        return mimic.get().getDrops(defMimic.get(), builder);
//    }
//
//    @Override
//    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
//        if(facing==Direction.UP && !(facingState.getBlock() instanceof RopeBlock)){
//            return defMimic.get();
//        }
//        return stateIn;
//    }
//
//    @Override
//    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
//        double d0 = (double) pos.getX() + 0.5D;
//        double d1 = (double) pos.getY() + 0.7D;
//        double d2 = (double) pos.getZ() + 0.5D;
//        double off1 = 0.1875D;
//        double off2 = 0.3125D;
//        double off3 = 0.0625D;
//        worldIn.addParticle(ParticleTypes.SMOKE, d0 - off1, d1, d2 - off2, 0.0D, 0.0D, 0.0D);
//        worldIn.addParticle(ParticleTypes.SMOKE, d0 - off2 - off3, d1, d2 + off1 - off3, 0.0D, 0.0D, 0.0D);
//        worldIn.addParticle(ParticleTypes.SMOKE, d0 + off1 - off3, d1, d2 + off2 + off3, 0.0D, 0.0D, 0.0D);
//        worldIn.addParticle(ParticleTypes.SMOKE, d0 + off2, d1, d2 - off1, 0.0D, 0.0D, 0.0D);
//
//        worldIn.addParticle(particleData.get(), d0 - off1, d1, d2 - off2, 0.0D, 0.0D, 0.0D);
//        worldIn.addParticle(particleData.get(), d0 - off2 - off3, d1, d2 + off1 - off3, 0.0D, 0.0D, 0.0D);
//        worldIn.addParticle(particleData.get(), d0 + off1 - off3, d1, d2 + off2 + off3, 0.0D, 0.0D, 0.0D);
//        worldIn.addParticle(particleData.get(), d0 + off2, d1, d2 - off1, 0.0D, 0.0D, 0.0D);
//    }
//
//    public static void tryConverting(BlockState state, IWorld world, BlockPos pos){
//        Block b = state.getBlock();
//        if(b == CompatObjects.CHANDELIER.get()){
//            world.setBlock(pos, DecoBlocksCompatRegistry.CHANDELIER_ROPE.defaultBlockState(), 3);
//        }
//        else if(b == CompatObjects.SOUL_CHANDELIER.get()){
//            world.setBlock(pos, DecoBlocksCompatRegistry.SOUL_CHANDELIER_ROPE.defaultBlockState(), 3);
//        }
//        else if(b == CompatObjects.ENDER_CHANDELIER.get()){
//            world.setBlock(pos, DecoBlocksCompatRegistry.ENDER_CHANDELIER_ROPE.defaultBlockState(), 3);
//        }
//        else if(b == CompatObjects.GLOW_CHANDELIER.get()){
//            world.setBlock(pos, DecoBlocksCompatRegistry.GLOW_CHANDELIER_ROPE.defaultBlockState(), 3);
//        }
//    }
//}
