package net.mehvahdjukaar.supplementaries.integration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Function;

//mod seems abandoned
public class CaveEnhancementsCompat {

    // private static final List<Supplier<? extends Block>> SPECTACLE_CANDLE_HOLDERS = new ArrayList<>();
    // private static Supplier<BlockEntityType<SpectacleCandleHolderTile>> tile;

//    public static void tick(Level level, BlockPos pos, BlockState state) {
//        tick(level, pos, state, null);
//    }
//
//    public static void tick(Level level, BlockPos pos, BlockState state, SpectacleCandleHolderTile e) {
//        SpectacleCandleBlockEntity.tick(level, pos, state);
//    }

    public static void registerCandle(ResourceLocation id, Function<BlockState, List<Vec3>> offsets) {
//        var name = id.getPath() + "_spectacle";
//        ResourceLocation res = id.withPath(name);
//        var b = RegHelper.registerBlockWithItem(res, () -> new SpectacleCandleHolder(null,
//                BlockBehaviour.Properties.ofFullCopy(ModRegistry.SCONCE.get()), () -> ParticleTypes.SMALL_FLAME,
//                offsets));
//        SPECTACLE_CANDLE_HOLDERS.add(b);
//
//        tile = RegHelper.registerBlockEntityType(res,
//                () -> PlatHelper.newBlockEntityType(SpectacleCandleHolderTile::new,
//                        SPECTACLE_CANDLE_HOLDERS.stream().map(Supplier::get).toArray(Block[]::new))
//        );
//
//        ModRegistry.ALL_CANDLE_HOLDERS.add(b);
    }


    public static void setupClient() {
//        SPECTACLE_CANDLE_HOLDERS.forEach(b -> ClientHelper.registerRenderType(b.get(), RenderType.cutout()));

    }

//    public static class SpectacleCandleHolderTile extends BlockEntity {
//
//        public SpectacleCandleHolderTile(BlockPos blockPos, BlockState blockState) {
//            super(tile.get(), blockPos, blockState);
//        }
//    }

//    private static class SpectacleCandleHolder extends CandleHolderBlock implements EntityBlock {
//
//
//        public SpectacleCandleHolder(DyeColor color, Properties properties, Supplier<ParticleType<? extends ParticleOptions>> particle,
//                                     Function<BlockState, List<Vec3>> offsets) {
//            super(color, properties, particle, offsets);
//        }
//
//        @Nullable
//        @Override
//        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
//            return new SpectacleCandleHolderTile(pos, state);
//        }
//
//        @Nullable
//        @Override
//        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
//            return level.isClientSide ? null : Utils.getTicker(blockEntityType, tile.get(), CaveEnhancementsCompat::tick);
//        }
//
//        @Override
//        public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
//            return super.getDrops(state, builder);
//        }
//    }
}
