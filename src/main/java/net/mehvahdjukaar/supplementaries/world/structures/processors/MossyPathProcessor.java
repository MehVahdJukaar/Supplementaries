package net.mehvahdjukaar.supplementaries.world.structures.processors;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;

public class MossyPathProcessor extends StructureProcessor {

    public static final MossyPathProcessor INSTANCE = new MossyPathProcessor();
    public static final Codec<MossyPathProcessor> CODEC = Codec.unit(() -> INSTANCE);
    public static IStructureProcessorType<MossyPathProcessor> ROAD_SIGN_PROCESSOR = () -> CODEC;

    MossyPathProcessor(){}

    @Override
    public Template.BlockInfo process(IWorldReader reader, BlockPos pos, BlockPos pos2, Template.BlockInfo structureBlockInfoLocal, Template.BlockInfo structureBlockInfoWorld, PlacementSettings structurePlacementData,  Template template) {

        return new Template.BlockInfo(structureBlockInfoWorld.pos,Blocks.DIAMOND_BLOCK.defaultBlockState(),null);
    }


    @Override
    protected IStructureProcessorType<?> getType() {
        return ROAD_SIGN_PROCESSOR;

    }

    public static void register(){
        Registry.register(Registry.STRUCTURE_PROCESSOR, new ResourceLocation(Supplementaries.MOD_ID, "data_processor"), ROAD_SIGN_PROCESSOR);
    }
}