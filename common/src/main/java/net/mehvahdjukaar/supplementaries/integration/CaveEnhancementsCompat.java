package net.mehvahdjukaar.supplementaries.integration;

import com.teamabode.cave_enhancements.common.block.entity.SpectacleCandleBlockEntity;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CandleHolderBlock;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.RegUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static net.mehvahdjukaar.supplementaries.reg.ModConstants.CANDLE_HOLDER_NAME;
import static net.mehvahdjukaar.supplementaries.reg.RegUtils.getTab;
import static net.mehvahdjukaar.supplementaries.reg.RegUtils.regWithItem;

public class CaveEnhancementsCompat {

    public static void tick(Level level, BlockPos pos, BlockState state) {
        tick(level, pos, state, null);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SpectacleCandleHolderTile e) {
        SpectacleCandleBlockEntity.tick(level, pos, state);
    }

    public static final Supplier<Block> SPECTACLE_CANDLE_HOLDER = regWithItem(CANDLE_HOLDER_NAME + "_spectacle",
            () -> new SpectacleCandleHolder(null,
                    BlockBehaviour.Properties.copy(ModRegistry.SCONCE.get()), () -> ParticleTypes.SMALL_FLAME),
            getTab(CreativeModeTab.TAB_DECORATIONS, CANDLE_HOLDER_NAME));
    public static final Supplier<BlockEntityType<SpectacleCandleHolderTile>> SPECTACLE_CANDLE_HOLDER_TILE =
            RegUtils.regTile(CANDLE_HOLDER_NAME + "_spectacle",
                    () -> PlatformHelper.newBlockEntityType(SpectacleCandleHolderTile::new, SPECTACLE_CANDLE_HOLDER.get())
            );

    public static void init() {
    }

    public static void setupClient() {
        ClientPlatformHelper.registerRenderType(SPECTACLE_CANDLE_HOLDER.get(), RenderType.cutout());
    }

    private static class SpectacleCandleHolderTile extends BlockEntity{

        public SpectacleCandleHolderTile(BlockPos blockPos, BlockState blockState) {
            super(SPECTACLE_CANDLE_HOLDER_TILE.get(), blockPos, blockState);
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
            return level.isClientSide ? null : Utils.getTicker(blockEntityType, SPECTACLE_CANDLE_HOLDER_TILE.get(), CaveEnhancementsCompat::tick);
        }
    }
}
