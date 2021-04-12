package net.mehvahdjukaar.supplementaries.world.structures;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.StructureSeparationSettings;

import java.util.Set;
import java.util.TreeSet;

public class WaySignStructure extends Structure<NoFeatureConfig> {
    public WaySignStructure(Codec<NoFeatureConfig> codec) {
        super(codec);
    }

    /**
     * This is how the worldgen code knows what to call when it
     * is time to create the pieces of the structure for generation.
     */
    @Override
    public  IStartFactory<NoFeatureConfig> getStartFactory() {
        return WaySignStructure.Start::new;
    }


    /**
     * Generation stage for when to generate the structure. there are 10 stages you can pick from!
     * This surface structure stage places the structure before plants and ores are generated.
     */
    //getDecorationStage
    @Override
    public GenerationStage.Decoration step() {
        return GenerationStage.Decoration.SURFACE_STRUCTURES;
    }




    /**
     * This is where extra checks can be done to determine if the structure can spawn here.
     * This only needs to be overridden if you're adding additional spawn conditions.
     *
     * Fun fact, if you set your structure separation/spacing to be 0/1, you can use
     * func_230363_a_ to return true only if certain chunk coordinates are passed in
     * which allows you to spawn structures only at certain coordinates in the world.
     *
     * Notice how the biome is also passed in. Though, you are not going to
     * do any biome checking here as you should've added this structure to
     * the biomes you wanted already with the biome load event.
     *
     * Basically, this method is used for determining if the land is at a suitable height,
     * if certain other structures are too close or not, or some other restrictive condition.
     *
     * For example, Pillager Outposts added a check to make sure it cannot spawn within 10 chunk of a Village.
     * (Bedrock Edition seems to not have the same check)
     *
     *
     * Also, please for the love of god, do not do dimension checking here. If you do and
     * another mod's dimension is trying to spawn your structure, the locate
     * command will make minecraft hang forever and break the game.
     *
     * Instead, use the addDimensionalSpacing method in StructureTutorialMain class.
     * If you check for the dimension there and do not add your structure's
     * spacing into the chunk generator, the structure will not spawn in that dimension!
     */


    private boolean isValidPos(ChunkGenerator gen, int x, int z, Set<Integer> heightMap){
        int y = gen.getFirstOccupiedHeight(x,z, Heightmap.Type.WORLD_SURFACE_WG);
        IBlockReader reader = gen.getBaseColumn(x,z);
        try {
            if (!reader.getFluidState(new BlockPos(x, y, z)).isEmpty()) return false;
        }
        catch(Exception e){
            return false;
        }
        heightMap.add(y);
        return true;
    }


    private boolean isNearOutpost(ChunkGenerator generator, long seed, SharedSeedRandom sharedSeedRandom, int chunkX, int chunkZ) {
        StructureSeparationSettings structureseparationsettings = generator.getSettings().getConfig(Structure.PILLAGER_OUTPOST);
        if (structureseparationsettings != null) {
            for (int i = chunkX - 8; i <= chunkX + 8; ++i) {
                for (int j = chunkZ - 8; j <= chunkZ + 8; ++j) {
                    ChunkPos chunkpos = Structure.PILLAGER_OUTPOST.getPotentialFeatureChunk(structureseparationsettings, seed, sharedSeedRandom, i, j);
                    if (i == chunkpos.x && j == chunkpos.z) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeProvider biomeSource, long seed, SharedSeedRandom chunkRandom, int chunkX, int chunkZ, Biome biome, ChunkPos chunkPos, NoFeatureConfig featureConfig) {
        int x = (chunkX << 4) + 7;
        int z = (chunkZ << 4) + 7;

        int y = chunkGenerator.getFirstOccupiedHeight(x,z, Heightmap.Type.WORLD_SURFACE_WG);

        if(y>105||y<chunkGenerator.getSeaLevel()-1)return false;

        TreeSet<Integer> set = new TreeSet<>();

        set.add(y);
        if(!isValidPos(chunkGenerator,x+2,z+2, set))return false;
        if(!isValidPos(chunkGenerator,x+2,z-2, set))return false;
        if(!isValidPos(chunkGenerator,x-2,z+2, set))return false;
        if(!isValidPos(chunkGenerator,x-2,z-2, set))return false;


        if(set.last()-set.first()>1) return false;

        if(isNearOutpost(chunkGenerator,seed,chunkRandom,chunkX,chunkZ))return false;


        return true;
    }


    @Override
    public String getFeatureName() {
        return super.getFeatureName();

    }

    /**
     * Handles calling up the structure's pieces class and height that structure will spawn at.
     */
    //
    public static class Start extends StructureStart<NoFeatureConfig>  {

        public Start(Structure<NoFeatureConfig> structureIn, int chunkX, int chunkZ, MutableBoundingBox mutableBoundingBox, int referenceIn, long seedIn) {
            super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
        }

        @Override
        public void generatePieces(DynamicRegistries dynamicRegistryManager, ChunkGenerator chunkGenerator, TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn, NoFeatureConfig config) {
            // Turns the chunk coordinates into actual coordinates we can use. (Gets center of that chunk)
            int x = (chunkX << 4) + 7;
            int z = (chunkZ << 4) + 7;

            //I could remove this but it makes for nicer generation
            int sum = 0;
            sum+=chunkGenerator.getFirstOccupiedHeight(x,z, Heightmap.Type.WORLD_SURFACE_WG);
            sum+=chunkGenerator.getFirstOccupiedHeight(x+2,z+2, Heightmap.Type.WORLD_SURFACE_WG);
            sum+=chunkGenerator.getFirstOccupiedHeight(x+2,z-2, Heightmap.Type.WORLD_SURFACE_WG);
            sum+=chunkGenerator.getFirstOccupiedHeight(x-2,z+2, Heightmap.Type.WORLD_SURFACE_WG);
            sum+=chunkGenerator.getFirstOccupiedHeight(x-2,z-2, Heightmap.Type.WORLD_SURFACE_WG);

            int y = Math.round(sum/5f);

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
            JigsawManager.addPieces(
                    dynamicRegistryManager,
                    new VillageConfig(() -> dynamicRegistryManager.registry(Registry.TEMPLATE_POOL_REGISTRY)
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
                    AbstractVillagePiece::new,
                    chunkGenerator,
                    templateManagerIn,
                    blockpos, // Position of the structure. Y value is ignored if last parameter is set to true.
                    this.pieces, // The list that will be populated with the jigsaw pieces after this method.
                    this.random,
                    false, // Special boundary adjustments for villages. It's... hard to explain. Keep this false and make your pieces not be partially intersecting.
                    // Either not intersecting or fully contained will make children pieces spawn just fine. It's easier that way.
                    false);  // Place at heightmap (top land). Set this to false for structure to be place at the passed in blockpos's Y value instead.
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
            this.calculateBoundingBox();
            this.boundingBox.x0-=2;
            this.boundingBox.x1+=3;
            this.boundingBox.z0-=2;
            this.boundingBox.z1+=3;
            this.boundingBox.y1+=4;
            this.boundingBox.y0-=0;

        }

        @Override
        protected int getMaxReferences() {
            return super.getMaxReferences();
        }

        //todo: bb is getting defaulted to a 1 block bb for no reason at all, making advancement not unlockable.. wtf
        @Override
        public MutableBoundingBox getBoundingBox() {
            return super.getBoundingBox();
        }

        @Override
        public int getReferences() {
            return super.getReferences();
        }

        @Override
        public BlockPos getLocatePos() {
            return new BlockPos((this.getChunkX() << 4) + 7, 0, (this.getChunkZ() << 4) + 8);
        }



    }
}