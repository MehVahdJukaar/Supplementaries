package net.mehvahdjukaar.supplementaries.world.structures.processors;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class SignDataProcessor extends StructureProcessor {

    public static final SignDataProcessor INSTANCE = new SignDataProcessor();
    public static final Codec<SignDataProcessor> CODEC = Codec.unit(() -> INSTANCE);
    public static StructureProcessorType<SignDataProcessor> ROAD_SIGN_PROCESSOR = () -> CODEC;

    SignDataProcessor(){}

    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader reader, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData,  StructureTemplate template) {
        //reader.get.setBlockState(structureBlockInfoWorld.pos, Blocks.DIAMOND_BLOCK.defaultBlockState(), false);
        //reader.getChunk(pos).setBlockState(pos, Blocks.GOLD_BLOCK.defaultBlockState(), false);
        //reader.getChunk(pos2).setBlockState(pos2, Blocks.EMERALD_BLOCK.defaultBlockState(), false);
        //reader.getChunk(structureBlockInfoLocal.pos).setBlockState(structureBlockInfoLocal.pos, Blocks.BEACON.defaultBlockState(), false);

        //double dist = pos2.distSqr(structureBlockInfoWorld.pos);
        //Random r = new Random(pos.asLong());
        //Block b = r.nextFloat()<(dist/5f)?Blocks.DIAMOND_BLOCK:Blocks.EMERALD_BLOCK;

        return structureBlockInfoWorld;
    }


    @Override
    protected StructureProcessorType<?> getType() {
        return ROAD_SIGN_PROCESSOR;

    }

    public static void register(){
        Registry.register(Registry.STRUCTURE_PROCESSOR, new ResourceLocation(Supplementaries.MOD_ID, "data_processor"), ROAD_SIGN_PROCESSOR);
    }
}