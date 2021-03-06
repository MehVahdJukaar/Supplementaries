package net.mehvahdjukaar.supplementaries.world;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RoadSignStructure {

    /*
    @SubscribeEvent
    public static void addFeatureToBiomes(BiomeLoadingEvent event) {
        Feature<NoFeatureConfig> feature = new Feature<NoFeatureConfig>(NoFeatureConfig.field_236558_a_) {

            @Override
            public boolean generate(ISeedReader world, ChunkGenerator generator, Random random, BlockPos pos, NoFeatureConfig config) {

                //StructureSeparationSettings structureseparationsettings = generator.func_235957_b_().func_236197_a_(Structure.VILLAGE);
                //ChunkPos chunkpos = Structure.VILLAGE.getChunkPosForStructure(structureseparationsettings, p_242782_2_, p_242782_4_, i, j);

                //chunk 0,0 coordinates
                int ci = (pos.getX() >> 4) << 4;
                int ck = (pos.getZ() >> 4) << 4;
                RegistryKey<World> dimensionType = world.getWorld().getDimensionKey();
                boolean dimensionCriteria = false;
                if (dimensionType == World.OVERWORLD)
                    dimensionCriteria = true;
                if (!dimensionCriteria)
                    return false;
                if ((random.nextInt(1000000) + 1) <= 50000) {
                    int count = random.nextInt(1) + 1;
                    for (int a = 0; a < count; a++) {
                        int i = ci + random.nextInt(16);
                        int k = ck + random.nextInt(16);
                        int j = world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, i, k);
                        j -= 1;
                        Rotation rotation = Rotation.values()[random.nextInt(3)];
                        Mirror mirror = Mirror.values()[random.nextInt(2)];
                        BlockPos spawnTo = new BlockPos(i + 0, j + 0, k + 0);
                        int x = spawnTo.getX();
                        int y = spawnTo.getY();
                        int z = spawnTo.getZ();
                        Template template = world.getWorld().getStructureTemplateManager()
                                .getTemplateDefaulted(new ResourceLocation(Supplementaries.MOD_ID, "hopper"));
                        if (template == null)
                            return false;
                        template.func_237144_a_(world, spawnTo, new PlacementSettings().setRotation(rotation).setRandom(random).setMirror(mirror)
                                .addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK).setChunk(null).setIgnoreEntities(false), random);
                        BlockState state = world.getBlockState(spawnTo);
                        if(state.getBlock() != Blocks.AIR){
                            world.setBlockState(spawnTo.up(3), Blocks.DIAMOND_BLOCK.getDefaultState(),3);
                        }
                    }
                }
                return true;
            }
        };
        event.getGeneration().getFeatures(GenerationStage.Decoration.SURFACE_STRUCTURES).add(() -> feature
                .withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));
    }
    */

}
