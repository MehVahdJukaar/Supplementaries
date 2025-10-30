package net.mehvahdjukaar.supplementaries.common.worldgen;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import net.mehvahdjukaar.moonlight.api.misc.WeakHashSet;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.mehvahdjukaar.supplementaries.reg.ModWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Optional;
import java.util.Set;

public class RoadSignStructure extends Structure {


    public static final MapCodec<RoadSignStructure> CODEC = RecordCodecBuilder.<RoadSignStructure>mapCodec(instance ->
            instance.group(RoadSignStructure.settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                    Codec.INT.fieldOf("min_y").forGetter(structure -> structure.minY),
                    Codec.INT.fieldOf("max_y").forGetter(structure -> structure.maxY),
                    DimensionPadding.CODEC.optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING).forGetter(structure -> structure.dimensionPadding),
                    LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings)
            ).apply(instance, RoadSignStructure::new));


    public static class Type implements StructureType<RoadSignStructure> {
        @Override
        public MapCodec<RoadSignStructure> codec() {
            return CODEC;
        }
    }

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int minY;
    private final int maxY;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;

    public RoadSignStructure(Structure.StructureSettings config,
                             Holder<StructureTemplatePool> startPool,
                             Optional<ResourceLocation> startJigsawName,
                             int minY, int maxY, DimensionPadding dimensionPadding,
                             LiquidSettings liquidSettings) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.minY = minY;
        this.maxY = maxY;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
    }

    @Override
    public StructureType<?> type() {
        return ModWorldgen.ROAD_SIGN_STRUCTURE.get();
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {

        Optional<BlockPos> suitablePosition = getSuitablePosition(context);
        // Check if the spot is valid for our structure. This is just as another method for cleanness.
        // Returning an empty optional tells the game to skip this spot as it will not generate the structure.
        if (suitablePosition.isEmpty()) {
            return Optional.empty();
        }
        BlockPos blockPos = suitablePosition.get();

        // Return the pieces' generator that is now set up so that the game runs it when it needs to create the layout of structure pieces.
        return JigsawPlacement.addPieces(
                context, // Used for JigsawPlacement to get all the proper behaviors done.
                this.startPool, // The starting pool to use to create the structure layout from
                this.startJigsawName, // Can be used to only spawn from one Jigsaw block. But we don't need to worry about this.
                3, // How deep a branch of pieces can go away from center piece. (5 means branches cannot be longer than 5 pieces from center piece)
                blockPos, // Where to spawn the structure.
                false, // "useExpansionHack" This is for legacy villages to generate properly. You should keep this false always.
                Optional.empty(), // Adds the terrain height's y value to the passed in blockpos's y value. (This uses WORLD_SURFACE_WG heightmap which stops at top water too)
                // Here, blockpos's y value is 60 which means the structure spawn 60 blocks above terrain height.
                // Set this to false for structure to be place only at the passed in blockpos's Y value instead.
                // Definitely keep this false when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
                32,
                PoolAliasLookup.EMPTY, // Optional thing that allows swapping a template pool with another per structure json instance. We don't need this but see vanilla JigsawStructure class for how to wire it up if you want it.
                this.dimensionPadding, // Optional thing to prevent generating too close to the bottom or top of the dimension.
                this.liquidSettings);  // Optional thing to control whether the structure will be waterlogged when replacing pre-existing water in the world.

    }


    /**
     * gets spawning position or empty if not suitable
     */
    private Optional<BlockPos> getSuitablePosition(Structure.GenerationContext context) {

        ChunkPos chunkPos = context.chunkPos();
        ChunkGenerator generator = context.chunkGenerator();
        LevelHeightAccessor levelHeightAccessor = context.heightAccessor();
        RandomState randomState = context.randomState();

        var biomes = context.biomeSource().possibleBiomes();

        boolean hasVillages = false;

        for (var v : VALID_BIOMES) {
            if (biomes.contains(v)) {
                hasVillages = true;
                break;
            }
        }

        if (!hasVillages) return Optional.empty();


        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        // Grab height of land. Will stop at first non-air block.
        int y = generator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);

        if (y < this.minY || y > this.maxY) return Optional.empty();
        if (y < generator.getSeaLevel()) return Optional.empty();

        IntList list = new IntArrayList();
        //I could remove this but it makes for nicer generation
        list.add(y);
        if (isPosNotValid(generator, x + 2, z + 2, list, levelHeightAccessor, randomState)) return Optional.empty();
        if (isPosNotValid(generator, x + 2, z - 2, list, levelHeightAccessor, randomState)) return Optional.empty();
        if (isPosNotValid(generator, x - 2, z + 2, list, levelHeightAccessor, randomState)) return Optional.empty();
        if (isPosNotValid(generator, x - 2, z - 2, list, levelHeightAccessor, randomState)) return Optional.empty();

        IntRBTreeSet set = new IntRBTreeSet(list);
        if (set.lastInt() - set.firstInt() > 1) return Optional.empty();

        int sum = 0;
        for (var v : list) sum += v;

        return Optional.of(new BlockPos(x, Math.round(sum / 5f) + 1, z));
    }

    private static boolean isPosNotValid(ChunkGenerator gen, int x, int z, IntList heightMap,
                                         LevelHeightAccessor heightLimitView, RandomState randomState) {
        // Grab height of land. Will stop at first non-air block.
        int y = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heightLimitView, randomState);

        NoiseColumn noisecolumn = gen.getBaseColumn(x, z, heightLimitView, randomState);

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


    private static final Set<Holder<Biome>> VALID_BIOMES = new WeakHashSet<>();

    public static void recomputeValidStructureCache(RegistryAccess access) {
        for (var s : access.registryOrThrow(Registries.STRUCTURE).getTagOrEmpty(ModTags.WAY_SIGN_DESTINATIONS)) {
            VALID_BIOMES.addAll(s.value().biomes().stream().toList());
        }
    }

    public static void clearCache() {
        VALID_BIOMES.clear();
    }

}
