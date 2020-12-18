package net.mehvahdjukaar.supplementaries.blocks.test;

import net.mehvahdjukaar.supplementaries.renderers.NoticeBoardBlockTileRenderer;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.item.FilledMapItem;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.SimplexNoiseGenerator;

public class map {
    //private PerlinNoiseGenerator p = new PerlinNoiseGenerator(1);
    SharedSeedRandom sharedseedrandom = new SharedSeedRandom(1);

    SimplexNoiseGenerator g = new SimplexNoiseGenerator(sharedseedrandom);

}
