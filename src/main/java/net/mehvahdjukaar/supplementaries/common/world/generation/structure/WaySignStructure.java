package net.mehvahdjukaar.supplementaries.common.world.generation.structure;

import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class WaySignStructure extends StructureFeature<JigsawConfiguration> {

    public WaySignStructure() {
        super(JigsawConfiguration.CODEC, WaySignStructure::createPiecesGenerator, PostPlacementProcessor.NONE);
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.STRONGHOLDS;
    }

    public static Optional<PieceGenerator<JigsawConfiguration>> createPiecesGenerator(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {

        // Check if the spot is valid for our structure. This is just as another method for cleanness.
        // Returning an empty optional tells the game to skip this spot as it will not generate the structure.
        if (!WaySignStructure.isFeatureChunk(context)) {
            return Optional.empty();
        }

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

        BlockPos blockpos = new BlockPos(x, y + 1, z);

        // Return the pieces generator that is now set up so that the game runs it when it needs to create the layout of structure pieces.
        return JigsawPlacement.addPieces(
                context, // Used for JigsawPlacement to get all the proper behaviors done.
                PoolElementStructurePiece::new, // Needed in order to create a list of jigsaw pieces when making the structure's layout.
                blockpos, // Position of the structure. Y value is ignored if last parameter is set to true.
                false,  // Special boundary adjustments for villages. It's... hard to explain. Keep this false and make your pieces not be partially intersecting.
                // Either not intersecting or fully contained will make children pieces spawn just fine. It's easier that way.
                false // Place at heightmap (top land). Set this to false for structure to be place at the passed in blockpos's Y value instead.
                // Definitely keep this false when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
        );
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


    private static boolean isFeatureChunk(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {

        ChunkPos chunkPos = context.chunkPos();
        ChunkGenerator generator = context.chunkGenerator();
        LevelHeightAccessor heightLimitView = context.heightAccessor();

        //if it can generate villages

        boolean hasVillages = generator.possibleStructureSets().anyMatch(f -> {
                    for (var s : f.value().structures()) {
                        if (s.structure().is(ModTags.WAY_SIGN_DESTINATIONS)) return true;
                    }
                    return false;
                }
        );

        if(!hasVillages)return false;

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

        //TODO add villages here
        return !context.chunkGenerator().hasFeatureChunkInRange(BuiltinStructureSets.PILLAGER_OUTPOSTS,
                context.seed(), chunkPos.x, chunkPos.z, 10);
    }


}