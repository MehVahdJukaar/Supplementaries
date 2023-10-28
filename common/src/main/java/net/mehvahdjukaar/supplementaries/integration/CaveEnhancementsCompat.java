package net.mehvahdjukaar.supplementaries.integration;

import com.teamabode.cave_enhancements.common.block.entity.SpectacleCandleBlockEntity;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CandleHolderBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CaveEnhancementsCompat {

    private static final List<Supplier<? extends Block>> SPECTACLE_CANDLE_HOLDERS = new ArrayList<>();
    private static Supplier<BlockEntityType<SpectacleCandleHolderTile>> tile;

    public static void tick(Level level, BlockPos pos, BlockState state) {
        tick(level, pos, state, null);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SpectacleCandleHolderTile e) {
        SpectacleCandleBlockEntity.tick(level, pos, state);
    }

    public static void registerCandle(ResourceLocation id) {
        var name = id.getPath() + "_spectacle";
        ResourceLocation res = new ResourceLocation(id.getNamespace(), name);
        var b = RegHelper.registerBlockWithItem(res, () -> new SpectacleCandleHolder(null,
                        BlockBehaviour.Properties.copy(ModRegistry.SCONCE.get()), () -> ParticleTypes.SMALL_FLAME));
        SPECTACLE_CANDLE_HOLDERS.add(b);

        tile = RegHelper.registerBlockEntityType(res,
                () -> PlatHelper.newBlockEntityType(SpectacleCandleHolderTile::new,
                        SPECTACLE_CANDLE_HOLDERS.stream().map(Supplier::get).toArray(Block[]::new))
        );
    }



    public static void setupClient() {
        SPECTACLE_CANDLE_HOLDERS.forEach(b -> ClientHelper.registerRenderType(b.get(), RenderType.cutout()));

    }

    private static class SpectacleCandleHolderTile extends BlockEntity {

        public SpectacleCandleHolderTile(BlockPos blockPos, BlockState blockState) {
            super(tile.get(), blockPos, blockState);
        }
    }

    private static class SpectacleCandleHolder extends CandleHolderBlock implements EntityBlock {


        public SpectacleCandleHolder(DyeColor color, Properties properties, Supplier<ParticleType<? extends ParticleOptions>> particle) {
            super(color, properties, particle);
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new SpectacleCandleHolderTile(pos, state);
        }

        @Nullable
        @Override
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
            return level.isClientSide ? null : Utils.getTicker(blockEntityType, tile.get(), CaveEnhancementsCompat::tick);
        }

        @Override
        public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
            return super.getDrops(state, builder);
        }
    }
}
