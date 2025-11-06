package net.mehvahdjukaar.supplementaries.common.worldgen;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModWorldgen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
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

public class GalleonStructure extends Structure {

    public static final MapCodec<GalleonStructure> CODEC = RecordCodecBuilder.<GalleonStructure>mapCodec(instance ->
            instance.group(GalleonStructure.settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                    Codec.INT.optionalFieldOf("y_offset", 0).forGetter(structure -> structure.yOffset),
                    Climate.ParameterPoint.CODEC.optionalFieldOf("biome_point").forGetter(structure -> structure.biomePoint),
                    Codec.BOOL.optionalFieldOf("require_sea_level", true).forGetter(structure -> structure.requireSeaLevel),
                    DimensionPadding.CODEC.optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING).forGetter(structure -> structure.dimensionPadding),
                    LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings)
            ).apply(instance, GalleonStructure::new));


    public static class Type implements StructureType<GalleonStructure> {
        @Override
        public MapCodec<GalleonStructure> codec() {
            return CODEC;
        }
    }

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int yOffset;
    private final Optional<Climate.ParameterPoint> biomePoint;
    private final boolean requireSeaLevel;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;

    public GalleonStructure(StructureSettings config,
                            Holder<StructureTemplatePool> startPool,
                            Optional<ResourceLocation> startJigsawName,
                            int yOffset,
                            Optional<Climate.ParameterPoint> biomePoint,
                            boolean requireSeaLevel,
                            DimensionPadding dimensionPadding,
                            LiquidSettings liquidSettings) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.yOffset = yOffset;
        this.biomePoint = biomePoint;
        this.requireSeaLevel = requireSeaLevel;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
    }

    @Override
    public StructureType<?> type() {
        return ModWorldgen.GALLEON_STRUCTURE.get();
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {

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
                10, // How deep a branch of pieces can go away from center piece. (5 means branches cannot be longer than 5 pieces from center piece)
                blockPos, // Where to spawn the structure.
                false, // "useExpansionHack" This is for legacy villages to generate properly. You should keep this false always.
                Optional.empty(), // Adds the terrain height's y value to the passed in blockpos's y value. (This uses WORLD_SURFACE_WG heightmap which stops at top water too)
                // Here, blockpos's y value is 60 which means the structure spawn 60 blocks above terrain height.
                // Set this to false for structure to be place only at the passed in blockpos's Y value instead.
                // Definitely keep this false when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
                64,
                PoolAliasLookup.EMPTY, // Optional thing that allows swapping a template pool with another per structure json instance. We don't need this but see vanilla JigsawStructure class for how to wire it up if you want it.
                this.dimensionPadding, // Optional thing to prevent generating too close to the bottom or top of the dimension.
                this.liquidSettings);  // Optional thing to control whether the structure will be waterlogged when replacing pre-existing water in the world.

    }


    /**
     * gets spawning position or empty if not suitable
     */
    private Optional<BlockPos> getSuitablePosition(GenerationContext context) {

        ChunkPos chunkPos = context.chunkPos();
        ChunkGenerator generator = context.chunkGenerator();
        LevelHeightAccessor levelHeightAccessor = context.heightAccessor();
        RandomState randomState = context.randomState();


        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        // Grab height of land. Will stop at first non-air block.
        int y = generator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState) + 1;

        int seaLevel = context.chunkGenerator().getSeaLevel();
        if (this.requireSeaLevel && y != seaLevel) return Optional.empty();

        Climate.TargetPoint paramAtPos = context.randomState().sampler().sample(x, y, z);
        if (this.biomePoint.isPresent() && !containsPoint(this.biomePoint.get(), paramAtPos)) return Optional.empty();


        return Optional.of(new BlockPos(x, y + yOffset, z));
    }

    private static boolean containsPoint(Climate.ParameterPoint cube, Climate.TargetPoint point) {
        return cube.continentalness().min() <= point.continentalness() &&
                cube.continentalness().max() >= point.continentalness() &&

                cube.erosion().min() <= point.erosion() &&
                cube.erosion().max() >= point.erosion() &&

                cube.temperature().min() <= point.temperature() &&
                cube.temperature().max() >= point.temperature() &&

                cube.humidity().min() <= point.humidity() &&
                cube.humidity().max() >= point.humidity() &&

                cube.weirdness().min() <= point.weirdness() &&
                cube.weirdness().max() >= point.weirdness() &&

                cube.depth().min() <= point.depth() &&
                cube.depth().max() >= point.depth();
    }


    private static final Component OMINOUS_FLAG_PATTERN_NAME = Component.translatable("block.supplementaries.ominous_flag").withStyle(ChatFormatting.GOLD);

    public static ItemStack getGalleonFlag(HolderGetter<BannerPattern> patternRegistry) {
        ItemStack itemStack = new ItemStack(ModRegistry.FLAGS.get(DyeColor.WHITE).get());
        BannerPatternLayers bannerPatternLayers = new BannerPatternLayers.Builder()
                .addIfRegistered(patternRegistry, BannerPatterns.RHOMBUS_MIDDLE, DyeColor.GREEN)
                .addIfRegistered(patternRegistry, BannerPatterns.STRIPE_LEFT, DyeColor.GRAY)
                .addIfRegistered(patternRegistry, BannerPatterns.TRIANGLES_TOP, DyeColor.GRAY)
                .addIfRegistered(patternRegistry, BannerPatterns.TRIANGLES_BOTTOM, DyeColor.GRAY)
                .addIfRegistered(patternRegistry, BannerPatterns.STRAIGHT_CROSS, DyeColor.BLACK)
                .addIfRegistered(patternRegistry, BannerPatterns.HALF_VERTICAL_MIRROR, DyeColor.GRAY)
                .addIfRegistered(patternRegistry, BannerPatterns.BORDER, DyeColor.BLACK)
                .build();

        itemStack.set(DataComponents.BANNER_PATTERNS, bannerPatternLayers);
        itemStack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        itemStack.set(DataComponents.ITEM_NAME, OMINOUS_FLAG_PATTERN_NAME);
        return itemStack;
    }

}
