package net.mehvahdjukaar.supplementaries.common.world.generation.structure;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

public class WaySignStructure extends StructureFeature<JigsawConfiguration> {

    public WaySignStructure(Codec<JigsawConfiguration> codec) {
        super(codec, (context) -> {
                    // Check if the spot is valid for structure gen. If false, return nothing to signal to the game to skip this spawn attempt.
                    if (!WaySignStructure.isFeatureChunk(context)) {
                        return Optional.empty();
                    }
                    // Create the pieces layout of the structure and give it to
                    else {
                        return WaySignStructure.createPiecesGenerator(context);
                    }
                },
                PostPlacementProcessor.NONE);
    }


    /**
     * Generation stage for when to generate the structure. there are 10 stages you can pick from!
     * This surface structure stage places the structure before plants and ores are generated.
     */
    //getDecorationStage
    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.STRONGHOLDS;
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


    private static boolean isPosNotValid(ChunkGenerator gen, int x, int z, Set<Integer> heightMap, LevelHeightAccessor heightLimitView) {
        // Grab height of land. Will stop at first non-air block.
        int y = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heightLimitView);

        NoiseColumn noisecolumn = gen.getBaseColumn(x, z, heightLimitView);

        // Grabs column of blocks at given position. In overworld, this column will be made of stone, water, and air.
        // In nether, it will be netherrack, lava, and air. End will only be endstone and air. It depends on what block
        // the chunk generator will place for that dimension.

        // Combine the column of blocks with land height and you get the top block itself which you can spawnParticleOnBoundingBox.

        BlockState state = noisecolumn.getBlock(y);

        /*
        if (types.isOpaque().test(state)){
            heightMap.add(y);
            return true;
        }
        */
        try {
            if (state.getFluidState().isEmpty()) {
                heightMap.add(y);
                return false;
            }
        } catch (Exception e) {
            return true;
        }

        return true;
    }


    private static boolean isNearOutpost(ChunkGenerator generator, long seed, ChunkPos chunkPos) {
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        StructureFeatureConfiguration featureConfiguration = generator.getSettings().getConfig(StructureFeature.PILLAGER_OUTPOST);
        if (featureConfiguration != null) {
            for (int i = chunkX - 8; i <= chunkX + 8; ++i) {
                for (int j = chunkZ - 8; j <= chunkZ + 8; ++j) {
                    ChunkPos chunkpos = StructureFeature.PILLAGER_OUTPOST.getPotentialFeatureChunk(featureConfiguration, seed, i, j);
                    if (i == chunkpos.x && j == chunkpos.z) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isFeatureChunk(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {

        ChunkPos chunkPos = context.chunkPos();
        ChunkGenerator generator = context.chunkGenerator();
        LevelHeightAccessor heightLimitView = context.heightAccessor();

        BlockPos blockPos = chunkPos.getWorldPosition();

        int x = blockPos.getX();
        int z = blockPos.getZ();
        // Grab height of land. Will stop at first non-air block.
        int y = generator.getFirstOccupiedHeight(blockPos.getX(), blockPos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, heightLimitView);

        if (y > 105 || y < generator.getSeaLevel()) return false;

        TreeSet<Integer> set = new TreeSet<>();


        set.add(y);
        if (isPosNotValid(generator, x + 2, z + 2, set, heightLimitView)) return false;
        if (isPosNotValid(generator, x + 2, z - 2, set, heightLimitView)) return false;
        if (isPosNotValid(generator, x - 2, z + 2, set, heightLimitView)) return false;
        if (isPosNotValid(generator, x - 2, z - 2, set, heightLimitView)) return false;


        if (set.last() - set.first() > 1) return false;

        if (isNearOutpost(generator, context.seed(), chunkPos)) return false;

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


    public static Optional<PieceGenerator<JigsawConfiguration>> createPiecesGenerator(
            PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
        // Turns the chunk coordinates into actual coordinates we can use. (Gets center of that chunk)
        ChunkPos chunkPos = context.chunkPos();
        ChunkGenerator generator = context.chunkGenerator();
        LevelHeightAccessor levelHeight = context.heightAccessor();
        int x = chunkPos.getMinBlockX();
        int z = chunkPos.getMinBlockZ();

        //I could remove this but it makes for nicer generation
        int sum = 0;
        sum += generator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, levelHeight);
        sum += generator.getFirstOccupiedHeight(x + 2, z + 2, Heightmap.Types.WORLD_SURFACE_WG, levelHeight);
        sum += generator.getFirstOccupiedHeight(x + 2, z - 2, Heightmap.Types.WORLD_SURFACE_WG, levelHeight);
        sum += generator.getFirstOccupiedHeight(x - 2, z + 2, Heightmap.Types.WORLD_SURFACE_WG, levelHeight);
        sum += generator.getFirstOccupiedHeight(x - 2, z - 2, Heightmap.Types.WORLD_SURFACE_WG, levelHeight);

        int y = Math.round(sum / 5f);

        /*
         * We pass this into addPieces to tell it where to generate the structure.
         * If addPieces's last parameter is true, blockpos's Y value is ignored and the
         * structure will spawn at terrain height instead. Set that parameter to false to
         * force the structure to spawn at blockpos's Y value instead. You got options here!
         */
        BlockPos blockpos = new BlockPos(x, y+1, z);


        /*
         * The only reason we are using JigsawConfiguration here is because further down, we are using
         * JigsawPlacement.addPieces which requires JigsawConfiguration. However, if you create your own
         * JigsawPlacement.addPieces, you could reduce the amount of workarounds like above that you need
         * and give yourself more opportunities and control over your structures.
         *
         * An example of a custom JigsawPlacement.addPieces in action can be found here:
         * https://github.com/TelepathicGrunt/RepurposedStructures/blob/1.18/src/main/java/com/telepathicgrunt/repurposedstructures/world/structures/pieces/PieceLimitedJigsawManager.java
         */
        JigsawConfiguration newConfig = new JigsawConfiguration(
                // The path to the starting Template Pool JSON file to read.
                //
                // Note, this is "structure_tutorial:run_down_house/start_pool" which means
                // the game will automatically look into the following path for the template pool:
                // "resources/data/structure_tutorial/worldgen/template_pool/run_down_house/start_pool.json"
                // This is why your pool files must be in "data/<modid>/worldgen/template_pool/<the path to the pool here>"
                // because the game automatically will check in worldgen/template_pool for the pools.
                () -> context.registryAccess().ownedRegistryOrThrow(Registry.TEMPLATE_POOL_REGISTRY)
                        .get(Supplementaries.res("way_sign/start_pool")),

                // How many pieces outward from center can a recursive jigsaw structure spawn.
                // Our structure is only 1 piece outward and isn't recursive so any value of 1 or more doesn't change anything.
                // However, I recommend you keep this a decent value like 7 so people can use datapacks to add additional pieces to your structure easily.
                // But don't make it too large for recursive structures like villages or you'll crash server due to hundreds of pieces attempting to generate!
                4
        );

        // Create a new context with the new config that has our json pool. We will pass this into JigsawPlacement.addPieces
        PieceGeneratorSupplier.Context<JigsawConfiguration> newContext = new PieceGeneratorSupplier.Context<>(
                context.chunkGenerator(),
                context.biomeSource(),
                context.seed(),
                context.chunkPos(),
                newConfig,
                context.heightAccessor(),
                context.validBiome(),
                context.structureManager(),
                context.registryAccess()
        );

        Optional<PieceGenerator<JigsawConfiguration>> structurePiecesGenerator =
                JigsawPlacement.addPieces(
                        newContext, // Used for JigsawPlacement to get all the proper behaviors done.
                        PoolElementStructurePiece::new, // Needed in order to create a list of jigsaw pieces when making the structure's layout.
                        blockpos, // Position of the structure. Y value is ignored if last parameter is set to true.
                        false,  // Special boundary adjustments for villages. It's... hard to explain. Keep this false and make your pieces not be partially intersecting.
                        // Either not intersecting or fully contained will make children pieces spawn just fine. It's easier that way.
                        false // Place at heightmap (top land). Set this to false for structure to be place at the passed in blockpos's Y value instead.
                        // Definitely keep this false when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
                );
        /*
         * Note, you are always free to make your own JigsawPlacement class and implementation of how the structure
         * should generate. It is tricky but extremely powerful if you are doing something that vanilla's jigsaw system cannot do.
         * Such as for example, forcing 3 pieces to always spawn every time, limiting how often a piece spawns, or remove the intersection limitation of pieces.
         *
         * An example of a custom JigsawPlacement.addPieces in action can be found here (warning, it is using Mojmap mappings):
         * https://github.com/TelepathicGrunt/RepurposedStructures/blob/1.18/src/main/java/com/telepathicgrunt/repurposedstructures/world/structures/pieces/PieceLimitedJigsawManager.java
         */


      //  context..pieces.forEach(piece -> piece.move(0, 1, 0));


        // Sets the bounds of the structure once you are finished.
       // BoundingBox boundingBox = this.getBoundingBox();
      //  boundingBox.inflate(2);
      //  boundingBox.move(0, 2, 0);
        //TODO: move up

        // Return the pieces generator that is now set up so that the game runs it when it needs to create the layout of structure pieces.
        return structurePiecesGenerator;
    }




}