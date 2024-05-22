package net.mehvahdjukaar.supplementaries.integration.fabric;

import com.google.common.base.Suppliers;
import lilypuree.decorative_blocks.blocks.ChandelierBlock;
import lilypuree.decorative_blocks.blocks.PalisadeBlock;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AbstractRopeBlock;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class DecoBlocksCompatImpl {

    public static final Supplier<Block> CHANDELIER_ROPE;
    public static final Supplier<Block> SOUL_CHANDELIER_ROPE;
    public static final Supplier<Block> ENDER_CHANDELIER_ROPE;
    public static final Supplier<Block> GLOW_CHANDELIER_ROPE;

    static {
        CHANDELIER_ROPE = RegHelper.registerBlock(Supplementaries.res("rope_chandelier"), () ->
                new RopeChandelierBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.WOOD)
                        .strength(0.3F)
                        .sound(SoundType.WOOD)
                        .noOcclusion()
                        .lightLevel((state) -> 15), CompatObjects.CHANDELIER, () -> ParticleTypes.FLAME));

        SOUL_CHANDELIER_ROPE = RegHelper.registerBlock(Supplementaries.res("rope_soul_chandelier"), () ->
                new RopeChandelierBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.WOOD)
                        .strength(0.3F)
                        .sound(SoundType.WOOD)
                        .noOcclusion()
                        .lightLevel((state) -> 11), CompatObjects.SOUL_CHANDELIER, () -> ParticleTypes.SOUL_FIRE_FLAME));

        if (CompatHandler.DECO_BLOCKS_ABNORMALS) {
            ENDER_CHANDELIER_ROPE = RegHelper.registerBlock(Supplementaries.res("rope_ender_chandelier"), () ->
                    new RopeChandelierBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(0.3F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                            .lightLevel((state) -> 15), CompatObjects.ENDER_CHANDELIER,
                            CompatObjects.ENDER_FLAME));
        } else ENDER_CHANDELIER_ROPE = null;

        if (CompatHandler.MUCH_MORE_MOD_COMPAT) {
            GLOW_CHANDELIER_ROPE = RegHelper.registerBlock(Supplementaries.res("rope_glow_chandelier"), () ->
                    new RopeChandelierBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(0.3F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                            .lightLevel((state) -> 15), CompatObjects.GLOW_CHANDELIER,
                            CompatObjects.GLOW_FLAME));
        } else GLOW_CHANDELIER_ROPE = null;
    }

    public static boolean isPalisade(BlockState state) {
        return state.getBlock() instanceof PalisadeBlock;
    }

    public static void tryConvertingRopeChandelier(BlockState facingState, LevelAccessor world, BlockPos facingPos) {
        Block b = facingState.getBlock();
        if (b == CompatObjects.CHANDELIER.get()) {
            world.setBlock(facingPos, CHANDELIER_ROPE.get().defaultBlockState(), 3);
        } else if (b == CompatObjects.SOUL_CHANDELIER.get()) {
            world.setBlock(facingPos, SOUL_CHANDELIER_ROPE.get().defaultBlockState(), 3);
        } else if (b == CompatObjects.ENDER_CHANDELIER.get()) {
            world.setBlock(facingPos, ENDER_CHANDELIER_ROPE.get().defaultBlockState(), 3);
        } else if (b == CompatObjects.GLOW_CHANDELIER.get()) {
            world.setBlock(facingPos, GLOW_CHANDELIER_ROPE.get().defaultBlockState(), 3);
        }
    }

    public static void init() {
    }


    public static class RopeChandelierBlock extends ChandelierBlock {
        private final Supplier<Block> mimic;
        private final Supplier<BlockState> defMimic;
        protected final Supplier<SimpleParticleType> particleData;

        public <T extends ParticleType<?>> RopeChandelierBlock(Properties properties, Supplier<Block> chandelier, Supplier<T> particleData) {
            super(properties, false);

            this.mimic = chandelier;
            this.defMimic = Suppliers.memoize(() -> this.mimic.get().defaultBlockState());

            this.particleData = Suppliers.memoize(() -> {
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
        public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
            return mimic.get().getCloneItemStack(level, pos, defMimic.get());
        }

        @Override
        public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
            return mimic.get().getDrops(defMimic.get(), builder);
        }

        @Override
        public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
            if (facing == Direction.UP && !(facingState.getBlock() instanceof AbstractRopeBlock)) {
                return defMimic.get();
            }
            return stateIn;
        }

        @Override
        public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
            double d0 = pos.getX() + 0.5D;
            double d1 = pos.getY() + 0.7D;
            double d2 = pos.getZ() + 0.5D;
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

    }


    public static void setupClient() {
        if (DecoBlocksCompatImpl.CHANDELIER_ROPE != null)
            ClientHelper.registerRenderType(DecoBlocksCompatImpl.CHANDELIER_ROPE.get(), RenderType.cutout());
        if (DecoBlocksCompatImpl.SOUL_CHANDELIER_ROPE != null)
            ClientHelper.registerRenderType(DecoBlocksCompatImpl.SOUL_CHANDELIER_ROPE.get(), RenderType.cutout());
        if (CompatHandler.DECO_BLOCKS_ABNORMALS) {
            if (DecoBlocksCompatImpl.ENDER_CHANDELIER_ROPE != null)
                ClientHelper.registerRenderType(DecoBlocksCompatImpl.ENDER_CHANDELIER_ROPE.get(), RenderType.cutout());
        }
        if (CompatHandler.MUCH_MORE_MOD_COMPAT) {
            if (DecoBlocksCompatImpl.GLOW_CHANDELIER_ROPE != null)
                ClientHelper.registerRenderType(DecoBlocksCompatImpl.GLOW_CHANDELIER_ROPE.get(), RenderType.cutout());
        }
    }
}
