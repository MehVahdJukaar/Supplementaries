package net.mehvahdjukaar.supplementaries.world.structures.processors;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class MossyPathProcessor extends StructureProcessor {

    public static final MossyPathProcessor INSTANCE = new MossyPathProcessor();
    public static final Codec<MossyPathProcessor> CODEC = Codec.unit(() -> INSTANCE);
    public static StructureProcessorType<MossyPathProcessor> ROAD_SIGN_PROCESSOR = () -> CODEC;

    MossyPathProcessor(){}

    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader reader, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData,  StructureTemplate template) {

        return new StructureTemplate.StructureBlockInfo(structureBlockInfoWorld.pos,Blocks.DIAMOND_BLOCK.defaultBlockState(),null);
    }


    @Override
    protected StructureProcessorType<?> getType() {
        return ROAD_SIGN_PROCESSOR;

    }

    public static void register(){
        Registry.register(Registry.STRUCTURE_PROCESSOR, new ResourceLocation(Supplementaries.MOD_ID, "data_processor"), ROAD_SIGN_PROCESSOR);
    }
}