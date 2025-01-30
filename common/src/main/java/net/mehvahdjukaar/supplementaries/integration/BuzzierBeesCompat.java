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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class BuzzierBeesCompat {

    public static final List<Supplier<? extends Block>> BB_CANDLES = new ArrayList<>();


    public static void registerCandle(ResourceLocation id) {
        addCandle(id, "_soul", CompatObjects.SMALL_SOUL_FLAME);

        if (CompatHandler.ENDERGETIC) {
            addCandle(id, "_ender", CompatObjects.SMALL_END_FLAME);
        }
        if (CompatHandler.CAVERNS_AND_CHASMS) {
            addCandle(id, "_cupric", CompatObjects.SMALL_CUPRIC_FLAME);
        }
    }

    private static void addCandle(ResourceLocation id, String _end, Supplier<ParticleType<?>> smallEndFlame) {
        var name = id.getPath() + _end;
        var b = RegHelper.registerBlockWithItem(id.withPath(name), () -> new CandleHolderBlock(null,
                BlockBehaviour.Properties.ofFullCopy(ModRegistry.SCONCE.get()), smallEndFlame));
        BB_CANDLES.add(b);
        ModRegistry.ALL_CANDLE_HOLDERS.add(b);
    }

    public static void setupClient() {
        BB_CANDLES.forEach(b -> ClientHelper.registerRenderType(b.get(), RenderType.cutout()));
    }

}
