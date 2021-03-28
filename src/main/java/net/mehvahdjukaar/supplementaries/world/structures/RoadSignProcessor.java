package net.mehvahdjukaar.supplementaries.world.structures;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.structure.VillageStructure;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;

public class RoadSignProcessor extends StructureProcessor {

    public static final RoadSignProcessor INSTANCE = new RoadSignProcessor();
    public static final Codec<RoadSignProcessor> CODEC = Codec.unit(() -> INSTANCE);
    public static IStructureProcessorType<RoadSignProcessor> ROAD_SIGN_PROCESSOR = () -> CODEC;

    public Template.BlockInfo process(IWorldReader reader, BlockPos pos, BlockPos blockPos, Template.BlockInfo structureBlockInfoLocal, Template.BlockInfo structureBlockInfoWorld, PlacementSettings structurePlacementData) {
        if (structureBlockInfoWorld.state.is(Blocks.COBBLESTONE)) {
            reader.getChunk(structureBlockInfoWorld.pos).setBlockState(structureBlockInfoWorld.pos, Blocks.DIAMOND_BLOCK.defaultBlockState(), false);
        }
        return structureBlockInfoWorld;
    }

    @Override
    protected IStructureProcessorType<?> getType() {
        return ROAD_SIGN_PROCESSOR;

    }

    public static void register(){
        Registry.register(Registry.STRUCTURE_PROCESSOR, new ResourceLocation(Supplementaries.MOD_ID, "data_processor"), ROAD_SIGN_PROCESSOR);
    }
}