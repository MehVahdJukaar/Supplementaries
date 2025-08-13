package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CandleHolderBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;


public class BuzzierBeesCompat {

    public static final List<Supplier<? extends Block>> BB_CANDLES = new ArrayList<>();


    public static void registerCandle(ResourceLocation id,  Function<BlockState, List<Vec3>> particleOffsets) {
        registerCandle(id, "_soul", CompatObjects.SMALL_SOUL_FLAME, particleOffsets);

        if (CompatHandler.ENDERGETIC) {
            registerCandle(id, "_ender", CompatObjects.SMALL_END_FLAME, particleOffsets);
        }
        if (CompatHandler.CAVERNS_AND_CHASMS) {
            registerCandle(id, "_cupric", CompatObjects.SMALL_CUPRIC_FLAME, particleOffsets);
        }
    }

    private static void registerCandle(ResourceLocation id, String _end, Supplier<ParticleType<?>> flame,
                                       Function<BlockState, List<Vec3>> offsets) {
        var name = id.getPath() + _end;
        var b = RegHelper.registerBlockWithItem(new ResourceLocation(id.getNamespace(), name),
                () -> new CandleHolderBlock(null, BlockBehaviour.Properties.copy(ModRegistry.SCONCE.get()),
                        flame, offsets));
        BB_CANDLES.add(b);
        ModRegistry.ALL_CANDLE_HOLDERS.add(b);
    }

    public static void setupClient() {
        BB_CANDLES.forEach(b -> ClientHelper.registerRenderType(b.get(), RenderType.cutout()));
    }

}
