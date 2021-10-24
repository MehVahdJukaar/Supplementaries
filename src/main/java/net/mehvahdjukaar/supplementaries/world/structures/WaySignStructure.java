package net.mehvahdjukaar.supplementaries.world.structures;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.RuinedPortalFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.TreeSet;

public class WaySignStructure extends StructureFeature<NoneFeatureConfiguration> {
    public WaySignStructure(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    /**
     * This is how the worldgen code knows what to call when it
     * is time to create the pieces of the structure for generation.
     */
    @Override
    public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return WaySignStructure.Start::new;
    }


    /**
     * Generation stage for when to generate the structure. there are 10 stages you can pick from!
     * This surface structure stage places the structure before plants and ores are generated.
     */
    //getDecorationStage
    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }


    /**
     * This is where extra checks can be done to determine if the structure can spawn here.
     * This only needs to be overridden if you're adding additional spawn conditions.
     * <p>
     * Fun fact, if you set your structure separation/spacing to be 0/1, you can use
     * func_230363_a_ to return true only if certain chunk coordinates are passed in
     * which allows you to spawn structures only at certain coordinates in the world.
     * <p>
     * Notice how the biome is also passed in. Though, you are not going to
     * do any biome checking here as you should've added this structure to
     * the biomes you wanted already with the biome load event.
     * <p>
     * Basically, this method is used for determining if the land is at a suitable height,
     * if certain other structures are too close or not, or some other restrictive condition.
     * <p>
     * For example, Pillager Outposts added a check to make sure it cannot spawn within 10 chunk of a Village.
     * (Bedrock Edition seems to not have the same check)
     * <p>
     * <p>
     * Also, please for the love of god, do not do dimension checking here. If you do and
     * another mod's dimension is trying to spawn your structure, the locate
     * command will make minecraft hang forever and break the game.
     * <p>
     * Instead, use the addDimensionalSpacing method in StructureTutorialMain class.
     * If you check for the dimension there and do not add your structure's
     * spacing into the chunk generator, the structure will not spawn in that dimension!
     */


    private boolean isValidPos(ChunkGenerator gen, int x, int z, Set<Integer> heightMap, LevelHeightAccessor heightLimitView) {
        // Grab height of land. Will stop at first non-air block.
        int y = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heightLimitView);

        NoiseColumn noisecolumn = gen.getBaseColumn(x, z,heightLimitView);

        Heightmap.Types types = Heightmap.Types.WORLD_SURFACE_WG;

        if (types.isOpaque().test(noisecolumn.getBlockState(new BlockPos(x, y, z)))){
            heightMap.add(y);
            return true;
        }
        /*
        try {
            if (!reader.getFluidState(new BlockPos(x, y, z)).isEmpty()) return false;
        } catch (Exception e) {
            return false;
        }
        */
        return false;
    }


    private boolean isNearOutpost(ChunkGenerator generator, long seed, WorldgenRandom sharedSeedRandom, ChunkPos chunkPos) {
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        StructureFeatureConfiguration featureConfiguration = generator.getSettings().getConfig(StructureFeature.PILLAGER_OUTPOST);
        if (featureConfiguration != null) {
            for (int i = chunkX - 8; i <= chunkX + 8; ++i) {
                for (int j = chunkZ - 8; j <= chunkZ + 8; ++j) {
                    ChunkPos chunkpos = StructureFeature.PILLAGER_OUTPOST.getPotentialFeatureChunk(featureConfiguration, seed, sharedSeedRandom, i, j);
                    if (i == chunkpos.x && j == chunkpos.z) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long seed, WorldgenRandom random,
                                     ChunkPos chunkPos1, Biome biome, ChunkPos chunkPos2, NoneFeatureConfiguration featureConfig, LevelHeightAccessor heightLimitView) {

        BlockPos blockPos = chunkPos1.getWorldPosition();

        int x = blockPos.getX();
        int z = blockPos.getZ();
        // Grab height of land. Will stop at first non-air block.
        int y = chunkGenerator.getFirstOccupiedHeight(blockPos.getX(), blockPos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, heightLimitView);

        if (y > 105 || y < chunkGenerator.getSeaLevel() - 1) return false;

        TreeSet<Integer> set = new TreeSet<>();

        set.add(y);
        if (!isValidPos(chunkGenerator, x + 2, z + 2, set, heightLimitView)) return false;
        if (!isValidPos(chunkGenerator, x + 2, z - 2, set, heightLimitView)) return false;
        if (!isValidPos(chunkGenerator, x - 2, z + 2, set, heightLimitView)) return false;
        if (!isValidPos(chunkGenerator, x - 2, z - 2, set, heightLimitView)) return false;


        if (set.last() - set.first() > 1) return false;

        if (isNearOutpost(chunkGenerator, seed, random, chunkPos1)) return false;

        return true;
    }


    @Override
    public String getFeatureName() {
        String name = super.getFeatureName();
        if (name == null) {
            //fail-safe stuff in case something goes wrong during registration so we dont nuke worlds
            Supplementaries.LOGGER.error(new Exception("failed to register way sign structure. this is a bug"));
            return ForgeRegistries.STRUCTURE_FEATURES.getKey(this).toString();
        }
        return name;
    }

    /**
     * Handles calling up the structure's pieces class and height that structure will spawn at.
     */
    //
    public static class Start extends StructureStart<NoneFeatureConfiguration> {

        public Start(StructureFeature<NoneFeatureConfiguration> structureIn, ChunkPos chunkPos, int referenceIn, long seedIn) {
            super(structureIn, chunkPos, referenceIn, seedIn);
        }

        @Override
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, ChunkPos pChunkPos, Biome biomeIn, NoneFeatureConfiguration config, LevelHeightAccessor pLevel) {
            // Turns the chunk coordinates into actual coordinates we can use. (Gets center of that chunk)
            BlockPos blockPos = pChunkPos.getWorldPosition();

            int x = blockPos.getX();
            int z = blockPos.getZ();

            //I could remove this but it makes for nicer generation
            int sum = 0;
            sum += chunkGenerator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, pLevel);
            sum += chunkGenerator.getFirstOccupiedHeight(x + 2, z + 2, Heightmap.Types.WORLD_SURFACE_WG, pLevel);
            sum += chunkGenerator.getFirstOccupiedHeight(x + 2, z - 2, Heightmap.Types.WORLD_SURFACE_WG, pLevel);
            sum += chunkGenerator.getFirstOccupiedHeight(x - 2, z + 2, Heightmap.Types.WORLD_SURFACE_WG, pLevel);
            sum += chunkGenerator.getFirstOccupiedHeight(x - 2, z - 2, Heightmap.Types.WORLD_SURFACE_WG, pLevel);

            int y = Math.round(sum / 5f);

            /*
             * We pass this into addPieces to tell it where to generate the structure.
             * If addPieces's last parameter is true, blockpos's Y value is ignored and the
             * structure will spawn at terrain height instead. Set that parameter to false to
             * force the structure to spawn at blockpos's Y value instead. You got options here!
             */
            BlockPos blockpos = new BlockPos(x, y, z);

            /*
             * If you are doing Nether structures, you'll probably want to spawn your structure on top of ledges.
             * Best way to do that is to use getColumnSample to grab a column of blocks at the structure's x/z position.
             * Then loop through it and look for land with air above it and set blockpos's Y value to it.
             * Make sure to set the final boolean in JigsawManager.addPieces to false so
             * that the structure spawns at blockpos's y value instead of placing the structure on the Bedrock roof!
             */
            //IBlockReader blockReader = chunkGenerator.getBaseColumn(blockpos.getX(), blockpos.getZ());

            // All a structure has to do is call this method to turn it into a jigsaw based structure!
            JigsawPlacement.addPieces(
                    registryAccess,
                    new JigsawConfiguration(() -> registryAccess.registry(Registry.TEMPLATE_POOL_REGISTRY)
                            // The path to the starting Template Pool JSON file to read.
                            //
                            // Note, this is "structure_tutorial:run_down_house/start_pool" which means
                            // the game will automatically look into the following path for the template pool:
                            // "resources/data/structure_tutorial/worldgen/template_pool/run_down_house/start_pool.json"
                            // This is why your pool files must be in "data/<modid>/worldgen/template_pool/<the path to the pool here>"
                            // because the game automatically will check in worldgen/template_pool for the pools.
                            .get().get(new ResourceLocation(Supplementaries.MOD_ID, "way_sign/start_pool")),

                            // How many pieces outward from center can a recursive jigsaw structure spawn.
                            // Our structure is only 1 piece outward and isn't recursive so any value of 1 or more doesn't change anything.
                            // However, I recommend you keep this a decent value like 10 so people can use datapacks to add additional pieces to your structure easily.
                            // But don't make it too large for recursive structures like villages or you'll crash server due to hundreds of pieces attempting to generate!
                            5),
                    PoolElementStructurePiece::new,
                    chunkGenerator,
                    structureManager,
                    blockpos, // Position of the structure. Y value is ignored if last parameter is set to true.
                    this, // The list that will be populated with the jigsaw pieces after this method.
                    this.random,
                    false, // Special boundary adjustments for villages. It's... hard to explain. Keep this false and make your pieces not be partially intersecting.
                    // Either not intersecting or fully contained will make children pieces spawn just fine. It's easier that way.
                    false,
                    pLevel);  // Place at heightmap (top land). Set this to false for structure to be place at the passed in blockpos's Y value instead.
            // Definitely keep this false when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.







            // **THE FOLLOWING TWO LINES ARE OPTIONAL**
            //
            // Right here, you can do interesting stuff with the pieces in this.components such as offset the
            // center piece by 50 blocks up for no reason, remove repeats of a piece or add a new piece so
            // only 1 of that piece exists, etc. But you do not have access to the piece's blocks as this list
            // holds just the piece's size and positions. Blocks will be placed later in JigsawManager.
            //
            // In this case, we do `piece.offset` to raise pieces up by 1 block so that the house is not right on
            // the surface of water or sunken into land a bit.
            //
            // Then we extend the bounding box down by 1 by doing `piece.getBoundingBox().minY` which will cause the
            // land formed around the structure to be lowered and not cover the doorstep. You can raise the bounding
            // box to force the structure to be buried as well. This bounding box stuff with land is only for structures
            // that you added to Structure.field_236384_t_ field handles adding land around the base of structures.
            //
            // By lifting the house up by 1 and lowering the bounding box, the land at bottom of house will now be
            // flush with the surrounding terrain without blocking off the doorstep.
            this.pieces.forEach(piece -> piece.move(0, 1, 0));


            // Sets the bounds of the structure once you are finished.
            BoundingBox boundingBox = this.getBoundingBox();
            boundingBox.inflate(2);
            boundingBox.move(0,2,0);

        }

        @Override
        protected int getMaxReferences() {
            return super.getMaxReferences();
        }

        @Override
        public int getReferences() {
            return super.getReferences();
        }

    }
}