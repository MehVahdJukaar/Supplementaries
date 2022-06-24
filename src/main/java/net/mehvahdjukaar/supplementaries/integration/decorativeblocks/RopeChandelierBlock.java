package net.mehvahdjukaar.supplementaries.integration.decorativeblocks;
/*
import lilypuree.decorative_blocks.blocks.ChandelierBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.Lazy;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class RopeChandelierBlock extends ChandelierBlock {
    private final Supplier<Block> mimic;
    private final Lazy<BlockState> defMimic;
    protected final Lazy<SimpleParticleType> particleData;

    public <T extends ParticleType<?>> RopeChandelierBlock(BlockBehaviour.Properties properties, Supplier<Block> chandelier, Supplier<T> particleData) {
        super(properties, false);

        this.mimic = chandelier;
        this.defMimic = Lazy.of(() -> this.mimic.get().defaultBlockState());

        this.particleData = Lazy.of(() -> {
            SimpleParticleType data = (SimpleParticleType) particleData.get();
            if (data == null) data = ParticleTypes.FLAME;
            return data;
        });
    }

    @Override
    public MutableComponent getName() {
        return mimic.get().getName();
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        return mimic.get().getCloneItemStack(defMimic.get(), target, world, pos, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return mimic.get().getDrops(defMimic.get(), builder);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.UP && !(facingState.getBlock() instanceof RopeBlock)) {
            return defMimic.get();
        }
        return stateIn;
    }

    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
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

    public static void tryConverting(BlockState state, LevelAccessor world, BlockPos pos) {
        Block b = state.getBlock();
        if (b == CompatObjects.CHANDELIER.get()) {
            world.setBlock(pos, DecoBlocksCompatRegistry.CHANDELIER_ROPE.get().defaultBlockState(), 3);
        } else if (b == CompatObjects.SOUL_CHANDELIER.get()) {
            world.setBlock(pos, DecoBlocksCompatRegistry.SOUL_CHANDELIER_ROPE.get().defaultBlockState(), 3);
        } else if (b == CompatObjects.ENDER_CHANDELIER.get()) {
            world.setBlock(pos, DecoBlocksCompatRegistry.ENDER_CHANDELIER_ROPE.get().defaultBlockState(), 3);
        } else if (b == CompatObjects.GLOW_CHANDELIER.get()) {
            world.setBlock(pos, DecoBlocksCompatRegistry.GLOW_CHANDELIER_ROPE.get().defaultBlockState(), 3);
        }
    }
}
*/

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class RopeChandelierBlock {
    public static void tryConverting(BlockState blockState, LevelAccessor worldIn, BlockPos down) {
    }
}